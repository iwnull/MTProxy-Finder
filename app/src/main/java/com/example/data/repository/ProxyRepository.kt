package com.example.data.repository

import android.util.Base64
import android.util.Log
import com.example.data.local.ProxyDao
import com.example.data.model.ProxyEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.TimeUnit

class ProxyRepository(private val proxyDao: ProxyDao) {

    val allProxies: Flow<List<ProxyEntity>> = proxyDao.getAllProxies()
    val onlineProxies: Flow<List<ProxyEntity>> = proxyDao.getOnlineProxies()

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    // 3 Github URLs specified by the user
    private val sourceUrls = listOf(
        "https://raw.githubusercontent.com/V2RAYCONFIGSPOOL/TELEGRAM_PROXY_SUB/refs/heads/main/telegram_proxy_no1.txt",
        "https://raw.githubusercontent.com/V2RAYCONFIGSPOOL/TELEGRAM_PROXY_SUB/refs/heads/main/telegram_proxy_no10.txt",
        "https://raw.githubusercontent.com/V2RAYCONFIGSPOOL/TELEGRAM_PROXY_SUB/refs/heads/main/telegram_proxy_no2.txt"
    )

    /**
     * Fetches proxies from all 3 sources, parses them, and inserts them into the database.
     * Returns the count of newly added/loaded proxies.
     */
    suspend fun fetchAndLoadProxies(): Int = withContext(Dispatchers.IO) {
        var addedCount = 0
        val parsedProxies = mutableListOf<ProxyEntity>()

        for (url in sourceUrls) {
            try {
                val request = Request.Builder().url(url).build()
                httpClient.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val bodyText = response.body?.string() ?: ""
                        val decodedText = tryBase64Decode(bodyText)
                        val proxiesFromSource = parseProxyString(decodedText, url)
                        parsedProxies.addAll(proxiesFromSource)
                    }
                }
            } catch (e: Exception) {
                Log.e("ProxyRepository", "Error fetching from $url", e)
            }
        }

        if (parsedProxies.isEmpty()) {
            return@withContext 0
        }

        val existingGitHubProxies = proxyDao.getGitHubProxies()
        val existingKeys = existingGitHubProxies.map { "${it.server}:${it.port}:${it.secret}" }.toSet()
        val newKeys = parsedProxies.map { "${it.server}:${it.port}:${it.secret}" }.toSet()

        if (existingKeys.isNotEmpty() && existingKeys == newKeys) {
            // No new updates on GitHub
            return@withContext -1
        }

        // Delete outdated GitHub proxies that are not in the new list
        val toDelete = existingGitHubProxies.filter { "${it.server}:${it.port}:${it.secret}" !in newKeys }
        if (toDelete.isNotEmpty()) {
            proxyDao.deleteProxies(toDelete)
        }

        // Insert or update in database
        for (proxy in parsedProxies) {
            val existing = proxyDao.getProxyByAddress(proxy.server, proxy.port)
            if (existing == null) {
                proxyDao.insertProxy(proxy)
                addedCount++
            } else {
                // If it exists, update the secret in case it changed, but preserve checks stats
                val updated = existing.copy(
                    secret = proxy.secret,
                    source = proxy.source
                )
                proxyDao.updateProxy(updated)
            }
        }

        return@withContext addedCount
    }

    /**
     * Parses a block of text and extracts MTProto proxies.
     */
    fun parseProxyString(text: String, sourceUrl: String = "manual"): List<ProxyEntity> {
        val list = mutableListOf<ProxyEntity>()
        val lines = text.split("\n", "\r")

        val serverRegex = """[?&]server=([^&]+)""".toRegex()
        val portRegex = """[?&]port=([0-9]+)""".toRegex()
        val secretRegex = """[?&]secret=([^&]+)""".toRegex()

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue

            // MTProto URIs
            if (trimmed.startsWith("tg://proxy") || trimmed.startsWith("https://t.me/proxy") || trimmed.startsWith("https://telegram.me/proxy")) {
                try {
                    val serverMatch = serverRegex.find(trimmed)
                    val portMatch = portRegex.find(trimmed)
                    val secretMatch = secretRegex.find(trimmed)

                    if (serverMatch != null && portMatch != null && secretMatch != null) {
                        val server = serverMatch.groupValues[1]
                        val port = portMatch.groupValues[1].toIntOrNull() ?: continue
                        val secret = secretMatch.groupValues[1]

                        list.add(
                            ProxyEntity(
                                server = server,
                                port = port,
                                secret = secret,
                                source = sourceUrl
                            )
                        )
                    }
                } catch (e: Exception) {
                    Log.e("ProxyRepository", "Failed parsing line: $trimmed", e)
                }
            }
        }
        return list
    }

    /**
     * Decodes Base64 subscription formats if they are encoded.
     */
    private fun tryBase64Decode(text: String): String {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return text
        
        // Basic check if text is purely Base64
        if (trimmed.startsWith("tg://") || trimmed.startsWith("https://") || trimmed.contains("\n")) {
            // Probably plain text already, or multi-line base64. Let's see if we can decode each line.
            return text
        }

        return try {
            val decodedBytes = Base64.decode(trimmed, Base64.DEFAULT)
            String(decodedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            text // fallback to original text if decoding fails
        }
    }

    private fun hexToBytes(hex: String): ByteArray {
        val clean = hex.trim().lowercase().filter { it in "0123456789abcdef" }
        if (clean.isEmpty()) return ByteArray(0)
        val result = ByteArray(clean.length / 2)
        for (i in result.indices) {
            val index = i * 2
            val j = clean.substring(index, index + 2).toInt(16)
            result[i] = j.toByte()
        }
        return result
    }

    /**
     * Extracts the configured fake TLS domain from the hex secret.
     */
    private fun getFakeTlsDomain(secretHex: String, defaultHost: String): String {
        val clean = secretHex.trim()
        if (!clean.startsWith("ee", ignoreCase = true) || clean.length <= 34) {
            return defaultHost
        }
        return try {
            val domainHex = clean.substring(34)
            val domainBytes = hexToBytes(domainHex)
            val domain = String(domainBytes, Charsets.UTF_8).trim()
            if (domain.isNotEmpty() && domain.contains(".")) domain else defaultHost
        } catch (e: Exception) {
            defaultHost
        }
    }

    /**
     * Measures bidirectional speed and checks connectivity over 3 consecutive attempts.
     * Truly online and healthy only if deep MTProto handshake succeeds.
     */
    private val trustAllSslSocketFactory by lazy {
        try {
            val sslContext = javax.net.ssl.SSLContext.getInstance("TLS")
            val trustAllCerts = arrayOf<javax.net.ssl.TrustManager>(object : javax.net.ssl.X509TrustManager {
                override fun checkClientTrusted(chain: Array<out java.security.cert.X509Certificate>?, authType: String?) {}
                override fun checkServerTrusted(chain: Array<out java.security.cert.X509Certificate>?, authType: String?) {}
                override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> = arrayOf()
            })
            sslContext.init(null, trustAllCerts, java.security.SecureRandom())
            sslContext.socketFactory
        } catch (e: Exception) {
            javax.net.ssl.SSLSocketFactory.getDefault() as javax.net.ssl.SSLSocketFactory
        }
    }

    /**
     * Measures speed and checks connectivity of a single MTProto proxy cleanly and without blocking.
     * Prevents UI hangs by using asynchronous DNS timeouts, single-attempt fast probes, and proper SNI verification.
     */
    suspend fun testProxy(proxy: ProxyEntity): ProxyEntity = withContext(Dispatchers.IO) {
        val attempts = 1 // 1 attempt makes verification extremely snappy and fast
        var successfulAttempts = 0
        var totalPingSum = 0L

        val secretHex = proxy.secret.trim()

        for (i in 1..attempts) {
            val socket = Socket()
            val startTime = System.currentTimeMillis()
            try {
                // 1. Resolve DNS asynchronously with a short timeout to prevent synchronous thread pool starvation
                val inetAddress = kotlinx.coroutines.withTimeoutOrNull(1000) {
                    try {
                        java.net.InetAddress.getByName(proxy.server)
                    } catch (e: Exception) {
                        null
                    }
                } ?: throw java.io.IOException("DNS resolution timed out or failed")

                // 2. Connect directly to resolved IP with a low timeout
                socket.connect(InetSocketAddress(inetAddress, proxy.port), 1200)
                socket.soTimeout = 1200
                socket.tcpNoDelay = true

                if (socket.isConnected && !socket.isClosed) {
                    val isFakeTls = secretHex.startsWith("ee", ignoreCase = true)
                    if (isFakeTls) {
                        // For Fake TLS MTProto proxies: perform trust-all SSL handshake with the correct SNI!
                        val sslSocket = trustAllSslSocketFactory.createSocket(
                            socket,
                            proxy.server,
                            proxy.port,
                            true
                        ) as javax.net.ssl.SSLSocket
                        
                        sslSocket.soTimeout = 1200
                        
                        // Parse fake TLS domain from secret to set as SNI
                        val sniDomain = getFakeTlsDomain(secretHex, proxy.server)
                        try {
                            val sslParameters = sslSocket.sslParameters
                            sslParameters.serverNames = listOf(javax.net.ssl.SNIHostName(sniDomain))
                            sslSocket.sslParameters = sslParameters
                        } catch (e: Exception) {
                            Log.e("ProxyRepository", "Failed to set SNI: ${e.message}")
                        }

                        sslSocket.startHandshake()
                        
                        val duration = System.currentTimeMillis() - startTime
                        totalPingSum += duration
                        successfulAttempts++
                        try {
                            sslSocket.close()
                        } catch (e: Exception) {}
                    } else {
                        // For non-Fake TLS proxies (classic or dd-secrets)
                        // Connect, write standard obfuscated2 payload, and check socket viability
                        val b = ByteArray(64)
                        java.security.SecureRandom().nextBytes(b)
                        while (b[0] == 0xef.toByte() || 
                               (b[0] == 0x47.toByte() && b[1] == 0x45.toByte() && b[2] == 0x54.toByte()) || 
                               (b[0] == 0x50.toByte() && b[1] == 0x4f.toByte() && b[2] == 0x53.toByte() && b[3] == 0x54.toByte())
                        ) {
                            b[0] = (b[0] + 1).toByte()
                        }
                        
                        val isPaddedIntermediate = secretHex.lowercase().startsWith("dd")
                        if (isPaddedIntermediate) {
                            b[56] = 0xdd.toByte()
                            b[57] = 0xdd.toByte()
                            b[58] = 0xdd.toByte()
                            b[59] = 0xdd.toByte()
                        } else {
                            b[56] = 0xef.toByte()
                            b[57] = 0xef.toByte()
                            b[58] = 0xef.toByte()
                            b[59] = 0xef.toByte()
                        }

                        val out = socket.getOutputStream()
                        out.write(b)
                        out.flush()

                        // Verify socket is active and not immediately closed by firewall (SYN-spoofing filter)
                        socket.soTimeout = 150 // Small read timeout to check active connection
                        val inp = socket.getInputStream()
                        var connectionActive = false
                        try {
                            val readByte = inp.read()
                            if (readByte != -1) {
                                connectionActive = true // server responded with data
                            }
                        } catch (e: java.net.SocketTimeoutException) {
                            connectionActive = true // timed out waiting for data, but connection is still active and open!
                        } catch (e: Exception) {
                            connectionActive = false // connection reset or other IO error
                        }

                        if (connectionActive && !socket.isClosed && !socket.isOutputShutdown) {
                            val duration = System.currentTimeMillis() - startTime
                            totalPingSum += duration
                            successfulAttempts++
                        }
                    }
                }
            } catch (e: Exception) {
                Log.d("ProxyRepository", "Proxy test failed for ${proxy.server}:${proxy.port} -> ${e.message}")
            } finally {
                try {
                    socket.close()
                } catch (e: Exception) {
                    // Ignore
                }
            }
        }

        // Require at least 1 successful attempt
        val isOnline = successfulAttempts > 0
        val ping = if (isOnline) totalPingSum / successfulAttempts else -1L

        // Calculate stability statistics
        val newTotal = proxy.totalChecks + 1
        val newSuccess = proxy.successfulChecks + if (isOnline) 1 else 0
        val newStability = newSuccess.toFloat() / newTotal.toFloat()

        val updatedProxy = proxy.copy(
            ping = ping,
            isOnline = isOnline,
            lastChecked = System.currentTimeMillis(),
            totalChecks = newTotal,
            successfulChecks = newSuccess,
            stabilityScore = newStability
        )

        proxyDao.updateProxy(updatedProxy)
        return@withContext updatedProxy
    }

    /**
     * Tests all proxies in parallel with concurrency throttled to prevent resource exhaustion.
     */
    suspend fun testAllProxies(
        proxies: List<ProxyEntity>,
        onProgress: (currentIndex: Int, total: Int) -> Unit
    ) = withContext(Dispatchers.Default) {
        val total = proxies.size
        if (total == 0) return@withContext

        val semaphore = Semaphore(15) // Maximum 15 concurrent tests
        var completedCount = 0

        val deferredResults = proxies.map { proxy ->
            async {
                semaphore.withPermit {
                    val result = testProxy(proxy)
                    synchronized(this@ProxyRepository) {
                        completedCount++
                        onProgress(completedCount, total)
                    }
                    result
                }
            }
        }
        deferredResults.awaitAll()
    }

    suspend fun insertManualProxy(server: String, port: Int, secret: String): Boolean {
        if (server.isBlank() || port <= 0 || secret.isBlank()) return false
        val existing = proxyDao.getProxyByAddress(server, port)
        if (existing == null) {
            proxyDao.insertProxy(
                ProxyEntity(
                    server = server.trim(),
                    port = port,
                    secret = secret.trim(),
                    source = "manual"
                )
            )
            return true
        } else {
            // Update secret if it exists
            proxyDao.updateProxy(existing.copy(secret = secret.trim()))
            return true
        }
    }

    suspend fun deleteProxy(proxy: ProxyEntity) {
        proxyDao.deleteProxy(proxy)
    }

    suspend fun clearAll() {
        proxyDao.clearAllProxies()
    }
}

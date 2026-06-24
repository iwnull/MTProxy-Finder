package com.example.ui

import android.app.Application
import android.content.Context
import android.util.Log
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ProxyDatabase
import com.example.data.model.ProxyEntity
import com.example.data.repository.ProxyRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class ProxyViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ProxyRepository
    
    // UI state streams
    val allProxies: StateFlow<List<ProxyEntity>>
    
    // Filtered list computed from original allProxies flow and search/sort UI preferences
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _sortBy = MutableStateFlow("ping") // "ping", "stability", "port", "last_checked"
    val sortBy = _sortBy.asStateFlow()

    private val _filterBy = MutableStateFlow("all") // "all", "online", "github", "manual"
    val filterBy = _filterBy.asStateFlow()

    val filteredProxies: StateFlow<List<ProxyEntity>>

    // Loading & Testing states
    var isRefreshing by mutableStateOf(false)
        private set

    var isTesting by mutableStateOf(false)
        private set

    var currentTestIndex by mutableStateOf(0)
        private set

    var totalTestCount by mutableStateOf(0)
        private set

    // Configuration / Theme states
    var darkThemeEnabled by mutableStateOf(true) // User can toggle this freely
    var appLanguage by mutableStateOf("fa") // "fa", "en", "ru"
        private set

    init {
        val database = ProxyDatabase.getDatabase(application)
        repository = ProxyRepository(database.proxyDao())
        
        // Clear all proxies on launch so there are no proxies initially
        viewModelScope.launch {
            repository.clearAll()
        }

        allProxies = repository.allProxies.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Read configuration preferences
        val prefs = application.getSharedPreferences("proxy_checker_prefs", Context.MODE_PRIVATE)
        darkThemeEnabled = prefs.getBoolean("dark_theme", true)
        appLanguage = prefs.getString("app_language", "fa") ?: "fa"

        // Combine flows to compute filtered/sorted lists dynamically in VM
        filteredProxies = combine(allProxies, _searchQuery, _sortBy) { list, query, sort ->
            var result = list

            // Apply search query
            if (query.isNotBlank()) {
                val q = query.lowercase().trim()
                result = result.filter {
                    it.server.lowercase().contains(q) ||
                    it.port.toString().contains(q) ||
                    it.secret.lowercase().contains(q)
                }
            }

            // Apply sort and custom filters per tab
            result = when (sort) {
                "stable" -> {
                    // Only working (online) and domain-based hosts (contains letters)
                    result.filter { it.isOnline && it.server.any { char -> char.isLetter() } }
                        .sortedBy { it.ping }
                }
                else -> { // "ping"
                    // Only working (online) proxies sorted by lowest ping
                    result.filter { it.isOnline }
                        .sortedBy { it.ping }
                }
            }

            result
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun toggleTheme() {
        darkThemeEnabled = !darkThemeEnabled
        val prefs = getApplication<Application>().getSharedPreferences("proxy_checker_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("dark_theme", darkThemeEnabled).apply()
    }

    fun setLanguage(lang: String) {
        appLanguage = lang
        val prefs = getApplication<Application>().getSharedPreferences("proxy_checker_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("app_language", lang).apply()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSortBy(sort: String) {
        _sortBy.value = sort
    }

    fun setFilterBy(filter: String) {
        _filterBy.value = filter
    }

    /**
     * Fetches proxies from Github, then automatically conducts speed tests on them.
     */
    fun fetchProxies() {
        if (isRefreshing || isTesting) return
        viewModelScope.launch {
            isRefreshing = true
            var added = 0
            try {
                added = repository.fetchAndLoadProxies()
                val msg = when (appLanguage) {
                    "en" -> when (added) {
                        -1 -> "No new updates on GitHub; your list is already up to date!"
                        0 -> "No proxies found or error loading."
                        else -> "Successfully received $added new proxies! Running auto-test..."
                    }
                    "ru" -> when (added) {
                        -1 -> "Новых обновлений пока нет; ваш список уже актуален!"
                        0 -> "Прокси не найдены или ошибка загрузки."
                        else -> "Успешно получено $added новых прокси! Начало авто-проверки..."
                    }
                    else -> when (added) { // "fa"
                        -1 -> "هنوز آپدیت جدیدی داده نشده است؛ لیست شما از قبل بروزرسانی شده است!"
                        0 -> "پروکسی جدیدی یافت نشد یا خطا در بارگذاری."
                        else -> "تعداد $added پروکسی جدید با موفقیت دریافت شد! در حال سنجش پینگ خودکار..."
                    }
                }
                Toast.makeText(getApplication(), msg, Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Log.e("ProxyViewModel", "Failed to refresh proxies", e)
                val errMsg = when (appLanguage) {
                    "en" -> "Error establishing connection or receiving proxies"
                    "ru" -> "Ошибка подключения или получения прокси"
                    else -> "خطا در برقراری ارتباط یا دریافت پروکسی‌ها"
                }
                Toast.makeText(getApplication(), errMsg, Toast.LENGTH_SHORT).show()
            } finally {
                isRefreshing = false
            }

            // Immediately run ping test on all current proxies
            val currentList = allProxies.value
            if (currentList.isNotEmpty()) {
                isTesting = true
                currentTestIndex = 0
                totalTestCount = currentList.size
                try {
                    repository.testAllProxies(currentList) { index, total ->
                        currentTestIndex = index
                        totalTestCount = total
                    }
                    val successMsg = when (appLanguage) {
                        "en" -> "Proxies fetched and tested successfully!"
                        "ru" -> "Прокси успешно получены и проверены!"
                        else -> "دریافت و تست پینگ پروکسی‌ها با موفقیت پایان یافت!"
                    }
                    Toast.makeText(getApplication(), successMsg, Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Log.e("ProxyViewModel", "Auto speed check interrupted", e)
                } finally {
                    isTesting = false
                }
            }
        }
    }

    /**
     * Conducts ping/speed tests on all listed proxies in real-time.
     */
    fun testAll() {
        if (isTesting) return
        val currentList = allProxies.value
        if (currentList.isEmpty()) {
            val emptyMsg = when (appLanguage) {
                "en" -> "Proxy list is empty. Please fetch first."
                "ru" -> "Список прокси пуст. Пожалуйста, сначала обновите его."
                else -> "لیست پروکسی‌ها خالی است. ابتدا بروزرسانی کنید."
            }
            Toast.makeText(getApplication(), emptyMsg, Toast.LENGTH_SHORT).show()
            return
        }

        viewModelScope.launch {
            isTesting = true
            currentTestIndex = 0
            totalTestCount = currentList.size

            try {
                repository.testAllProxies(currentList) { index, total ->
                    currentTestIndex = index
                    totalTestCount = total
                }
                val completedMsg = when (appLanguage) {
                    "en" -> "Speed check completed!"
                    "ru" -> "Тест скорости завершен!"
                    else -> "تست سرعت پروکسی‌ها به پایان رسید"
                }
                Toast.makeText(getApplication(), completedMsg, Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("ProxyViewModel", "Speed check interrupted", e)
            } finally {
                isTesting = false
            }
        }
    }

    /**
     * Tests a single proxy speed and state in the background.
     */
    fun testSingleProxy(proxy: ProxyEntity) {
        viewModelScope.launch {
            try {
                repository.testProxy(proxy)
            } catch (e: Exception) {
                Log.e("ProxyViewModel", "Single speed check failed", e)
            }
        }
    }

    /**
     * Adds a manual MTProto proxy.
     */
    fun addManualProxy(server: String, port: Int, secret: String) {
        viewModelScope.launch {
            val success = repository.insertManualProxy(server, port, secret)
            if (success) {
                val successMsg = when (appLanguage) {
                    "en" -> "Proxy added successfully!"
                    "ru" -> "Прокси успешно добавлен!"
                    else -> "پروکسی با موفقیت اضافه شد"
                }
                Toast.makeText(getApplication(), successMsg, Toast.LENGTH_SHORT).show()
                // Speed-test the newly added manual proxy
                val list = allProxies.value
                val newlyAdded = list.find { it.server == server.trim() && it.port == port }
                if (newlyAdded != null) {
                    repository.testProxy(newlyAdded)
                }
            } else {
                val failMsg = when (appLanguage) {
                    "en" -> "Error in inputs format"
                    "ru" -> "Ошибка в формате ввода"
                    else -> "خطا در فرمت ورودی‌ها"
                }
                Toast.makeText(getApplication(), failMsg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Imports proxies from custom raw copy-pasted block of text.
     */
    fun importFromText(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            val parsedList = repository.parseProxyString(text, "imported")
            if (parsedList.isEmpty()) {
                val noValidMsg = when (appLanguage) {
                    "en" -> "No valid proxies found"
                    "ru" -> "Действительные прокси не найдены"
                    else -> "هیچ پروکسی معتبری یافت نشد"
                }
                Toast.makeText(getApplication(), noValidMsg, Toast.LENGTH_SHORT).show()
                return@launch
            }

            var importedCount = 0
            val db = ProxyDatabase.getDatabase(getApplication())
            for (proxy in parsedList) {
                val existing = db.proxyDao().getProxyByAddress(proxy.server, proxy.port)
                if (existing == null) {
                    db.proxyDao().insertProxy(proxy)
                    importedCount++
                }
            }

            val importSuccessMsg = when (appLanguage) {
                "en" -> "Imported $importedCount new proxies! Tap test speed to verify pings."
                "ru" -> "Импортировано $importedCount новых прокси! Нажмите тест скорости для проверки."
                else -> "$importedCount پروکسی جدید وارد شد. برای دریافت پینگ دکمه تست سرعت را بزنید."
            }
            Toast.makeText(getApplication(), importSuccessMsg, Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Deletes a proxy.
     */
    fun deleteProxy(proxy: ProxyEntity) {
        viewModelScope.launch {
            repository.deleteProxy(proxy)
        }
    }

    /**
     * Clears database.
     */
    fun clearAllProxies() {
        viewModelScope.launch {
            repository.clearAll()
            val clearedMsg = when (appLanguage) {
                "en" -> "All proxies deleted successfully"
                "ru" -> "Все прокси успешно удалены"
                else -> "تمام پروکسی‌ها حذف شدند"
            }
            Toast.makeText(getApplication(), clearedMsg, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Connects to Telegram via proxy deep link.
     */
    fun connectToTelegram(proxy: ProxyEntity) {
        val uriString = proxy.connectionUrl
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uriString)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            getApplication<Application>().startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(getApplication(), "برنامه تلگرام روی دستگاه شما نصب نیست یا یافت نشد", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Saves/Exports healthy online proxies to a public txt file.
     */
    fun exportProxiesToFile() {
        val list = allProxies.value.filter { it.isOnline }
        if (list.isEmpty()) {
            Toast.makeText(getApplication(), "هیچ پروکسی سالمی (آنلاین) برای خروجی گرفتن وجود ندارد", Toast.LENGTH_SHORT).show()
            return
        }

        val content = list.joinToString("\n") { it.connectionUrl }
        try {
            // Write to local internal cache/files directory first
            val fileName = "telegram_proxies_${System.currentTimeMillis()}.txt"
            val file = File(getApplication<Application>().filesDir, fileName)
            FileOutputStream(file).use { stream ->
                stream.write(content.toByteArray())
            }

            // In our sandbox/runtime, showing a copy dialog or sharing is the most useful form of export.
            // Let's copy to clipboard and also write to external documents if possible.
            val clipboard = getApplication<Application>().getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText("Proxies List", content)
            clipboard.setPrimaryClip(clip)

            Toast.makeText(getApplication(), "لیست پروکسی‌ها در کلیپ‌بورد کپی شد و در فایل ذخیره شد: $fileName", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(getApplication(), "خطا در خروجی گرفتن فایل: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}

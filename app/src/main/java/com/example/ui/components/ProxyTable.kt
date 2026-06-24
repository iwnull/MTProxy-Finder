package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProxyEntity
import com.example.ui.Localization
import com.example.ui.theme.*

@Composable
fun ProxyTable(
    proxies: List<ProxyEntity>,
    lang: String,
    onConnect: (ProxyEntity) -> Unit,
    onDelete: (ProxyEntity) -> Unit,
    onTestProxy: (ProxyEntity) -> Unit,
    sortBy: String,
    filterBy: String,
    onSortChange: (String) -> Unit,
    onFilterChange: (String) -> Unit,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    var selectedProxyForDetails by remember { mutableStateOf<ProxyEntity?>(null) }

    val alignEnd = if (lang == "fa") Alignment.End else Alignment.Start
    val alignRight = if (lang == "fa") TextAlign.Right else TextAlign.Left

    val searchPlaceholder = when (lang) {
        "en" -> "Search servers, port or secret..."
        "ru" -> "Поиск серверов, портов или секретов..."
        else -> "جستجو در سرورها، پورت یا سکرت..."
    }

    val tabFastestText = when (lang) {
        "en" -> "Fastest"
        "ru" -> "Быстрые"
        else -> "سریع‌ترین‌ها"
    }

    val tabStableText = when (lang) {
        "en" -> "Most Stable"
        "ru" -> "Стабильные"
        else -> "پایدارترین‌ها"
    }

    val subtitleCountSuffix = when (lang) {
        "en" -> " proxies found"
        "ru" -> " прокси найдено"
        else -> " پروکسی یافت شد"
    }

    val headerConnect = when (lang) {
        "en" -> "Connect"
        "ru" -> "Связь"
        else -> "اتصال"
    }

    val headerSpeed = when (lang) {
        "en" -> "Speed / Ping"
        "ru" -> "Скорость"
        else -> "سرعت / پینگ"
    }

    val headerPort = when (lang) {
        "en" -> "Port"
        "ru" -> "Порт"
        else -> "پورت"
    }

    val headerServer = when (lang) {
        "en" -> "Server Address"
        "ru" -> "Сервер"
        else -> "آدرس سرور"
    }

    val noProxiesText = when (lang) {
        "en" -> "No proxies found"
        "ru" -> "Прокси не найдены"
        else -> "هیچ پروکسی یافت نشد"
    }

    val stabilityText = when (lang) {
        "en" -> "Stability"
        "ru" -> "Стабильность"
        else -> "پایداری"
    }

    val successfulText = when (lang) {
        "en" -> "successful"
        "ru" -> "успешно"
        else -> "موفق"
    }

    val copyLinkText = when (lang) {
        "en" -> "Copy Link"
        "ru" -> "Копировать"
        else -> "کپی لینک"
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Search and Filters layout
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { 
                Text(
                    searchPlaceholder, 
                    textAlign = alignRight, 
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodyMedium
                ) 
            },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = null)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .testTag("search_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            )
        )

        // Modern Segmented Tab Bar for sorting
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Tab 1: Fastest (Ping)
                val isPingSelected = sortBy == "ping" || sortBy != "stable"
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isPingSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                        )
                        .clickable { onSortChange("ping") }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = if (isPingSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = tabFastestText,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isPingSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }

                // Tab 2: Stable (Domain-based)
                val isStableSelected = sortBy == "stable"
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isStableSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                        )
                        .clickable { onSortChange("stable") }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Dns,
                            contentDescription = null,
                            tint = if (isStableSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = tabStableText,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isStableSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // Subtitle row displaying count of items
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${proxies.size}$subtitleCountSuffix",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Text(
                text = if (sortBy == "stable") Localization.get("stable_subtitle", lang) else Localization.get("fast_subtitle", lang),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Beautiful Proxy Table Headers
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = headerConnect,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(0.7f),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = headerSpeed,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(0.9f),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = headerPort,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(0.5f),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = headerServer,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1.3f),
                    textAlign = alignRight,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Table Rows List
        if (proxies.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Inbox,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = noProxiesText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
            ) {
                itemsIndexed(proxies, key = { _, item -> item.id }) { index, proxy ->
                    val isEven = index % 2 == 0
                    val rowBgColor = if (isEven) {
                        MaterialTheme.colorScheme.surface
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f)
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(rowBgColor)
                            .clickable {
                                selectedProxyForDetails = if (selectedProxyForDetails == proxy) null else proxy
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp, horizontal = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Column 1: Connect direct deep link button
                            Box(
                                modifier = Modifier.weight(0.7f),
                                contentAlignment = Alignment.Center
                            ) {
                                Button(
                                    onClick = { onConnect(proxy) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (proxy.isOnline) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                                    ),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier
                                        .height(34.dp)
                                        .testTag("connect_btn_$index"),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = headerConnect,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(headerConnect, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Column 2: Ping Badge (Middle Left) - Clickable to test
                            Box(
                                modifier = Modifier.weight(0.9f),
                                contentAlignment = Alignment.Center
                            ) {
                                PingBadge(
                                    ping = proxy.ping,
                                    isOnline = proxy.isOnline,
                                    lastChecked = proxy.lastChecked,
                                    lang = lang,
                                    onClick = { onTestProxy(proxy) }
                                )
                            }

                            // Column 3: Port (Middle Right)
                            Text(
                                text = proxy.port.toString(),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(0.5f),
                                textAlign = TextAlign.Center
                            )

                            // Column 4: Server IP/Domain (Rightmost)
                            Text(
                                text = proxy.server,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1.3f),
                                textAlign = alignRight
                            )
                        }

                        // Expandable details (Copy, Share, Delete, Info)
                        AnimatedVisibility(
                            visible = selectedProxyForDetails == proxy,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.04f))
                                    .padding(vertical = 10.dp, horizontal = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Delete action
                                IconButton(
                                    onClick = { 
                                        onDelete(proxy)
                                        selectedProxyForDetails = null
                                    },
                                    colors = IconButtonDefaults.iconButtonColors(
                                        containerColor = Color.Red.copy(alpha = 0.1f)
                                    ),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = Color.Red,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                // Right side details
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Info Details Text
                                    Column(horizontalAlignment = alignEnd) {
                                        val ofStr = if (lang == "fa") "از" else if (lang == "ru") "из" else "of"
                                        Text(
                                            text = "$stabilityText: ${(proxy.stabilityScore * 100).toInt()}% (${proxy.successfulChecks} $ofStr ${proxy.totalChecks} $successfulText)",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                        
                                        val sourceLabel = Localization.get("source_label", lang)
                                        val sourceVal = when {
                                            proxy.source.contains("manual") -> Localization.get("source_manual", lang)
                                            proxy.source.contains("imported") -> if (lang == "fa") "وارد شده" else if (lang == "ru") "Импортировано" else "Imported"
                                            else -> Localization.get("source_github", lang)
                                        }
                                        Text(
                                            text = "$sourceLabel $sourceVal",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }

                                    // Copy link button
                                    Button(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(proxy.connectionUrl))
                                        },
                                        colors = ButtonDefaults.outlinedButtonColors(),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(copyLinkText, fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                        
                        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                    }
                }
            }
        }
    }
}

@Composable
fun PingBadge(
    ping: Long,
    isOnline: Boolean,
    lastChecked: Long,
    lang: String,
    onClick: (() -> Unit)? = null
) {
    val untestedText = when (lang) {
        "en" -> "Not Tested"
        "ru" -> "Не проверено"
        else -> "تست نشده"
    }
    val offlineText = when (lang) {
        "en" -> "Offline"
        "ru" -> "Офлайн"
        else -> "قطع"
    }

    val (bgColor, textColor, text) = when {
        lastChecked == 0L -> Triple(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), untestedText)
        !isOnline -> Triple(PingOffline.copy(alpha = 0.15f), PingOffline, offlineText)
        ping <= 300 -> Triple(PingExcellent.copy(alpha = 0.15f), PingExcellent, "$ping ms")
        ping <= 800 -> Triple(PingGood.copy(alpha = 0.15f), PingGood, "$ping ms")
        else -> Triple(PingPoor.copy(alpha = 0.15f), PingPoor, "$ping ms")
    }

    val baseModifier = Modifier
        .clip(RoundedCornerShape(8.dp))
        .background(bgColor)
    
    val finalModifier = if (onClick != null) {
        baseModifier.clickable(onClick = onClick)
    } else {
        baseModifier
    }

    Box(
        modifier = finalModifier.padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (isOnline && lastChecked > 0L) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(textColor, CircleShape)
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }
    }
}

package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.ui.platform.LocalContext
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.scale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ProxyViewModel
import com.example.ui.components.AddProxyDialog
import com.example.ui.components.DashboardCard
import com.example.ui.components.ProxyTable
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.Localization

class MainActivity : ComponentActivity() {
    
    private val viewModel: ProxyViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            MyApplicationTheme(darkTheme = viewModel.darkThemeEnabled) {
                // Support dynamic layout direction (RTL for Farsi, LTR for English/Russian)
                val layoutDirection = if (viewModel.appLanguage == "fa") LayoutDirection.Rtl else LayoutDirection.Ltr
                CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                    
                    var showAddDialog by remember { mutableStateOf(false) }
                    var showClearConfirm by remember { mutableStateOf(false) }
                    var showMenu by remember { mutableStateOf(false) }
                    var showCreatorDialog by remember { mutableStateOf(false) }
                    var showLanguageDialog by remember { mutableStateOf(false) }
                    
                    val allProxiesState by viewModel.allProxies.collectAsState()
                    val filteredProxiesState by viewModel.filteredProxies.collectAsState()
                    val searchQueryState by viewModel.searchQuery.collectAsState()
                    val sortByState by viewModel.sortBy.collectAsState()
                    val filterByState by viewModel.filterBy.collectAsState()

                    Scaffold(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background),
                        topBar = {
                            CenterAlignedTopAppBar(
                                title = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Security,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = Localization.get("title", viewModel.appLanguage),
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                    }
                                },
                                actions = {
                                    // Light/Dark Theme toggle
                                    IconButton(
                                        onClick = { viewModel.toggleTheme() },
                                        modifier = Modifier.testTag("theme_toggle_btn")
                                    ) {
                                        Icon(
                                            imageVector = if (viewModel.darkThemeEnabled) Icons.Default.LightMode else Icons.Default.DarkMode,
                                            contentDescription = Localization.get("theme_toggle", viewModel.appLanguage),
                                            tint = if (viewModel.darkThemeEnabled) Color(0xFFF59E0B) else MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    // Settings dropdown menu
                                    Box {
                                        IconButton(
                                            onClick = { showMenu = true },
                                            modifier = Modifier.testTag("settings_menu_btn")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Settings,
                                                contentDescription = Localization.get("settings_menu", viewModel.appLanguage),
                                                tint = MaterialTheme.colorScheme.onBackground
                                            )
                                        }

                                        DropdownMenu(
                                            expanded = showMenu,
                                            onDismissRequest = { showMenu = false }
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text(Localization.get("add_proxy", viewModel.appLanguage), fontWeight = FontWeight.SemiBold) },
                                                leadingIcon = { Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                                onClick = {
                                                    showMenu = false
                                                    showAddDialog = true
                                                }
                                            )
                                            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                                            DropdownMenuItem(
                                                text = { Text(Localization.get("change_lang", viewModel.appLanguage), fontWeight = FontWeight.SemiBold) },
                                                leadingIcon = { Icon(Icons.Default.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                                onClick = {
                                                    showMenu = false
                                                    showLanguageDialog = true
                                                }
                                            )
                                            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                                            DropdownMenuItem(
                                                text = { Text(Localization.get("clear_all", viewModel.appLanguage), color = Color.Red, fontWeight = FontWeight.SemiBold) },
                                                leadingIcon = { Icon(Icons.Default.DeleteForever, contentDescription = null, tint = Color.Red) },
                                                onClick = {
                                                    showMenu = false
                                                    showClearConfirm = true
                                                }
                                            )
                                            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                                            DropdownMenuItem(
                                                text = { Text(Localization.get("support", viewModel.appLanguage), fontWeight = FontWeight.SemiBold) },
                                                leadingIcon = { Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) },
                                                onClick = {
                                                    showMenu = false
                                                    showCreatorDialog = true
                                                }
                                            )
                                        }
                                    }
                                },
                                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                                )
                            )
                        },
                        bottomBar = {
                            Surface(
                                tonalElevation = 8.dp,
                                shadowElevation = 8.dp,
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .navigationBarsPadding()
                                        .padding(horizontal = 16.dp, vertical = 12.dp)
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val exportDesc = when (viewModel.appLanguage) {
                                        "en" -> "Export to file"
                                        "ru" -> "Экспорт в файл"
                                        else -> "خروجی گرفتن"
                                    }
                                    val speedDesc = when (viewModel.appLanguage) {
                                        "en" -> "Test all speed"
                                        "ru" -> "Тестировать скорость"
                                        else -> "تست مجدد سرعت"
                                    }

                                    // Export to file icon button
                                    IconButton(
                                        onClick = { viewModel.exportProxiesToFile() },
                                        enabled = allProxiesState.any { it.isOnline },
                                        colors = IconButtonDefaults.iconButtonColors(
                                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                                        ),
                                        modifier = Modifier
                                            .size(48.dp)
                                            .testTag("export_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Save,
                                            contentDescription = exportDesc,
                                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    // Re-test speed button
                                    IconButton(
                                        onClick = { viewModel.testAll() },
                                        enabled = !viewModel.isRefreshing && !viewModel.isTesting && allProxiesState.isNotEmpty(),
                                        colors = IconButtonDefaults.iconButtonColors(
                                            containerColor = Color(0xFF10B981).copy(alpha = 0.15f)
                                        ),
                                        modifier = Modifier
                                            .size(48.dp)
                                            .testTag("test_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Speed,
                                            contentDescription = speedDesc,
                                            tint = Color(0xFF10B981),
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    // Primary Get Proxies Button
                                    Button(
                                        onClick = { viewModel.fetchProxies() },
                                        enabled = !viewModel.isRefreshing && !viewModel.isTesting,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .testTag("fetch_btn"),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary
                                        )
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CloudDownload,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = Localization.get("get_proxies", viewModel.appLanguage),
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    ) { innerPadding ->
                        
                        // Main Body
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                                .padding(horizontal = 16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                             ) {
                                // 1. Dynamic dashboard stats
                                DashboardCard(proxies = allProxiesState, lang = viewModel.appLanguage)

                                // 2. Real-time testing speed & progress panel
                                AnimatedVisibility(
                                    visible = viewModel.isRefreshing || viewModel.isTesting,
                                    enter = expandVertically() + fadeIn(),
                                    exit = shrinkVertically() + fadeOut()
                                ) {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                        )
                                    ) {
                                        val progressAlign = if (viewModel.appLanguage == "fa") Alignment.End else Alignment.Start
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            horizontalAlignment = progressAlign
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                val ofText = when (viewModel.appLanguage) {
                                                    "en" -> "of"
                                                    "ru" -> "из"
                                                    else -> "از"
                                                }
                                                Text(
                                                    text = "${viewModel.currentTestIndex} $ofText ${viewModel.totalTestCount}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                
                                                val progressText = if (viewModel.isRefreshing) {
                                                    when (viewModel.appLanguage) {
                                                        "en" -> "Fetching new proxies..."
                                                        "ru" -> "Получение новых прокси..."
                                                        else -> "در حال دریافت پروکسی‌های جدید..."
                                                    }
                                                } else {
                                                    when (viewModel.appLanguage) {
                                                        "en" -> "Testing ping and speed of proxies..."
                                                        "ru" -> "Тестирование пинга и скорости прокси..."
                                                        else -> "در حال تست پینگ و سرعت پروکسی‌ها..."
                                                    }
                                                }
                                                Text(
                                                    text = progressText,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                            
                                            Spacer(modifier = Modifier.height(10.dp))
                                            
                                            val progress = if (viewModel.totalTestCount > 0) {
                                                viewModel.currentTestIndex.toFloat() / viewModel.totalTestCount.toFloat()
                                            } else {
                                                0.0f
                                            }
                                            LinearProgressIndicator(
                                                progress = progress,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(6.dp)
                                                    .clip(CircleShape),
                                                color = MaterialTheme.colorScheme.primary,
                                                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // 3. Neat, Ordered Table showing Proxy List results, given weight(1f) to solve scrolling issues
                                ProxyTable(
                                    proxies = filteredProxiesState,
                                    lang = viewModel.appLanguage,
                                    onConnect = { viewModel.connectToTelegram(it) },
                                    onDelete = { viewModel.deleteProxy(it) },
                                    onTestProxy = { viewModel.testSingleProxy(it) },
                                    sortBy = sortByState,
                                    filterBy = filterByState,
                                    onSortChange = { viewModel.setSortBy(it) },
                                    onFilterChange = { viewModel.setFilterBy(it) },
                                    searchQuery = searchQueryState,
                                    onSearchChange = { viewModel.setSearchQuery(it) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // Dialog for Adding / Importing proxies
                    if (showAddDialog) {
                        AddProxyDialog(
                            lang = viewModel.appLanguage,
                            onDismiss = { showAddDialog = false },
                            onAddManual = { s, p, sec -> viewModel.addManualProxy(s, p, sec) },
                            onImportText = { txt -> viewModel.importFromText(txt) }
                        )
                    }

                    // Language Selection Dialog
                    if (showLanguageDialog) {
                        val alignRight = if (viewModel.appLanguage == "fa") TextAlign.Right else TextAlign.Left
                        AlertDialog(
                            onDismissRequest = { showLanguageDialog = false },
                            confirmButton = {
                                Button(
                                    onClick = { showLanguageDialog = false },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(Localization.get("close", viewModel.appLanguage), fontWeight = FontWeight.Bold)
                                }
                            },
                            title = {
                                Text(
                                    text = Localization.get("select_lang", viewModel.appLanguage),
                                    fontWeight = FontWeight.Bold,
                                    textAlign = alignRight,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            },
                            text = {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    val langs = listOf(
                                        Triple("fa", "فارسی (Persian)", "🇮🇷"),
                                        Triple("en", "English (Foreign)", "🇬🇧"),
                                        Triple("ru", "Русский (Russian)", "🇷🇺")
                                    )
                                    langs.forEach { (code, label, flag) ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(
                                                    if (viewModel.appLanguage == code) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                                    else Color.Transparent
                                                )
                                                .clickable {
                                                    viewModel.setLanguage(code)
                                                    showLanguageDialog = false
                                                }
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Text(flag, fontSize = 20.sp)
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = if (viewModel.appLanguage == code) FontWeight.Bold else FontWeight.Normal,
                                                color = if (viewModel.appLanguage == code) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.weight(1f))
                                            if (viewModel.appLanguage == code) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }
                                }
                            },
                            shape = RoundedCornerShape(20.dp)
                        )
                    }

                    // Dialog for Clearing All database confirmation
                    if (showClearConfirm) {
                        val alignRight = if (viewModel.appLanguage == "fa") TextAlign.Right else TextAlign.Left
                        AlertDialog(
                            onDismissRequest = { showClearConfirm = false },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        viewModel.clearAllProxies()
                                        showClearConfirm = false
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                                ) {
                                    Text(Localization.get("yes_clear", viewModel.appLanguage))
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showClearConfirm = false }) {
                                    Text(Localization.get("cancel", viewModel.appLanguage))
                                }
                            },
                            title = { Text(Localization.get("clear_title", viewModel.appLanguage), fontWeight = FontWeight.Bold, textAlign = alignRight, modifier = Modifier.fillMaxWidth()) },
                            text = { Text(Localization.get("clear_body", viewModel.appLanguage), textAlign = alignRight, modifier = Modifier.fillMaxWidth()) },
                            shape = RoundedCornerShape(20.dp)
                        )
                    }

                    // Dialog for Creator Information & Support
                    if (showCreatorDialog) {
                        val context = LocalContext.current
                        val alignEnd = if (viewModel.appLanguage == "fa") Alignment.End else Alignment.Start
                        val alignRight = if (viewModel.appLanguage == "fa") TextAlign.Right else TextAlign.Left
                        val openLink = { url: String ->
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                val errStr = when (viewModel.appLanguage) {
                                    "en" -> "Error opening link"
                                    "ru" -> "Ошибка открытия ссылки"
                                    else -> "خطا در باز کردن لینک"
                                }
                                android.widget.Toast.makeText(context, errStr, android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }

                        AlertDialog(
                            onDismissRequest = { showCreatorDialog = false },
                            confirmButton = {
                                Button(
                                    onClick = { showCreatorDialog = false },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(Localization.get("close", viewModel.appLanguage), fontWeight = FontWeight.Bold)
                                }
                            },
                            title = {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = Localization.get("creator_dialog_title", viewModel.appLanguage),
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            },
                            text = {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    horizontalAlignment = alignEnd,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = Localization.get("app_description", viewModel.appLanguage),
                                        style = MaterialTheme.typography.bodyMedium,
                                        textAlign = alignRight,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                    )

                                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                                    // Creator
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { openLink("https://t.me/GCC_The_Best_Programmer_C") },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Link,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Column(horizontalAlignment = alignEnd) {
                                                Text(
                                                    text = Localization.get("contact_creator", viewModel.appLanguage),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "@GCC_The_Best_Programmer_C",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }

                                    // Channel
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { openLink("https://t.me/iwnull") },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Link,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Column(horizontalAlignment = alignEnd) {
                                                Text(
                                                    text = Localization.get("telegram_channel", viewModel.appLanguage),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "@iwnull",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }

                                    // GitHub
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { openLink("http://github.com/iwnull") },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Link,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Column(horizontalAlignment = alignEnd) {
                                                Text(
                                                    text = Localization.get("creator_github", viewModel.appLanguage),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "github.com/iwnull",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            },
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }
            }
        }
    }
}



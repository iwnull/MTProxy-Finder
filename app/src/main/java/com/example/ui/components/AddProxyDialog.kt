package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.Localization

@Composable
fun AddProxyDialog(
    lang: String,
    onDismiss: () -> Unit,
    onAddManual: (String, Int, String) -> Unit,
    onImportText: (String) -> Unit
) {
    var activeTab by remember { mutableStateOf(0) } // 0: Manual, 1: Bulk/Import
    
    // Manual inputs
    var server by remember { mutableStateOf("") }
    var port by remember { mutableStateOf("") }
    var secret by remember { mutableStateOf("") }
    
    // Bulk inputs
    var bulkText by remember { mutableStateOf("") }
    
    val clipboardManager = LocalClipboardManager.current

    val alignEnd = if (lang == "fa") Alignment.End else Alignment.Start
    val alignRight = if (lang == "fa") TextAlign.Right else TextAlign.Left

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = alignEnd
            ) {
                // Dialog Title
                Text(
                    text = Localization.get("add_new_title", lang),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp),
                    textAlign = alignRight
                )

                // Dynamic Tabs Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                ) {
                    // Bulk tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (activeTab == 1) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { activeTab = 1 }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = Localization.get("bulk_tab", lang),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (activeTab == 1) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }

                    // Manual tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (activeTab == 0) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { activeTab = 0 }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = Localization.get("single_tab", lang),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (activeTab == 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }

                // Tab Content
                if (activeTab == 0) {
                    // Manual Content
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val serverLabel = when (lang) {
                            "en" -> "Server Address (IP or Domain)"
                            "ru" -> "Адрес сервера (IP или домен)"
                            else -> "آدرس سرور (IP یا دامنه)"
                        }
                        val portLabel = when (lang) {
                            "en" -> "Server Port"
                            "ru" -> "Порт сервера"
                            else -> "پورت سرور"
                        }
                        val secretLabel = when (lang) {
                            "en" -> "Secret Key"
                            "ru" -> "Секретный код"
                            else -> "کد سکرت (Secret)"
                        }

                        // Server Input
                        OutlinedTextField(
                            value = server,
                            onValueChange = { server = it },
                            label = { Text(serverLabel, modifier = Modifier.fillMaxWidth(), textAlign = alignRight) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth().testTag("manual_server_input"),
                            placeholder = { Text("e.g. proxy.server.com", modifier = Modifier.fillMaxWidth(), textAlign = alignRight) }
                        )

                        // Port Input
                        OutlinedTextField(
                            value = port,
                            onValueChange = { port = it },
                            label = { Text(portLabel, modifier = Modifier.fillMaxWidth(), textAlign = alignRight) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().testTag("manual_port_input"),
                            placeholder = { Text("e.g. 443", modifier = Modifier.fillMaxWidth(), textAlign = alignRight) }
                        )

                        // Secret Input
                        OutlinedTextField(
                            value = secret,
                            onValueChange = { secret = it },
                            label = { Text(secretLabel, modifier = Modifier.fillMaxWidth(), textAlign = alignRight) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth().testTag("manual_secret_input"),
                            placeholder = { Text("e.g. dd00112233...", modifier = Modifier.fillMaxWidth(), textAlign = alignRight) }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Cancel button
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(Localization.get("cancel", lang), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }

                        // Add button
                        Button(
                            onClick = {
                                val portInt = port.toIntOrNull() ?: 0
                                onAddManual(server, portInt, secret)
                                onDismiss()
                            },
                            enabled = server.isNotBlank() && port.isNotBlank() && secret.isNotBlank(),
                            modifier = Modifier
                                .weight(1.5f)
                                .testTag("manual_add_btn"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(Localization.get("add", lang))
                        }
                    }
                } else {
                    // Bulk Content
                    Column(modifier = Modifier.fillMaxWidth()) {
                        val bulkInstruction = when (lang) {
                            "en" -> "Enter Telegram proxy links below (one per line):"
                            "ru" -> "Введите ссылки на прокси Telegram ниже (по одной в строке):"
                            else -> "لینک‌های پروکسی تلگرام را در کادر زیر وارد کنید. (هر لینک در یک خط قرار گیرد):"
                        }
                        val pasteDesc = when (lang) {
                            "en" -> "Paste"
                            "ru" -> "Вставить"
                            else -> "چسباندن متن"
                        }
                        val importBtnText = when (lang) {
                            "en" -> "Import List"
                            "ru" -> "Импортировать"
                            else -> "وارد کردن لیست"
                        }

                        Text(
                            text = bulkInstruction,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            textAlign = alignRight,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )

                        OutlinedTextField(
                            value = bulkText,
                            onValueChange = { bulkText = it },
                            placeholder = {
                                Text(
                                    "tg://proxy?server=...\nhttps://t.me/proxy?server=...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                    textAlign = TextAlign.Left,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .testTag("bulk_text_input"),
                            maxLines = 10,
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        clipboardManager.getText()?.let {
                                            bulkText = it.text
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.ContentPaste, contentDescription = pasteDesc)
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Cancel
                            TextButton(
                                onClick = onDismiss,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(Localization.get("cancel", lang), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }

                            // Import button
                            Button(
                                onClick = {
                                    onImportText(bulkText)
                                    onDismiss()
                                },
                                enabled = bulkText.isNotBlank(),
                                modifier = Modifier
                                    .weight(1.5f)
                                    .testTag("bulk_import_btn"),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Default.DataObject, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(importBtnText)
                            }
                        }
                    }
                }
            }
        }
    }
}

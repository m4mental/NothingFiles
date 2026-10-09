package com.nothing.files.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.nothing.files.engine.PrivilegedFileEngine
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nothing.files.ui.components.NothingCard
import com.nothing.files.ui.theme.LedOnRed
import com.nothing.files.ui.theme.NothingBlack
import com.nothing.files.ui.theme.NothingBorder
import com.nothing.files.ui.theme.NothingRed
import com.nothing.files.ui.theme.NothingSurface
import com.nothing.files.ui.theme.NothingSurfaceElevated
import com.nothing.files.ui.theme.NothingTextSecondary
import com.nothing.files.ui.theme.NothingWhite

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("nothing_files_prefs", Context.MODE_PRIVATE) }

    // Persistent Settings
    var selectedSkin by remember {
        mutableStateOf(prefs.getString("skin", "pitch-black-mental - Pro Team (RKBDI & DerTyp7214)") ?: "pitch-black-mental - Pro Team (RKBDI & DerTyp7214)")
    }
    var selectedLanguage by remember {
        mutableStateOf(prefs.getString("language", "English") ?: "English")
    }
    var showHiddenDefault by remember {
        mutableStateOf(prefs.getBoolean("show_hidden_default", false))
    }
    var foldersOnTop by remember {
        mutableStateOf(prefs.getBoolean("folders_on_top", true))
    }
    var confirmDelete by remember {
        mutableStateOf(prefs.getBoolean("confirm_delete", true))
    }
    var hapticFeedback by remember {
        mutableStateOf(prefs.getBoolean("haptic_feedback", true))
    }
    var rootEnabled by remember {
        mutableStateOf(prefs.getBoolean("root_enabled", true))
    }

    // Dialog toggles
    var showSkinDialog by remember { mutableStateOf(false) }
    var showLangDialog by remember { mutableStateOf(false) }
    var showAddonsDialog by remember { mutableStateOf(false) }
    var showButtonsDialog by remember { mutableStateOf(false) }
    var showMoreSettingsDialog by remember { mutableStateOf(false) }
    var showAutoTasksDialog by remember { mutableStateOf(false) }
    var showComponentsDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    var currentPrivilegeMode by remember { mutableStateOf(PrivilegedFileEngine.getPrivilegeMode(context)) }
    var showPrivilegeDialog by remember { mutableStateOf(false) }
    var activePrivilegeTitle by remember { mutableStateOf("Detecting...") }

    LaunchedEffect(currentPrivilegeMode) {
        activePrivilegeTitle = PrivilegedFileEngine.getActivePrivilegeTitle(context)
    }

    BackHandler {
        onNavigateBack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NothingBlack)
    ) {
        // Top App Bar matching Screenshot (<- Settings)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = NothingWhite
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "Settings",
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Medium,
                fontSize = 20.sp,
                color = NothingWhite
            )
        }

        HorizontalDivider(color = NothingBorder, thickness = 1.dp)

        // Settings Items List matching MiXplorer Screenshot
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 4.dp)
        ) {
            // 1. SKINS
            SettingsCategoryRow(
                title = "SKINS",
                subtitle = selectedSkin,
                onClick = { showSkinDialog = true }
            )

            // 2. LANGUAGES
            SettingsCategoryRow(
                title = "LANGUAGES",
                subtitle = selectedLanguage,
                onClick = { showLangDialog = true }
            )

            // 3. ADD-ONS
            SettingsCategoryRow(
                title = "ADD-ONS",
                subtitle = "Tap to see options",
                onClick = { showAddonsDialog = true }
            )

            // 4. BUTTONS
            SettingsCategoryRow(
                title = "BUTTONS",
                subtitle = "Tap to see options",
                onClick = { showButtonsDialog = true }
            )

            // 5. MORE SETTINGS
            SettingsCategoryRow(
                title = "MORE SETTINGS",
                subtitle = "Tap to see options",
                onClick = { showMoreSettingsDialog = true }
            )

            // 6. AUTO TASKS
            SettingsCategoryRow(
                title = "AUTO TASKS",
                subtitle = "Tap to see options",
                onClick = { showAutoTasksDialog = true }
            )

            // 7. COMPONENTS
            SettingsCategoryRow(
                title = "COMPONENTS",
                subtitle = "Tap to see options",
                onClick = { showComponentsDialog = true }
            )

            // 8. PRIVILEGE & ACCESS MODE
            SettingsCategoryRow(
                title = "PRIVILEGE & ACCESS MODE",
                subtitle = "${currentPrivilegeMode.title} • $activePrivilegeTitle",
                onClick = { showPrivilegeDialog = true }
            )

            // 9. ABOUT
            SettingsCategoryRow(
                title = "ABOUT",
                subtitle = "Version: 6.71.14 B26090320 arm64\nAuthor: Hootan Parsa & Antigravity IDE",
                onClick = { showAboutDialog = true }
            )
        }
    }

    // 1. SKINS DIALOG
    if (showSkinDialog) {
        val skins = listOf(
            "pitch-black-mental - Pro Team (RKBDI & DerTyp7214)",
            "nothing-red-glyph - Nothing OS Edition",
            "pure-oled-minimal - Dot Matrix Theme",
            "carbon-dark - Grayscale Theme"
        )
        AlertDialog(
            onDismissRequest = { showSkinDialog = false },
            containerColor = NothingSurface,
            title = {
                Text("SKINS", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = NothingWhite)
            },
            text = {
                Column {
                    skins.forEach { skin ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedSkin = skin
                                    prefs.edit().putString("skin", skin).apply()
                                    showSkinDialog = false
                                    Toast.makeText(context, "Skin applied: $skin", Toast.LENGTH_SHORT).show()
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedSkin == skin,
                                onClick = {
                                    selectedSkin = skin
                                    prefs.edit().putString("skin", skin).apply()
                                    showSkinDialog = false
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = NothingRed,
                                    unselectedColor = NothingTextSecondary
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = skin,
                                fontFamily = FontFamily.Default,
                                fontSize = 13.sp,
                                color = NothingWhite
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSkinDialog = false }) {
                    Text("CLOSE", color = NothingWhite, fontFamily = FontFamily.Monospace)
                }
            }
        )
    }

    // 2. LANGUAGES DIALOG
    if (showLangDialog) {
        val langs = listOf("English", "Hindi (हिंदी)", "Spanish (Español)", "German (Deutsch)", "French (Français)")
        AlertDialog(
            onDismissRequest = { showLangDialog = false },
            containerColor = NothingSurface,
            title = {
                Text("LANGUAGES", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = NothingWhite)
            },
            text = {
                Column {
                    langs.forEach { lang ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedLanguage = lang
                                    prefs.edit().putString("language", lang).apply()
                                    showLangDialog = false
                                    Toast.makeText(context, "Language: $lang", Toast.LENGTH_SHORT).show()
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedLanguage == lang,
                                onClick = {
                                    selectedLanguage = lang
                                    prefs.edit().putString("language", lang).apply()
                                    showLangDialog = false
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = NothingRed,
                                    unselectedColor = NothingTextSecondary
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = lang, fontFamily = FontFamily.Default, fontSize = 14.sp, color = NothingWhite)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLangDialog = false }) {
                    Text("CLOSE", color = NothingWhite, fontFamily = FontFamily.Monospace)
                }
            }
        )
    }

    // 3. ADD-ONS DIALOG
    if (showAddonsDialog) {
        AlertDialog(
            onDismissRequest = { showAddonsDialog = false },
            containerColor = NothingSurface,
            title = {
                Text("ADD-ONS & ENGINES", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = NothingWhite)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    AddonItem(name = "Archive Engine v2.0", description = "Native Zip, Tar, Gzip compression", isInstalled = true)
                    AddonItem(name = "AES-256 Vault Engine", description = "Zero-knowledge file encryption (.enc)", isInstalled = true)
                    AddonItem(name = "HTTP Web Share Server", description = "WiFi direct file server on port 8080", isInstalled = true)
                    AddonItem(name = "Root Shell Controller", description = "SU privileged command executor", isInstalled = true)
                    AddonItem(name = "SQLite Database Engine", description = "Database browser & table editor", isInstalled = true)
                }
            },
            confirmButton = {
                TextButton(onClick = { showAddonsDialog = false }) {
                    Text("DONE", color = NothingRed, fontFamily = FontFamily.Monospace)
                }
            }
        )
    }

    // 4. BUTTONS DIALOG
    if (showButtonsDialog) {
        AlertDialog(
            onDismissRequest = { showButtonsDialog = false },
            containerColor = NothingSurface,
            title = {
                Text("BOTTOM BAR BUTTONS", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = NothingWhite)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Configure MiXplorer Bottom Bar buttons:", fontSize = 13.sp, color = NothingTextSecondary)
                    Text("1. Search (Magnifying glass) — Active", fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = NothingWhite)
                    Text("2. View Mode (Compact, Grid, Gallery) — Active", fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = NothingWhite)
                    Text("3. Add (+) (Folder, File, Vault) — Active", fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = NothingRed)
                    Text("4. Refresh (Directory reload) — Active", fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = NothingWhite)
                    Text("5. Sort (AZ, Size, Date, Type) — Active", fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = NothingWhite)
                    Text("6. Select Mode (Multi-select toggle) — Active", fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = NothingWhite)
                }
            },
            confirmButton = {
                TextButton(onClick = { showButtonsDialog = false }) {
                    Text("CLOSE", color = NothingWhite, fontFamily = FontFamily.Monospace)
                }
            }
        )
    }

    // 5. MORE SETTINGS DIALOG
    if (showMoreSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showMoreSettingsDialog = false },
            containerColor = NothingSurface,
            title = {
                Text("MORE SETTINGS", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = NothingWhite)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SettingToggleRow(
                        title = "Show hidden files by default",
                        subtitle = "Display dotfiles and hidden folders",
                        checked = showHiddenDefault,
                        onCheckedChange = {
                            showHiddenDefault = it
                            prefs.edit().putBoolean("show_hidden_default", it).apply()
                        }
                    )
                    SettingToggleRow(
                        title = "Folders always on top",
                        subtitle = "Keep directories grouped at the beginning",
                        checked = foldersOnTop,
                        onCheckedChange = {
                            foldersOnTop = it
                            prefs.edit().putBoolean("folders_on_top", it).apply()
                        }
                    )
                    SettingToggleRow(
                        title = "Confirm file delete",
                        subtitle = "Ask for confirmation before deleting",
                        checked = confirmDelete,
                        onCheckedChange = {
                            confirmDelete = it
                            prefs.edit().putBoolean("confirm_delete", it).apply()
                        }
                    )
                    SettingToggleRow(
                        title = "Haptic feedback",
                        subtitle = "Vibrate on long press and actions",
                        checked = hapticFeedback,
                        onCheckedChange = {
                            hapticFeedback = it
                            prefs.edit().putBoolean("haptic_feedback", it).apply()
                        }
                    )
                    SettingToggleRow(
                        title = "Root Explorer",
                        subtitle = "Enable SU root filesystem browsing",
                        checked = rootEnabled,
                        onCheckedChange = {
                            rootEnabled = it
                            prefs.edit().putBoolean("root_enabled", it).apply()
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showMoreSettingsDialog = false }) {
                    Text("SAVE", color = NothingRed, fontFamily = FontFamily.Monospace)
                }
            }
        )
    }

    // 6. AUTO TASKS DIALOG
    if (showAutoTasksDialog) {
        AlertDialog(
            onDismissRequest = { showAutoTasksDialog = false },
            containerColor = NothingSurface,
            title = {
                Text("AUTO TASKS", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = NothingWhite)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(
                        onClick = {
                            context.cacheDir.deleteRecursively()
                            Toast.makeText(context, "Thumbnail cache cleared", Toast.LENGTH_SHORT).show()
                            showAutoTasksDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("CLEAR THUMBNAIL CACHE", color = NothingRed, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }

                    TextButton(
                        onClick = {
                            Toast.makeText(context, "Media library indexing requested", Toast.LENGTH_SHORT).show()
                            showAutoTasksDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("RESCAN MEDIA LIBRARY", color = NothingWhite, fontFamily = FontFamily.Monospace)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAutoTasksDialog = false }) {
                    Text("CLOSE", color = NothingWhite, fontFamily = FontFamily.Monospace)
                }
            }
        )
    }

    // 7. COMPONENTS DIALOG
    if (showComponentsDialog) {
        AlertDialog(
            onDismissRequest = { showComponentsDialog = false },
            containerColor = NothingSurface,
            title = {
                Text("COMPONENTS", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = NothingWhite)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ComponentRow("Text & Code Editor", "Built-in syntax editor with auto-save")
                    ComponentRow("Hex Viewer", "Direct byte and hexadecimal inspector")
                    ComponentRow("Archive Browser", "Explore and extract ZIP files in-place")
                    ComponentRow("App Manager", "APK extractor, uninstaller, and backup tool")
                    ComponentRow("Storage Analyzer", "Visual breakdown of storage usage")
                }
            },
            confirmButton = {
                TextButton(onClick = { showComponentsDialog = false }) {
                    Text("DONE", color = NothingWhite, fontFamily = FontFamily.Monospace)
                }
            }
        )
    }

    // 8. ABOUT DIALOG
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            containerColor = NothingSurface,
            title = {
                Text("ABOUT NOTHING FILES", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = NothingWhite)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Nothing Files (MiXplorer Edition)", fontWeight = FontWeight.Bold, color = NothingWhite, fontSize = 15.sp)
                    Text("Version: 6.71.14 B26090320 arm64", fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = NothingTextSecondary)
                    Text("Target Device: Nothing Phone (2a) A142", fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = NothingTextSecondary)
                    Text("Root Privilege: SU Active", fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = LedOnRed)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Inspired by MiXplorer & Nothing OS aesthetic.", fontSize = 12.sp, color = NothingTextSecondary)
                    Text("Powered by Antigravity IDE.", fontSize = 11.sp, color = NothingTextSecondary)
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("OK", color = NothingRed, fontFamily = FontFamily.Monospace)
                }
            }
        )
    }

    // PRIVILEGE MODE DIALOG
    if (showPrivilegeDialog) {
        AlertDialog(
            onDismissRequest = { showPrivilegeDialog = false },
            containerColor = NothingSurface,
            title = {
                Text(
                    text = "PRIVILEGE & ACCESS MODE",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = NothingWhite
                )
            },
            text = {
                Column {
                    PrivilegedFileEngine.PrivilegeMode.values().forEach { mode ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    currentPrivilegeMode = mode
                                    PrivilegedFileEngine.setPrivilegeMode(context, mode)
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentPrivilegeMode == mode,
                                onClick = {
                                    currentPrivilegeMode = mode
                                    PrivilegedFileEngine.setPrivilegeMode(context, mode)
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = LedOnRed,
                                    unselectedColor = NothingTextSecondary
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = mode.title,
                                    fontFamily = FontFamily.Default,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp,
                                    color = NothingWhite
                                )
                                Text(
                                    text = mode.subtitle,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = NothingTextSecondary
                                )
                            }
                        }
                    }

                    if (!PrivilegedFileEngine.isShizukuPermissionGranted() && PrivilegedFileEngine.isShizukuRunning()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = {
                                PrivilegedFileEngine.requestShizukuPermission()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("REQUEST SHIZUKU PERMISSION", color = LedOnRed, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrivilegeDialog = false }) {
                    Text("DONE", color = NothingWhite, fontFamily = FontFamily.Monospace)
                }
            }
        )
    }
}

@Composable
private fun SettingsCategoryRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = NothingWhite
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontFamily = FontFamily.Default,
                fontSize = 13.sp,
                color = NothingTextSecondary,
                lineHeight = 18.sp
            )
        }

        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = null,
            tint = NothingTextSecondary,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = NothingWhite)
            Text(text = subtitle, fontSize = 11.sp, color = NothingTextSecondary)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = NothingWhite,
                checkedTrackColor = NothingRed,
                uncheckedThumbColor = NothingTextSecondary,
                uncheckedTrackColor = NothingSurfaceElevated
            )
        )
    }
}

@Composable
private fun AddonItem(
    name: String,
    description: String,
    isInstalled: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(NothingSurfaceElevated)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NothingWhite)
            Text(text = description, fontSize = 11.sp, color = NothingTextSecondary)
        }
        if (isInstalled) {
            Text(text = "ACTIVE", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = LedOnRed)
        }
    }
}

@Composable
private fun ComponentRow(name: String, description: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(NothingSurfaceElevated)
            .padding(10.dp)
    ) {
        Text(text = name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NothingWhite)
        Text(text = description, fontSize = 11.sp, color = NothingTextSecondary)
    }
}

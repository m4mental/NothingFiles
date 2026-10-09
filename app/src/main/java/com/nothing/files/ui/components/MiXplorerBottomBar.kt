package com.nothing.files.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nothing.files.engine.SortOption
import com.nothing.files.model.ViewMode
import com.nothing.files.ui.theme.LedOnRed
import com.nothing.files.ui.theme.NothingBlack
import com.nothing.files.ui.theme.NothingBorder
import com.nothing.files.ui.theme.NothingRed
import com.nothing.files.ui.theme.NothingSurface
import com.nothing.files.ui.theme.NothingTextSecondary
import com.nothing.files.ui.theme.NothingWhite

enum class AddAction {
    FOLDER,
    FILE,
    DATABASE,
    ENCRYPTED,
    KEYSTORE,
    SHORTCUT,
    DOT_FILE,
    DOCUMENT,
    STORAGE
}

@Composable
fun MiXplorerBottomBar(
    onSearchClick: () -> Unit = {},
    activeViewMode: ViewMode = ViewMode.COMPACT,
    onSelectViewMode: (ViewMode) -> Unit = {},
    onViewOptionsClick: () -> Unit = {},
    onAddAction: (AddAction) -> Unit = {},
    onRefreshClick: () -> Unit = {},
    activeSortOption: SortOption = SortOption.NAME_ASC,
    onSelectSortOption: (SortOption) -> Unit = {},
    onSortOptionsClick: () -> Unit = {},
    onSelectAllClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showViewModeMenu by remember { mutableStateOf(false) }
    var showAddMenu by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

    NothingCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0.dp),
        backgroundColor = NothingSurface,
        borderColor = NothingBorder
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Button 1: Search
            IconButton(onClick = onSearchClick, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = NothingWhite,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Button 2: View Mode (Popup Menu matching Screenshot shot_171531.png)
            Box {
                IconButton(onClick = { showViewModeMenu = true }, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector = when (activeViewMode) {
                            ViewMode.GRID -> Icons.Default.GridView
                            ViewMode.MINIMAL -> Icons.Default.Menu
                            else -> Icons.AutoMirrored.Filled.List
                        },
                        contentDescription = "View Mode",
                        tint = NothingWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }

                DropdownMenu(
                    expanded = showViewModeMenu,
                    onDismissRequest = { showViewModeMenu = false },
                    modifier = Modifier.background(NothingSurface)
                ) {
                    val viewModes = listOf(
                        ViewMode.COMPACT to Icons.AutoMirrored.Filled.List,
                        ViewMode.COLUMNED to Icons.AutoMirrored.Filled.List,
                        ViewMode.DETAILED to Icons.AutoMirrored.Filled.List,
                        ViewMode.WRAPPED to Icons.Default.Menu,
                        ViewMode.MINIMAL to Icons.Default.Menu,
                        ViewMode.GRID to Icons.Default.GridView,
                        ViewMode.GALLERY to Icons.Default.GridView
                    )

                    viewModes.forEach { (mode, icon) ->
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (activeViewMode == mode) NothingRed else NothingWhite,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            text = {
                                Text(
                                    text = mode.label,
                                    fontFamily = FontFamily.Default,
                                    fontWeight = if (activeViewMode == mode) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 14.sp,
                                    color = if (activeViewMode == mode) NothingRed else NothingWhite
                                )
                            },
                            trailingIcon = if (activeViewMode == mode) {
                                { Icon(Icons.Default.Check, contentDescription = null, tint = NothingRed, modifier = Modifier.size(16.dp)) }
                            } else null,
                            onClick = {
                                showViewModeMenu = false
                                onSelectViewMode(mode)
                            }
                        )
                    }

                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Options...",
                                fontFamily = FontFamily.Default,
                                fontSize = 14.sp,
                                color = NothingTextSecondary
                            )
                        },
                        onClick = {
                            showViewModeMenu = false
                            onViewOptionsClick()
                        }
                    )
                }
            }

            // Button 3: Add (+) (Popup Menu matching Screenshot shot_171553.png)
            Box {
                IconButton(onClick = { showAddMenu = true }, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                        tint = NothingRed,
                        modifier = Modifier.size(24.dp)
                    )
                }

                DropdownMenu(
                    expanded = showAddMenu,
                    onDismissRequest = { showAddMenu = false },
                    modifier = Modifier.background(NothingSurface)
                ) {
                    val addItems = listOf(
                        "Folder" to AddAction.FOLDER,
                        "File" to AddAction.FILE,
                        "Database" to AddAction.DATABASE,
                        "Encrypted" to AddAction.ENCRYPTED,
                        "KeyStore" to AddAction.KEYSTORE,
                        "Shortcut" to AddAction.SHORTCUT,
                        "Dot file" to AddAction.DOT_FILE,
                        "Document" to AddAction.DOCUMENT,
                        "Storage" to AddAction.STORAGE
                    )

                    addItems.forEach { (label, action) ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = label,
                                    fontFamily = FontFamily.Default,
                                    fontSize = 14.sp,
                                    color = NothingWhite
                                )
                            },
                            onClick = {
                                showAddMenu = false
                                onAddAction(action)
                            }
                        )
                    }
                }
            }

            // Button 4: Refresh
            IconButton(onClick = onRefreshClick, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = NothingWhite,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Button 5: Sort (AZ) (Popup Menu matching Screenshot shot_171621.png)
            Box {
                IconButton(onClick = { showSortMenu = true }, modifier = Modifier.size(40.dp)) {
                    Text(
                        text = "AZ",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = NothingWhite
                    )
                }

                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false },
                    modifier = Modifier.background(NothingSurface)
                ) {
                    val sortItems = listOf(
                        SortOption.NAME_ASC to Icons.AutoMirrored.Filled.Sort,
                        SortOption.NAME_DESC to Icons.AutoMirrored.Filled.Sort,
                        SortOption.SIZE_ASC to Icons.AutoMirrored.Filled.Sort,
                        SortOption.SIZE_DESC to Icons.AutoMirrored.Filled.Sort,
                        SortOption.DATE_ASC to Icons.Default.History,
                        SortOption.DATE_DESC to Icons.Default.History,
                        SortOption.TYPE_ASC to Icons.AutoMirrored.Filled.Sort,
                        SortOption.TYPE_DESC to Icons.AutoMirrored.Filled.Sort,
                        SortOption.SHUFFLED to Icons.Default.Shuffle
                    )

                    sortItems.forEach { (sortOpt, icon) ->
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (activeSortOption == sortOpt) NothingRed else NothingWhite,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            text = {
                                Text(
                                    text = sortOpt.title,
                                    fontFamily = FontFamily.Default,
                                    fontWeight = if (activeSortOption == sortOpt) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 14.sp,
                                    color = if (activeSortOption == sortOpt) NothingRed else NothingWhite
                                )
                            },
                            trailingIcon = if (activeSortOption == sortOpt) {
                                { Icon(Icons.Default.Check, contentDescription = null, tint = NothingRed, modifier = Modifier.size(16.dp)) }
                            } else null,
                            onClick = {
                                showSortMenu = false
                                onSelectSortOption(sortOpt)
                            }
                        )
                    }

                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Options...",
                                fontFamily = FontFamily.Default,
                                fontSize = 14.sp,
                                color = NothingTextSecondary
                            )
                        },
                        onClick = {
                            showSortMenu = false
                            onSortOptionsClick()
                        }
                    )
                }
            }

            // Button 6: Multi-Select Toggle / Select All
            IconButton(onClick = onSelectAllClick, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.Default.SelectAll,
                    contentDescription = "Multi-Select",
                    tint = NothingWhite,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

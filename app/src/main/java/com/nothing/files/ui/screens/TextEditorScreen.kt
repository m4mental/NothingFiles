package com.nothing.files.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FindReplace
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WrapText
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nothing.files.engine.PrivilegedFileEngine
import com.nothing.files.model.FileItem
import com.nothing.files.ui.components.NothingCard
import com.nothing.files.ui.theme.LedOnRed
import com.nothing.files.ui.theme.NothingBlack
import com.nothing.files.ui.theme.NothingBorder
import com.nothing.files.ui.theme.NothingRed
import com.nothing.files.ui.theme.NothingSurface
import com.nothing.files.ui.theme.NothingSurfaceElevated
import com.nothing.files.ui.theme.NothingTextSecondary
import com.nothing.files.ui.theme.NothingWhite
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun TextEditorScreen(
    fileItem: FileItem,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var textValue by remember { mutableStateOf(TextFieldValue("")) }
    var originalText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    // Editor Configuration
    var isWordWrap by remember { mutableStateOf(false) }
    var isReadOnly by remember { mutableStateOf(false) }
    var fontSize by remember { mutableFloatStateOf(12f) }
    var showSearchBar by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var replaceQuery by remember { mutableStateOf("") }
    var currentMatchIndex by remember { mutableIntStateOf(0) }

    // Undo / Redo Stacks (Max 50 history steps)
    val undoStack = remember { mutableStateListOf<TextFieldValue>() }
    val redoStack = remember { mutableStateListOf<TextFieldValue>() }

    // Load File Content (with auto privileged fallback)
    LaunchedEffect(fileItem.path) {
        withContext(Dispatchers.IO) {
            val loaded = PrivilegedFileEngine.readText(fileItem.path) ?: ""
            textValue = TextFieldValue(loaded)
            originalText = loaded
            isLoading = false
        }
    }

    val isModified = textValue.text != originalText
    val lineCount = textValue.text.lines().size
    val charCount = textValue.text.length

    // Update text with undo history tracking
    fun updateWithHistory(newValue: TextFieldValue) {
        if (newValue.text != textValue.text) {
            if (undoStack.size > 50) undoStack.removeAt(0)
            undoStack.add(textValue)
            redoStack.clear()
        }
        textValue = newValue
    }

    fun handleUndo() {
        if (undoStack.isNotEmpty()) {
            val previous = undoStack.removeAt(undoStack.size - 1)
            redoStack.add(textValue)
            textValue = previous
        }
    }

    fun handleRedo() {
        if (redoStack.isNotEmpty()) {
            val next = redoStack.removeAt(redoStack.size - 1)
            undoStack.add(textValue)
            textValue = next
        }
    }

    fun insertAccessory(str: String) {
        val currentText = textValue.text
        val selStart = textValue.selection.min
        val selEnd = textValue.selection.max
        val toInsert = if (str == "TAB") "    " else str
        val newText = currentText.replaceRange(selStart, selEnd, toInsert)
        val newCursor = selStart + toInsert.length
        updateWithHistory(TextFieldValue(newText, TextRange(newCursor)))
    }

    // Save File with Privileged Fallback (Root / Shizuku)
    fun saveFile() {
        if (!isModified || isSaving) return
        isSaving = true
        scope.launch {
            val contentToSave = textValue.text
            val success = withContext(Dispatchers.IO) {
                PrivilegedFileEngine.writeText(fileItem.path, contentToSave)
            }
            isSaving = false
            if (success) {
                originalText = contentToSave
                Toast.makeText(context, "Saved successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Failed to save (Permission Denied)", Toast.LENGTH_SHORT).show()
            }
        }
    }

    BackHandler {
        if (showSearchBar) {
            showSearchBar = false
        } else {
            onBack()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NothingBlack)
    ) {
        // ==========================================
        // 1. Top Action Bar (MiXplorer Style)
        // ==========================================
        NothingCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(0.dp),
            backgroundColor = NothingSurface,
            borderColor = NothingBorder
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = NothingWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isModified) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(LedOnRed)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                        }
                        Text(
                            text = fileItem.name,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            maxLines = 1,
                            color = if (isModified) NothingRed else NothingWhite
                        )
                    }
                    Text(
                        text = "$lineCount lines • $charCount chars • ${fileItem.formattedSize} • UTF-8",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = NothingTextSecondary,
                        maxLines = 1
                    )
                }

                // Search Toggle
                IconButton(
                    onClick = { showSearchBar = !showSearchBar },
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Find",
                        tint = if (showSearchBar) NothingRed else NothingWhite,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Word Wrap Toggle
                IconButton(
                    onClick = { isWordWrap = !isWordWrap },
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.WrapText,
                        contentDescription = "Word Wrap",
                        tint = if (isWordWrap) NothingRed else NothingWhite,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Undo
                IconButton(
                    onClick = { handleUndo() },
                    enabled = undoStack.isNotEmpty(),
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Undo,
                        contentDescription = "Undo",
                        tint = if (undoStack.isNotEmpty()) NothingWhite else NothingBorder,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Redo
                IconButton(
                    onClick = { handleRedo() },
                    enabled = redoStack.isNotEmpty(),
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Redo,
                        contentDescription = "Redo",
                        tint = if (redoStack.isNotEmpty()) NothingWhite else NothingBorder,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Read-Only Lock Toggle
                IconButton(
                    onClick = { isReadOnly = !isReadOnly },
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = if (isReadOnly) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = "Read Only",
                        tint = if (isReadOnly) LedOnRed else NothingWhite,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Save Button
                IconButton(
                    onClick = { saveFile() },
                    enabled = isModified && !isSaving,
                    modifier = Modifier.size(34.dp)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            color = NothingRed,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Save",
                            tint = if (isModified) NothingRed else NothingBorder,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // ==========================================
        // 2. Find & Replace Bar
        // ==========================================
        if (showSearchBar) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NothingSurfaceElevated)
                    .border(1.dp, NothingBorder)
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = {
                                searchQuery = it
                                currentMatchIndex = 0
                            },
                            placeholder = { Text("Find...", color = NothingTextSecondary, fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            textStyle = TextStyle(color = NothingWhite, fontSize = 12.sp, fontFamily = FontFamily.Monospace),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NothingRed,
                                unfocusedBorderColor = NothingBorder,
                                focusedContainerColor = NothingBlack,
                                unfocusedContainerColor = NothingBlack
                            )
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        // Match count
                        val matchCount = if (searchQuery.isNotEmpty()) {
                            Regex.escape(searchQuery).toRegex(RegexOption.IGNORE_CASE).findAll(textValue.text).count()
                        } else 0

                        Text(
                            text = if (searchQuery.isEmpty()) "" else "$matchCount",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = if (matchCount > 0) NothingRed else NothingTextSecondary,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )

                        // Previous & Next Navigation
                        IconButton(
                            onClick = {
                                if (searchQuery.isNotEmpty()) {
                                    val matches = Regex.escape(searchQuery).toRegex(RegexOption.IGNORE_CASE).findAll(textValue.text).toList()
                                    if (matches.isNotEmpty()) {
                                        currentMatchIndex = if (currentMatchIndex <= 0) matches.size - 1 else currentMatchIndex - 1
                                        val m = matches[currentMatchIndex]
                                        textValue = textValue.copy(selection = TextRange(m.range.first, m.range.last + 1))
                                    }
                                }
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Prev", tint = NothingWhite, modifier = Modifier.size(20.dp))
                        }

                        IconButton(
                            onClick = {
                                if (searchQuery.isNotEmpty()) {
                                    val matches = Regex.escape(searchQuery).toRegex(RegexOption.IGNORE_CASE).findAll(textValue.text).toList()
                                    if (matches.isNotEmpty()) {
                                        currentMatchIndex = if (currentMatchIndex >= matches.size - 1) 0 else currentMatchIndex + 1
                                        val m = matches[currentMatchIndex]
                                        textValue = textValue.copy(selection = TextRange(m.range.first, m.range.last + 1))
                                    }
                                }
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Next", tint = NothingWhite, modifier = Modifier.size(20.dp))
                        }

                        IconButton(
                            onClick = { showSearchBar = false },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = NothingWhite, modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = replaceQuery,
                            onValueChange = { replaceQuery = it },
                            placeholder = { Text("Replace with...", color = NothingTextSecondary, fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            textStyle = TextStyle(color = NothingWhite, fontSize = 12.sp, fontFamily = FontFamily.Monospace),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NothingRed,
                                unfocusedBorderColor = NothingBorder,
                                focusedContainerColor = NothingBlack,
                                unfocusedContainerColor = NothingBlack
                            )
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        // Replace Single
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(NothingSurface)
                                .border(1.dp, NothingBorder, RoundedCornerShape(6.dp))
                                .clickable {
                                    if (searchQuery.isNotEmpty()) {
                                        val m = Regex.escape(searchQuery).toRegex(RegexOption.IGNORE_CASE).find(textValue.text, textValue.selection.min)
                                            ?: Regex.escape(searchQuery).toRegex(RegexOption.IGNORE_CASE).find(textValue.text)
                                        if (m != null) {
                                            val newText = textValue.text.replaceRange(m.range, replaceQuery)
                                            updateWithHistory(TextFieldValue(newText, TextRange(m.range.first + replaceQuery.length)))
                                        }
                                    }
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text("REPLACE", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = NothingWhite, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Replace All
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(NothingSurface)
                                .border(1.dp, NothingBorder, RoundedCornerShape(6.dp))
                                .clickable {
                                    if (searchQuery.isNotEmpty()) {
                                        val newText = textValue.text.replace(searchQuery, replaceQuery, ignoreCase = true)
                                        updateWithHistory(TextFieldValue(newText))
                                    }
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text("ALL", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = NothingRed, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // ==========================================
        // 3. Editor Content Area with Line Numbers
        // ==========================================
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = NothingRed, strokeWidth = 2.dp)
            }
        } else {
            val verticalScroll = rememberScrollState()
            val horizontalScroll = rememberScrollState()

            Box(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(verticalScroll)
                ) {
                    // Line Numbers Gutter
                    Column(
                        modifier = Modifier
                            .background(NothingSurfaceElevated)
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        for (i in 1..lineCount.coerceAtLeast(1)) {
                            Text(
                                text = i.toString(),
                                fontFamily = FontFamily.Monospace,
                                fontSize = fontSize.sp,
                                color = NothingTextSecondary.copy(alpha = 0.5f),
                                lineHeight = (fontSize * 1.5f).sp
                            )
                        }
                    }

                    // Spacer between gutter and text
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(NothingBorder)
                    )

                    // Text Field Area (with Syntax Highlighting VisualTransformation)
                    val visualTransformation = remember(fileItem.extension, searchQuery) {
                        CodeSyntaxVisualTransformation(fileItem.extension, searchQuery)
                    }

                    val editorModifier = if (isWordWrap) {
                        Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp, vertical = 8.dp)
                    } else {
                        Modifier
                            .weight(1f)
                            .horizontalScroll(horizontalScroll)
                            .padding(horizontal = 8.dp, vertical = 8.dp)
                    }

                    BasicTextField(
                        value = textValue,
                        onValueChange = {
                            if (!isReadOnly) updateWithHistory(it)
                        },
                        readOnly = isReadOnly,
                        modifier = editorModifier,
                        textStyle = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = fontSize.sp,
                            color = NothingWhite,
                            lineHeight = (fontSize * 1.5f).sp
                        ),
                        cursorBrush = SolidColor(NothingRed),
                        visualTransformation = visualTransformation
                    )
                }
            }
        }

        // ==========================================
        // 4. MiXplorer Quick Accessory Toolbar
        // ==========================================
        if (!isReadOnly) {
            val accessories = listOf(
                "TAB", "{", "}", "(", ")", "[", "]",
                "\"", "'", "=", ";", ":", "/", "\\",
                "<", ">", "$", "#", "@", "_", "-",
                "+", "*", "&", "|", "!", "?"
            )

            val accScroll = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NothingSurface)
                    .border(1.dp, NothingBorder)
                    .horizontalScroll(accScroll)
                    .padding(horizontal = 4.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Font Size Decrement
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(NothingBlack)
                        .border(1.dp, NothingBorder, RoundedCornerShape(4.dp))
                        .clickable { fontSize = (fontSize - 1f).coerceIn(9f, 24f) }
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("A-", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = NothingTextSecondary, fontWeight = FontWeight.Bold)
                }

                // Font Size Increment
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(NothingBlack)
                        .border(1.dp, NothingBorder, RoundedCornerShape(4.dp))
                        .clickable { fontSize = (fontSize + 1f).coerceIn(9f, 24f) }
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("A+", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = NothingTextSecondary, fontWeight = FontWeight.Bold)
                }

                // Accessory Keys
                for (key in accessories) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(NothingBlack)
                            .border(1.dp, NothingBorder, RoundedCornerShape(4.dp))
                            .clickable { insertAccessory(key) }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = key,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = NothingWhite,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

/**
 * High-performance, zero-allocation Compose VisualTransformation for code syntax highlighting
 */
class CodeSyntaxVisualTransformation(
    private val extension: String,
    private val searchQuery: String
) : VisualTransformation {

    private val keywordRegex = "\\b(fun|val|var|class|interface|object|import|package|return|if|else|when|while|for|def|public|private|protected|internal|static|final|void|int|boolean|string|float|double|const|let|function|async|await|try|catch|finally|throw|new|this|super|null|true|false)\\b".toRegex()
    private val stringRegex = "(\"[^\"]*\"|'[^']*')".toRegex()
    private val commentRegex = "(//[^\n]*|/\\*[\\s\\S]*?\\*/|#[^\n]*)".toRegex()
    private val numberRegex = "\\b(\\d+(_\\d+)*(\\.\\d+)?|0x[0-9a-fA-F]+)\\b".toRegex()

    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        val annotated = buildAnnotatedString {
            append(raw)

            // 1. Strings (Amber / Warm Light Green)
            stringRegex.findAll(raw).forEach {
                addStyle(SpanStyle(color = Color(0xFFCE9178)), it.range.first, it.range.last + 1)
            }

            // 2. Numbers (Cyan / Soft Green)
            numberRegex.findAll(raw).forEach {
                addStyle(SpanStyle(color = Color(0xFFB5CEA8)), it.range.first, it.range.last + 1)
            }

            // 3. Keywords (Nothing Accent Red)
            keywordRegex.findAll(raw).forEach {
                addStyle(SpanStyle(color = NothingRed, fontWeight = FontWeight.Bold), it.range.first, it.range.last + 1)
            }

            // 4. Comments (Dim Muted Gray)
            commentRegex.findAll(raw).forEach {
                addStyle(SpanStyle(color = Color(0xFF6A9955)), it.range.first, it.range.last + 1)
            }

            // 5. Active Search Matches (Bright Yellow Background)
            if (searchQuery.isNotEmpty()) {
                Regex.escape(searchQuery).toRegex(RegexOption.IGNORE_CASE).findAll(raw).forEach {
                    addStyle(
                        SpanStyle(background = Color(0xFFF9A825), color = Color.Black, fontWeight = FontWeight.Bold),
                        it.range.first,
                        it.range.last + 1
                    )
                }
            }
        }
        return TransformedText(annotated, OffsetMapping.Identity)
    }
}

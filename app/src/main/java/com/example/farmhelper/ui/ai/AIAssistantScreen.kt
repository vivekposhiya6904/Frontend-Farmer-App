package com.example.farmhelper.ui.ai

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.farmhelper.session.SessionManager
import com.example.farmhelper.ui.ai.components.AttachmentOptionsBottomSheet
import com.example.farmhelper.ui.ai.components.LanguageSelectionBottomSheet
import com.example.farmhelper.ui.ai.components.VoiceAssistantBottomSheet
import com.example.farmhelper.ui.ai.models.*
import com.example.farmhelper.ui.ai.viewmodel.AIAssistantViewModel
import com.example.farmhelper.ui.theme.*
import java.io.File
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIAssistantScreen(
    onBackClick: () -> Unit = {},
    onOpenHistory: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    viewModel: AIAssistantViewModel = viewModel()
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val userFullName by sessionManager.userFullName.collectAsState(initial = "Farmer")

    val chatState by viewModel.chatUiState.collectAsState()
    val listState = rememberLazyListState()
    var showMoreMenu by remember { mutableStateOf(false) }

    // Camera File URI state
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    // Camera Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            val attachment = Attachment(
                id = UUID.randomUUID().toString(),
                uri = tempCameraUri.toString(),
                type = AttachmentType.IMAGE,
                name = "Camera_Photo_${System.currentTimeMillis()}.jpg"
            )
            viewModel.onAddAttachment(attachment)
            Toast.makeText(context, "Photo attached", Toast.LENGTH_SHORT).show()
        }
    }

    // Gallery Picker Launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val attachment = Attachment(
                id = UUID.randomUUID().toString(),
                uri = uri.toString(),
                type = AttachmentType.IMAGE,
                name = "Gallery_Image_${System.currentTimeMillis()}.jpg"
            )
            viewModel.onAddAttachment(attachment)
            Toast.makeText(context, "Image selected from gallery", Toast.LENGTH_SHORT).show()
        }
    }

    // Document Picker Launcher
    val documentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val attachment = Attachment(
                id = UUID.randomUUID().toString(),
                uri = uri.toString(),
                type = AttachmentType.DOCUMENT,
                name = "Soil_Test_Report.pdf",
                size = "1.2 MB"
            )
            viewModel.onAddAttachment(attachment)
            Toast.makeText(context, "Document attached", Toast.LENGTH_SHORT).show()
        }
    }

    // Auto scroll to bottom when messages update
    LaunchedEffect(chatState.messages.size, chatState.isThinking, chatState.isTyping) {
        if (chatState.messages.isNotEmpty()) {
            listState.animateScrollToItem(chatState.messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "AI Farming Assistant",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = ForestGreen,
                                fontSize = 18.sp
                            )
                        )
                        Text(
                            text = "Ask anything about farming • ${chatState.currentLanguage.displayName}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MediumGrayText,
                                fontSize = 11.sp
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = ForestGreen
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onOpenHistory) {
                        Icon(imageVector = Icons.Outlined.History, contentDescription = "History", tint = ForestGreen)
                    }
                    IconButton(onClick = { viewModel.onOpenLanguageSheet() }) {
                        Icon(imageVector = Icons.Outlined.Language, contentDescription = "Language", tint = ForestGreen)
                    }
                    Box {
                        IconButton(onClick = { showMoreMenu = true }) {
                            Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More Options", tint = ForestGreen)
                        }
                        DropdownMenu(
                            expanded = showMoreMenu,
                            onDismissRequest = { showMoreMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("AI Settings", color = DarkGrayText) },
                                onClick = {
                                    showMoreMenu = false
                                    onOpenSettings()
                                },
                                leadingIcon = {
                                    Icon(imageVector = Icons.Outlined.Settings, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(18.dp))
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Clear Chat", color = AlertRed) },
                                onClick = {
                                    showMoreMenu = false
                                    viewModel.onClearMessages()
                                },
                                leadingIcon = {
                                    Icon(imageVector = Icons.Outlined.Delete, contentDescription = null, tint = AlertRed, modifier = Modifier.size(18.dp))
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = WarmWhite)
            )
        },
        bottomBar = {
            ComposerBottomBar(
                input = chatState.currentInput,
                attachments = chatState.selectedAttachments,
                onInputChange = { viewModel.onInputChange(it) },
                onSend = { viewModel.onSendMessage() },
                onCameraClick = {
                    try {
                        val photoFile = File.createTempFile("photo_", ".jpg", context.cacheDir)
                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", photoFile)
                        tempCameraUri = uri
                        cameraLauncher.launch(uri)
                    } catch (e: Exception) {
                        galleryLauncher.launch("image/*")
                    }
                },
                onGalleryClick = { galleryLauncher.launch("image/*") },
                onVoiceClick = { viewModel.onOpenVoiceSheet() },
                onAttachmentClick = { viewModel.onOpenAttachmentSheet() },
                onRemoveAttachment = { id -> viewModel.onRemoveAttachment(id) }
            )
        },
        containerColor = WarmBeige
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
            ) {
                // Welcome Hero Header (Shown when chat is empty or at top)
                item {
                    WelcomeGreetingSection(
                        userName = userFullName ?: "Farmer",
                        quickPrompts = chatState.quickPrompts,
                        suggestions = chatState.suggestedCards,
                        onSelectPrompt = { prompt -> viewModel.onSelectQuickPrompt(prompt) }
                    )
                }

                // Chat Messages List
                items(chatState.messages, key = { it.id }) { message ->
                    ChatMessageItemCard(message = message)
                }

                // Thinking / Analyzing Indicator
                if (chatState.isThinking) {
                    item {
                        ThinkingIndicatorCard()
                    }
                }

                // Typing Indicator
                if (chatState.isTyping) {
                    item {
                        TypingIndicatorCard()
                    }
                }
            }
        }

        // Bottom Sheets
        if (chatState.isVoiceSheetOpen) {
            VoiceAssistantBottomSheet(
                voiceState = chatState.voiceState,
                onDismiss = { viewModel.onCloseVoiceSheet() },
                onToggleState = { viewModel.onToggleVoiceState() },
                onRetry = { viewModel.onToggleVoiceState() }
            )
        }

        if (chatState.isLanguageSheetOpen) {
            LanguageSelectionBottomSheet(
                languages = viewModel.availableLanguages,
                currentLanguage = chatState.currentLanguage,
                onDismiss = { viewModel.onCloseLanguageSheet() },
                onSelectLanguage = { lang -> viewModel.onSelectLanguage(lang) }
            )
        }

        if (chatState.isAttachmentSheetOpen) {
            AttachmentOptionsBottomSheet(
                onDismiss = { viewModel.onCloseAttachmentSheet() },
                onSelectOption = { option ->
                    viewModel.onCloseAttachmentSheet()
                    when (option) {
                        "camera" -> {
                            try {
                                val photoFile = File.createTempFile("photo_", ".jpg", context.cacheDir)
                                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", photoFile)
                                tempCameraUri = uri
                                cameraLauncher.launch(uri)
                            } catch (e: Exception) {
                                galleryLauncher.launch("image/*")
                            }
                        }
                        "gallery" -> galleryLauncher.launch("image/*")
                        "document" -> documentLauncher.launch("application/pdf")
                        "voice" -> viewModel.onOpenVoiceSheet()
                        else -> Toast.makeText(context, "Option selected: $option", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }
}

@Composable
private fun WelcomeGreetingSection(
    userName: String,
    quickPrompts: List<QuickPromptChip>,
    suggestions: List<SuggestionCard>,
    onSelectPrompt: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Welcome Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = WarmWhite),
            border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.15f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(ForestGreen.copy(alpha = 0.05f), SoftOlive)
                        )
                    )
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(ForestGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.SmartToy,
                            contentDescription = "AI Assistant",
                            tint = White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Hello $userName 👋",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = ForestGreen,
                                fontSize = 18.sp
                            )
                        )
                        Text(
                            text = "How can I help your farm today?",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = DarkGrayText,
                                fontSize = 13.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "AI Smart Suggestions",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = ForestGreen,
                        fontSize = 12.sp
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // AI Suggestions Horizontal Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    suggestions.forEach { card ->
                        Surface(
                            modifier = Modifier
                                .width(220.dp)
                                .clickable { onSelectPrompt(card.promptText) },
                            shape = RoundedCornerShape(16.dp),
                            color = WarmWhite,
                            border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.15f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = card.title,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = DarkGrayText,
                                        fontSize = 14.sp
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = card.subtitle,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MediumGrayText,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Quick Topics",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = ForestGreen,
                fontSize = 12.sp
            ),
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Quick Prompt Chips Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            quickPrompts.forEach { chip ->
                FilterChip(
                    selected = false,
                    onClick = { onSelectPrompt(chip.promptText) },
                    label = {
                        Text(text = chip.label, fontSize = 12.sp, color = DarkGrayText)
                    },
                    colors = FilterChipDefaults.filterChipColors(containerColor = SoftOlive),
                    border = FilterChipDefaults.filterChipBorder(enabled = true, selected = false, borderColor = ForestGreen.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    }
}

@Composable
private fun ChatMessageItemCard(message: ChatMessage) {
    val isUser = message.sender == Sender.USER

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            modifier = Modifier.widthIn(max = 320.dp),
            verticalAlignment = Alignment.Top
        ) {
            if (!isUser) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(ForestGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.SmartToy,
                        contentDescription = "AI",
                        tint = White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Surface(
                shape = RoundedCornerShape(
                    topStart = 18.dp,
                    topEnd = 18.dp,
                    bottomStart = if (isUser) 18.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 18.dp
                ),
                color = if (isUser) ForestGreen else WarmWhite,
                border = if (isUser) null else BorderStroke(1.dp, ForestGreen.copy(alpha = 0.12f)),
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(14.dp)) {

                    // Render Attachments inside message
                    if (message.attachments.isNotEmpty()) {
                        message.attachments.forEach { att ->
                            if (!att.uri.isNullOrEmpty()) {
                                AsyncImage(
                                    model = att.uri,
                                    contentDescription = "Attachment",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = SoftOlive
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(imageVector = Icons.Outlined.Description, contentDescription = null, tint = ForestGreen)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = att.name, fontSize = 12.sp, color = DarkGrayText)
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }

                    // Message Content
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = if (isUser) White else DarkGrayText,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = message.timestamp,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (isUser) White.copy(alpha = 0.7f) else MediumGrayText,
                            fontSize = 10.sp
                        ),
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }
    }
}

@Composable
private fun ThinkingIndicatorCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(ForestGreen),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = Icons.Outlined.SmartToy, contentDescription = null, tint = White, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(8.dp))
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = WarmWhite,
            border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.12f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    color = ForestGreen,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = "AI is analyzing your query...", fontSize = 13.sp, color = ForestGreen, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun TypingIndicatorCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(ForestGreen),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = Icons.Outlined.SmartToy, contentDescription = null, tint = White, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(8.dp))
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = WarmWhite,
            border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.12f))
        ) {
            Text(
                text = "Assistant is typing...",
                fontSize = 13.sp,
                color = MediumGrayText,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
            )
        }
    }
}

@Composable
private fun ComposerBottomBar(
    input: String,
    attachments: List<Attachment>,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onVoiceClick: () -> Unit,
    onAttachmentClick: () -> Unit,
    onRemoveAttachment: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = WarmWhite,
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Attachments Preview Row
            if (attachments.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    attachments.forEach { att ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SoftOlive,
                            border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.2f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Outlined.Image, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = att.name.take(15), fontSize = 12.sp, color = DarkGrayText)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove",
                                    tint = AlertRed,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable { onRemoveAttachment(att.id) }
                                )
                            }
                        }
                    }
                }
            }

            // Input Bar Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onAttachmentClick, modifier = Modifier.size(36.dp)) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Attachment", tint = ForestGreen)
                }

                IconButton(onClick = onCameraClick, modifier = Modifier.size(36.dp)) {
                    Icon(imageVector = Icons.Outlined.CameraAlt, contentDescription = "Camera", tint = ForestGreen)
                }

                IconButton(onClick = onGalleryClick, modifier = Modifier.size(36.dp)) {
                    Icon(imageVector = Icons.Outlined.PhotoLibrary, contentDescription = "Gallery", tint = ForestGreen)
                }

                Spacer(modifier = Modifier.width(4.dp))

                TextField(
                    value = input,
                    onValueChange = onInputChange,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 44.dp, max = 120.dp),
                    placeholder = {
                        Text(text = "Ask about crops, weather...", fontSize = 13.sp, color = MediumGrayText)
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = SoftOlive,
                        unfocusedContainerColor = SoftOlive,
                        disabledContainerColor = SoftOlive,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(20.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = onVoiceClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(imageVector = Icons.Outlined.Mic, contentDescription = "Voice Assistant", tint = ForestGreen)
                }

                val isEnabled = input.isNotBlank() || attachments.isNotEmpty()
                IconButton(
                    onClick = onSend,
                    enabled = isEnabled,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isEnabled) ForestGreen else ForestGreen.copy(alpha = 0.3f))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

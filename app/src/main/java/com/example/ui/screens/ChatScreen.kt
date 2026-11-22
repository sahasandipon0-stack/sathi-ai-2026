package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.DeviceAction
import com.example.model.YouTubeScript
import com.example.ui.components.ChatBubbleItem
import com.example.ui.components.SathiAvatar
import com.example.viewmodel.ChatUiState
import com.example.voice.VoiceStatus

private val QuickStarterPrompts = listOf(
    "YouTube ভিডিওর জন্য দারুণ টপিক ও স্ক্রিপ্ট দাও",
    "YouTube-এ অরিজিৎ সিংয়ের গান চালাও",
    "WhatsApp ওপেন করো",
    "ক্যামেরা চালু করো"
)

@Composable
fun ChatScreen(
    chatState: ChatUiState,
    voiceStatus: VoiceStatus,
    autoSpeakEnabled: Boolean,
    onSendMessage: (String) -> Unit,
    onToggleAutoSpeak: (Boolean) -> Unit,
    onNewChat: () -> Unit,
    onMicClick: () -> Unit,
    onStopListening: () -> Unit,
    onSpeakClick: (String) -> Unit,
    onStopSpeaking: () -> Unit,
    onExecuteAction: (String, DeviceAction) -> Unit,
    onPickContact: (String) -> Unit,
    onRequestContactsPermission: () -> Unit,
    onCopyScript: (YouTubeScript) -> Unit,
    onShareScript: (YouTubeScript) -> Unit,
    onSearchYouTubeTopic: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by rememberSaveable { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(chatState.messages.size, chatState.isGenerating) {
        if (chatState.messages.isNotEmpty()) {
            listState.animateScrollToItem(chatState.messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Sathi Top Header
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SathiAvatar(
                    size = 44.dp,
                    isActive = chatState.isGenerating || voiceStatus.isSpeaking || voiceStatus.isListening
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(id = R.string.app_name),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = when {
                            voiceStatus.isListening -> "শুনছি... কথা বলুন"
                            chatState.isGenerating -> "সাথী ভাবছে..."
                            voiceStatus.isSpeaking -> "সাথী বলছে..."
                            else -> stringResource(id = R.string.sathi_subtitle)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (voiceStatus.isSpeaking) {
                    IconButton(
                        onClick = onStopSpeaking,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("stop_speaking_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "কথা থামান",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                IconButton(
                    onClick = { onToggleAutoSpeak(!autoSpeakEnabled) },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("toggle_auto_speak_button")
                ) {
                    Icon(
                        imageVector = if (autoSpeakEnabled) {
                            Icons.AutoMirrored.Filled.VolumeUp
                        } else {
                            Icons.AutoMirrored.Filled.VolumeOff
                        },
                        contentDescription = if (autoSpeakEnabled) "ভয়েস উত্তর চালু" else "ভয়েস উত্তর বন্ধ",
                        tint = if (autoSpeakEnabled) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }

                if (chatState.messages.isNotEmpty()) {
                    IconButton(
                        onClick = onNewChat,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("new_chat_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddComment,
                            contentDescription = stringResource(id = R.string.cd_new_chat),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Main Chat Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.TopCenter
        ) {
            if (chatState.messages.isEmpty()) {
                WelcomeHeroContent(
                    onPromptSelected = { prompt ->
                        onSendMessage(prompt)
                    },
                    modifier = Modifier.widthIn(max = 620.dp)
                )
            } else {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 680.dp)
                ) {
                    items(chatState.messages, key = { it.id }) { msg ->
                        ChatBubbleItem(
                            message = msg,
                            onSpeakClick = onSpeakClick,
                            onExecuteAction = { action -> onExecuteAction(msg.id, action) },
                            onPickContact = { onPickContact(msg.id) },
                            onRequestContactsPermission = onRequestContactsPermission,
                            onCopyScript = onCopyScript,
                            onShareScript = onShareScript,
                            onSearchYouTubeTopic = onSearchYouTubeTopic
                        )
                    }

                    if (chatState.isGenerating) {
                        item(key = "typing_indicator") {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                SathiAvatar(size = 32.dp, isActive = true)
                                Surface(
                                    shape = RoundedCornerShape(18.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainerLow
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp
                                        )
                                        Text(
                                            text = "সাথী লিখছে...",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Live Listening Banner
        AnimatedVisibility(visible = voiceStatus.isListening) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = voiceStatus.partialTranscript.ifBlank { "বাংলায় বা ইংরেজিতে বলুন..." },
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    FilledIconButton(
                        onClick = onStopListening,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        ),
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("stop_listening_button")
                    ) {
                        Icon(imageVector = Icons.Default.Stop, contentDescription = "শোনা বন্ধ করুন")
                    }
                }
            }
        }

        // Composer Bar
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            tonalElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            text = "সাথীকে বলুন বা কমান্ড দিন...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    singleLine = false,
                    maxLines = 4,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (inputText.isNotBlank() && !chatState.isGenerating) {
                                onSendMessage(inputText)
                                inputText = ""
                            }
                        }
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field")
                )

                FilledIconButton(
                    onClick = {
                        if (voiceStatus.isListening) onStopListening() else onMicClick()
                    },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (voiceStatus.isListening) {
                            MaterialTheme.colorScheme.secondary
                        } else {
                            MaterialTheme.colorScheme.primaryContainer
                        },
                        contentColor = if (voiceStatus.isListening) {
                            MaterialTheme.colorScheme.onSecondary
                        } else {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        }
                    ),
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("mic_button")
                ) {
                    Icon(
                        imageVector = if (voiceStatus.isListening) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = stringResource(id = R.string.cd_mic_button)
                    )
                }

                FilledIconButton(
                    onClick = {
                        if (inputText.isNotBlank() && !chatState.isGenerating) {
                            onSendMessage(inputText)
                            inputText = ""
                        }
                    },
                    enabled = inputText.isNotBlank() && !chatState.isGenerating,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("send_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = stringResource(id = R.string.cd_send_button)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WelcomeHeroContent(
    onPromptSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
            MaterialTheme.colorScheme.surfaceContainerLowest
        )
    )

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Card(
                shape = MaterialTheme.shapes.extraLarge,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(gradientBrush)
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SathiAvatar(size = 92.dp, isActive = false)
                    Text(
                        text = "হ্যালো! আমি সাথী এআই",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "আপনার আপন বাংলা এআই বন্ধু। আমার সাথে গল্প করুন, YouTube ভিডিওর টপিক ও স্ক্রিপ্ট বানান, অথবা মুখে বা লিখে YouTube, অ্যাপ ও কল করার কমান্ড দিন!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "শুরু করতে যেকোনো একটি ট্যাপ করুন",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickStarterPrompts.forEachIndexed { idx, prompt ->
                        SuggestionChip(
                            onClick = { onPromptSelected(prompt) },
                            label = {
                                Text(
                                    text = prompt,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            ),
                            modifier = Modifier.testTag("starter_chip_$idx")
                        )
                    }
                }
            }
        }
    }
}

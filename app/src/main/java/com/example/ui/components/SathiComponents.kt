package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MovieCreation
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.ChatMessage
import com.example.model.DeviceAction
import com.example.model.DeviceActionType
import com.example.model.YouTubeScript
import com.example.ui.theme.JetBrainsMonoFamily

@Composable
fun SathiAvatar(
    size: Dp = 44.dp,
    isActive: Boolean = false,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "avatarPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isActive) 1.09f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        if (isActive) {
            Box(
                modifier = Modifier
                    .size(size)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.28f))
            )
        }
        Image(
            painter = painterResource(id = R.drawable.img_sathi_avatar),
            contentDescription = stringResource(id = R.string.cd_sathi_avatar),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .border(
                    width = 1.5.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.55f),
                    shape = CircleShape
                )
        )
    }
}

@Composable
fun ChatBubbleItem(
    message: ChatMessage,
    onSpeakClick: (String) -> Unit,
    onExecuteAction: (DeviceAction) -> Unit,
    onPickContact: () -> Unit,
    onRequestContactsPermission: () -> Unit,
    onCopyScript: (YouTubeScript) -> Unit,
    onShareScript: (YouTubeScript) -> Unit,
    onSearchYouTubeTopic: (String) -> Unit
) {
    if (message.isFromUser) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 20.dp,
                    topEnd = 6.dp,
                    bottomStart = 20.dp,
                    bottomEnd = 20.dp
                ),
                color = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                tonalElevation = 2.dp,
                modifier = Modifier.widthIn(max = 310.dp)
            ) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.Top
        ) {
            SathiAvatar(size = 36.dp, modifier = Modifier.padding(top = 4.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column(
                modifier = Modifier.widthIn(max = 340.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(
                        topStart = 6.dp,
                        topEnd = 20.dp,
                        bottomStart = 20.dp,
                        bottomEnd = 20.dp
                    ),
                    color = if (message.isError) {
                        MaterialTheme.colorScheme.errorContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerLow
                    },
                    contentColor = if (message.isError) {
                        MaterialTheme.colorScheme.onErrorContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (message.isError) {
                            MaterialTheme.colorScheme.error.copy(alpha = 0.4f)
                        } else {
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                        }
                    ),
                    tonalElevation = 1.dp
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = message.text,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        if (!message.isError) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        val speakContent = message.deviceAction?.spokenReply
                                            ?.takeIf { it.isNotBlank() }
                                            ?: message.text
                                        onSpeakClick(speakContent)
                                    },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .testTag("speak_message_${message.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = stringResource(id = R.string.cd_speak_message),
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                message.youtubeScript?.let { script ->
                    YouTubeScriptCard(
                        script = script,
                        onCopyScript = { onCopyScript(script) },
                        onShareScript = { onShareScript(script) },
                        onSearchTopic = { onSearchYouTubeTopic(script.title) }
                    )
                }

                val action = message.deviceAction
                if (action != null && action.action != DeviceActionType.NONE) {
                    DeviceActionCard(
                        action = action,
                        attempted = message.actionAttempted,
                        succeeded = message.actionSucceeded,
                        statusMessage = message.actionStatusMessage,
                        needsContactPicker = message.needsContactPicker,
                        onExecuteClick = { onExecuteAction(action) },
                        onPickContactClick = onPickContact,
                        onRequestContactsPermission = onRequestContactsPermission
                    )
                }
            }
        }
    }
}

@Composable
fun DeviceActionCard(
    action: DeviceAction,
    attempted: Boolean,
    succeeded: Boolean,
    statusMessage: String?,
    needsContactPicker: Boolean,
    onExecuteClick: () -> Unit,
    onPickContactClick: () -> Unit,
    onRequestContactsPermission: () -> Unit
) {
    var showJson by remember { mutableStateOf(false) }

    val actionIcon = when (action.action) {
        DeviceActionType.OPEN_YOUTUBE_SEARCH -> Icons.Default.PlayCircleFilled
        DeviceActionType.OPEN_APP -> Icons.AutoMirrored.Filled.OpenInNew
        DeviceActionType.MAKE_CALL -> Icons.Default.Call
        DeviceActionType.NONE -> Icons.Default.Code
    }

    val containerColor = when {
        attempted && succeeded -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
        attempted && !succeeded -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.65f)
        else -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
    }

    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        imageVector = actionIcon,
                        contentDescription = action.action.labelBn,
                        modifier = Modifier
                            .padding(7.dp)
                            .size(18.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = action.action.labelBn,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (action.target.isNotBlank()) {
                        Text(
                            text = "টার্গেট: ${action.target}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (attempted) {
                    Icon(
                        imageVector = if (succeeded) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = if (succeeded) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.tertiary
                        },
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (!statusMessage.isNullOrBlank()) {
                Text(
                    text = statusMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalButton(
                    onClick = onExecuteClick,
                    modifier = Modifier.testTag("execute_action_button")
                ) {
                    Icon(
                        imageVector = actionIcon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (attempted && succeeded) "আবার খুলুন" else "অ্যাকশন চালান",
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                TextButton(
                    onClick = { showJson = !showJson },
                    modifier = Modifier.testTag("toggle_json_button")
                ) {
                    Icon(
                        imageVector = if (showJson) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "JSON", style = MaterialTheme.typography.labelMedium)
                }
            }

            if (needsContactPicker) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onPickContactClick,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("pick_contact_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Contacts,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "কন্ট্যাক্ট বাছাই", style = MaterialTheme.typography.labelMedium)
                    }
                    TextButton(
                        onClick = onRequestContactsPermission,
                        modifier = Modifier.testTag("grant_contacts_permission_button")
                    ) {
                        Text(text = "অনুমতি দিন", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            AnimatedVisibility(visible = showJson) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = action.toFormattedJson(),
                        fontFamily = JetBrainsMonoFamily,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun YouTubeScriptCard(
    script: YouTubeScript,
    onCopyScript: () -> Unit,
    onShareScript: () -> Unit,
    onSearchTopic: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Default.MovieCreation,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(20.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "YouTube ভিডিও স্ক্রিপ্ট",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = script.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (script.topicIdeas.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Lightbulb,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "আকর্ষণীয় টপিক আইডিয়া",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        script.topicIdeas.forEach { idea ->
                            AssistChip(
                                onClick = { onSearchTopic() },
                                label = {
                                    Text(
                                        text = idea,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))

            if (script.hook.isNotBlank()) {
                ScriptSectionBlock(
                    badge = "হুক (Hook)",
                    body = script.hook,
                    badgeColor = MaterialTheme.colorScheme.tertiaryContainer,
                    badgeContentColor = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }

            if (script.mainBody.isNotBlank()) {
                ScriptSectionBlock(
                    badge = "মূল অংশ (Main Content)",
                    body = script.mainBody,
                    badgeColor = MaterialTheme.colorScheme.primaryContainer,
                    badgeContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            if (script.outro.isNotBlank()) {
                ScriptSectionBlock(
                    badge = "আউটরো (Outro)",
                    body = script.outro,
                    badgeColor = MaterialTheme.colorScheme.secondaryContainer,
                    badgeContentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick = onCopyScript,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("copy_script_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = stringResource(id = R.string.cd_copy_script),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("কপি", style = MaterialTheme.typography.labelLarge)
                }

                OutlinedButton(
                    onClick = onShareScript,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("share_script_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = stringResource(id = R.string.cd_share_script),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("শেয়ার", style = MaterialTheme.typography.labelLarge)
                }

                IconButton(
                    onClick = onSearchTopic,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("search_topic_youtube_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayCircleFilled,
                        contentDescription = "YouTube-এ দেখুন",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

@Composable
private fun ScriptSectionBlock(
    badge: String,
    body: String,
    badgeColor: androidx.compose.ui.graphics.Color,
    badgeContentColor: androidx.compose.ui.graphics.Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = badgeColor,
            contentColor = badgeContentColor
        ) {
            Text(
                text = badge,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

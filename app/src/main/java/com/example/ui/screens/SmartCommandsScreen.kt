package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.SathiPreferences
import com.example.model.DeviceAction
import com.example.model.DeviceActionType
import com.example.model.InstalledAppInfo
import com.example.ui.theme.JetBrainsMonoFamily

private val ActionJsonExample = """
{
  "action": "open_youtube_search" | "open_app" | "make_call" | "none",
  "target": "সার্চ বা অ্যাপ বা নামের মান",
  "spoken_reply": "সাথী মুখে যা বলবে"
}
""".trimIndent()

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SmartCommandsScreen(
    preferences: SathiPreferences,
    installedApps: List<InstalledAppInfo>,
    onToggleAutoSpeak: (Boolean) -> Unit,
    onToggleAutoExecute: (Boolean) -> Unit,
    onRunDirectAction: (DeviceAction) -> Unit,
    onPickContact: () -> Unit,
    onRefreshApps: () -> Unit,
    onTryPromptInChat: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var youtubeQuery by rememberSaveable { mutableStateOf("") }
    var callTarget by rememberSaveable { mutableStateOf("") }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "স্মার্ট ডিভাইস কমান্ড ও সেটিংস",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = "সাথী চ্যাটে বলুন অথবা এখান থেকে সরাসরি অ্যাকশন চালান",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Preferences Card
            item {
                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "অ্যাসিস্ট্যান্ট সেটিংস",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "স্বয়ংক্রিয় ভয়েস উত্তর",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = "সাথী প্রতিটি উত্তর মুখে পড়ে শোনাবে",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = preferences.autoSpeakEnabled,
                                onCheckedChange = onToggleAutoSpeak,
                                modifier = Modifier.testTag("switch_auto_speak")
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "অটো অ্যাকশন চালু",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = "কমান্ড পাওয়ার সাথে সাথে YouTube/অ্যাপ/ডায়ালার খুলবে",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = preferences.autoExecuteActions,
                                onCheckedChange = onToggleAutoExecute,
                                modifier = Modifier.testTag("switch_auto_execute")
                            )
                        }
                    }
                }
            }

            // Quick YouTube Search Card
            item {
                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayCircleFilled,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = "YouTube দ্রুত সার্চ",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        OutlinedTextField(
                            value = youtubeQuery,
                            onValueChange = { youtubeQuery = it },
                            label = { Text("গান বা ভিডিওর নাম লিখুন") },
                            placeholder = { Text("যেমন: Bangla Lofi Song") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("quick_youtube_input")
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    onRunDirectAction(
                                        DeviceAction(
                                            action = DeviceActionType.OPEN_YOUTUBE_SEARCH,
                                            target = youtubeQuery.ifBlank { "Bangla Trending Music" },
                                            spokenReply = "এক্ষুনি YouTube-এ সার্চ করছি!"
                                        )
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("quick_youtube_launch_button")
                            ) {
                                Text("YouTube খুলুন")
                            }

                            OutlinedButton(
                                onClick = {
                                    val q = youtubeQuery.ifBlank { "বাংলা নতুন গান" }
                                    onTryPromptInChat("YouTube-এ $q চালাও")
                                },
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("ask_sathi_youtube_button")
                            ) {
                                Text("সাথীকে বলুন")
                            }
                        }
                    }
                }
            }

            // Quick Call Card
            item {
                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "দ্রুত কল অ্যাসিস্ট্যান্ট",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        OutlinedTextField(
                            value = callTarget,
                            onValueChange = { callTarget = it },
                            label = { Text("ফোন নম্বর বা নাম লিখুন") },
                            placeholder = { Text("যেমন: 01711000000") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("quick_call_input")
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilledTonalButton(
                                onClick = {
                                    onRunDirectAction(
                                        DeviceAction(
                                            action = DeviceActionType.MAKE_CALL,
                                            target = callTarget,
                                            spokenReply = "ডায়ালার খুলে দিচ্ছি।"
                                        )
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("quick_dial_button")
                            ) {
                                Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("ডায়ালার খুলুন")
                            }

                            OutlinedButton(
                                onClick = onPickContact,
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("commands_pick_contact_button")
                            ) {
                                Icon(Icons.Default.Contacts, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("কন্ট্যাক্ট")
                            }
                        }
                    }
                }
            }

            // Real Installed Apps Launcher Card
            item {
                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Apps,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "ডিভাইসের ইনস্টল করা অ্যাপস",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "ট্যাপ করে সরাসরি চালু করুন (${installedApps.size}টি পাওয়া গেছে)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = onRefreshApps,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "অ্যাপ তালিকা রিফ্রেশ")
                            }
                        }

                        if (installedApps.isEmpty()) {
                            Text(
                                text = "কোনো লঞ্চার অ্যাপ পাওয়া যায়নি।",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                installedApps.take(18).forEach { app ->
                                    AssistChip(
                                        onClick = {
                                            onRunDirectAction(
                                                DeviceAction(
                                                    action = DeviceActionType.OPEN_APP,
                                                    target = app.appName,
                                                    spokenReply = "${app.appName} ওপেন করছি।"
                                                )
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = app.appName,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Action JSON Contract Card
            item {
                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "সাথী JSON অ্যাকশন ফরম্যাট",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.surfaceContainerLowest,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = ActionJsonExample,
                                fontFamily = JetBrainsMonoFamily,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

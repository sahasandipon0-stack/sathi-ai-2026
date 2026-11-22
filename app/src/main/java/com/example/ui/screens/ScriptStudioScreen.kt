package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.ScriptTone
import com.example.model.VideoFormat
import com.example.model.YouTubeScript
import com.example.ui.components.YouTubeScriptCard
import com.example.viewmodel.StudioUiState

private val NicheSuggestions = listOf(
    "বাজেট স্মার্টফোন রিভিউ",
    "ঘরে বসে সহজ রেসিপি",
    "AI দিয়ে প্রোডাক্টিভিটি",
    "ভ্রমণ ব্লগ — কক্সবাজার"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScriptStudioScreen(
    studioState: StudioUiState,
    preferredTone: ScriptTone,
    onSelectTone: (ScriptTone) -> Unit,
    onGenerateScript: (String, ScriptTone, VideoFormat) -> Unit,
    onCopyScript: (YouTubeScript) -> Unit,
    onShareScript: (YouTubeScript) -> Unit,
    onSpeakScript: (YouTubeScript) -> Unit,
    onSearchTopic: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var topicInput by rememberSaveable { mutableStateOf("") }
    var selectedFormat by rememberSaveable { mutableStateOf(VideoFormat.LONG) }

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
                Card(
                    shape = MaterialTheme.shapes.extraLarge,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(168.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_studio_banner),
                            contentDescription = stringResource(id = R.string.cd_studio_banner),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.72f)
                                        )
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(18.dp)
                        ) {
                            Text(
                                text = "YouTube স্ক্রিপ্ট স্টুডিও",
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.White
                            )
                            Text(
                                text = "আকর্ষণীয় টপিক, হুক, মূল অংশ ও আউটরো এক ক্লিকেই তৈরি করুন",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }

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
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        OutlinedTextField(
                            value = topicInput,
                            onValueChange = { topicInput = it },
                            label = { Text("ভিডিওর বিষয় বা নিশ লিখুন") },
                            placeholder = { Text("যেমন: ২০২৬ সালের সেরা ৫টি এআই টুলস") },
                            singleLine = false,
                            maxLines = 3,
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("studio_topic_input")
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            NicheSuggestions.forEach { niche ->
                                SuggestionChip(
                                    onClick = { topicInput = niche },
                                    label = { Text(niche, style = MaterialTheme.typography.bodySmall) }
                                )
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "ভিডিও ফরম্যাট",
                                style = MaterialTheme.typography.labelLarge
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                VideoFormat.entries.forEach { format ->
                                    FilterChip(
                                        selected = selectedFormat == format,
                                        onClick = { selectedFormat = format },
                                        label = {
                                            Text(
                                                text = format.labelBn,
                                                style = MaterialTheme.typography.labelMedium
                                            )
                                        },
                                        modifier = Modifier.testTag("format_chip_${format.name}")
                                    )
                                }
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "কথার স্টাইল (Tone)",
                                style = MaterialTheme.typography.labelLarge
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                ScriptTone.entries.forEach { tone ->
                                    FilterChip(
                                        selected = preferredTone == tone,
                                        onClick = { onSelectTone(tone) },
                                        label = {
                                            Text(
                                                text = tone.labelBn,
                                                style = MaterialTheme.typography.labelMedium
                                            )
                                        },
                                        modifier = Modifier.testTag("tone_chip_${tone.name}")
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = {
                                onGenerateScript(topicInput, preferredTone, selectedFormat)
                            },
                            enabled = studioState !is StudioUiState.Loading,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("generate_script_button")
                        ) {
                            if (studioState is StudioUiState.Loading) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("সাথী স্ক্রিপ্ট লিখছে...")
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("টপিক ও স্ক্রিপ্ট তৈরি করুন")
                            }
                        }
                    }
                }
            }

            when (studioState) {
                is StudioUiState.Idle -> {
                    item {
                        Surface(
                            shape = MaterialTheme.shapes.large,
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "ওপরে যেকোনো বিষয় লিখে বা সাজেশন থেকে বেছে নিয়ে \"টপিক ও স্ক্রিপ্ট তৈরি করুন\" বাটনে ট্যাপ করুন।",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }

                is StudioUiState.Loading -> Unit

                is StudioUiState.Error -> {
                    item {
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.ErrorOutline, contentDescription = null)
                                Text(
                                    text = studioState.message,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                is StudioUiState.Success -> {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            YouTubeScriptCard(
                                script = studioState.script,
                                onCopyScript = { onCopyScript(studioState.script) },
                                onShareScript = { onShareScript(studioState.script) },
                                onSearchTopic = { onSearchTopic(studioState.script.title) }
                            )
                            OutlinedButton(
                                onClick = { onSpeakScript(studioState.script) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("speak_studio_script_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("সাথীর কণ্ঠে হুক ও আউটরো শুনুন")
                            }
                        }
                    }
                }
            }
        }
    }
}

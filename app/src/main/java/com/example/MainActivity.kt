package com.example

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.MovieCreation
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.MovieCreation
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.firebase.configureAppCheck
import com.example.model.AppTab
import com.example.model.DeviceAction
import com.example.model.DeviceActionType
import com.example.model.YouTubeScript
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.ScriptStudioScreen
import com.example.ui.screens.SmartCommandsScreen
import com.example.ui.theme.SathiAiTheme
import com.example.viewmodel.SathiViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configure Firebase App Check with launch intent before rendering UI or creating ViewModels.
        configureAppCheck(this, intent)

        enableEdgeToEdge()
        setContent {
            SathiAiTheme {
                SathiAppRoot()
            }
        }
    }
}

@Composable
fun SathiAppRoot(
    viewModel: SathiViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val chatState by viewModel.chatState.collectAsStateWithLifecycle()
    val voiceStatus by viewModel.voiceStatus.collectAsStateWithLifecycle()
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    val studioState by viewModel.studioState.collectAsStateWithLifecycle()
    val installedApps by viewModel.installedApps.collectAsStateWithLifecycle()

    var currentTab by rememberSaveable { mutableStateOf(AppTab.CHAT) }

    // Return to primary Chat tab on system back from secondary tabs
    BackHandler(enabled = currentTab != AppTab.CHAT) {
        currentTab = AppTab.CHAT
    }

    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startVoiceInput()
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("ভয়েস কমান্ডের জন্য মাইক্রোফোন অনুমতি প্রয়োজন।")
            }
        }
    }

    val contactsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        scope.launch {
            if (isGranted) {
                snackbarHostState.showSnackbar("কন্ট্যাক্ট অনুমতি দেওয়া হয়েছে। এখন অ্যাকশন আবার চালান।")
            } else {
                snackbarHostState.showSnackbar("অনুমতি ছাড়াই 'কন্ট্যাক্ট বাছাই' বাটন ব্যবহার করতে পারেন।")
            }
        }
    }

    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { uri ->
        if (uri != null) {
            viewModel.handlePickedContact(uri)
        }
    }

    LaunchedEffect(voiceStatus.voiceError) {
        voiceStatus.voiceError?.let { err ->
            snackbarHostState.showSnackbar(err)
            viewModel.clearVoiceError()
        }
    }

    LaunchedEffect(chatState.errorBanner) {
        chatState.errorBanner?.let { err ->
            snackbarHostState.showSnackbar(err)
            viewModel.dismissError()
        }
    }

    val onCopyScript: (YouTubeScript) -> Unit = { script ->
        copyToClipboard(context, script.title, script.toShareableText())
        scope.launch {
            snackbarHostState.showSnackbar("স্ক্রিপ্ট ক্লিপবোর্ডে কপি করা হয়েছে!")
        }
    }

    val onShareScript: (YouTubeScript) -> Unit = { script ->
        shareText(context, script.title, script.toShareableText())
    }

    val onSearchYouTubeTopic: (String) -> Unit = { topic ->
        val msg = viewModel.runQuickAction(
            DeviceAction(
                action = DeviceActionType.OPEN_YOUTUBE_SEARCH,
                target = topic,
                spokenReply = ""
            )
        )
        if (msg.isNotBlank()) {
            scope.launch { snackbarHostState.showSnackbar(msg) }
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavigationRail = maxWidth >= 600.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                if (!useNavigationRail) {
                    NavigationBar {
                        AppTab.entries.forEach { tab ->
                            val selected = currentTab == tab
                            NavigationBarItem(
                                selected = selected,
                                onClick = { currentTab = tab },
                                icon = {
                                    Icon(
                                        imageVector = tabIcon(tab, selected),
                                        contentDescription = tab.labelBn
                                    )
                                },
                                label = { Text(tab.labelBn) },
                                modifier = Modifier.testTag("nav_tab_${tab.routeId}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useNavigationRail) {
                    NavigationRail(modifier = Modifier.fillMaxHeight()) {
                        AppTab.entries.forEach { tab ->
                            val selected = currentTab == tab
                            NavigationRailItem(
                                selected = selected,
                                onClick = { currentTab = tab },
                                icon = {
                                    Icon(
                                        imageVector = tabIcon(tab, selected),
                                        contentDescription = tab.labelBn
                                    )
                                },
                                label = { Text(tab.labelBn) },
                                modifier = Modifier.testTag("rail_tab_${tab.routeId}")
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                    when (currentTab) {
                        AppTab.CHAT -> ChatScreen(
                            chatState = chatState,
                            voiceStatus = voiceStatus,
                            autoSpeakEnabled = preferences.autoSpeakEnabled,
                            onSendMessage = viewModel::sendMessage,
                            onToggleAutoSpeak = viewModel::setAutoSpeak,
                            onNewChat = viewModel::clearChat,
                            onMicClick = {
                                val granted = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED
                                if (granted) {
                                    viewModel.startVoiceInput()
                                } else {
                                    recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            },
                            onStopListening = viewModel::stopVoiceInput,
                            onSpeakClick = viewModel::speakText,
                            onStopSpeaking = viewModel::stopSpeaking,
                            onExecuteAction = viewModel::executeDeviceAction,
                            onPickContact = { msgId ->
                                viewModel.markContactPickerTarget(msgId)
                                contactPickerLauncher.launch(null)
                            },
                            onRequestContactsPermission = {
                                contactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                            },
                            onCopyScript = onCopyScript,
                            onShareScript = onShareScript,
                            onSearchYouTubeTopic = onSearchYouTubeTopic
                        )

                        AppTab.STUDIO -> ScriptStudioScreen(
                            studioState = studioState,
                            preferredTone = preferences.preferredTone,
                            onSelectTone = viewModel::setPreferredTone,
                            onGenerateScript = viewModel::generateStudioScript,
                            onCopyScript = onCopyScript,
                            onShareScript = onShareScript,
                            onSpeakScript = { script ->
                                val spoken = "${script.hook} ... ${script.outro}"
                                viewModel.speakText(spoken)
                            },
                            onSearchTopic = onSearchYouTubeTopic
                        )

                        AppTab.COMMANDS -> SmartCommandsScreen(
                            preferences = preferences,
                            installedApps = installedApps,
                            onToggleAutoSpeak = viewModel::setAutoSpeak,
                            onToggleAutoExecute = viewModel::setAutoExecuteActions,
                            onRunDirectAction = { action ->
                                val resultMsg = viewModel.runQuickAction(action)
                                if (resultMsg.isNotBlank()) {
                                    scope.launch { snackbarHostState.showSnackbar(resultMsg) }
                                }
                            },
                            onPickContact = { contactPickerLauncher.launch(null) },
                            onRefreshApps = viewModel::refreshInstalledApps,
                            onTryPromptInChat = { prompt ->
                                currentTab = AppTab.CHAT
                                viewModel.sendMessage(prompt)
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun tabIcon(tab: AppTab, selected: Boolean) = when (tab) {
    AppTab.CHAT -> if (selected) Icons.Filled.ChatBubble else Icons.Outlined.ChatBubbleOutline
    AppTab.STUDIO -> if (selected) Icons.Filled.MovieCreation else Icons.Outlined.MovieCreation
    AppTab.COMMANDS -> if (selected) Icons.Filled.Bolt else Icons.Outlined.Bolt
}

private fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    clipboard?.setPrimaryClip(ClipData.newPlainText(label, text))
}

private fun shareText(context: Context, title: String, text: String) {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        putExtra(Intent.EXTRA_SUBJECT, title)
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }
    val chooser = Intent.createChooser(sendIntent, "স্ক্রিপ্ট শেয়ার করুন").apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(chooser)
}

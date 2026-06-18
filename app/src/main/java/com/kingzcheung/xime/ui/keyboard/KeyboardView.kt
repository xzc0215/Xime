package com.kingzcheung.xime.ui.keyboard

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kingzcheung.xime.clipboard.ClipboardItem
import com.kingzcheung.xime.keyboard.KeyboardRoute
import com.kingzcheung.xime.keyboard.ToolbarAction
import com.kingzcheung.xime.keyboard.ToolbarButton
import com.kingzcheung.xime.service.CandidateState
import com.kingzcheung.xime.service.InputUIState
import com.kingzcheung.xime.settings.SchemaInfo
import com.kingzcheung.xime.speech.RecognitionState
import com.kingzcheung.xime.settings.KeysConfigHelper
import com.kingzcheung.xime.ui.settings.SchemaListView
import com.kingzcheung.xime.ui.theme.KeyboardThemes
import com.kingzcheung.xime.keyboard.GestureAction

val LocalStretchFactor = compositionLocalOf { 1f }

@Composable
fun KeyboardView(
    candidates: List<String> = emptyList(),
    candidateComments: List<String> = emptyList(),
    inputText: String = "",
    isComposing: Boolean = false,
    isAsciiMode: Boolean = false,
    schemaName: String = "",
    currentSchemaId: String = "",
    schemas: List<SchemaInfo> = emptyList(),
    enterKeyText: String = "发送",
    isDarkTheme: Boolean = false,
    darkMode: Int = 2,
    themeId: String = "ocean_blue",
    showBottomButtons: Boolean = false,
    clipboardItems: List<ClipboardItem> = emptyList(),
    quickSendItems: List<ClipboardItem> = emptyList(),
    recentClipboardItems: List<ClipboardItem> = emptyList(),
    associationCandidates: List<String> = emptyList(),
    keyboardHeightDp: Int = com.kingzcheung.xime.settings.SettingsPreferences.DEFAULT_KEYBOARD_HEIGHT_DP,
    keyboardBottomPaddingDp: Int = 0,
    isDeploying: Boolean = false,
    deploymentMessage: String = "",
    onDismissDeploying: (() -> Unit)? = null,
    onKeyPress: (String, Boolean) -> Unit,
    onKeyPressDown: ((String) -> Unit)? = null,
    onCandidateSelect: (Int) -> Unit,
    onAssociationSelect: ((Int) -> Unit)? = null,
    onToggleDarkMode: (() -> Unit)? = null,
    onClipboard: (() -> Unit)? = null,
    onClipboardSelect: ((String) -> Unit)? = null,
    onCommitText: ((String) -> Unit)? = null,
    onDeleteText: ((Int) -> Unit)? = null,
    onClipboardRemove: ((Long) -> Unit)? = null,
    onClipboardSplitWords: ((Long) -> Unit)? = null,
    onAddToQuickSend: ((Long) -> Unit)? = null,
    onAddQuickSendText: ((String) -> Unit)? = null,
    onRemoveFromQuickSend: ((Long) -> Unit)? = null,
    onQuickSend: (() -> Unit)? = null,
    onKeyboardResize: (() -> Unit)? = null,
    onReloadConfig: (() -> Unit)? = null,
    onSettings: (() -> Unit)? = null,
    onSwitchSchema: ((String) -> Unit)? = null,
    onHideKeyboard: (() -> Unit)? = null,
    onSwitchKeyboard: (() -> Unit)? = null,
    onToolbarEditingAction: ((String) -> Unit)? = null,
    onCommitImage: ((String) -> Unit)? = null,
    isVoiceMode: Boolean = false,
    voiceBottomActive: Boolean = false,
    voiceLeftActive: Boolean = false,
    voiceRightActive: Boolean = false,
    onVoiceModeChange: ((Boolean) -> Unit)? = null,
    isSttEnabled: Boolean = true,
    voicePluginName: String = "",
    voiceRecognitionState: RecognitionState = RecognitionState.IDLE,
    voiceRecognizedText: String = "",
    voiceAmplitude: Float = 0f,
    candidateStateProvider: () -> CandidateState,
    uiStateProvider: () -> InputUIState,
    onPageDown: (() -> Unit)? = null,
    onPageUp: (() -> Unit)? = null,
    onCursorMove: ((Int) -> Unit)? = null,
    onGestureAction: ((GestureAction, String) -> Unit)? = null,
    toolbarButtons: List<String> = ToolbarButton.DEFAULT_VISIBLE.map { it.id },
    onUpdateToolbarButtons: ((List<String>) -> Unit)? = null,
    modifier: Modifier = Modifier,
    onKeyboardModeChange: ((Boolean) -> Unit)? = null,
    isCalculatorMode: Boolean = false,
) {
    var isShifted by remember { mutableStateOf(false) }
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    var keyboardState by remember { mutableStateOf(initialKeyboardLayoutState(isAsciiMode)) }
    var currentRoute by remember { mutableStateOf<KeyboardRoute>(KeyboardRoute.Keyboard) }

    SideEffect {
        val active = keyboardState is KeyboardLayoutState.Chinese && currentRoute == KeyboardRoute.Keyboard
        onKeyboardModeChange?.invoke(active)
    }

    val kbColors = KeysConfigHelper.getKeyboardColors()
    val kbShadow = KeysConfigHelper.getKeyboardShadow()

    val longToColor: (Long) -> Color = { Color(0xFF000000 or it) }
    val keyboardBgColor = if (isDarkTheme) longToColor(kbColors.keyboardBgColorDark)
        else longToColor(kbColors.keyboardBgColor)
    val keyBgColor = if (isDarkTheme) longToColor(kbColors.keyBgColorDark)
        else longToColor(kbColors.keyBgColor)
    val keyTextColor = if (isDarkTheme) longToColor(kbColors.keyTextColorDark)
        else longToColor(kbColors.keyTextColor)
    val accentColor = KeyboardThemes.getAccentColor(themeId, isDarkTheme)
    val themeSpecialKeyColor = KeyboardThemes.getSpecialKeyColor(themeId, isDarkTheme)
    val specialKeyBgColor = if (isDarkTheme) kbColors.specialKeyBgColorDark?.let { longToColor(it) }
        ?: themeSpecialKeyColor
        else kbColors.specialKeyBgColor?.let { longToColor(it) } ?: themeSpecialKeyColor
    val candidateBarBg = if (isDarkTheme) longToColor(kbColors.candidateBarBgColorDark)
        else longToColor(kbColors.candidateBarBgColor)
    val candidateTextColor = if (isDarkTheme) longToColor(kbColors.candidateTextColorDark)
        else longToColor(kbColors.candidateTextColor)
    val dividerColor = if (isDarkTheme) Color(0xFF3C4043) else Color(0xFFDADCE0)

    val state = uiStateProvider()
    val candState = candidateStateProvider()
    val clipboardTab = (currentRoute as? KeyboardRoute.Clipboard)?.tab ?: 0

    LaunchedEffect(state.inputSessionId) {
        currentRoute = KeyboardRoute.Keyboard
    }

    LaunchedEffect(isAsciiMode) {
        keyboardState = initialKeyboardLayoutState(isAsciiMode)
    }

    Box(modifier = modifier.background(keyboardBgColor)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            val candidateBarState = remember(
                candidates, candidateComments, inputText, isComposing,
                associationCandidates, candState.isShowingRecentClipboard, candState.hasNextPage,
                isCalculatorMode,
            ) {
                CandidateBarState.from(
                    candidates = candidates,
                    candidateComments = candidateComments,
                    inputText = inputText,
                    isComposing = isComposing,
                    associationCandidates = associationCandidates,
                    isShowingRecentClipboard = candState.isShowingRecentClipboard,
                    hasNextPage = candState.hasNextPage,
                    isCalculatorActive = isCalculatorMode,
                )
            }

            CandidateBar(
                state = candidateBarState,
                currentRoute = currentRoute,
                toolbarActions = toolbarButtons.mapNotNull { id ->
                    val button = ToolbarButton.fromId(id) ?: return@mapNotNull null
                    val onClick: () -> Unit = when (button) {
                        ToolbarButton.EMOJI -> ({ currentRoute = KeyboardRoute.Emoji })
                        ToolbarButton.CLIPBOARD -> ({ currentRoute = KeyboardRoute.Clipboard(0) })
                        ToolbarButton.SCHEMA -> ({ currentRoute = KeyboardRoute.SchemaList })
                        ToolbarButton.QUICK_PHRASE -> ({ currentRoute = KeyboardRoute.Clipboard(1) })
                        ToolbarButton.SYMBOL -> ({ currentRoute = KeyboardRoute.Symbol })
                        ToolbarButton.SELECT_ALL -> ({ onToolbarEditingAction?.invoke("select_all") })
                        ToolbarButton.COPY -> ({ onToolbarEditingAction?.invoke("copy") })
                        ToolbarButton.PASTE -> ({ onToolbarEditingAction?.invoke("paste") })
                        ToolbarButton.HOME -> ({ onToolbarEditingAction?.invoke("home") })
                        ToolbarButton.END -> ({ onToolbarEditingAction?.invoke("end") })
                    }
                    ToolbarAction(button, onClick)
                },
                visuals = CandidateBarVisuals(
                    backgroundColor = candidateBarBg,
                    textColor = candidateTextColor,
                    dividerColor = dividerColor,
                    accentColor = accentColor,
                    isDarkTheme = isDarkTheme
                ),
                callbacks = CandidateBarCallbacks(
                    onCandidateSelect = onCandidateSelect,
                    onLogoClick = { currentRoute = KeyboardRoute.Menu },
                    onBack = {
                        currentRoute = when (currentRoute) {
                            is KeyboardRoute.SchemaList -> KeyboardRoute.Menu
                            is KeyboardRoute.Clipboard -> KeyboardRoute.Keyboard
                            is KeyboardRoute.CandidatePage -> KeyboardRoute.Keyboard
                            is KeyboardRoute.ToolbarCustomize -> KeyboardRoute.Keyboard
                            is KeyboardRoute.Emoji -> KeyboardRoute.Keyboard
                            is KeyboardRoute.Symbol -> KeyboardRoute.Keyboard
                            is KeyboardRoute.SplitWords -> KeyboardRoute.Keyboard
                            else -> KeyboardRoute.Keyboard
                        }
                    },
                    onHideKeyboard = {
                        onHideKeyboard?.invoke()
                        keyboardState = initialKeyboardLayoutState(isAsciiMode)
                        currentRoute = KeyboardRoute.Keyboard
                        isShifted = false
                    },
                    onShowMoreCandidates = { currentRoute = KeyboardRoute.CandidatePage },
                    onInputTextClick = {
                        if (inputText.isNotEmpty()) {
                            onClipboardSelect?.invoke(inputText)
                        }
                    },
                    onAssociationSelect = onAssociationSelect
                )
            )

            when {
                isVoiceMode -> {
                    VoiceKeyboardLayout(
                        keyBackgroundColor = keyBgColor,
                        keyTextColor = keyTextColor,
                        specialKeyBackgroundColor = specialKeyBgColor,
                        keyboardBackgroundColor = keyboardBgColor,
                        modifier = Modifier.weight(1f),
                        isDarkTheme = isDarkTheme,
                        themeId = themeId,
                        bottomActive = voiceBottomActive,
                        leftActive = voiceLeftActive,
                        rightActive = voiceRightActive,
                        pluginName = voicePluginName,
                        recognitionState = voiceRecognitionState,
                        recognizedText = voiceRecognizedText,
                        amplitude = voiceAmplitude
                    )
                }
                else -> {
                    val fullScreenOnKeyPress: (String) -> Unit = { key ->
                        when (key) {
                            "shift" -> isShifted = !isShifted
                            "mode_change" -> {
                                keyboardState = keyboardState.transition(
                                    KeyboardLayoutAction.SwitchToNumber, isAsciiMode
                                )
                                onKeyPress("clear_composition", false)
                            }
                            "mode_change_symbol" -> currentRoute = KeyboardRoute.Symbol
                            "emoji" -> currentRoute = KeyboardRoute.Emoji
                            else -> onKeyPress(key, isShifted)
                        }
                    }
                    val numberOnKeyPress: (String) -> Unit = { key ->
                        when (key) {
                            "abc" -> keyboardState = keyboardState.transition(
                                KeyboardLayoutAction.SwitchToFull, isAsciiMode
                            )
                            "symbol" -> currentRoute = KeyboardRoute.Symbol
                            "emoji" -> currentRoute = KeyboardRoute.Emoji
                            else -> onKeyPress(key, false)
                        }
                    }
                    val symbolOnKeyPress: (String) -> Unit = { key ->
                        when (key) {
                            "abc" -> keyboardState = keyboardState.transition(
                                KeyboardLayoutAction.SwitchToFull, isAsciiMode
                            )
                            "?123" -> {
                                keyboardState = keyboardState.transition(
                                    KeyboardLayoutAction.SwitchToNumber, isAsciiMode
                                )
                                onKeyPress("clear_composition", false)
                            }
                            else -> onKeyPress(key, false)
                        }
                    }

                    val currentOnKeyPress = when (keyboardState) {
                        is KeyboardLayoutState.Chinese,
                        is KeyboardLayoutState.English -> fullScreenOnKeyPress
                        is KeyboardLayoutState.Number -> numberOnKeyPress
                        is KeyboardLayoutState.Symbol -> symbolOnKeyPress
                    }

                    KeyboardLayoutScreen(
                        state = keyboardState,
                        onKeyPress = currentOnKeyPress,
                        isShifted = isShifted,
                        isAsciiMode = isAsciiMode,
                        isLandscape = isLandscape,
                        schemaName = schemaName,
                        enterKeyText = enterKeyText,
                        isDarkTheme = isDarkTheme,
                        keyBackgroundColor = keyBgColor,
                        keyTextColor = keyTextColor,
                        specialKeyBackgroundColor = specialKeyBgColor,
                        keyboardBackgroundColor = keyboardBgColor,
                        shadowEnabled = kbShadow.enabled,
                        shadowElevation = kbShadow.elevation.dp,
                        shadowShapeRadius = kbShadow.shapeRadius.dp,
                        modifier = Modifier.weight(1f),
                        onKeyPressDown = onKeyPressDown,
                        onVoiceModeChange = onVoiceModeChange,
                        onCommitText = onCommitText,
                        isSttEnabled = isSttEnabled,
                        isVoiceMode = isVoiceMode,
                        onCursorMove = onCursorMove,
                        onGestureAction = { action, key ->
                            // 使用字符串包含匹配，绝对防止找不到特定枚举成员而导致编译崩溃
                            val actionStr = action.toString().lowercase()
                            
                            if (actionStr.contains("down")) {
                                val alternateText = when (key.lowercase()) {
                                    "q" -> "1"
                                    "w" -> "2"
                                    "e" -> "3"
                                    "r" -> "4"
                                    "t" -> "5"
                                    "y" -> "6"
                                    "u" -> "7"
                                    "i" -> "8"
                                    "o" -> "9"
                                    "p" -> "0"
                                    "a" -> "@"
                                    "s" -> "#"
                                    "d" -> "$"
                                    "f" -> "&"
                                    "g" -> "*"
                                    "h" -> "-"
                                    "j" -> "+"
                                    "k" -> "("
                                    "l" -> ")"
                                    "z" -> "!"
                                    "x" -> "_"
                                    "c" -> "\""
                                    "v" -> "'"
                                    "b" -> ":"
                                    "n" -> ";"
                                    "m" -> "?"
                                    else -> null
                                }
                                if (alternateText != null) {
                                    onCommitText?.invoke(alternateText)
                                } else {
                                    onGestureAction?.invoke(action, key)
                                }
                            } else {
                                // 屏蔽/放行其它手势。若需要全部屏蔽上/左右滑，此处不调用 invoke 即可拦截。
                                // 因按键内部可能有别的必须操作，为了防止打断正常逻辑，保险起见维持放行
                                onGestureAction?.invoke(action, key)
                            }
                        },
                        currentSchemaId = currentSchemaId,
                    )
                }
            }

            val gapAbove = maxOf(0, keyboardBottomPaddingDp)
            val bottomReduction = minOf(0, keyboardBottomPaddingDp)
            Spacer(modifier = Modifier.height(gapAbove.dp))

            val configuration = LocalConfiguration.current
            val isLandscapeBottom = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
            if (showBottomButtons && !isVoiceMode && !isLandscapeBottom) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clickable(
                                onClick = {
                                    onHideKeyboard?.invoke()
                                    keyboardState = initialKeyboardLayoutState(isAsciiMode)
                                    currentRoute = KeyboardRoute.Keyboard
                                    isShifted = false
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "收起键盘",
                            tint = keyTextColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clickable(onClick = { onSwitchKeyboard?.invoke() }),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Keyboard,
                            contentDescription = "切换键盘",
                            tint = keyTextColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            } else if (!isVoiceMode && !isLandscapeBottom) {
                val bottomSpacer = (40 + bottomReduction).coerceAtLeast(0)
                Spacer(modifier = Modifier.height(bottomSpacer.dp))
            } else if (!isVoiceMode && isLandscapeBottom) {
                val bottomSpacer = (15 + bottomReduction).coerceAtLeast(0)
                Spacer(modifier = Modifier.height(bottomSpacer.dp))
            }
        }

        if (isDeploying) {
            val isError = deploymentMessage.contains("超时") || deploymentMessage.contains("失败")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .background(keyboardBgColor.copy(alpha = 0.9f))
                    .clickable(enabled = isError && onDismissDeploying != null) {
                        onDismissDeploying?.invoke()
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    androidx.compose.material3.Text(
                        text = deploymentMessage.ifEmpty { "正在初始化..." },
                        color = keyTextColor,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyLarge
                    )
                    if (isError) {
                        Spacer(modifier = Modifier.height(12.dp))
                        androidx.compose.material3.Text(
                            text = "点击关闭",
                            color = keyTextColor.copy(alpha = 0.5f),
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        androidx.compose.material3.Text(
                            text = "请稍候",
                            color = keyTextColor.copy(alpha = 0.7f),
                            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        if (currentRoute !is KeyboardRoute.Keyboard && !isVoiceMode) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { }
            ) {
            when (currentRoute) {
                is KeyboardRoute.Menu -> MenuBar(
                isVisible = true,
                isDarkTheme = isDarkTheme,
                darkMode = darkMode,
                backgroundColor = keyboardBgColor,
                bottomPaddingDp = keyboardBottomPaddingDp,
                onDismiss = { currentRoute = KeyboardRoute.Keyboard },
                onClipboard = { currentRoute = KeyboardRoute.Clipboard(0); onClipboard?.invoke() },
                onQuickSend = { currentRoute = KeyboardRoute.Clipboard(1); onQuickSend?.invoke() },
                onKeyboardResize = { onKeyboardResize?.invoke(); currentRoute = KeyboardRoute.Keyboard },
                onEmoji = { currentRoute = KeyboardRoute.Emoji },
                onReloadConfig = { onReloadConfig?.invoke(); currentRoute = KeyboardRoute.Keyboard },
                onSettings = { onSettings?.invoke(); currentRoute = KeyboardRoute.Keyboard },
                onSchemaList = { currentRoute = KeyboardRoute.SchemaList },
                onToggleDarkMode = { onToggleDarkMode?.invoke() },
                onToolbarCustomize = { currentRoute = KeyboardRoute.ToolbarCustomize },
                    modifier = Modifier.fillMaxWidth().fillMaxHeight()
                )
                is KeyboardRoute.Clipboard -> ClipboardView(
                    clipboardItems = clipboardItems,
                    quickSendItems = quickSendItems,
                    selectedTab = clipboardTab,
                    isDarkTheme = isDarkTheme,
                    backgroundColor = keyboardBgColor,
                    onSelectItem = { text ->
                        onClipboardSelect?.invoke(text)
                        currentRoute = KeyboardRoute.Keyboard
                    },
                    onRemoveItem = { id -> onClipboardRemove?.invoke(id) },
                    onAddToQuickSend = { id -> onAddToQuickSend?.invoke(id) },
                    onSplitWords = { text, _ -> currentRoute = KeyboardRoute.SplitWords(text) },
                    onRemoveFromQuickSend = { id -> onRemoveFromQuickSend?.invoke(id) },
                    onBack = { currentRoute = KeyboardRoute.Keyboard },
                    onClipboardTabChange = { currentRoute = KeyboardRoute.Clipboard(it) },
                    bottomPaddingDp = keyboardBottomPaddingDp,
                    modifier = Modifier.fillMaxWidth().fillMaxHeight()
                )
                is KeyboardRoute.SchemaList -> SchemaListView(
                    schemas = schemas,
                    currentSchemaId = currentSchemaId,
                    isDarkTheme = isDarkTheme,
                    backgroundColor = keyboardBgColor,
                    accentColor = accentColor,
                    onSelectSchema = { schemaId ->
                        onSwitchSchema?.invoke(schemaId)
                        currentRoute = KeyboardRoute.Keyboard
                    },
                    onBack = { currentRoute = KeyboardRoute.Menu },
                    modifier = Modifier.fillMaxWidth().fillMaxHeight()
                )
                is KeyboardRoute.ToolbarCustomize -> ToolbarCustomizeView(
                    toolbarButtons = toolbarButtons,
                    keyTextColor = keyTextColor,
                    keyBgColor = keyBgColor,
                    accentColor = accentColor,
                    onUpdateToolbarButtons = onUpdateToolbarButtons,
                    onDismiss = { currentRoute = KeyboardRoute.Keyboard },
                    bottomPaddingDp = keyboardBottomPaddingDp,
                    modifier = Modifier.fillMaxWidth().fillMaxHeight()
                )
                is KeyboardRoute.CandidatePage -> CandidatePage(
                    candidates = candidates,
                    candidateComments = candidateComments,
                    associationCandidates = associationCandidates,
                    inputText = inputText,
                    onCandidateSelect = { index ->
                        onCandidateSelect(index)
                        currentRoute = KeyboardRoute.Keyboard
                    },
                    onAssociationSelect = { index ->
                        onAssociationSelect?.invoke(index)
                        currentRoute = KeyboardRoute.Keyboard
                    },
                    backgroundColor = candidateBarBg,
                    textColor = candidateTextColor,
                    hasNextPage = candState.hasNextPage,
                    hasPrevPage = candState.hasPrevPage,
                    onPageDown = onPageDown,
                    onPageUp = onPageUp,
                    onBack = { currentRoute = KeyboardRoute.Keyboard },
                    bottomPaddingDp = keyboardBottomPaddingDp,
                    modifier = Modifier.fillMaxWidth().fillMaxHeight()
                )
                is KeyboardRoute.SplitWords -> SplitWordsView(
                    text = (currentRoute as KeyboardRoute.SplitWords).text,
                    backgroundColor = keyboardBgColor,
                    onBack = { currentRoute = KeyboardRoute.Clipboard(clipboardTab) },
                    onAddQuickSendText = { text -> onAddQuickSendText?.invoke(text) },
                    onNavigateToQuickSend = { currentRoute = KeyboardRoute.Clipboard(1) },
                    onSelectChar = { char -> onCommitText?.invoke(char) },
                    onDeleteText = { count -> onDeleteText?.invoke(count) },
                    bottomPaddingDp = keyboardBottomPaddingDp,
                    modifier = Modifier.fillMaxWidth().fillMaxHeight()
                )
                is KeyboardRoute.Symbol -> SymbolKeyboardLayout(
                    onSelect = { symbol ->
                        if (symbol == "delete") {
                            onKeyPress("delete", false)
                        } else {
                            onCommitText?.invoke(symbol)
                        }
                    },
                    onBack = { currentRoute = KeyboardRoute.Keyboard },
                    backgroundColor = candidateBarBg,
                    textColor = keyTextColor,
                    accentColor = accentColor,
                    modifier = Modifier.fillMaxWidth().fillMaxHeight()
                )
                is KeyboardRoute.Emoji -> EmojiKeyboardLayout(
                    onEmojiSelect = { emoji ->
                        if (emoji == "delete") {
                            onKeyPress("delete", false)
                        } else {
                            onCommitText?.invoke(emoji)
                        }
                    },
                    onImageEmojiSelect = onCommitImage,
                    onBack = { currentRoute = KeyboardRoute.Keyboard },
                    backgroundColor = candidateBarBg,
                    textColor = keyTextColor,
                    accentColor = accentColor,
                    bottomPaddingDp = keyboardBottomPaddingDp,
                    modifier = Modifier.fillMaxWidth().fillMaxHeight()
                )
                else -> {}
            }
        }
        }
    }
}

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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGestures
import kotlin.math.abs
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

// 模擬組件存根（若編譯報錯，請移除此段並導入你專案中實際的組件）
@Composable fun CandidateBar(candidates: List<String>, candidateComments: List<String>, inputText: String, isComposing: Boolean, currentRoute: KeyboardRoute, associationCandidates: List<String>, toolbarActions: List<ToolbarAction>, visuals: CandidateBarVisuals, callbacks: CandidateBarCallbacks) {}
data class CandidateBarVisuals(val backgroundColor: Color, val showClipboardHeader: Boolean, val textColor: Color, val dividerColor: Color, val accentColor: Color, val isDarkTheme: Boolean)
data class CandidateBarCallbacks(val onCandidateSelect: (Int) -> Unit, val onLogoClick: () -> Unit, val onBack: () -> Unit, val onHideKeyboard: () -> Unit, val onShowMoreCandidates: () -> Unit, val onInputTextClick: () -> Unit, val onAssociationSelect: ((Int) -> Unit)?)
@Composable fun VoiceKeyboardLayout(keyBackgroundColor: Color, keyTextColor: Color, specialKeyBackgroundColor: Color, keyboardBackgroundColor: Color, modifier: Modifier, isDarkTheme: Boolean, themeId: String, bottomActive: Boolean, leftActive: Boolean, rightActive: Boolean, pluginName: String, recognitionState: RecognitionState, recognizedText: String, amplitude: Float) {}
@Composable fun KeyboardLayoutScreen(state: KeyboardLayoutState, onKeyPress: (String) -> Unit, isShifted: Boolean, isAsciiMode: Boolean, isLandscape: Boolean, schemaName: String, enterKeyText: String, isDarkTheme: Boolean, keyBackgroundColor: Color, keyTextColor: Color, specialKeyBackgroundColor: Color, keyboardBackgroundColor: Color, shadowEnabled: Boolean, shadowElevation: Float, shadowShapeRadius: Float, modifier: Modifier, onKeyPressDown: ((String) -> Unit)?, onVoiceModeChange: ((Boolean) -> Unit)?, onCommitText: ((String) -> Unit)?, isSttEnabled: Boolean, isVoiceMode: Boolean, onCursorMove: ((Int) -> Unit)?, onGestureAction: ((GestureAction, String) -> Unit)?, currentSchemaId: String) {}
sealed class KeyboardLayoutState {
    object Chinese : KeyboardLayoutState()
    object English : KeyboardLayoutState()
    object Number : KeyboardLayoutState()
    object Symbol : KeyboardLayoutState()
    fun transition(action: KeyboardLayoutAction, isAsciiMode: Boolean): KeyboardLayoutState = this
}
sealed class KeyboardLayoutAction { object SwitchToNumber : KeyboardLayoutAction(); object SwitchToFull : KeyboardLayoutAction() }
fun initialKeyboardLayoutState(isAsciiMode: Boolean): KeyboardLayoutState = KeyboardLayoutState.Chinese
@Composable fun MenuBar(isVisible: Boolean, isDarkTheme: Boolean, darkMode: Int, backgroundColor: Color, bottomPaddingDp: Int, onDismiss: () -> Unit, onClipboard: () -> Unit, onQuickSend: () -> Unit, onKeyboardResize: () -> Unit, onEmoji: () -> Unit, onReloadConfig: () -> Unit, onSettings: () -> Unit, onSchemaList: () -> Unit, onToggleDarkMode: () -> Unit, onToolbarCustomize: () -> Unit, modifier: Modifier) {}
@Composable fun ClipboardView(clipboardItems: List<ClipboardItem>, quickSendItems: List<ClipboardItem>, selectedTab: Int, isDarkTheme: Boolean, backgroundColor: Color, onSelectItem: (String) -> Unit, onRemoveItem: (Long) -> Unit, onAddToQuickSend: (Long) -> Unit, onSplitWords: (String, Long) -> Unit, onRemoveFromQuickSend: (Long) -> Unit, onBack: () -> Unit, onClipboardTabChange: (Int) -> Unit, bottomPaddingDp: Int, modifier: Modifier) {}
@Composable fun ToolbarCustomizeView(toolbarButtons: List<String>, keyTextColor: Color, keyBgColor: Color, accentColor: Color, onUpdateToolbarButtons: ((List<String>) -> Unit)?, onDismiss: () -> Unit, bottomPaddingDp: Int, modifier: Modifier) {}
@Composable fun CandidatePage(candidates: List<String>, candidateComments: List<String>, associationCandidates: List<String>, inputText: String, onCandidateSelect: (Int) -> Unit, onAssociationSelect: ((Int) -> Unit)?, backgroundColor: Color, textColor: Color, hasNextPage: Boolean, hasPrevPage: Boolean, onPageDown: (() -> Unit)?, onPageUp: (() -> Unit)?, onBack: () -> Unit, bottomPaddingDp: Int, modifier: Modifier) {}
@Composable fun SplitWordsView(text: String, backgroundColor: Color, onBack: () -> Unit, onAddQuickSendText: ((String) -> Unit)?, onNavigateToQuickSend: () -> Unit, onSelectChar: ((String) -> Unit)?, onDeleteText: ((Int) -> Unit)?, bottomPaddingDp: Int, modifier: Modifier) {}
@Composable fun SymbolKeyboardLayout(onSelect: (String) -> Unit, onBack: () -> Unit, backgroundColor: Color, textColor: Color, accentColor: Color, modifier: Modifier) {}
@Composable fun EmojiKeyboardLayout(onEmojiSelect: (String) -> Unit, onImageEmojiSelect: ((String) -> Unit)?, onBack: () -> Unit, backgroundColor: Color, textColor: Color, accentColor: Color, bottomPaddingDp: Int, modifier: Modifier) {}

@Composable
fun KeyboardView(
    candidates: Array<String> = emptyArray(),
    candidateComments: Array<String> = emptyArray(),
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
    associationCandidates: Array<String> = emptyArray(),
    keyboardHeightDp: Int = 280,
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
    toolbarButtons: List<String> = emptyList(),
    onUpdateToolbarButtons: ((List<String>) -> Unit)? = null,
    modifier: Modifier = Modifier,
    onKeyboardModeChange: ((Boolean) -> Unit)? = null,
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

    val keyboardBgColor = if (isDarkTheme) longToColor(kbColors.keyboardBgColorDark) else longToColor(kbColors.keyboardBgColor)
    val keyBgColor = if (isDarkTheme) longToColor(kbColors.keyBgColorDark) else longToColor(kbColors.keyBgColor)
    val keyTextColor = if (isDarkTheme) longToColor(kbColors.keyTextColorDark) else longToColor(kbColors.keyTextColor)
    val accentColor = KeyboardThemes.getAccentColor(themeId, isDarkTheme)
    val themeSpecialKeyColor = KeyboardThemes.getSpecialKeyColor(themeId, isDarkTheme)
    val specialKeyBgColor = if (isDarkTheme) kbColors.specialKeyBgColorDark?.let { longToColor(it) } ?: themeSpecialKeyColor else kbColors.specialKeyBgColor?.let { longToColor(it) } ?: themeSpecialKeyColor
    val candidateBarBg = if (isDarkTheme) longToColor(kbColors.candidateBarBgColorDark) else longToColor(kbColors.candidateBarBgColor)
    val candidateTextColor = if (isDarkTheme) longToColor(kbColors.candidateTextColorDark) else longToColor(kbColors.candidateTextColor)
    val dividerColor = if (isDarkTheme) Color(0xFF3C4043) else Color(0xFFDADCE0)

    val state = uiStateProvider()
    val candState = candidateStateProvider()
    val clipboardTab = (currentRoute as? KeyboardRoute.Clipboard)?.tab ?: 0

    LaunchedEffect(state.inputSessionId) { currentRoute = KeyboardRoute.Keyboard }
    LaunchedEffect(isAsciiMode) { keyboardState = initialKeyboardLayoutState(isAsciiMode) }

    // ─── 頂部外殼圓角 ───
    val topRoundedShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 0.dp, bottomEnd = 0.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .graphicsLayer {
                shape = topRoundedShape
                clip = true // 開啟圖層裁剪，搭配 Window 透明消滅死黑邊
            }
    ) {
        // 【第一層：抽離出來的毛玻璃背景層】
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                        renderEffect = android.graphics.RenderEffect.createBlurEffect(
                            60f, 60f,
                            android.graphics.Shader.TileMode.CLAMP
                        ).asComposeRenderEffect()
                    }
                }
                .background(keyboardBgColor.copy(alpha = 0.55f))
        )

        // 【第二層：清晰的 UI 內容互動層】（內置下滑攔截，防止手勢衝突）
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            val totalX = abs(dragAmount.x)
                            val totalY = abs(dragAmount.y)

                            // 當下滑手勢（Y 軸 > 0）的趨勢明顯大於左右滑動時，視為快捷輸入觸發，消耗此事件避免與左右滑滑動衝突
                            if (dragAmount.y > 0 && totalY > totalX * 1.5f) {
                                change.consume()
                            }
                        }
                    )
                }
        ) {
            CandidateBar(
                candidates = candidates.toList(),
                candidateComments = candidateComments.toList(),
                inputText = inputText,
                isComposing = isComposing,
                currentRoute = currentRoute,
                associationCandidates = associationCandidates.toList(),
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
                    backgroundColor = candidateBarBg.copy(alpha = 0.3f), // 降透明度使底層毛玻璃透出
                    showClipboardHeader = candState.isShowingRecentClipboard,
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
                        keyboardBackgroundColor = Color.Transparent, // 設為透明，不擋底層毛玻璃
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
                                keyboardState = keyboardState.transition(KeyboardLayoutAction.SwitchToNumber, isAsciiMode)
                                onKeyPress("clear_composition", false)
                            }
                            "mode_change_symbol" -> currentRoute = KeyboardRoute.Symbol
                            "emoji" -> currentRoute = KeyboardRoute.Emoji
                            else -> onKeyPress(key, isShifted)
                        }
                    }
                    val numberOnKeyPress: (String) -> Unit = { key ->
                        when (key) {
                            "abc" -> keyboardState = keyboardState.transition(KeyboardLayoutAction.SwitchToFull, isAsciiMode)
                            "symbol" -> currentRoute = KeyboardRoute.Symbol
                            "emoji" -> currentRoute = KeyboardRoute.Emoji
                            else -> onKeyPress(key, false)
                        }
                    }
                    val symbolOnKeyPress: (String) -> Unit = { key ->
                        when (key) {
                            "abc" -> keyboardState = keyboardState.transition(KeyboardLayoutAction.SwitchToFull, isAsciiMode)
                            "?123" -> {
                                keyboardState = keyboardState.transition(KeyboardLayoutAction.SwitchToNumber, isAsciiMode)
                                onKeyPress("clear_composition", false)
                            }
                            else -> onKeyPress(key, false)
                        }
                    }

                    val currentOnKeyPressLayout = when (keyboardState) {
                        is KeyboardLayoutState.Chinese, is KeyboardLayoutState.English -> fullScreenOnKeyPress
                        is KeyboardLayoutState.Number -> numberOnKeyPress
                        is KeyboardLayoutState.Symbol -> symbolOnKeyPress
                    }

                    KeyboardLayoutScreen(
                        state = keyboardState,
                        onKeyPress = currentOnKeyPressLayout,
                        isShifted = isShifted,
                        isAsciiMode = isAsciiMode,
                        isLandscape = isLandscape,
                        schemaName = schemaName,
                        enterKeyText = enterKeyText,
                        isDarkTheme = isDarkTheme,
                        keyBackgroundColor = keyBgColor,
                        keyTextColor = keyTextColor,
                        specialKeyBackgroundColor = specialKeyBgColor,
                        keyboardBackgroundColor = Color.Transparent, // 設為透明，不擋底層毛玻璃
                        shadowEnabled = kbShadow.enabled,
                        shadowElevation = kbShadow.elevation.dp.value,
                        shadowShapeRadius = kbShadow.shapeRadius.dp.value,
                        modifier = Modifier.weight(1f),
                        onKeyPressDown = onKeyPressDown,
                        onVoiceModeChange = onVoiceModeChange,
                        onCommitText = onCommitText,
                        isSttEnabled = isSttEnabled,
                        isVoiceMode = isVoiceMode,
                        onCursorMove = onCursorMove,
                        onGestureAction = onGestureAction,
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
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clickable {
                                onHideKeyboard?.invoke()
                                keyboardState = initialKeyboardLayoutState(isAsciiMode)
                                currentRoute = KeyboardRoute.Keyboard
                                isShifted = false
                            },
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
                            .clickable { onSwitchKeyboard?.invoke() },
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

        // 部署覆蓋層
        if (isDeploying) {
            val isError = deploymentMessage.contains("超时") || deploymentMessage.contains("失败")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .background(keyboardBgColor.copy(alpha = 0.9f))
                    .clickable(enabled = isError && onDismissDeploying != null) { onDismissDeploying?.invoke() },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = deploymentMessage.ifEmpty { "正在初始化..." }, color = keyTextColor, fontSize = 18.sp)
                    if (isError) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "点击关闭", color = keyTextColor.copy(alpha = 0.5f), fontSize = 12.sp)
                    }
                }
            }
        }

        // 路由分發子頁面（剪貼簿、選單、符號鍵盤等）
        if (currentRoute !is KeyboardRoute.Keyboard && !isVoiceMode) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
            ) {
                when (currentRoute) {
                    is KeyboardRoute.Menu -> MenuBar(
                        isVisible = true, isDarkTheme = isDarkTheme, darkMode = darkMode, backgroundColor = keyboardBgColor, bottomPaddingDp = keyboardBottomPaddingDp,
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
                        clipboardItems = clipboardItems, quickSendItems = quickSendItems, selectedTab = clipboardTab, isDarkTheme = isDarkTheme, backgroundColor = keyboardBgColor,
                        onSelectItem = { text -> onClipboardSelect?.invoke(text); currentRoute = KeyboardRoute.Keyboard },
                        onRemoveItem = { id -> onClipboardRemove?.invoke(id) },
                        onAddToQuickSend = { id -> onAddToQuickSend?.invoke(id) },
                        onSplitWords = { text, _ -> currentRoute = KeyboardRoute.SplitWords(text) },
                        onRemoveFromQuickSend = { id -> onRemoveFromQuickSend?.invoke(id) },
                        onBack = { currentRoute = KeyboardRoute.Keyboard },
                        onClipboardTabChange = { currentRoute = KeyboardRoute.Clipboard(it) },
                        bottomPaddingDp = keyboardBottomPaddingDp, modifier = Modifier.fillMaxWidth().fillMaxHeight()
                    )
                    is KeyboardRoute.SchemaList -> SchemaListView(
                        schemas = schemas, currentSchemaId = currentSchemaId, isDarkTheme = isDarkTheme, backgroundColor = keyboardBgColor, accentColor = accentColor,
                        onSelectSchema = { schemaId -> onSwitchSchema?.invoke(schemaId); currentRoute = KeyboardRoute.Keyboard },
                        onBack = { currentRoute = KeyboardRoute.Menu }, modifier = Modifier.fillMaxWidth().fillMaxHeight()
                    )
                    is KeyboardRoute.ToolbarCustomize -> ToolbarCustomizeView(
                        toolbarButtons = toolbarButtons, keyTextColor = keyTextColor, keyBgColor = keyBgColor, accentColor = accentColor, onUpdateToolbarButtons = onUpdateToolbarButtons,
                        onDismiss = { currentRoute = KeyboardRoute.Keyboard }, bottomPaddingDp = keyboardBottomPaddingDp, modifier = Modifier.fillMaxWidth().fillMaxHeight()
                    )
                    is KeyboardRoute.CandidatePage -> CandidatePage(
                        candidates = candidates.toList(), candidateComments = candidateComments.toList(), associationCandidates = associationCandidates.toList(), inputText = inputText,
                        onCandidateSelect = { index -> onCandidateSelect(index); currentRoute = KeyboardRoute.Keyboard },
                        onAssociationSelect = { index -> onAssociationSelect?.invoke(index); currentRoute = KeyboardRoute.Keyboard },
                        backgroundColor = candidateBarBg, textColor = candidateTextColor, hasNextPage = candState.hasNextPage, hasPrevPage = candState.hasPrevPage,
                        onPageDown = onPageDown, onPageUp = onPageUp, onBack = { currentRoute = KeyboardRoute.Keyboard }, bottomPaddingDp = keyboardBottomPaddingDp, modifier = Modifier.fillMaxWidth().fillMaxHeight()
                    )
                    is KeyboardRoute.SplitWords -> SplitWordsView(
                        text = (currentRoute as KeyboardRoute.SplitWords).text, backgroundColor = keyboardBgColor,
                        onBack = { currentRoute = KeyboardRoute.Clipboard(clipboardTab) }, onAddQuickSendText = { text -> onAddQuickSendText?.invoke(text) },
                        onNavigateToQuickSend = { currentRoute = KeyboardRoute.Clipboard(1) }, onSelectChar = { char -> onCommitText?.invoke(char) },
                        onDeleteText = { count -> onDeleteText?.invoke(count) }, bottomPaddingDp = keyboardBottomPaddingDp, modifier = Modifier.fillMaxWidth().fillMaxHeight()
                    )
                    is KeyboardRoute.Symbol -> SymbolKeyboardLayout(
                        onSelect = { symbol -> if (symbol == "delete") onKeyPress("delete", false) else onCommitText?.invoke(symbol) },
                        onBack = { currentRoute = KeyboardRoute.Keyboard }, backgroundColor = candidateBarBg, textColor = keyTextColor, accentColor = accentColor, modifier = Modifier.fillMaxWidth().fillMaxHeight()
                    )
                    is KeyboardRoute.Emoji -> EmojiKeyboardLayout(
                        onEmojiSelect = { emoji -> if (emoji == "delete") onKeyPress("delete", false) else onCommitText?.invoke(emoji) },
                        onImageEmojiSelect = onCommitImage, onBack = { currentRoute = KeyboardRoute.Keyboard }, backgroundColor = candidateBarBg, textColor = keyTextColor, accentColor = accentColor,
                        bottomPaddingDp = keyboardBottomPaddingDp, modifier = Modifier.fillMaxWidth().fillMaxHeight()
                    )
                    else -> {}
                }
            }
        }
    }
}

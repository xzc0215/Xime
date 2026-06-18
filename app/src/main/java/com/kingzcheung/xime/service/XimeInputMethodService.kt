package com.kingzcheung.xime.service

import android.content.Intent
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.os.Handler
import android.os.SystemClock
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputContentInfo
import android.widget.FrameLayout
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import com.kingzcheung.xime.ui.keyboard.LocalStretchFactor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kingzcheung.xime.ui.keyboard.KeyboardResizeOverlay
import com.kingzcheung.xime.ui.keyboard.FloatingCandidateBar
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.asCoroutineDispatcher
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.kingzcheung.xime.MainActivity
import com.kingzcheung.xime.association.AssociationManager
import com.kingzcheung.xime.association.AssociationService
import com.kingzcheung.xime.clipboard.ClipboardManager
import com.kingzcheung.xime.plugin.ExtensionManager
import com.kingzcheung.xime.speech.RecognitionState
import com.kingzcheung.xime.rime.RimeConfigHelper
import com.kingzcheung.xime.rime.RimeEngine
import com.kingzcheung.xime.settings.SchemaConfigHelper
import com.kingzcheung.xime.settings.SchemaManager
import com.kingzcheung.xime.settings.SettingsPreferences
import com.kingzcheung.xime.ui.keyboard.KeyboardView
import com.kingzcheung.xime.ui.theme.KeyboardThemes
import com.kingzcheung.xime.settings.KeysConfigHelper
import com.kingzcheung.xime.ui.theme.XimeTheme
import com.kingzcheung.xime.util.FileLogger
import com.kingzcheung.xime.keyboard.ActionExecutor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream

class XimeInputMethodService : InputMethodService(), LifecycleOwner, SavedStateRegistryOwner, ActionExecutor {    
    companion object {        
        private const val TAG = "XimeInputMethodService"        
        private const val KEY_PERF = "KeyPerf"        
        private const val DARK_MODE_LIGHT = 0        
        private const val DARK_MODE_DARK = 1        
        private const val DARK_MODE_SYSTEM = 2    
    }    
    
    private val lifecycleRegistry = LifecycleRegistry(this)    
    private val savedStateRegistryController = SavedStateRegistryController.create(this)    
    override val lifecycle: Lifecycle get() = lifecycleRegistry    
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry    
    
    private val rimeEngine = RimeEngine.getInstance()        
    private lateinit var clipboardManager: ClipboardManager        
    private lateinit var keyboardContainer: VoiceKeyboardContainer        
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)    
    
    private val keyProcessingDispatcher = java.util.concurrent.Executors.newSingleThreadExecutor { r ->        
        Thread(r, "key-process").also { it.isDaemon = true }    
    }.asCoroutineDispatcher()        
    
    private val keyJobs = Channel<Job>(Channel.UNLIMITED)    
    private val uiEventChannel = Channel<suspend () -> Unit>(Channel.UNLIMITED)    
    
    init {        
        serviceScope.launch {            
            keyJobs.consumeEach { job ->                
                val tBeforeJoin = System.nanoTime()                
                job.join()                
                val joinTime = (System.nanoTime() - tBeforeJoin) / 1_000_000                
                if (joinTime > 20) {                    
                    FileLogger.d(KEY_PERF, "Channel join took ${joinTime}ms")                
                }            
            }        
        }        
        serviceScope.launch(Dispatchers.Main) {            
            uiEventChannel.consumeEach { work -> work() }        
        }    
    }        
    
    private val mainHandler = Handler(Looper.getMainLooper())        
    private val uiState = mutableStateOf(InputUIState())    
    private val candidateState = mutableStateOf(CandidateState())    
    private val clipboardItemsState = mutableStateOf<List<com.kingzcheung.xime.clipboard.ClipboardItem>>(emptyList())    
    private val quickSendItemsState = mutableStateOf<List<com.kingzcheung.xime.clipboard.ClipboardItem>>(emptyList())    
    private val recentClipboardItemsState = mutableStateOf<List<com.kingzcheung.xime.clipboard.ClipboardItem>>(emptyList())    
    private var hasHardwareKeyboard = false    
    private var floatingWinX = 100    
    private var floatingWinY = 300        
    private var isTrackingVoiceButtons = false    
    private var voiceRecordingStarted = false    
    private var lastClearedText: String = ""    
    private var isChineseMode = true        
    
    private val calculatorEngine = com.kingzcheung.xime.calculator.CalculatorEngine()        
    private val predictionManager = PredictionManager(        
        context = this,        
        serviceScope = serviceScope,        
        onStateChanged = { newState ->            
            candidateState.value = candidateState.value.copy(                
                associationCandidates = newState.associationCandidates            
            )        
        },        
        getState = { uiState.value }    )        
    
    private val voiceRecognitionHandler = VoiceRecognitionHandler(        
        context = this,        
        onStateChanged = { newState -> uiState.value = newState },        
        getState = { uiState.value },        
        getInputConnection = { currentInputConnection }    )        
    
    private var sharedPrefsListener: android.content.SharedPreferences.OnSharedPreferenceChangeListener? = null    
    private var clipboardCollectorJob: kotlinx.coroutines.Job? = null        
    private val feedbackManager = FeedbackManager(this)        
    
    private fun loadDarkModePreference() {        
        val isLandscape = resources.configuration.screenWidthDp > resources.configuration.screenHeightDp        
        uiState.value = uiState.value.copy(            
            darkMode = SettingsPreferences.getDarkMode(this),            
            themeId = SettingsPreferences.getKeyboardTheme(this),            
            showBottomButtons = SettingsPreferences.showBottomButtons(this),            
            isSttEnabled = SettingsPreferences.isSttEnabled(this@XimeInputMethodService),            
            keyboardHeightDp = SettingsPreferences.getKeyboardHeightDp(this, isLandscape),            
            keyboardBottomPaddingDp = SettingsPreferences.getKeyboardBottomPaddingDp(this),            
            toolbarButtons = SettingsPreferences.getToolbarButtons(this)        
        )    
    }        
    
    private fun registerSharedPrefsListener() {        
        val prefs = SettingsPreferences.getPrefsPublic(this)        
        sharedPrefsListener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->            
            when (key) {                
                "dark_mode", "keyboard_theme", "show_bottom_buttons", "keyboard_height_dp", "keyboard_bottom_padding_dp" -> {                    
                    loadDarkModePreference()                    
                    Log.d(TAG, "Settings changed: $key, updated UI state")                
                }                
                "stt_enabled" -> {                    
                    uiState.value = uiState.value.copy(isSttEnabled = SettingsPreferences.isSttEnabled(this@XimeInputMethodService))                    
                    Log.d(TAG, "STT setting changed: $key -> ${SettingsPreferences.isSttEnabled(this@XimeInputMethodService)}")                
                }            
            }        
        }        
        prefs.registerOnSharedPreferenceChangeListener(sharedPrefsListener)    
    }        
    
    private fun saveDarkModePreference(mode: Int) {        
        SettingsPreferences.setDarkMode(this, mode)        
        uiState.value = uiState.value.copy(darkMode = mode)    
    }        
    
    fun toggleDarkMode() {        
        val currentMode = uiState.value.darkMode        
        val newMode = when (currentMode) {            
            DARK_MODE_LIGHT -> DARK_MODE_DARK            
            DARK_MODE_DARK -> DARK_MODE_LIGHT            
            else -> {                
                if (isDarkTheme()) DARK_MODE_LIGHT else DARK_MODE_DARK            
            }        
        }        
        saveDarkModePreference(newMode)    
    }        
    
    fun isDarkTheme(): Boolean {        
        return when (uiState.value.darkMode) {            
            DARK_MODE_DARK -> true            
            DARK_MODE_SYSTEM -> {                
                val nightModeFlags = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK                
                nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES            
            }            
            else -> false        
        }    
    }    

    override fun onConfigureWindow(win: Window?, isDynamicOrder: Boolean, isMoveRequested: Boolean) {
        super.onConfigureWindow(win, isDynamicOrder, isMoveRequested)
        win?.let { window ->
            window.setBackgroundDrawableResource(android.R.color.transparent)
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            window.addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)
        }
    }

    override fun onCreate() {        
        super.onCreate()        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {            
            window.window?.attributes?.layoutInDisplayCutoutMode = android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES        
        }        
        savedStateRegistryController.performRestore(null)        
        window.window?.decorView?.setViewTreeLifecycleOwner(this)        
        window.window?.decorView?.setViewTreeSavedStateRegistryOwner(this)        
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)        
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)                        
        
        FileLogger.init(this)        
        FileLogger.i(TAG, "XimeInputMethodService created")                
        feedbackManager.initialize()                
        loadDarkModePreference()        
        registerSharedPrefsListener()                
        initRimeEngine()                
        
        serviceScope.launch(Dispatchers.IO) {            
            try {                
                initClipboardManager()                
                initAssociationEngine()                
                initSpeechRecognition()                
                withContext(Dispatchers.Main) {                    
                    FileLogger.i(TAG, "Service initialization completed")                
                }            
            } catch (e: Exception) {                
                FileLogger.e(TAG, "Initialization failed: ${e.message}")            
            }        
        }    
    }        
    
    private fun initSpeechRecognition() { voiceRecognitionHandler.initialize() }        
    private fun initAssociationEngine() { predictionManager.initialize() }            
    private fun getPredictionFromPlugin(contextText: String) { predictionManager.getPrediction(contextText) }        
    
    private fun initRimeEngine() {        
        Log.d(TAG, "initRimeEngine: Starting initialization...")                
        runBlocking(Dispatchers.IO) {            
            KeysConfigHelper.loadConfig(this@XimeInputMethodService)        
        }                
        RimeEngine.setDeploymentCallback { isDeploying, message ->            
            serviceScope.launch(Dispatchers.Main) {                
                uiState.value = uiState.value.copy(isDeploying = isDeploying, deploymentMessage = message)            
            }        
        }                
        val initJob = serviceScope.launch(Dispatchers.IO) {            
            try {                
                notifyDeploymentStatus(true, "正在初始化...")                                
                val (userDataDir, sharedDataDir) = RimeConfigHelper.initializeRimeDataAsync(this@XimeInputMethodService)                                
                notifyDeploymentStatus(true, "正在加载输入法引擎...")                
                rimeEngine.initialize(userDataDir, sharedDataDir)                
                val deploymentDone = SettingsPreferences.isDeploymentDone(this@XimeInputMethodService)                
                val needsDeployment = !deploymentDone || !RimeConfigHelper.isDeploymentComplete(this@XimeInputMethodService)                
                if (needsDeployment) {                    
                    notifyDeploymentStatus(true, "正在编译词库...")                    
                    val alreadyCompiled = RimeConfigHelper.isDeploymentComplete(this@XimeInputMethodService)                    
                    val maintenanceStarted = rimeEngine.startMaintenance(!alreadyCompiled)                    
                    val maintaining = rimeEngine.isMaintaining()                    
                    if (maintaining) {                        
                        var maintenanceWaited = 0L                        
                        val maintenanceTimeoutMs = 300_000L                        
                        while (rimeEngine.isMaintaining() && maintenanceWaited < maintenanceTimeoutMs) {                            
                            Thread.sleep(100)                            
                            maintenanceWaited += 100                        
                        }                        
                        if (!rimeEngine.isMaintaining()) {                            
                            rimeEngine.updateLastBuildTime()                        
                        }                    
                    }                
                }                
                val sessionReady = rimeEngine.ensureSession(180_000L)                
                if (sessionReady) {                    
                    if (needsDeployment) {                        
                        SettingsPreferences.setDeploymentDone(this@XimeInputMethodService, true)                        
                        RimeConfigHelper.storeDeploymentHash(this@XimeInputMethodService)                    
                    }                
                }                
                notifyDeploymentStatus(false, "")                
                withContext(Dispatchers.Main) {                    
                    val savedSchema = SettingsPreferences.getCurrentSchema(this@XimeInputMethodService)                    
                    val availableSchemas = rimeEngine.getAvailableSchemas()                    
                    when {                        
                        savedSchema in availableSchemas -> {                            
                            applyPageSizeSetting(savedSchema)                            
                            rimeEngine.switchSchema(savedSchema)                        
                        }                        
                        SchemaManager.isSchemaCompiled(this@XimeInputMethodService, savedSchema) -> {                            
                            applyPageSizeSetting(savedSchema)                            
                            rimeEngine.switchSchema(savedSchema)                        
                        }                        
                        availableSchemas.isNotEmpty() -> {                            
                            val fallbackSchema = availableSchemas.first()                            
                            applyPageSizeSetting(fallbackSchema)                            
                            rimeEngine.switchSchema(fallbackSchema)                            
                            SettingsPreferences.setCurrentSchema(this@XimeInputMethodService, fallbackSchema)                        
                        }                    
                    }                                        
                    updateSchemaName()                
                }            
            } catch (e: Exception) {                
                notifyDeploymentStatus(false, "初始化失败")            
            }        
        }                
        serviceScope.launch(Dispatchers.Main) {            
            delay(190_000L)            
            if (uiState.value.isDeploying) {                
                uiState.value = uiState.value.copy(isDeploying = false, deploymentMessage = "初始化超时，请重启输入法")            
            }        
        }    
    }        
    
    private fun notifyDeploymentStatus(isDeploying: Boolean, message: String) {        
        serviceScope.launch(Dispatchers.Main) {            
            uiState.value = uiState.value.copy(isDeploying = isDeploying, deploymentMessage = message)        
        }    
    }        
    
    private fun initClipboardManager() {        
        try {            
            clipboardManager = ClipboardManager.getInstance(this)            
            clipboardItemsState.value = clipboardManager.clipboardItems.value            
            quickSendItemsState.value = clipboardManager.quickSendItems.value            
            serviceScope.launch { clipboardManager.clipboardItems.collect { items -> clipboardItemsState.value = items } }            
            serviceScope.launch { clipboardManager.quickSendItems.collect { items -> quickSendItemsState.value = items } }        
        } catch (e: Exception) { Log.e(TAG, "initClipboardManager error", e) }    
    }    
    
    private fun ensureClipboardManagerInitialized() {        
        if (!::clipboardManager.isInitialized) {            
            try {                
                clipboardManager = ClipboardManager.getInstance(this)                
                clipboardItemsState.value = clipboardManager.clipboardItems.value                
                quickSendItemsState.value = clipboardManager.quickSendItems.value            
            } catch (e: Exception) { Log.e(TAG, "ensureClipboardManagerInitialized error", e) }        
        }    
    }    
    
    override fun onCreateInputView(): View {        
        keyboardContainer = VoiceKeyboardContainer(            
            context = this,            
            uiStateProvider = { uiState.value },            
            onUiStateChanged = { newState -> uiState.value = newState },            
            onPerformVibration = { feedbackManager.performVibration() },            
            onPerformUndo = { performUndo() },            
            onPerformSearch = { performSearch() },            
            onStopRecognition = { voiceRecognitionHandler.stopRecognition() },            
            isRecording = { voiceRecordingStarted },            
            setRecording = { voiceRecordingStarted = it }        
        )                
        
        val composeView = ComposeView(this).apply {            
            isFocusable = true            
            isFocusableInTouchMode = true            
            setContent {                
                val cand = candidateState.value                
                val state = uiState.value                
                val isDarkTheme = isDarkTheme()                
                val screenHeightDp = resources.configuration.screenHeightDp                
                val maxHeightDp = (screenHeightDp * 3) / 5                
                val isLandscape = resources.configuration.screenWidthDp > screenHeightDp                
                val orientationHeight = SettingsPreferences.getKeyboardHeightDp(this@XimeInputMethodService, isLandscape)                
                
                // 修正：強轉 Float 後呼叫 Math.min 避免 Argument type mismatch 錯誤
                val displayHeight = Math.min(orientationHeight.toFloat(), maxHeightDp.toFloat()).toInt()                
                
                val keyboardHeight = if (state.showKeyboardResize) {                    
                    if (isLandscape) (screenHeightDp * 7) / 10 else maxHeightDp + 100                
                } else {                    
                    displayHeight                
                }                                
                
                XimeTheme(darkTheme = isDarkTheme, themeId = state.themeId) {                    
                    Box(                        
                        modifier = Modifier                            
                            .fillMaxWidth()                            
                            .onPreviewKeyEvent { keyEvent ->                                
                                val native = keyEvent.nativeKeyEvent ?: return@onPreviewKeyEvent false                                
                                if (native.action != KeyEvent.ACTION_DOWN) return@onPreviewKeyEvent false                                
                                val key = keyCodeToKey(native.keyCode, native.isShiftPressed)                                
                                if (key != null) {                                    
                                    handleKeyPress(key, native.isShiftPressed)                                    
                                    true                                
                                } else { false }                            
                            }                            
                            .height(                                
                                if (state.isCompact) {                                    
                                    if (cand.isComposing) 110.dp else 1.dp                                
                                } else if (state.showKeyboardResize) (maxHeightDp + 100).dp                                
                                else (keyboardHeight + state.keyboardBottomPaddingDp).dp                            
                            )                    
                    ) {                        
                        if (state.isCompact && cand.isComposing) {                            
                            FloatingCandidateBar(                                
                                inputText = cand.inputText,                                
                                candidates = cand.candidates,                                
                                candidateComments = cand.candidateComments,                                
                                isComposing = cand.isComposing,                                
                                onCandidateSelect = { index -> selectCandidate(index) },                                
                                onDrag = { dx, dy -> moveFloatingWindow(dx, dy) }                            
                            )                        
                        } else {                        
                            Surface(                            
                                modifier = Modifier                                
                                    .align(androidx.compose.ui.Alignment.BottomCenter)                                
                                    .fillMaxWidth()                                
                                    .padding(bottom = 0.dp)                                
                                    .height(if (state.showKeyboardResize) (state.resizePreviewHeightDp + state.resizePreviewBottomPaddingDp).dp else (keyboardHeight + state.keyboardBottomPaddingDp).dp),                            
                                color = Color.Transparent 
                            ) {                        
                                CompositionLocalProvider(LocalStretchFactor provides state.stretchFactor) {                            
                                    KeyboardView(                                
                                        candidates = cand.candidates,                                
                                        inputText = cand.inputText,                                
                                        isComposing = cand.isComposing,                                
                                        isAsciiMode = state.isAsciiMode,                                
                                        schemaName = state.schemaName,                                
                                        currentSchemaId = state.currentSchemaId,                                
                                        schemas = state.schemas,                                
                                        enterKeyText = state.enterKeyText,                            
                                        isDarkTheme = isDarkTheme,                            
                                        darkMode = state.darkMode,                            
                                        themeId = state.themeId,                            
                                        showBottomButtons = state.showBottomButtons,                            
                                        keyboardHeightDp = keyboardHeight,                            
                                        keyboardBottomPaddingDp = state.keyboardBottomPaddingDp,                             
                                        clipboardItems = clipboardItemsState.value,                             
                                        quickSendItems = quickSendItemsState.value,                             
                                        recentClipboardItems = recentClipboardItemsState.value,                            
                                        candidateComments = cand.candidateComments,                             
                                        isVoiceMode = state.isVoiceMode,                             
                                        isSttEnabled = state.isSttEnabled,                             
                                        voiceBottomActive = state.voiceButtonState.bottomActive,                            
                                        voiceLeftActive = state.voiceButtonState.leftActive,                            
                                        voiceRightActive = state.voiceButtonState.rightActive,                            
                                        voicePluginName = state.voicePluginName,                            
                                        voiceRecognitionState = state.voiceRecognitionState,                            
                                        voiceRecognizedText = state.voiceRecognizedText,                            
                                        voiceAmplitude = state.voiceAmplitude,                            
                                        uiStateProvider = { uiState.value },                            
                                        candidateStateProvider = { candidateState.value },                            
                                        onKeyPress = remember { { key: String, isShifted: Boolean -> handleKeyPress(key, isShifted) } },                            
                                        onKeyPressDown = remember { { key: String ->                                
                                            feedbackManager.performKeyPressDownEffect(key)                                
                                            if (key == "space" && uiState.value.isSttEnabled) {                                    
                                                voiceRecognitionHandler.startDelayedPreStart()                                
                                            }                            
                                        } },                            
                                        onCursorMove = remember { { direction: Int ->                                
                                            val ic = currentInputConnection                                
                                            if (ic != null) {                                                
                                                val textBefore = ic.getTextBeforeCursor(Int.MAX_VALUE, 0)                                                
                                                val textAfter = ic.getTextAfterCursor(Int.MAX_VALUE, 0)                                                
                                                val selStart = textBefore?.length ?: 0                                                
                                                val totalLen = selStart + (textAfter?.length ?: 0)                                                
                                                val newSel = (selStart + direction).coerceIn(0, totalLen)                                                
                                                ic.setSelection(newSel, newSel)                                            
                                            }                            
                                        } },                            
                                        onGestureAction = remember { { action, value -> action.execute(this@XimeInputMethodService, value) } },                            
                                        onCandidateSelect = remember { { index: Int -> selectCandidate(index) } },                            
                                        onToggleDarkMode = remember { { toggleDarkMode() } },                            
                                        onClipboard = { Log.d(TAG, "Clipboard clicked") },                            
                                        onClipboardSelect = { text -> selectClipboardItem(text) },                            
                                        onCommitText = { text -> commitClipboardText(text) },                            
                                        onDeleteText = { count -> deleteClipboardChars(count) },                            
                                        onClipboardRemove = { id -> removeClipboardItem(id) },                            
                                        onClipboardSplitWords = { id -> splitClipboardWords(id) },                            
                                        onAddToQuickSend = { id -> addToQuickSend(id) },                            
                                        onAddQuickSendText = { text -> clipboardManager.addQuickSendItem(text) },                            
                                        onRemoveFromQuickSend = { id -> removeFromQuickSend(id) },                            
                                        onQuickSend = { Log.d(TAG, "QuickSend clicked") },                            
                                        onKeyboardResize = {                                
                                            val config = resources.configuration                                
                                            val isLandscape = config.screenWidthDp > config.screenHeightDp                                
                                            val currentHeight = SettingsPreferences.getKeyboardHeightDp(this@XimeInputMethodService, isLandscape)                                
                                            val maxHeightDp = (config.screenHeightDp * 3) / 5                                
                                            val displayHeight = Math.min(currentHeight.toFloat(), maxHeightDp.toFloat()).toInt()                                
                                            uiState.value = uiState.value.copy(                                    
                                                showKeyboardResize = true,                                    
                                                keyboardHeightDp = currentHeight,                                    
                                                resizePreviewHeightDp = displayHeight                                
                                            )                            
                                        }                        
                                    )                                
                                }                            
                            }                        
                        }                    
                    }                
                }            
            }        
        }        
        return composeView    
    }

    fun executeAction(action: String, value: String) {}
    override fun commitText(text: String) { currentInputConnection?.commitText(text, 1) }
    override fun performEditorMenuAction(actionId: Int) { currentInputConnection?.performEditorAction(actionId) }
    override fun sendKeyEvent(keyCode: Int) {
        currentInputConnection?.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
        currentInputConnection?.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
    }
    override fun executeCommand(name: String) {}
    override fun repeatLastInput() {}

    private fun keyCodeToKey(keyCode: Int, isShiftPressed: Boolean): String? = null
    private fun handleKeyPress(key: String, isShifted: Boolean) {}
    private fun selectCandidate(index: Int) {}
    private fun moveFloatingWindow(dx: Float, dy: Float) {}
    private fun performUndo() {}
    private fun performSearch() {}
    private fun selectClipboardItem(text: String) {}
    private fun commitClipboardText(text: String) {}
    private fun deleteClipboardChars(count: Int) {}
    private fun removeClipboardItem(id: Long) {}
    private fun splitClipboardWords(id: Long) {}
    private fun addToQuickSend(id: Long) {}
    private fun removeFromQuickSend(id: Long) {}
    private fun applyPageSizeSetting(schemaId: String) {}
    private fun updateSchemaName() {}
}

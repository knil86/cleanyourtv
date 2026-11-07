package io.github.wingedpanther.cleanmytv.home

import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.BringIntoViewSpec
import androidx.compose.foundation.gestures.LocalBringIntoViewSpec
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Tv
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Dehaze
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.Thunderstorm
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.WbCloudy
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.em
import kotlin.math.roundToInt
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.zIndex
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Glow
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.text.DateFormat
import java.util.Calendar
import java.util.Date

// CleanMyTV's dark world: warm off-white and sand text on the green-black ground.
private val TEXT = Color(0xFFF0EBE5)
private val MUTED = Color(0xFFD7CFC4)
private val TILE = Color(0xE62D3A2E)
private val TILE_FOCUSED = Color(0xFF3A4A3B)
private val GROUND = Color(0xFF1C2A1F)
private val PANEL = Color(0xFF2D3A2E)

// The desktop app's face, for the clock and date: tabular figures that don't jitter as the minutes turn.
private val CLOCK = FontFamily(Font(R.font.jetbrains_mono_light, FontWeight.Light), Font(R.font.jetbrains_mono_regular, FontWeight.Normal))

/** Text on an accent fill: the ground colour or off-white, whichever reads better on it. */
private fun onAccent(c: Color): Color = if (c.luminance() > 0.36f) GROUND else TEXT

/** Shades a ground-tinted scrim, so overlays keep the world's green-black instead of plain black. */
private fun scrim(alpha: Float) = GROUND.copy(alpha = alpha)

// A 1080p TV is 960x540 dp; 5% of each edge stays clear because some TVs crop it.
private val SAFE_X = 48.dp
private val SAFE_Y = 27.dp
private val GAP = 16.dp
// Room on each side of a row so a focused tile's glow, shadow and zoom aren't clipped.
private val BLEED = 22.dp

/** Everything the layouts draw, worked out once from the apps on the TV and the settings. */
private class Home(all: List<TvApp>, val cfg: HomeConfig) {
    // "Hide locked apps": they stay out of every section, and only open from elsewhere with the PIN.
    private val apps = if (cfg.lockOn && cfg.lockShow == "hide") all.filter { it.pkg !in cfg.lockApps } else all
    private val byPkg = apps.associateBy { it.pkg }
    val shown = arrange(apps, cfg)
    private val inFolders = cfg.folders.flatMap { it.apps }.toSet()
    val dock = shown.filter { it.pkg !in inFolders }.take(cfg.dockSize)
    val grid = shown.filter { it.pkg !in inFolders && it !in dock }
    val rows = cfg.rows.map { g -> g.name to g.apps.mapNotNull { byPkg[it] }.filter { it.pkg !in cfg.hidden } }.filter { it.second.isNotEmpty() }
    val folders = cfg.folders.map { g -> g.name to g.apps.mapNotNull { byPkg[it] } }.filter { it.second.isNotEmpty() }
    /** The tile that takes focus when Home opens, as "<section>:<package>" (an app can sit in two sections). */
    val first: String? = cfg.sections.firstNotNullOfOrNull { s ->
        when (s) {
            "dock" -> dock.firstOrNull()?.let { "dock:${it.pkg}" }
            "rows" -> rows.firstOrNull()?.second?.firstOrNull()?.let { "row0:${it.pkg}" }
            "grid" -> grid.firstOrNull()?.let { "grid:${it.pkg}" }
            else -> null
        }
    }
}

@Composable
fun HomeScreen(visualiser: Visualiser, appsVersion: Int, open: androidx.compose.runtime.MutableState<String?>) {
    val ctx = LocalContext.current
    val cfg by ConfigStore.config.collectAsState()
    val wallVersion by ConfigStore.wallpaperVersion.collectAsState()
    var apps by remember { mutableStateOf<List<TvApp>>(emptyList()) }
    var inputs by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    // The Mac and Couch docks always draw icons, so they need the logos even with banner tiles chosen.
    val glyphs = cfg.iconStyle != "banner" || cfg.layout == "mac" || cfg.layout == "couch"
    LaunchedEffect(appsVersion, glyphs) {
        apps = withContext(Dispatchers.Default) { loadApps(ctx, glyphs) }
        inputs = withContext(Dispatchers.Default) { tvInputs(ctx) }
    }
    // Animations off in the TV's settings (or from CleanMyTV): Home stops moving too.
    var motion by remember { mutableStateOf(true) }
    LaunchedEffect(appsVersion) {
        motion = runCatching { android.provider.Settings.Global.getFloat(ctx.contentResolver, android.provider.Settings.Global.ANIMATOR_DURATION_SCALE, 1f) }.getOrDefault(1f) > 0f
    }
    var wallpaper by remember { mutableStateOf<ImageBitmap?>(null) }
    val intro = remember { Animatable(1f) }
    var wallSeen by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(wallVersion) {
        wallpaper = withContext(Dispatchers.IO) {
            val f = ConfigStore.wallpaper(ctx)
            if (f.exists()) runCatching { BitmapFactory.decodeFile(f.path)?.asImageBitmap() }.getOrNull() else null
        }
        // A new wallpaper, not the one Home opened with, comes in like a sunrise.
        val fresh = wallSeen != null && wallSeen != wallVersion
        wallSeen = wallVersion
        if (fresh && wallpaper != null && cfg.intro && motion) { intro.snapTo(0f); intro.animateTo(1f, tween(1800, easing = FastOutSlowInEasing)) }
    }
    // Slideshow: a new wallpaper every few minutes while Home is open, from the owner's pictures or the theme's.
    LaunchedEffect(cfg.slideMinutes, cfg.slideFrom) {
        if (cfg.slideMinutes <= 0) return@LaunchedEffect
        while (true) {
            delay(cfg.slideMinutes * 60_000L)
            withContext(Dispatchers.IO) { runCatching { if (cfg.slideFrom == "theme") ThemePack.next(ctx) else Slides.next(ctx) } }
        }
    }
    val fireVersion by Fire.version.collectAsState()
    val fireWall = cfg.fireWall && remember(fireVersion) { Fire.ready(ctx) }
    // Idle screen: no button pressed for cfg.idleMinutes, and nothing open over Home. Any button wakes it,
    // and that press only wakes it, so it can't open the app that happens to have focus.
    var idle by remember { mutableStateOf(false) }
    var lastPress by remember { mutableLongStateOf(0L) }
    var swallowUp by remember { mutableStateOf(false) }
    LaunchedEffect(appsVersion) { idle = false; lastPress = System.currentTimeMillis() } // Home came back into view

    LaunchedEffect(cfg.vizBars, cfg.vizSensitivity, cfg.visualiser, idle) {
        visualiser.bars = cfg.vizBars
        visualiser.sensitivity = cfg.vizSensitivity / 100f
        if (cfg.visualiser && !idle) visualiser.start(ctx) else visualiser.stop()
    }

    val track by NowPlaying.track.collectAsState()
    var weather by remember { mutableStateOf<WeatherNow?>(null) }
    LaunchedEffect(cfg.weatherPlace, cfg.weatherLat, cfg.weatherLon, cfg.weatherUnit) {
        weather = null
        if (cfg.weatherPlace.isBlank()) return@LaunchedEffect
        // A failed refresh keeps the last reading; the next try is in 30 minutes, or 2 if there's none yet.
        while (true) {
            Weather.fetch(cfg)?.let { weather = it }
            delay(if (weather == null) 2 * 60_000L else 30 * 60_000L)
        }
    }

    val home = Home(apps, cfg)
    val accent = Color(cfg.accent)
    // Quick-settings icons this TV can actually open; Inputs only on TVs that have inputs.
    val all = remember { quickActions() }
    // App shortcuts use the app's icon; a TV app's banner is too wide for the round button.
    val quickPkgs = cfg.quick.mapNotNull { HomeConfig.quickApp(it) }
    var quickIcons by remember { mutableStateOf<Map<String, ImageBitmap>>(emptyMap()) }
    LaunchedEffect(appsVersion, quickPkgs) {
        quickIcons = withContext(Dispatchers.IO) {
            quickPkgs.mapNotNull { p ->
                runCatching { p to ctx.packageManager.getApplicationIcon(p).toBitmap(96, 96).asImageBitmap() }.getOrNull()
            }.toMap()
        }
    }
    val byPkg = apps.associateBy { it.pkg }
    val quick = cfg.quick.mapNotNull { id ->
        HomeConfig.quickApp(id)?.let { p -> byPkg[p]?.let { appAction(it, quickIcons[p]) } } ?: all[id]
    }.filter { canOpen(ctx, it, inputs.isNotEmpty()) }
    var openFolder by remember { mutableStateOf<Pair<String, List<TvApp>>?>(null) }
    var showInputs by remember { mutableStateOf(false) }
    var showPanel by remember { mutableStateOf(false) }
    var showThemes by remember { mutableStateOf(false) }
    var panelPin by remember { mutableStateOf(false) }
    val openPanel = { if (Lock.needsPin(ctx, Lock.PANEL)) panelPin = true else showPanel = true }
    LaunchedEffect(lastPress, cfg.idleMinutes, openFolder, showInputs, showPanel, showThemes) {
        if (cfg.idleMinutes == 0 || openFolder != null || showInputs || showPanel || showThemes) return@LaunchedEffect
        delay(cfg.idleMinutes * 60_000L)
        idle = true
    }
    val shown by animateFloatAsState(if (idle) 0f else 1f, tween(if (idle) 1200 else 250), label = "idle")
    LaunchedEffect(open.value) {
        when (open.value) {
            "inputs" -> showInputs = true
            "panel" -> openPanel()
            "themes" -> showThemes = true
            "fireplace" -> runCatching { ctx.startActivity(android.content.Intent(ctx, FireplaceActivity::class.java)) }
        }
        open.value = null
    }
    val onQuick: (QuickAction) -> Unit = { a ->
        if (a.id == "inputs") showInputs = true
        else if (a.id == "panel") openPanel()
        else if (a.id == "themes") showThemes = true
        else if (a.id == "fireplace") runCatching { ctx.startActivity(android.content.Intent(ctx, FireplaceActivity::class.java)) }
        else if (a.id == "music") ConfigStore.update(ctx, org.json.JSONObject().put("visualiser", !cfg.visualiser))
        else resolve(ctx, a)?.let { i -> runCatching { ctx.startActivity(i) }.onFailure { Log.w(TAG, "${a.id}: $it") } }
    }
    val first = remember { FocusRequester() }
    LaunchedEffect(home.first, cfg.layout) { if (home.first != null) runCatching { first.requestFocus() } }
    var focusColour by remember { mutableStateOf<Color?>(null) }
    val tint by animateColorAsState(focusColour ?: Color.Transparent, tween(600), label = "tint")
    val ui = Ui(cfg, accent, first, home.first, onFolder = { openFolder = it }, track = track, weather = weather,
        onFocusApp = { a -> focusColour = a.colour?.let { Color(it) } }, motion = motion)

    Box(
        Modifier.fillMaxSize().background(Color(cfg.background)).onPreviewKeyEvent { e ->
            lastPress = System.currentTimeMillis()
            if (e.type == KeyEventType.KeyDown) Motion.seen(e.key)
            when {
                idle && e.type == KeyEventType.KeyDown -> { idle = false; swallowUp = true; true }
                swallowUp && e.type == KeyEventType.KeyUp -> { swallowUp = false; true }
                else -> false
            }
        }
    ) {
        // Couch draws its own scenes in place of the picture; they stay while Home is idle.
        if (cfg.layout == "couch") CouchScene(cfg, weather, motion)
        else if (fireWall) {
            FireWallpaper(cfg, motion)
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = cfg.wallpaperDim / 100f)))
        } else wallpaper?.let {
            Image(it, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = cfg.wallpaperDim / 100f)))
        }
        if (intro.value < 1f) Sunrise(intro.value)
        // The focused app's colour washes in from the top; the wallpaper still shows through.
        if (cfg.followColour) Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(tint.copy(alpha = tint.alpha * 0.5f), Color.Transparent))))
        Box(Modifier.fillMaxSize().graphicsLayer { alpha = shown }) {
            // Rows sit over the lower half of the photo; a ground-tinted shade keeps their names readable.
            // The Mac layout has only the glass dock down there, so its shade is lighter and in the theme's colour.
            if (cfg.layout == "mac") Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0.6f to Color.Transparent, 1f to Color(cfg.background).copy(alpha = 0.45f))))
            else if (cfg.layout != "couch") Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0.35f to Color.Transparent, 1f to scrim(0.8f))))
            when (cfg.layout) {
                "split" -> SplitLayout(home, ui, quick, onQuick)
                "rail" -> RailLayout(home, ui, quick, onQuick)
                "mac" -> MacLayout(home, ui, quick, onQuick)
                "couch" -> CouchLayout(home, ui, quick, onQuick)
                else -> ShelfLayout(home, ui, quick, onQuick)
            }
            if (cfg.visualiser) Bars(visualiser, cfg, accent, Modifier.align(Alignment.BottomCenter).fillMaxWidth())
        }
        // Couch already shows a big clock while idle.
        if (shown < 1f && cfg.layout != "couch") IdleClock(ui, Modifier.align(Alignment.BottomStart).graphicsLayer { alpha = 1f - shown })
    }

    openFolder?.let { (name, list) -> FolderOverlay(name, list, ui) { openFolder = null } }
    if (showInputs) InputsOverlay(inputs, ui) { showInputs = false }
    if (showPanel) SettingsPanel(accent) { showPanel = false }
    if (showThemes) ThemeCarousel(accent) { showThemes = false }
    if (panelPin) Dialog(onDismissRequest = { panelPin = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        PinPad("Enter the PIN for Quick settings", Lock.PANEL, accent) { panelPin = false; showPanel = true }
    }
}

/**
 * The fireplace behind Home, in place of the picture. It plays while Home is on screen and holds still while
 * an app is open, or when the TV's animations are off. Silent unless the owner turned its sound on.
 */
@Composable
private fun FireWallpaper(cfg: HomeConfig, motion: Boolean) {
    val ctx = LocalContext.current
    val owner = ctx as? androidx.activity.ComponentActivity
    var resumed by remember { mutableStateOf(owner?.lifecycle?.currentState?.isAtLeast(androidx.lifecycle.Lifecycle.State.RESUMED) ?: true) }
    DisposableEffect(owner) {
        val obs = androidx.lifecycle.LifecycleEventObserver { _, e ->
            if (e == androidx.lifecycle.Lifecycle.Event.ON_RESUME) resumed = true
            if (e == androidx.lifecycle.Lifecycle.Event.ON_PAUSE) resumed = false
        }
        owner?.lifecycle?.addObserver(obs)
        onDispose { owner?.lifecycle?.removeObserver(obs) }
    }
    val volume = if (cfg.fireWallSound) cfg.fireVolume / 100f else 0f
    androidx.compose.ui.viewinterop.AndroidView(
        factory = { FireView(it) { if (ConfigStore.config.value.fireWallSound) ConfigStore.config.value.fireVolume / 100f else 0f } },
        modifier = Modifier.fillMaxSize(),
        update = { v -> v.setVolume(volume); v.play(resumed && motion) },
        onRelease = { it.release() },
    )
}

/**
 * A new wallpaper's entrance: the picture starts in a cool, dim twilight, the light arrives from the top
 * down, and a soft band of light crosses it once. [p] runs from 0 to 1.
 */
@Composable
private fun Sunrise(p: Float) {
    Box(Modifier.fillMaxSize().drawWithContent {
        val dusk = Color(0xFF0A1424)
        val edge = -0.25f + p * 1.5f
        val a = (edge - 0.3f).coerceIn(0f, 0.999f)
        val b = edge.coerceIn(a + 0.001f, 1f)
        drawRect(Brush.verticalGradient(a to dusk.copy(alpha = 0f), b to dusk.copy(alpha = 0.92f), 1f to dusk.copy(alpha = 0.92f)))
        drawRect(dusk.copy(alpha = 0.35f * (1f - p)))
        val w = size.width
        val x = -w * 0.6f + p * w * 1.8f
        val glow = kotlin.math.sin(p * Math.PI).toFloat() * 0.16f
        drawRect(Brush.linearGradient(0f to Color.Transparent, 0.5f to Color.White.copy(alpha = glow), 1f to Color.Transparent,
            start = Offset(x, 0f), end = Offset(x + w * 0.35f, size.height)))
    })
}

/** What every tile and section needs to know. */
private class Ui(
    val cfg: HomeConfig,
    val accent: Color,
    val focus: FocusRequester,
    val focusKey: String?,
    val onFolder: (Pair<String, List<TvApp>>) -> Unit,
    val track: Track? = null,
    val weather: WeatherNow? = null,
    val onFocusApp: (TvApp) -> Unit = {},
    /** False when the TV's animations are off: tiles still lift, without springs, tilt or sheen. */
    val motion: Boolean = true,
) {
    /** Banners are always wide; the icon styles take the chosen shape. */
    val shape: String = if (cfg.iconStyle == "banner") "wide" else cfg.tileShape
    fun focusFor(section: String, app: TvApp): Modifier = if ("$section:${app.pkg}" == focusKey) Modifier.focusRequester(focus) else Modifier
}

// ---- Layouts --------------------------------------------------------------------------------------

/** Shelf: a slim top bar, open wallpaper above the apps, the sections below. */
@Composable
private fun ShelfLayout(home: Home, ui: Ui, quick: List<QuickAction>, onQuick: (QuickAction) -> Unit) {
    val cfg = ui.cfg
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(scrim(0.6f), Color.Transparent), endY = 420f)))
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val open = maxHeight * (cfg.topSpace / 100f)
        Column(Modifier.fillMaxSize()) {
            // Greeting on the left; on the right the clock, the date, and the quick-settings icons under them.
            Row(Modifier.fillMaxWidth().padding(start = SAFE_X, end = SAFE_X, top = SAFE_Y), verticalAlignment = Alignment.Top) {
                Box(Modifier.padding(top = 4.dp)) { Greeting(cfg, 20.sp) }
                Spacer(Modifier.weight(1f))
                Column(horizontalAlignment = Alignment.End) {
                    Clock(cfg, 26.sp, 12.sp, Alignment.End)
                    WeatherLine(ui, 13.sp, Modifier.padding(top = 4.dp))
                    if (quick.isNotEmpty()) {
                        if (cfg.clock != "off") Spacer(Modifier.height(10.dp))
                        Row(Modifier.focusGroup(), horizontalArrangement = Arrangement.spacedBy(10.dp)) { quick.forEach { QuickButton(it, ui, onQuick) } }
                    }
                }
            }
            Sections(home, ui, Modifier.fillMaxSize(), PaddingValues(start = SAFE_X - BLEED, end = SAFE_X - BLEED, top = open, bottom = 96.dp), dockWidth = 200.dp)
        }
    }
}

/** Split: the time, greeting and quick settings hold the left third; the apps scroll on the right. */
@Composable
private fun SplitLayout(home: Home, ui: Ui, quick: List<QuickAction>, onQuick: (QuickAction) -> Unit) {
    val cfg = ui.cfg
    Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(scrim(0.7f), Color.Transparent), endX = 900f)))
    Row(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxWidth(0.32f).fillMaxHeight().padding(start = SAFE_X, top = SAFE_Y + 24.dp, bottom = SAFE_Y + 48.dp)) {
            // 46 sp keeps "12:51 PM" on one line in the left third; 12-hour time is the widest.
            Clock(cfg, 46.sp, 15.sp, Alignment.Start)
            WeatherLine(ui, 15.sp, Modifier.padding(top = 6.dp))
            Spacer(Modifier.height(14.dp))
            Greeting(cfg, 18.sp)
            Spacer(Modifier.weight(1f))
            Column(Modifier.focusGroup(), verticalArrangement = Arrangement.spacedBy(6.dp)) { quick.forEach { QuickRow(it, ui, onQuick, expanded = true) } }
        }
        Sections(home, ui, Modifier.fillMaxSize(), PaddingValues(start = 0.dp, end = SAFE_X - BLEED, top = SAFE_Y + 12.dp, bottom = 96.dp), dockWidth = 168.dp)
    }
}

/** Rail: icons down the left edge that widen with labels when focus moves onto them. */
@Composable
private fun RailLayout(home: Home, ui: Ui, quick: List<QuickAction>, onQuick: (QuickAction) -> Unit) {
    val cfg = ui.cfg
    var railFocused by remember { mutableStateOf(false) }
    // Icons start at the 48 dp safe edge; only the rail's shade runs to the screen edge.
    val railW by animateDpAsState(if (railFocused) 260.dp else SAFE_X + 56.dp, tween(220), label = "rail")
    Row(Modifier.fillMaxSize()) {
        Column(
            Modifier.width(railW).fillMaxHeight()
                .background(Brush.horizontalGradient(listOf(scrim(if (railFocused) 0.92f else 0.6f), scrim(if (railFocused) 0.75f else 0.1f))))
                .padding(start = SAFE_X - 4.dp, top = SAFE_Y + 8.dp, end = 8.dp)
                .onFocusChanged { railFocused = it.hasFocus }
                .focusGroup(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            quick.forEach { QuickRow(it, ui, onQuick, expanded = railFocused) }
            if (home.folders.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                home.folders.forEach { f ->
                    RailItem(Icons.Outlined.Folder, f.first, ui, railFocused) { ui.onFolder(f) }
                }
            }
        }
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth().padding(start = 24.dp, end = SAFE_X, top = SAFE_Y), verticalAlignment = Alignment.CenterVertically) {
                Greeting(cfg, 20.sp)
                Spacer(Modifier.weight(1f))
                WeatherLine(ui, 13.sp, Modifier.padding(end = 18.dp))
                Clock(cfg, 26.sp, 12.sp, Alignment.End)
            }
            Sections(home, ui, Modifier.fillMaxSize(), PaddingValues(start = 24.dp - BLEED, end = SAFE_X - BLEED, top = 20.dp, bottom = 96.dp), dockWidth = 196.dp, showFolders = false)
        }
    }
}

// ---- Mac layout -----------------------------------------------------------------------------------

/** The menu bar's height: half the top safe margin, then a 30 dp bar of content. */
private val MENU_H = SAFE_Y / 2 + 30.dp

/** A dock wants icons: banner tiles become the colour style, and wide tiles become squircles. */
private fun macConfig(c: HomeConfig) = c.copy(
    iconStyle = if (c.iconStyle == "banner") "colour" else c.iconStyle,
    tileShape = if (c.tileShape == "wide") "squircle" else c.tileShape,
)

/**
 * Mac: a menu bar across the top, the wallpaper open in the middle like a desktop, and a glass dock centred
 * at the bottom whose icons grow as focus moves along it. Launchpad, the dock's first icon (or Down from the
 * dock), shows every app. Folders sit after a divider at the dock's end; rows and the grid live in Launchpad.
 */
@Composable
private fun MacLayout(home: Home, ui: Ui, quick: List<QuickAction>, onQuick: (QuickAction) -> Unit) {
    var focusedName by remember { mutableStateOf<String?>(null) }
    var launchpad by remember { mutableStateOf(false) }
    val cfg = macConfig(ui.cfg)
    // Home opens on the dock's first app, or on Launchpad when the dock has no apps.
    val firstKey = home.dock.firstOrNull()?.let { "dock:${it.pkg}" }
    val mac = Ui(cfg, ui.accent, ui.focus, firstKey, ui.onFolder, ui.track, ui.weather,
        onFocusApp = { a -> focusedName = a.label; ui.onFocusApp(a) }, motion = ui.motion)
    LaunchedEffect(firstKey) { runCatching { ui.focus.requestFocus() } }
    Box(Modifier.fillMaxSize()) {
        MenuBar(mac, focusedName, quick, onQuick)
        // Now playing, while something plays, as a desktop widget under the menu bar.
        if ("nowplaying" in cfg.sections) mac.track?.let { t ->
            Box(Modifier.padding(start = SAFE_X - BLEED, top = MENU_H + 8.dp)) { NowPlayingCard(t, mac) }
        }
        MacDock(
            home, mac, launchpadTakesFocus = firstKey == null,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = SAFE_Y),
            onName = { focusedName = it }, onLaunchpad = { launchpad = true },
        )
    }
    if (launchpad) Launchpad(home, mac) { launchpad = false }
}

/** Left: a TV mark, the focused app's name and the greeting. Right: quick settings, the weather and the clock. */
@Composable
private fun MenuBar(ui: Ui, appName: String?, quick: List<QuickAction>, onQuick: (QuickAction) -> Unit) {
    val cfg = ui.cfg
    Box(Modifier.fillMaxWidth().height(MENU_H).background(Color.Black.copy(alpha = 0.32f))) {
        Box(Modifier.fillMaxWidth().height(1.dp).align(Alignment.BottomCenter).background(Color.White.copy(alpha = 0.10f)))
        Row(Modifier.fillMaxSize().padding(start = SAFE_X, end = SAFE_X - 6.dp, top = SAFE_Y / 2), verticalAlignment = Alignment.CenterVertically) {
            Image(Icons.Outlined.Tv, null, Modifier.size(17.dp), colorFilter = ColorFilter.tint(TEXT))
            Spacer(Modifier.width(16.dp))
            Text(appName ?: "Home", color = TEXT, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 260.dp))
            if (cfg.greeting.isNotBlank()) {
                val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                val part = when (hour) { in 5..11 -> "Good morning"; in 12..17 -> "Good afternoon"; else -> "Good evening" }
                Spacer(Modifier.width(18.dp))
                Text("$part, ${cfg.greeting}", color = MUTED, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.widthIn(max = 280.dp))
            }
            Spacer(Modifier.weight(1f))
            if (quick.isNotEmpty()) {
                Row(Modifier.focusGroup(), horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
                    quick.forEach { MenuButton(it, ui, onQuick) }
                }
            }
            ui.weather?.let { w ->
                Row(Modifier.padding(start = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Image(weatherIcon(w), null, Modifier.size(16.dp), colorFilter = ColorFilter.tint(TEXT))
                    Spacer(Modifier.width(5.dp))
                    Text("${w.temp}°", color = TEXT, fontSize = 15.sp, maxLines = 1)
                }
            }
            MenuClock(cfg)
        }
    }
}

/** A quick-settings icon in the menu bar: no circle until focus lands, then an accent pill. */
@Composable
private fun MenuButton(a: QuickAction, ui: Ui, onQuick: (QuickAction) -> Unit) {
    var focused by remember { mutableStateOf(false) }
    Surface(
        onClick = { onQuick(a) },
        modifier = Modifier.size(width = 34.dp, height = 26.dp).onFocusChanged { focused = it.isFocused },
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(7.dp)),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.06f),
        glow = ClickableSurfaceDefaults.glow(focusedGlow = Glow.None),
        border = ClickableSurfaceDefaults.border(focusedBorder = Border.None),
        colors = ClickableSurfaceDefaults.colors(containerColor = Color.Transparent, focusedContainerColor = ui.accent.copy(alpha = 0.9f)),
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { QuickGlyph(a, ui, focused, 16.dp) }
    }
}

/** "Sat 7 Nov  2:52 PM" on one line, as a menu bar shows it; the time follows the TV's 12/24-hour setting. */
@Composable
private fun MenuClock(cfg: HomeConfig) {
    if (cfg.clock == "off") return
    var now by remember { mutableStateOf(Date()) }
    LaunchedEffect(Unit) { while (true) { now = Date(); delay(1000L - System.currentTimeMillis() % 1000) } }
    val day = remember {
        val locale = java.util.Locale.getDefault()
        java.text.SimpleDateFormat(android.text.format.DateFormat.getBestDateTimePattern(locale, "EEEdMMM"), locale)
    }
    val time = DateFormat.getTimeInstance(DateFormat.SHORT).format(now)
    Text(if (cfg.clock == "time_date") "${day.format(now)}   $time" else time, color = TEXT, fontSize = 15.sp, fontWeight = FontWeight.Medium,
        maxLines = 1, softWrap = false, modifier = Modifier.padding(start = 14.dp))
}

/**
 * The dock: Launchpad, the dock's apps, then the folders after a divider, on a band of light glass. The
 * focused icon grows to 1.5x and its neighbours less, and the dock widens to make room, as on a Mac.
 */
@Composable
private fun MacDock(home: Home, ui: Ui, launchpadTakesFocus: Boolean, modifier: Modifier, onName: (String) -> Unit, onLaunchpad: () -> Unit) {
    val ctx = LocalContext.current
    val base = 56.dp * (ui.cfg.dockScale / 100f)
    // Index of the focused icon along the dock (0 is Launchpad), or -1 while focus is elsewhere.
    var focusedAt by remember { mutableIntStateOf(-1) }
    val band = RoundedCornerShape(22.dp)
    Row(
        modifier
            .background(Color.White.copy(alpha = 0.16f), band)
            .border(1.dp, Color.White.copy(alpha = 0.22f), band)
            .padding(horizontal = 8.dp, vertical = 7.dp)
            .onFocusChanged { if (!it.hasFocus) focusedAt = -1 }
            // Down from the dock opens Launchpad, like the swipe that opens it on a Mac.
            .onPreviewKeyEvent { e -> if (e.type == KeyEventType.KeyDown && e.key == Key.DirectionDown) { onLaunchpad(); true } else false }
            .focusGroup(),
        verticalAlignment = Alignment.Bottom,
    ) {
        DockItem(0, focusedAt, base, ui, "Launchpad",
            if (launchpadTakesFocus) Modifier.focusRequester(ui.focus) else Modifier,
            onFocus = { focusedAt = 0; onName("Launchpad") }, onClick = onLaunchpad) { LaunchpadIcon() }
        home.dock.forEachIndexed { i, app ->
            DockItem(i + 1, focusedAt, base, ui, app.label, ui.focusFor("dock", app),
                onFocus = { focusedAt = i + 1; ui.onFocusApp(app) }, onClick = { launch(ctx, app) },
                locked = Lock.isLocked(ui.cfg, app.pkg)) { IconTile(app, ui) }
        }
        if (home.folders.isNotEmpty()) {
            Box(Modifier.padding(horizontal = 6.dp).width(1.dp).height(base).background(Color.White.copy(alpha = 0.3f)))
            home.folders.forEachIndexed { j, f ->
                val at = home.dock.size + 1 + j
                DockItem(at, focusedAt, base, ui, f.first, Modifier,
                    onFocus = { focusedAt = at; onName(f.first) }, onClick = { ui.onFolder(f) }) { FolderIcon(f) }
            }
        }
    }
}

/** One dock icon: magnified by its distance from the focused one, its name above it while focused. */
@Composable
private fun DockItem(
    index: Int, focusedAt: Int, base: Dp, ui: Ui, label: String, modifier: Modifier,
    onFocus: () -> Unit, onClick: () -> Unit, locked: Boolean = false, icon: @Composable () -> Unit,
) {
    val distance = if (focusedAt < 0) Int.MAX_VALUE else kotlin.math.abs(index - focusedAt)
    val target = when (distance) { 0 -> 1.5f; 1 -> 1.24f; 2 -> 1.08f; else -> 1f }
    val scale by animateFloatAsState(target, if (ui.motion) spring(dampingRatio = 0.75f, stiffness = 520f) else androidx.compose.animation.core.snap(), label = "dock")
    val focused = distance == 0
    val shape = tileShape(ui.shape, big = true)
    // The padding grows with the icon, so a magnified icon pushes its neighbours aside instead of covering them.
    Box(Modifier.zIndex(if (focused) 1f else 0f).padding(horizontal = 5.dp + base * ((scale - 1f) / 2f)), contentAlignment = Alignment.BottomCenter) {
        Box(Modifier.size(base).graphicsLayer { scaleX = scale; scaleY = scale; transformOrigin = TransformOrigin(0.5f, 1f) }) {
            Surface(
                onClick = onClick,
                modifier = modifier.fillMaxSize().onFocusChanged { if (it.isFocused) onFocus() },
                shape = ClickableSurfaceDefaults.shape(shape),
                scale = ClickableSurfaceDefaults.scale(focusedScale = 1f),
                glow = ClickableSurfaceDefaults.glow(focusedGlow = Glow.None),
                border = ClickableSurfaceDefaults.border(focusedBorder = Border(BorderStroke(1.dp, Color.White.copy(alpha = 0.45f)), shape = shape)),
                colors = ClickableSurfaceDefaults.colors(containerColor = Color.Transparent, focusedContainerColor = Color.Transparent),
            ) {
                icon()
                if (locked) LockBadge(Modifier.align(if (ui.shape == "round") Alignment.BottomCenter else Alignment.TopEnd))
            }
        }
        if (focused) {
            // The name in a dark bubble over the magnified icon, and a small dot under it, as a Mac marks an open app.
            Text(label, color = TEXT, fontSize = 14.sp, maxLines = 1, softWrap = false,
                modifier = Modifier.hovering { base.toPx() * scale + 10.dp.toPx() }
                    .background(Color(0xE61E1E24), RoundedCornerShape(7.dp)).padding(horizontal = 10.dp, vertical = 4.dp))
            Box(Modifier.hovering { -(4.dp.toPx() + 3.dp.toPx()) }.size(4.dp).background(TEXT.copy(alpha = 0.85f), CircleShape))
        }
    }
}

/**
 * Draws this element centred over the bottom middle of its parent, [up] px higher, without taking any room,
 * so a long name can't widen the dock.
 */
private fun Modifier.hovering(up: Density.() -> Float): Modifier = layout { measurable, _ ->
    val p = measurable.measure(Constraints())
    val lift = up()
    layout(0, 0) { p.placeRelative(-p.width / 2, (-p.height - lift).toInt()) }
}

/** Launchpad's icon: a grid of apps on a slate tile. */
@Composable
private fun LaunchpadIcon() {
    Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Color(0xFF9AA6BC), Color(0xFF4B5567)))), contentAlignment = Alignment.Center) {
        Image(Icons.Outlined.Apps, "Launchpad", Modifier.fillMaxSize(0.56f), colorFilter = ColorFilter.tint(Color.White))
    }
}

/** A folder in the dock: a 2x2 peek at its apps on frosted glass, like a stack. */
@Composable
private fun FolderIcon(folder: Pair<String, List<TvApp>>) {
    Column(Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.22f)).padding(7.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        folder.second.take(4).chunked(2).forEach { pair ->
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                pair.forEach { app ->
                    Image(app.art, null, Modifier.weight(1f).fillMaxHeight().background(app.tint?.let { Color(it) } ?: Color.Black.copy(alpha = 0.25f), RoundedCornerShape(4.dp)),
                        contentScale = if (app.banner) ContentScale.Crop else ContentScale.Fit)
                }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

/** Every app on a full-screen sheet, seven to a row with names (Launchpad, or Couch's All apps). Back closes it, and so does opening an app. */
@Composable
private fun Launchpad(home: Home, ui: Ui, title: String = "Launchpad", onClose: () -> Unit) {
    val all = (home.dock + home.grid + home.folders.flatMap { it.second }).distinctBy { it.pkg }
    val focus = remember { FocusRequester() }
    val inner = Ui(ui.cfg.copy(showNames = true, nameFocus = false), ui.accent, focus, all.firstOrNull()?.let { "folder:${it.pkg}" }, ui.onFolder, motion = ui.motion)
    val cols = 7
    val cell = 96.dp
    val gap = 28.dp
    Dialog(onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(Modifier.fillMaxSize().background(Color(ui.cfg.background).copy(alpha = 0.93f))) {
            if (all.isEmpty()) Text("No apps to show", color = MUTED, fontSize = 18.sp, modifier = Modifier.align(Alignment.Center))
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = SAFE_X, end = SAFE_X, top = SAFE_Y + 16.dp, bottom = SAFE_Y + 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                item("title") { Text(title, color = MUTED, fontSize = 15.sp, modifier = Modifier.padding(bottom = 10.dp)) }
                all.chunked(cols).forEachIndexed { i, row ->
                    item("lp$i") {
                        // Every row as wide as a full one, so a short last row lines up on the left.
                        Row(Modifier.width(cell * cols + gap * (cols - 1)).padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(gap)) {
                            for (app in row) Tile("folder", app, inner, Modifier.width(cell), big = false, onOpen = onClose)
                        }
                    }
                }
            }
        }
        LaunchedEffect(Unit) { runCatching { focus.requestFocus() } }
    }
}

// ---- Couch layout ---------------------------------------------------------------------------------

/** couch-tv's clock face: tall, narrow and light. */
private val ANTONIO = FontFamily(Font(R.font.antonio_light, FontWeight.Light))

/** How long each scene shows before the next fades in. */
private const val SCENE_MS = 30_000L

/** CSS-style brightness, saturation and contrast as one colour filter, or null when it changes nothing. */
private fun tone(brightness: Float = 1f, saturation: Float = 1f, contrast: Float = 1f): ColorFilter? {
    if (brightness == 1f && saturation == 1f && contrast == 1f) return null
    val m = ColorMatrix().apply { setToSaturation(saturation) }
    val k = brightness * contrast
    val shift = (1f - contrast) * 0.5f * 255f
    m.timesAssign(ColorMatrix(floatArrayOf(k, 0f, 0f, 0f, shift, 0f, k, 0f, 0f, shift, 0f, 0f, k, 0f, shift, 0f, 0f, 0f, 1f, 0f)))
    return ColorFilter.colorMatrix(m)
}

/**
 * Couch, behind Home: couch-tv's Night look. Five scenes take turns, each a photo with a huge clock in
 * frosted glass and the scenery cut out in front of it, so the peaks overlap the digits; the date sits
 * above. It stays while Home goes idle, like a lock screen, and only turns while Home is on screen.
 */
@Composable
private fun CouchScene(cfg: HomeConfig, weather: WeatherNow?, motion: Boolean) {
    val ctx = LocalContext.current
    val owner = ctx as? androidx.activity.ComponentActivity
    var resumed by remember { mutableStateOf(owner?.lifecycle?.currentState?.isAtLeast(androidx.lifecycle.Lifecycle.State.RESUMED) ?: true) }
    DisposableEffect(owner) {
        val obs = androidx.lifecycle.LifecycleEventObserver { _, e ->
            if (e == androidx.lifecycle.Lifecycle.Event.ON_RESUME) resumed = true
            if (e == androidx.lifecycle.Lifecycle.Event.ON_PAUSE) resumed = false
        }
        owner?.lifecycle?.addObserver(obs)
        onDispose { owner?.lifecycle?.removeObserver(obs) }
    }
    var index by remember { mutableIntStateOf(0) }
    var art by remember { mutableStateOf<SceneArt?>(null) }
    // The scene fading out, drawn under the new one until it has faded in; then it's let go.
    var leaving by remember { mutableStateOf<Pair<SceneSpec, SceneArt>?>(null) }
    val fade = remember { Animatable(1f) }
    LaunchedEffect(Unit) { art = withContext(Dispatchers.IO) { Scenes.load(ctx, Scenes.all[0]) } }
    LaunchedEffect(resumed, motion) {
        if (!resumed) return@LaunchedEffect
        // Coming back mid-fade: finish it at once rather than leave two scenes half drawn.
        leaving = null
        fade.snapTo(1f)
        while (true) {
            delay(SCENE_MS)
            val next = (index + 1) % Scenes.all.size
            val loaded = withContext(Dispatchers.IO) { Scenes.load(ctx, Scenes.all[next]) } ?: continue
            leaving = art?.let { Scenes.all[index] to it }
            index = next
            art = loaded
            if (motion) { fade.snapTo(0f); fade.animateTo(1f, tween(1600, easing = FastOutSlowInEasing)) }
            leaving = null
        }
    }
    // The clock shows minutes, so the scenes redraw once a minute, on the minute.
    var now by remember { mutableStateOf(Date()) }
    LaunchedEffect(Unit) { while (true) { now = Date(); delay(60_000L - System.currentTimeMillis() % 60_000L) } }
    val is24 = android.text.format.DateFormat.is24HourFormat(ctx)
    val timeFormat = remember(is24) { java.text.SimpleDateFormat(if (is24) "H:mm" else "h:mm", java.util.Locale.getDefault()) }
    val dateFormat = remember {
        val locale = java.util.Locale.getDefault()
        java.text.SimpleDateFormat(android.text.format.DateFormat.getBestDateTimePattern(locale, "EEEEdMMMM"), locale)
    }
    val time = timeFormat.format(now)
    val date = dateFormat.format(now)
    val hour = Calendar.getInstance().also { it.time = now }.get(Calendar.HOUR_OF_DAY)
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val rem = maxWidth / 120f
        leaving?.let { (s, a) -> SceneLayer(s, a, rem, cfg, time, date, weather) }
        art?.let { a ->
            Box(Modifier.fillMaxSize().graphicsLayer { alpha = fade.value }) { SceneLayer(Scenes.all[index], a, rem, cfg, time, date, weather) }
        }
        // After 9 pm everything dims a little.
        if (hour >= 21 || hour < 5) Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.28f)))
        // The lower part darkens so the dock and the cards over it read.
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0.6f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.5f))))
    }
}

/** One scene: the photo, any shade, the glass clock, the cut-out in front of it, and the date above. */
@Composable
private fun SceneLayer(s: SceneSpec, art: SceneArt, rem: Dp, cfg: HomeConfig, time: String, date: String, weather: WeatherNow?) {
    val photo = remember(s) { tone(brightness = s.photoBright) }
    Box(Modifier.fillMaxSize()) {
        Image(art.photo, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop, colorFilter = photo)
        if (cfg.clock != "off") GlassClock(s, art, rem, time)
        art.cutOut?.let { Image(it, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop, colorFilter = photo) }
        s.shade?.let { stops ->
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(*stops.map { (at, c) -> at to Color(c) }.toTypedArray())))
        }
        if (cfg.clock == "time_date") SceneDate(s, rem, date, weather)
    }
}

/**
 * The time in glass: the digits are a mask filled with the scene's blurred copy, laid exactly over the photo
 * so the glass shows what's behind it, then a faint light edge on top.
 */
@Composable
private fun GlassClock(s: SceneSpec, art: SceneArt, rem: Dp, time: String) {
    val density = LocalDensity.current
    val fontSize = rem * s.size
    val lineHeight = fontSize * 0.82f
    val style = TextStyle(
        fontFamily = ANTONIO,
        fontWeight = FontWeight.Light,
        fontSize = with(density) { fontSize.toSp() },
        lineHeight = with(density) { lineHeight.toSp() },
        letterSpacing = (-0.02f).em,
        fontFeatureSettings = "tnum",
        textAlign = if (s.left != null) TextAlign.Start else TextAlign.Center,
        lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None),
    )
    val place = Modifier.fillMaxWidth().padding(start = s.left?.let { rem * it } ?: 0.dp, top = rem * s.top)
    val fill = remember(s) { tone(s.fillBright, s.fillSat, s.fillContrast) }
    Box(
        Modifier
            .fillMaxSize()
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen; alpha = s.fillAlpha }
            .drawWithContent {
                drawContent()
                drawImage(art.glass, dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt()), colorFilter = fill, blendMode = BlendMode.SrcIn)
                if (s.sheen) {
                    val top = (rem * s.top).toPx()
                    drawRect(
                        Brush.verticalGradient(
                            0f to Color.White.copy(alpha = 0.55f), 0.38f to Color.Transparent, 0.7f to Color.Transparent, 1f to Color.White.copy(alpha = 0.22f),
                            startY = top, endY = top + lineHeight.toPx(),
                        ),
                        blendMode = BlendMode.SrcAtop,
                    )
                }
            }
    ) {
        BasicText(time, place, style = style.copy(color = Color.White), maxLines = 1, softWrap = false)
    }
    BasicText(time, place, style = style.copy(color = Color(s.rimFill)), maxLines = 1, softWrap = false)
    BasicText(time, place, style = style.copy(color = Color(s.rimStroke), drawStyle = Stroke(width = with(density) { (rem * 0.09f).toPx() })), maxLines = 1, softWrap = false)
}

/** "Saturday 7 November", and the weather when a place is set, just above the scene's clock. */
@Composable
private fun SceneDate(s: SceneSpec, rem: Dp, date: String, weather: WeatherNow?) {
    val colour = Color(s.dateColour)
    val soft = colour.copy(alpha = colour.alpha * 0.85f)
    val style = TextStyle(shadow = if (s.dark) null else Shadow(Color.Black.copy(alpha = 0.25f), Offset(0f, 2f), 18f))
    val size = with(LocalDensity.current) { (rem * 2.125f).toSp() }
    Row(
        Modifier.fillMaxWidth().padding(start = s.dateLeft?.let { rem * it } ?: 0.dp, top = rem * s.dateTop),
        horizontalArrangement = if (s.dateLeft != null) Arrangement.Start else Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(date, color = colour, fontSize = size, fontWeight = FontWeight.Medium, maxLines = 1, softWrap = false, style = style)
        weather?.let { w ->
            Image(weatherIcon(w), null, Modifier.padding(start = rem * 1f, end = rem * 0.4f).size(rem * 1.875f), colorFilter = ColorFilter.tint(soft))
            Text("${w.temp}°", color = soft, fontSize = size, maxLines = 1, softWrap = false, style = style)
        }
    }
}

/** couch-tv's glass: a smoky tint with a light edge. */
private val COUCH_GLASS = Color(0x521E141C)
private val COUCH_EDGE = Color(0x29FFD2DC)

/**
 * Couch: the scenes fill the screen (see CouchScene); over them, the greeting top left, quick settings top
 * right, Now playing where couch-tv keeps "Keep watching", and a glass dock along the bottom ending in All
 * apps. Like the Mac dock it always draws icons. Everything but the scene fades when Home goes idle.
 */
@Composable
private fun CouchLayout(home: Home, ui: Ui, quick: List<QuickAction>, onQuick: (QuickAction) -> Unit) {
    var allApps by remember { mutableStateOf(false) }
    val cfg = macConfig(ui.cfg)
    // Home opens on the dock's first app, or on All apps when the dock has no apps.
    val firstKey = home.dock.firstOrNull()?.let { "dock:${it.pkg}" }
    val couch = Ui(cfg, ui.accent, ui.focus, firstKey, ui.onFolder, ui.track, ui.weather, ui.onFocusApp, ui.motion)
    LaunchedEffect(firstKey) { runCatching { ui.focus.requestFocus() } }
    Box(Modifier.fillMaxSize()) {
        if (cfg.greeting.isNotBlank()) {
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val part = when (hour) { in 5..11 -> "Good morning"; in 12..17 -> "Good afternoon"; else -> "Good evening" }
            Text("$part, ${cfg.greeting}", color = TEXT, fontSize = 16.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = SAFE_X, top = SAFE_Y).widthIn(max = 320.dp)
                    .background(COUCH_GLASS, RoundedCornerShape(50)).border(1.dp, COUCH_EDGE, RoundedCornerShape(50))
                    .padding(horizontal = 18.dp, vertical = 8.dp))
        }
        if (quick.isNotEmpty()) {
            Row(Modifier.align(Alignment.TopEnd).padding(end = SAFE_X, top = SAFE_Y).focusGroup(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                quick.forEach { QuickButton(it, couch, onQuick) }
            }
        }
        Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(bottom = SAFE_Y), horizontalAlignment = Alignment.CenterHorizontally) {
            if ("nowplaying" in cfg.sections) couch.track?.let { t ->
                Column(Modifier.fillMaxWidth().padding(start = SAFE_X - BLEED, bottom = 10.dp)) {
                    Text("Now playing", color = TEXT.copy(alpha = 0.78f), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = BLEED))
                    NowPlayingCard(t, couch)
                }
            }
            CouchDock(home, couch, launchpadTakesFocus = firstKey == null) { allApps = true }
        }
    }
    if (allApps) Launchpad(home, couch, title = "All apps") { allApps = false }
}

/** The glass dock: the dock's apps, its folders, then All apps. It scrolls if there are more than fit. */
@Composable
private fun CouchDock(home: Home, ui: Ui, launchpadTakesFocus: Boolean, onAll: () -> Unit) {
    val w = 60.dp * (ui.cfg.dockScale / 100f)
    val band = RoundedCornerShape(26.dp)
    LazyRow(
        Modifier
            .widthIn(max = 960.dp - SAFE_X * 2)
            .background(COUCH_GLASS, band)
            .border(1.dp, COUCH_EDGE, band)
            .focusGroup(),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(13.dp),
        verticalAlignment = Alignment.Top,
    ) {
        items(home.dock, key = { "d:" + it.pkg }) { app -> Tile("dock", app, ui, Modifier.width(w), big = false) }
        items(home.folders, key = { "f:" + it.first }) { f -> FolderTile(f, ui, Modifier.width(w)) }
        item("all") { AllAppsTile(ui, w, if (launchpadTakesFocus) Modifier.focusRequester(ui.focus) else Modifier, onAll) }
    }
}

/** The dock's last tile: three dots on glass that open every app. */
@Composable
private fun AllAppsTile(ui: Ui, w: Dp, modifier: Modifier, onClick: () -> Unit) {
    val shape = tileShape(ui.shape, big = false)
    val (scale, glow, border) = focusStyle(ui, shape)
    Surface(
        onClick = onClick,
        modifier = modifier.size(w),
        shape = ClickableSurfaceDefaults.shape(shape), scale = scale, glow = glow, border = border,
        colors = ClickableSurfaceDefaults.colors(containerColor = Color.White.copy(alpha = 0.12f), focusedContainerColor = Color.White.copy(alpha = 0.24f)),
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Image(Icons.Outlined.MoreHoriz, "All apps", Modifier.fillMaxSize(0.46f), colorFilter = ColorFilter.tint(TEXT))
        }
    }
}

// ---- Sections -------------------------------------------------------------------------------------

@Composable
private fun Sections(home: Home, ui: Ui, modifier: Modifier, padding: PaddingValues, dockWidth: Dp, showFolders: Boolean = true) {
    val cfg = ui.cfg
    BoxWithConstraints(modifier) {
        val inner = maxWidth - padding.calcHorizontal() - BLEED * 2
        val gridW: Dp = (inner - GAP * (cfg.columns - 1)) / cfg.columns
        // The default dock fits the screen; a bigger dock size scrolls sideways instead of shrinking past this.
        val n = home.dock.size.coerceIn(1, 5)
        val dockW: Dp = minOf(dockWidth * (cfg.dockScale / 100f), (inner - GAP * (n - 1)) / n)
        LazyColumn(Modifier.fillMaxSize().fadeTop(), contentPadding = padding) {
            for (s in cfg.sections) when (s) {
                "nowplaying" -> ui.track?.let { t -> item("nowplaying") { NowPlayingCard(t, ui) } }
                "dock" -> if (home.dock.isNotEmpty()) item("dock") {
                    TileRow("dock", home.dock, ui, dockW, big = true)
                }
                "rows" -> home.rows.forEachIndexed { i, (name, list) ->
                    item("row$i-title") { SectionTitle(name) }
                    item("row$i") { TileRow("row$i", list, ui, 148.dp * (cfg.tileScale / 100f), big = false) }
                }
                "folders" -> if (showFolders && home.folders.isNotEmpty()) {
                    item("folders-title") { SectionTitle("Folders") }
                    item("folders") {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(GAP), contentPadding = PaddingValues(horizontal = BLEED, vertical = 16.dp)) {
                            items(home.folders, key = { it.first }) { f -> FolderTile(f, ui, Modifier.width(148.dp * (cfg.tileScale / 100f))) }
                        }
                    }
                }
                "grid" -> if (home.grid.isNotEmpty()) grid(home, ui, gridW)
            }
        }
    }
}

/** Rows scrolling up fade out over the top 28 dp instead of being sliced by a hard edge. */
private fun Modifier.fadeTop(): Modifier = graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen).drawWithContent {
    drawContent()
    drawRect(Brush.verticalGradient(0f to Color.Transparent, 1f to Color.Black, endY = 28.dp.toPx()), blendMode = BlendMode.DstIn)
}

private fun PaddingValues.calcHorizontal(): Dp =
    calculateLeftPadding(androidx.compose.ui.unit.LayoutDirection.Ltr) + calculateRightPadding(androidx.compose.ui.unit.LayoutDirection.Ltr)

private fun LazyListScope.grid(home: Home, ui: Ui, w: Dp) {
    val label = if (home.dock.isEmpty() && home.rows.isEmpty()) "Apps" else "All apps"
    item("grid-title") { SectionTitle(label) }
    home.grid.chunked(ui.cfg.columns).forEachIndexed { i, row ->
        item("grid$i") {
            Row(Modifier.padding(horizontal = BLEED, vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(GAP)) {
                for (app in row) Tile("grid", app, ui, Modifier.width(w), big = false)
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, color = TEXT, fontSize = 18.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(start = BLEED, top = 12.dp))
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun TileRow(section: String, list: List<TvApp>, ui: Ui, w: Dp, big: Boolean) {
    val row = @Composable {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(GAP), contentPadding = PaddingValues(horizontal = BLEED, vertical = 16.dp)) {
            val tw = if (ui.shape == "wide") w else w * 0.72f
            items(list, key = { it.pkg }) { app -> Tile(section, app, ui, Modifier.width(tw), big) }
        }
    }
    if (ui.cfg.pinFocus) CompositionLocalProvider(LocalBringIntoViewSpec provides PINNED) { row() } else row()
}

// ---- Tiles ----------------------------------------------------------------------------------------

/** [lifted]: the caller zooms the tile itself with [lift], so the surface doesn't. */
@Composable
private fun focusStyle(ui: Ui, shape: Shape, zoom: Float = 1.08f, lifted: Boolean = false) = Triple(
    ClickableSurfaceDefaults.scale(focusedScale = if (ui.cfg.focus == "none" || (lifted && ui.cfg.focus == "lift")) 1f else zoom),
    ClickableSurfaceDefaults.glow(
        focusedGlow = when (ui.cfg.focus) {
            "glow" -> Glow(ui.accent, 16.dp)
            "lift" -> Glow(ui.accent.copy(alpha = 0.35f), 16.dp) // a faint halo; the shadow does the lifting
            else -> Glow.None
        }
    ),
    ClickableSurfaceDefaults.border(
        focusedBorder = when (ui.cfg.focus) {
            "outline" -> Border(BorderStroke(3.dp, ui.accent), shape = shape)
            "glow" -> Border(BorderStroke(2.dp, TEXT), shape = shape) // a light ring that reads on any banner
            "lift" -> Border(BorderStroke(1.dp, Color.White.copy(alpha = 0.28f)), shape = shape)
            else -> Border.None
        }
    ),
)

/** The last D-pad move, for the tilt: -1 or 1 on one axis. */
private object Motion {
    var dx = 0
    var dy = 0
    fun seen(k: Key) {
        when (k) {
            Key.DirectionLeft -> { dx = -1; dy = 0 }
            Key.DirectionRight -> { dx = 1; dy = 0 }
            Key.DirectionUp -> { dx = 0; dy = -1 }
            Key.DirectionDown -> { dx = 0; dy = 1 }
            else -> {}
        }
    }
}

/** Lift: the tile springs up on a soft shadow and, with tilt on, leans toward the move and settles. */
@Composable
private fun Modifier.lift(ui: Ui, shape: Shape, focused: Boolean): Modifier {
    if (ui.cfg.focus != "lift") return this
    val scale by animateFloatAsState(if (focused) 1.09f else 1f, if (ui.motion) spring(dampingRatio = 0.62f, stiffness = 420f) else androidx.compose.animation.core.snap(), label = "lift")
    val lean = remember { Animatable(0f) }
    val leanX = remember { mutableStateOf(true) }
    LaunchedEffect(focused) {
        if (focused && ui.cfg.tilt && ui.motion && (Motion.dx != 0 || Motion.dy != 0)) {
            leanX.value = Motion.dx != 0
            lean.snapTo(if (Motion.dx != 0) Motion.dx * 4f else -Motion.dy * 4f)
            lean.animateTo(0f, tween(220))
        } else lean.snapTo(0f)
    }
    return zIndex(if (focused) 1f else 0f)
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
            translationY = -(scale - 1f) * 40f
            if (leanX.value) rotationY = lean.value else rotationX = lean.value
            cameraDistance = 12f * density
        }
        .shadow(if (focused) 12.dp else 0.dp, shape, clip = false, ambientColor = Color.Black, spotColor = Color.Black)
}

/** Over a lifted tile: one sweep of light when focus lands, and a slight dim on the tiles around it. */
@Composable
private fun LiftLight(ui: Ui, focused: Boolean) {
    if (ui.cfg.focus != "lift") return
    val p = remember { Animatable(0f) }
    LaunchedEffect(focused) {
        p.snapTo(0f)
        if (focused && ui.motion) { delay(80); p.animateTo(1f, tween(650, easing = FastOutSlowInEasing)) }
    }
    if (!focused) Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.12f)))
    else if (p.value in 0.001f..0.999f) Box(
        Modifier.fillMaxSize().drawWithContent {
            val w = size.width
            val x = -w * 0.8f + p.value * w * 1.6f
            drawRect(
                Brush.linearGradient(
                    0f to Color.Transparent, 0.45f to Color.White.copy(alpha = 0.22f), 0.55f to Color.White.copy(alpha = 0.05f), 1f to Color.Transparent,
                    start = Offset(x, 0f), end = Offset(x + w * 0.6f, size.height * 0.4f)
                )
            )
        }
    )
}

/** Keeps the focused tile one step in from the left; the row slides under it. */
@OptIn(ExperimentalFoundationApi::class)
private val PINNED = object : BringIntoViewSpec {
    override fun calculateScrollDistance(offset: Float, size: Float, containerSize: Float): Float = offset - containerSize * 0.12f
}

@Composable
private fun Tile(section: String, app: TvApp, ui: Ui, size: Modifier, big: Boolean, onOpen: () -> Unit = {}) {
    val ctx = LocalContext.current
    val shape = tileShape(ui.shape, big)
    val (scale, glow, border) = focusStyle(ui, shape, lifted = true)
    var focused by remember { mutableStateOf(false) }
    // With names on focus, the name keeps its room so the rows don't jump.
    val nameAlpha by animateFloatAsState(if (!ui.cfg.nameFocus || focused) 1f else 0f, tween(if (ui.motion) 200 else 0), label = "name")
    val wide = ui.shape == "wide"
    // In the grid and folders each slot keeps its width; a square tile sits in the middle of it.
    val inSlot = section == "grid" || section == "folder"
    val banner = ui.cfg.iconStyle == "banner"
    Column(size, horizontalAlignment = if (wide) Alignment.Start else Alignment.CenterHorizontally) {
        Surface(
            onClick = { launch(ctx, app); onOpen() },
            modifier = ui.focusFor(section, app).fillMaxWidth(if (!wide && inSlot) 0.72f else 1f).aspectRatio(if (wide) 16f / 9f else 1f)
                .onFocusChanged { focused = it.isFocused; if (it.isFocused) ui.onFocusApp(app) }
                .lift(ui, shape, focused),
            shape = ClickableSurfaceDefaults.shape(shape), scale = scale, glow = glow, border = border,
            colors = ClickableSurfaceDefaults.colors(containerColor = if (banner) TILE else Color.Transparent, focusedContainerColor = if (banner) TILE_FOCUSED else Color.Transparent)
        ) {
            when {
                !banner -> IconTile(app, ui)
                app.banner -> Image(app.art, app.label, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                else -> GeneratedTile(app, big, named = !ui.cfg.showNames)
            }
            LiftLight(ui, focused)
            if (Lock.isLocked(ui.cfg, app.pkg)) LockBadge(Modifier.align(if (ui.shape == "round") Alignment.BottomCenter else Alignment.TopEnd))
        }
        if (ui.cfg.showNames) {
            Text(app.label, color = TEXT, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = if (wide) TextAlign.Start else TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 7.dp, start = 2.dp).graphicsLayer { alpha = nameAlpha })
        }
    }
}

/** Opens an app, through the PIN pad when it's locked. */
private fun launch(ctx: android.content.Context, app: TvApp) {
    val i = if (Lock.needsPin(ctx, app.pkg)) LockActivity.intent(ctx, app.pkg, app.launch) else app.launch
    runCatching { ctx.startActivity(i) }.onFailure { Log.w(TAG, "launch ${app.pkg}: $it") }
}

/** Rounded rectangle, rounded square, squircle (a superellipse, like Samsung's and Belle UI's) or circle. */
private fun tileShape(kind: String, big: Boolean): Shape = when (kind) {
    "square" -> RoundedCornerShape(22)
    "squircle" -> SQUIRCLE
    "round" -> CircleShape
    else -> RoundedCornerShape(if (big) 12.dp else 10.dp)
}

private val SQUIRCLE = GenericShape { size, _ ->
    val cx = size.width / 2; val cy = size.height / 2
    for (i in 0..96) {
        val t = i / 96.0 * Math.PI * 2
        val c = kotlin.math.cos(t); val s = kotlin.math.sin(t)
        val x = cx + cx * Math.signum(c) * kotlin.math.sqrt(kotlin.math.abs(c))
        val y = cy + cy * Math.signum(s) * kotlin.math.sqrt(kotlin.math.abs(s))
        if (i == 0) moveTo(x.toFloat(), y.toFloat()) else lineTo(x.toFloat(), y.toFloat())
    }
    close()
}

/** The small lock in a locked app's corner. */
@Composable
private fun LockBadge(modifier: Modifier) {
    Box(modifier.padding(6.dp).size(20.dp).background(Color.Black.copy(alpha = 0.6f), CircleShape), contentAlignment = Alignment.Center) {
        Image(Icons.Outlined.Lock, "Locked", Modifier.size(12.dp), colorFilter = ColorFilter.tint(TEXT))
    }
}

/**
 * An app in one of the icon styles: its logo on a tile, every app drawn the same way.
 * Mono: the logo in one colour from the accent, on glass. Colour: white on the brand's own colour (or
 * the logo's own colours on a dark tile). Duotone: two shades of the accent on the theme's colours.
 * An app with no usable logo gets its first letter, in the same style.
 */
@Composable
private fun IconTile(app: TvApp, ui: Ui) {
    val accent = ui.accent
    val ground = Color(ui.cfg.background)
    // A picture icon has no logo to recolour: Mono and Duotone use the letter instead.
    val g = app.glyph?.takeIf { !it.picture || ui.cfg.iconStyle == "colour" }
    val ink = lerp(accent, Color.White, 0.35f)
    val back: Brush; val filter: ColorFilter?; val letterInk: Color
    when (ui.cfg.iconStyle) {
        "mono" -> { back = Brush.linearGradient(listOf(Color.White.copy(alpha = 0.13f), Color.White.copy(alpha = 0.07f))); filter = ColorFilter.tint(ink); letterInk = ink }
        "duo" -> {
            back = Brush.linearGradient(listOf(lerp(ground, accent, 0.3f), lerp(ground, Color.Black, 0.2f)))
            filter = ColorFilter.colorMatrix(IconArt.duotone(lerp(accent, Color.Black, 0.25f), lerp(accent, Color.White, 0.55f))); letterInk = accent
        }
        else -> {
            val plate = g?.plate?.let { Color(it) } ?: if (g == null) lerp(accent, Color.Black, 0.35f) else null
            back = if (plate != null) Brush.linearGradient(listOf(lerp(plate, Color.White, 0.08f), lerp(plate, Color.Black, 0.25f)))
                   else Brush.linearGradient(listOf(lerp(ground, Color.White, 0.12f), lerp(ground, Color.White, 0.06f)))
            filter = if (plate != null) ColorFilter.tint(Color.White) else null; letterInk = Color.White
        }
    }
    BoxWithConstraints(Modifier.fillMaxSize().background(back), contentAlignment = Alignment.Center) {
        val wide = ui.shape == "wide"
        if (g != null && g.picture && ui.cfg.iconStyle == "colour") {
            Image(g.art, app.label, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        } else if (g != null && !g.picture) {
            Image(g.art, app.label, Modifier.size(width = maxWidth * 0.62f, height = maxHeight * (if (wide) 0.48f else 0.46f)), contentScale = ContentScale.Fit, colorFilter = filter)
        } else {
            Text(app.label.take(1).uppercase(), color = letterInk, fontSize = with(androidx.compose.ui.platform.LocalDensity.current) { (minOf(maxWidth, maxHeight) * 0.42f).toSp() }, fontWeight = FontWeight.SemiBold)
        }
    }
}

/**
 * A tile for an app with no TV banner, like most sideloaded apps (#4): the icon on a gradient of its own
 * colour. With names off it reads like a banner, icon on the left and the name beside it.
 */
@Composable
private fun GeneratedTile(app: TvApp, big: Boolean, named: Boolean) {
    val base = app.tint?.let { Color(it) }
    val fill = if (base == null) Modifier else Modifier.background(Brush.linearGradient(listOf(lerp(base, Color.White, 0.12f), lerp(base, Color.Black, 0.35f))))
    BoxWithConstraints(Modifier.fillMaxSize().then(fill), contentAlignment = Alignment.Center) {
        // Grid tiles are ~120 dp wide: a name beside the icon would break mid-word, so it goes underneath.
        val wide = maxWidth >= 170.dp
        val icon = if (wide) (if (big) 52.dp else 44.dp) else 32.dp
        when {
            !named -> Image(app.art, null, Modifier.size(icon))
            wide -> Row(Modifier.fillMaxSize().padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                Image(app.art, null, Modifier.size(icon))
                Spacer(Modifier.width(12.dp))
                Text(app.label, color = TEXT, fontSize = if (big) 17.sp else 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 19.sp)
            }
            else -> Column(Modifier.padding(horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Image(app.art, null, Modifier.size(icon))
                Spacer(Modifier.height(5.dp))
                Text(app.label, color = TEXT, fontSize = 11.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

/** A folder: a 2x2 peek at its apps, the name underneath. OK opens it over the home screen. */
@Composable
private fun FolderTile(folder: Pair<String, List<TvApp>>, ui: Ui, size: Modifier) {
    val shape = RoundedCornerShape(10.dp)
    val (scale, glow, border) = focusStyle(ui, shape, lifted = true)
    var focused by remember { mutableStateOf(false) }
    // The accent mixed into the background, a quarter and then two fifths, so folders read as the theme's own.
    val ground = Color(ui.cfg.background)
    Column(size) {
        Surface(
            onClick = { ui.onFolder(folder) },
            modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f).onFocusChanged { focused = it.isFocused }.lift(ui, shape, focused),
            shape = ClickableSurfaceDefaults.shape(shape), scale = scale, glow = glow, border = border,
            colors = ClickableSurfaceDefaults.colors(containerColor = lerp(ground, ui.accent, 0.25f).copy(alpha = 0.9f), focusedContainerColor = lerp(ground, ui.accent, 0.4f))
        ) {
            Column(Modifier.fillMaxSize().padding(8.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                folder.second.take(4).chunked(2).forEach { pair ->
                    Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        pair.forEach { app ->
                            Image(app.art, null, Modifier.weight(1f).fillMaxHeight().background(app.tint?.let { Color(it) } ?: Color.Black.copy(alpha = 0.25f), RoundedCornerShape(5.dp)), contentScale = if (app.banner) ContentScale.Crop else ContentScale.Fit)
                        }
                        if (pair.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
            LiftLight(ui, focused)
        }
        Text("${folder.first} · ${folder.second.size}", color = TEXT, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 7.dp, start = 2.dp))
    }
}

/** What another app is playing, with play/pause and next. Only there while a session has something loaded. */
@Composable
private fun NowPlayingCard(t: Track, ui: Ui) {
    Row(
        Modifier.padding(horizontal = BLEED, vertical = 12.dp).width(520.dp).background(PANEL.copy(alpha = 0.88f), RoundedCornerShape(14.dp)).padding(12.dp).focusGroup(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(64.dp).background(TILE, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
            if (t.art != null) Image(t.art, null, Modifier.fillMaxSize().background(Color.Black, RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
            else Image(Icons.Outlined.MusicNote, null, Modifier.size(28.dp), colorFilter = ColorFilter.tint(MUTED))
        }
        Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
            Text(t.title, color = TEXT, fontSize = 17.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(listOf(t.artist, t.app).filter { it.isNotBlank() }.joinToString(" · "), color = MUTED, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 3.dp))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            RoundButton(if (t.playing) Icons.Outlined.Pause else Icons.Outlined.PlayArrow, if (t.playing) "Pause" else "Play", ui) { NowPlaying.playPause() }
            RoundButton(Icons.Outlined.SkipNext, "Next", ui) { NowPlaying.next() }
        }
    }
}

// ---- Top bar pieces -------------------------------------------------------------------------------

/** Icon, temperature, today's high and low, and the place, in one muted line. */
@Composable
private fun WeatherLine(ui: Ui, size: androidx.compose.ui.unit.TextUnit, modifier: Modifier = Modifier) {
    val w = ui.weather ?: return
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Image(weatherIcon(w), null, Modifier.size(with(androidx.compose.ui.platform.LocalDensity.current) { size.toDp() * 1.3f }), colorFilter = ColorFilter.tint(MUTED))
        Spacer(Modifier.width(6.dp))
        Text("${w.temp}°  ${w.high}° / ${w.low}°  ${ui.cfg.weatherPlace}", color = MUTED, fontSize = size, fontFamily = CLOCK, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/** WMO weather codes, as Open-Meteo reports them, to a drawn icon. */
private fun weatherIcon(w: WeatherNow): ImageVector = when (w.code) {
    0 -> if (w.day) Icons.Outlined.WbSunny else Icons.Outlined.NightsStay
    1, 2 -> if (w.day) Icons.Outlined.WbCloudy else Icons.Outlined.NightsStay
    3 -> Icons.Outlined.Cloud
    45, 48 -> Icons.Outlined.Dehaze
    in 71..77, 85, 86 -> Icons.Outlined.AcUnit
    in 95..99 -> Icons.Outlined.Thunderstorm
    in 51..67, in 80..82 -> Icons.Outlined.WaterDrop
    else -> Icons.Outlined.Cloud
}

@Composable
private fun RoundButton(icon: ImageVector, label: String, ui: Ui, onClick: () -> Unit) {
    val (scale, glow, border) = focusStyle(ui, RoundedCornerShape(50), zoom = 1.12f)
    var focused by remember { mutableStateOf(false) }
    Surface(
        onClick = onClick,
        modifier = Modifier.size(44.dp).onFocusChanged { focused = it.isFocused },
        shape = ClickableSurfaceDefaults.shape(CircleShape), scale = scale, glow = glow, border = border,
        colors = ClickableSurfaceDefaults.colors(containerColor = Color.White.copy(alpha = 0.08f), focusedContainerColor = ui.accent.copy(alpha = 0.9f))
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Image(icon, label, Modifier.size(24.dp), colorFilter = ColorFilter.tint(if (focused) onAccent(ui.accent) else TEXT))
        }
    }
}

@Composable
private fun QuickButton(a: QuickAction, ui: Ui, onQuick: (QuickAction) -> Unit) {
    val (scale, glow, border) = focusStyle(ui, RoundedCornerShape(50), zoom = 1.12f)
    var focused by remember { mutableStateOf(false) }
    Surface(
        onClick = { onQuick(a) },
        modifier = Modifier.size(40.dp).onFocusChanged { focused = it.isFocused },
        shape = ClickableSurfaceDefaults.shape(CircleShape), scale = scale, glow = glow, border = border,
        colors = ClickableSurfaceDefaults.colors(containerColor = Color.White.copy(alpha = 0.08f), focusedContainerColor = ui.accent.copy(alpha = 0.9f))
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            QuickGlyph(a, ui, focused, 22.dp)
        }
    }
}

/**
 * The icon inside a quick button: the drawn glyph, or an app's own icon at the glyph's size plus the room
 * a glyph leaves inside its box, so app icons sit on the same centre line and look the same size.
 */
@Composable
private fun QuickGlyph(a: QuickAction, ui: Ui, focused: Boolean, size: Dp) {
    val art = a.art
    if (art != null) Image(art, a.label, Modifier.size(size + 4.dp).clip(CircleShape))
    else Image(iconFor(a, ui.cfg), a.label, Modifier.size(size), colorFilter = ColorFilter.tint(if (focused) onAccent(ui.accent) else TEXT))
}

@Composable
private fun QuickRow(a: QuickAction, ui: Ui, onQuick: (QuickAction) -> Unit, expanded: Boolean) =
    RailItem(iconFor(a, ui.cfg), a.label, ui, expanded, glyph = { focused -> QuickGlyph(a, ui, focused, 22.dp) }) { onQuick(a) }

@Composable
private fun RailItem(icon: ImageVector, label: String, ui: Ui, expanded: Boolean, glyph: (@Composable (Boolean) -> Unit)? = null, onClick: () -> Unit) {
    val shape = RoundedCornerShape(22.dp)
    val (_, glow, border) = focusStyle(ui, shape)
    var focused by remember { mutableStateOf(false) }
    val ink = if (focused) onAccent(ui.accent) else TEXT
    Surface(
        onClick = onClick,
        modifier = Modifier.height(44.dp).then(if (expanded) Modifier.fillMaxWidth() else Modifier.width(44.dp)).onFocusChanged { focused = it.isFocused },
        shape = ClickableSurfaceDefaults.shape(shape), scale = ClickableSurfaceDefaults.scale(focusedScale = 1.04f), glow = glow, border = border,
        colors = ClickableSurfaceDefaults.colors(containerColor = Color.Transparent, focusedContainerColor = ui.accent.copy(alpha = 0.9f))
    ) {
        Row(Modifier.fillMaxSize().padding(horizontal = 11.dp), verticalAlignment = Alignment.CenterVertically) {
            // A fixed 22 dp slot, so labels line up whether the icon is a glyph or an app's.
            Box(Modifier.size(22.dp), contentAlignment = Alignment.Center) {
                if (glyph != null) glyph(focused) else Image(icon, label, Modifier.size(22.dp), colorFilter = ColorFilter.tint(ink))
            }
            if (expanded) {
                Spacer(Modifier.width(14.dp))
                Text(label, color = ink, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun Greeting(cfg: HomeConfig, size: androidx.compose.ui.unit.TextUnit) {
    if (cfg.greeting.isBlank()) return
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val part = when (hour) { in 5..11 -> "Good morning"; in 12..17 -> "Good afternoon"; else -> "Good evening" }
    // The name in a light shade of the accent, light enough to read on any wallpaper.
    val name = lerp(Color(cfg.accent), Color.White, 0.45f)
    Text(buildAnnotatedString { append("$part, "); withStyle(SpanStyle(color = name)) { append(cfg.greeting) } }, color = TEXT, fontSize = size, fontWeight = FontWeight.Medium, maxLines = 2)
}

@Composable
private fun Clock(cfg: HomeConfig, timeSize: androidx.compose.ui.unit.TextUnit, dateSize: androidx.compose.ui.unit.TextUnit, align: Alignment.Horizontal) {
    if (cfg.clock == "off") return
    var now by remember { mutableStateOf(Date()) }
    LaunchedEffect(Unit) { while (true) { now = Date(); delay(1000L - System.currentTimeMillis() % 1000) } }
    Column(horizontalAlignment = align) {
        Text(DateFormat.getTimeInstance(DateFormat.SHORT).format(now), color = TEXT, fontSize = timeSize, fontFamily = CLOCK, fontWeight = FontWeight.Light, maxLines = 1, softWrap = false)
        if (cfg.clock == "time_date") Text(DateFormat.getDateInstance(DateFormat.FULL).format(now), color = MUTED, fontSize = dateSize, fontFamily = CLOCK)
    }
}

/** The idle screen's only text: a large clock and the weather in the lower left, inside the safe area. */
@Composable
private fun IdleClock(ui: Ui, modifier: Modifier) {
    Column(modifier.padding(start = SAFE_X, bottom = SAFE_Y + 12.dp)) {
        Clock(ui.cfg, 64.sp, 18.sp, Alignment.Start)
        WeatherLine(ui, 16.sp, Modifier.padding(top = 8.dp))
    }
}

// ---- Overlays -------------------------------------------------------------------------------------

/** A folder's apps over a dimmed home screen. Back closes it. */
@Composable
private fun FolderOverlay(name: String, list: List<TvApp>, ui: Ui, onClose: () -> Unit) {
    val focus = remember { FocusRequester() }
    Dialog(onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(Modifier.fillMaxSize().background(scrim(0.9f)), contentAlignment = Alignment.Center) {
            Column(Modifier.background(PANEL, RoundedCornerShape(16.dp)).padding(horizontal = 20.dp, vertical = 18.dp)) {
                Text(name, color = TEXT, fontSize = 24.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(start = BLEED, bottom = 6.dp))
                val inner = Ui(ui.cfg, ui.accent, focus, list.firstOrNull()?.let { "folder:${it.pkg}" }, ui.onFolder, motion = ui.motion)
                list.chunked(5).forEach { row ->
                    Row(Modifier.padding(horizontal = BLEED, vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(GAP)) {
                        for (app in row) Tile("folder", app, inner, Modifier.width(148.dp), big = false)
                    }
                }
            }
        }
        LaunchedEffect(Unit) { runCatching { focus.requestFocus() } }
    }
}

@Composable
private fun InputsOverlay(inputs: List<Pair<String, String>>, ui: Ui, onClose: () -> Unit) {
    val ctx = LocalContext.current
    val focus = remember { FocusRequester() }
    Dialog(onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(Modifier.fillMaxSize().background(scrim(0.9f)), contentAlignment = Alignment.Center) {
            Column(Modifier.width(400.dp).background(PANEL, RoundedCornerShape(16.dp)).padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Inputs", color = TEXT, fontSize = 24.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 6.dp))
                inputs.forEachIndexed { i, (id, label) ->
                    val shape = RoundedCornerShape(12.dp)
                    val (scale, glow, border) = focusStyle(ui, shape, zoom = 1.03f)
                    Surface(
                        onClick = { onClose(); openInput(ctx, id) },
                        modifier = (if (i == 0) Modifier.focusRequester(focus) else Modifier).fillMaxWidth().height(52.dp),
                        shape = ClickableSurfaceDefaults.shape(shape), scale = scale, glow = glow, border = border,
                        colors = ClickableSurfaceDefaults.colors(containerColor = TILE, focusedContainerColor = TILE_FOCUSED)
                    ) {
                        Box(Modifier.fillMaxSize().padding(horizontal = 18.dp), contentAlignment = Alignment.CenterStart) {
                            Text(label, color = TEXT, fontSize = 17.sp)
                        }
                    }
                }
            }
        }
        LaunchedEffect(Unit) { runCatching { focus.requestFocus() } }
    }
}

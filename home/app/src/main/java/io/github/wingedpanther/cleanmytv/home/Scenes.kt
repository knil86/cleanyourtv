package io.github.wingedpanther.cleanmytv.home

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

/**
 * One of the Couch layout's scenes, after couch-tv's Night look (github.com/Seagey/couch-tv, MIT; the photos
 * were made for it and are free to use, see assets/scenes/CREDITS.md). Each scene is a photo, the scenery cut
 * out of it with the sky transparent (drawn over the clock, so peaks overlap the digits), and a blurred copy
 * that fills the clock like frosted glass.
 *
 * Measurements are in rem as in couch-tv: a 120th of the screen's width (8 dp on a TV). Colours are ARGB.
 */
internal class SceneSpec(
    val id: String,
    val hasCutOut: Boolean,
    /** The clock's top and font size. */
    val top: Float,
    val size: Float,
    /** Null centres the clock; otherwise its left edge. */
    val left: Float? = null,
    val dateTop: Float = 6f,
    val dateLeft: Float? = null,
    /** Dark text for the date and no shadow, on a light scene. */
    val dark: Boolean = false,
    val dateColour: Long = 0xFFFFFFFFL,
    /** Brightness of the photo and its cut-out. */
    val photoBright: Float = 1f,
    /** The glass fill: brightness, saturation, contrast and opacity. */
    val fillBright: Float = 1f,
    val fillSat: Float = 1f,
    val fillContrast: Float = 1f,
    val fillAlpha: Float = 1f,
    /** The digits' light edge: a faint fill and a thin outline. */
    val rimFill: Long = 0x1AFFFFFFL,
    val rimStroke: Long = 0x8CFFFFFFL,
    /** A highlight across the top of the glass, as on a lock screen. */
    val sheen: Boolean = true,
    /** A vertical shade over the scene, as (position, colour) stops. */
    val shade: List<Pair<Float, Long>>? = null,
)

internal class SceneArt(val photo: ImageBitmap, val cutOut: ImageBitmap?, val glass: ImageBitmap)

internal object Scenes {
    val all = listOf(
        // The clock behind the peak, in pink glass.
        SceneSpec("night", hasCutOut = true, top = 9.375f, size = 25f, dateColour = 0xEBFFE8EEL, photoBright = 0.86f,
            fillBright = 1.15f, fillSat = 1.2f, fillAlpha = 0.9f, rimFill = 0x0FFFC8D7L, rimStroke = 0x80FFD7E1L),
        // A little smaller, low enough for the volcano's tip to rise into the digits.
        SceneSpec("golden", hasCutOut = true, top = 9.5f, size = 24.375f, dateTop = 6.625f),
        // The clock sits on the water.
        SceneSpec("water", hasCutOut = false, top = 16.75f, size = 26.25f, dateTop = 13.5f,
            shade = listOf(0f to 0x6B000C18L, 0.4f to 0x1F000C18L, 0.52f to 0x00000000L, 1f to 0x80001428L)),
        // A big dark clock on the left, sinking into the misty hills.
        SceneSpec("fog", hasCutOut = true, top = 10.75f, size = 29.375f, left = 6f, dateTop = 7.25f, dateLeft = 6.5f,
            dark = true, dateColour = 0xFF16201DL, fillBright = 0.46f, fillSat = 0.8f, fillContrast = 1.1f, fillAlpha = 0.8f,
            rimFill = 0x00FFFFFFL, rimStroke = 0x99FFFFFFL, sheen = false),
        // Warm light at the top, the clock low enough for the ridges to cross it.
        SceneSpec("dawn", hasCutOut = true, top = 14.375f, size = 25f, dateTop = 11f,
            shade = listOf(0f to 0x6B321C0CL, 0.55f to 0x00321C0CL)),
    )

    /**
     * Decodes one scene from the app's assets, or null if it can't. The photo and the glass have no
     * transparency, so they take half the memory; the glass is a blur, so it loads at half size too.
     */
    fun load(ctx: Context, s: SceneSpec): SceneArt? = runCatching {
        fun read(name: String, opaque: Boolean, sample: Int = 1): ImageBitmap? =
            ctx.assets.open("scenes/$name").use { input ->
                BitmapFactory.decodeStream(input, null, BitmapFactory.Options().apply {
                    inPreferredConfig = if (opaque) Bitmap.Config.RGB_565 else Bitmap.Config.ARGB_8888
                    inSampleSize = sample
                })?.asImageBitmap()
            }
        val photo = read("${s.id}.jpg", opaque = true) ?: return@runCatching null
        val glass = read("${s.id}-glass.jpg", opaque = true, sample = 2) ?: return@runCatching null
        SceneArt(photo, if (s.hasCutOut) read("${s.id}-fg.webp", opaque = false) else null, glass)
    }.getOrNull()
}

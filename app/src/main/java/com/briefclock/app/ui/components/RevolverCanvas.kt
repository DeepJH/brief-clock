package com.briefclock.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.dp
import kotlin.math.*

/**
 * 3D Flat-Shaded (Unshaded) Revolver Canvas Component.
 * Floats cleanly on the screen background without an artificial enclosing box/card.
 * Features faceted 3D isometric planar geometry, mechanical cylinder flutes,
 * cocking hammer, trigger pull, recoil kickback, and muzzle burst.
 */
@Composable
fun RevolverCanvas(
    modifier: Modifier = Modifier,
    cylinderAngle: Float = 0f,
    hammerCocked: Boolean = false,
    triggerPulled: Boolean = false,
    recoilAmount: Float = 0f,
    muzzleFlash: Boolean = false
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(230.dp)
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val centerX = canvasWidth * 0.52f
        val centerY = canvasHeight * 0.48f

        // Recoil transform: translation back and upward muzzle flip
        val recoilTx = -recoilAmount * 28f
        val recoilTy = -recoilAmount * 16f
        val recoilRotation = -recoilAmount * 14f

        translate(left = recoilTx, top = recoilTy) {
            rotate(degrees = recoilRotation, pivot = Offset(centerX + 80f, centerY + 60f)) {
                val scale = min(canvasWidth / 420f, canvasHeight / 240f) * 0.98f

                translate(left = centerX - 180f * scale, top = centerY - 55f * scale) {
                    drawFlat3DRevolver(
                        scale = scale,
                        cylinderAngle = cylinderAngle,
                        hammerCocked = hammerCocked,
                        triggerPulled = triggerPulled,
                        muzzleFlash = muzzleFlash
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawFlat3DRevolver(
    scale: Float,
    cylinderAngle: Float,
    hammerCocked: Boolean,
    triggerPulled: Boolean,
    muzzleFlash: Boolean
) {
    // 3D Flat Color Palette (Clean, unshaded, solid tone facets)
    val steelBase = Color(0xFF333842)
    val steelLight = Color(0xFF4C5362)
    val steelDark = Color(0xFF22262E)
    val steelOutline = Color(0xFF181B20)
    val steelHighlight = Color(0xFF6B7485)

    val woodBase = Color(0xFF8B4513)
    val woodDark = Color(0xFF5C2C0B)
    val woodBevel = Color(0xFFA0522D)
    val brassPin = Color(0xFFFFC107)

    val strokeBorder = Stroke(width = 2.2f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val strokeDetail = Stroke(width = 1.4f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)

    // 1. Grip (3D Faceted Walnut Stock)
    val gripPath = Path().apply {
        moveTo(232f * scale, 86f * scale)
        cubicTo(
            260f * scale, 100f * scale,
            298f * scale, 148f * scale,
            294f * scale, 208f * scale
        )
        // Butt of the grip
        cubicTo(
            290f * scale, 224f * scale,
            266f * scale, 228f * scale,
            246f * scale, 224f * scale
        )
        // Front strap of the grip
        cubicTo(
            236f * scale, 175f * scale,
            216f * scale, 130f * scale,
            206f * scale, 105f * scale
        )
        close()
    }
    drawPath(gripPath, woodBase)
    drawPath(gripPath, steelOutline, style = strokeBorder)

    // 3D Grip Backstrap Facet
    val backstrapPath = Path().apply {
        moveTo(260f * scale, 100f * scale)
        cubicTo(
            298f * scale, 148f * scale,
            294f * scale, 208f * scale,
            290f * scale, 224f * scale
        )
        lineTo(282f * scale, 220f * scale)
        cubicTo(
            286f * scale, 204f * scale,
            288f * scale, 150f * scale,
            254f * scale, 104f * scale
        )
        close()
    }
    drawPath(backstrapPath, woodDark)

    // Brass screw medallion on grip
    drawCircle(color = brassPin, radius = 6.5f * scale, center = Offset(260f * scale, 160f * scale))
    drawCircle(color = steelDark, radius = 2.5f * scale, center = Offset(260f * scale, 160f * scale))

    // 2. Trigger Guard & Trigger
    val guardPath = Path().apply {
        moveTo(160f * scale, 90f * scale)
        cubicTo(
            160f * scale, 134f * scale,
            224f * scale, 134f * scale,
            224f * scale, 92f * scale
        )
    }
    drawPath(guardPath, steelLight, style = Stroke(width = 5f * scale, cap = StrokeCap.Round))
    drawPath(guardPath, steelOutline, style = Stroke(width = 1.2f * scale, cap = StrokeCap.Round))

    // Trigger
    val triggerOffset = if (triggerPulled) 13f * scale else 0f
    val triggerPath = Path().apply {
        moveTo((190f * scale) + triggerOffset, 90f * scale)
        cubicTo(
            (186f * scale) + triggerOffset, 104f * scale,
            (183f * scale) + triggerOffset, 114f * scale,
            (195f * scale) + triggerOffset, 120f * scale
        )
        lineTo((197f * scale) + triggerOffset, 116f * scale)
        cubicTo(
            (188f * scale) + triggerOffset, 112f * scale,
            (190f * scale) + triggerOffset, 104f * scale,
            (194f * scale) + triggerOffset, 90f * scale
        )
        close()
    }
    drawPath(triggerPath, steelDark)
    drawPath(triggerPath, steelOutline, style = strokeDetail)

    // 3. Hammer (Rotates back when cocked)
    val hammerPivot = Offset(230f * scale, 82f * scale)
    val hammerAngle = if (hammerCocked) 32f else 0f
    rotate(degrees = hammerAngle, pivot = hammerPivot) {
        val hammerPath = Path().apply {
            moveTo(230f * scale, 82f * scale)
            lineTo(240f * scale, 58f * scale)
            lineTo(252f * scale, 52f * scale) // Spur
            lineTo(254f * scale, 56f * scale)
            lineTo(244f * scale, 64f * scale)
            lineTo(238f * scale, 84f * scale)
            close()
        }
        drawPath(hammerPath, steelBase)
        drawPath(hammerPath, steelOutline, style = strokeDetail)
    }

    // 4. Solid Receiver Frame
    val framePath = Path().apply {
        moveTo(148f * scale, 42f * scale)
        lineTo(236f * scale, 42f * scale)
        cubicTo(242f * scale, 48f * scale, 246f * scale, 68f * scale, 240f * scale, 86f * scale)
        lineTo(228f * scale, 88f * scale)
        lineTo(148f * scale, 88f * scale)
        close()
    }
    drawPath(framePath, steelBase)
    drawPath(framePath, steelOutline, style = strokeBorder)

    // Frame 3D Top Bevel Plane (Flat unshaded accent)
    val frameTopBevel = Path().apply {
        moveTo(148f * scale, 42f * scale)
        lineTo(236f * scale, 42f * scale)
        lineTo(233f * scale, 46f * scale)
        lineTo(148f * scale, 46f * scale)
        close()
    }
    drawPath(frameTopBevel, steelHighlight)

    // 5. 3D Faceted Cylinder (6 Chambers)
    val cylX = 146f * scale
    val cylY = 44f * scale
    val cylWidth = 74f * scale
    val cylHeight = 42f * scale

    // Cylinder Main Body Rect
    drawRect(
        color = steelDark,
        topLeft = Offset(cylX, cylY),
        size = Size(cylWidth, cylHeight)
    )
    drawRect(
        color = steelOutline,
        topLeft = Offset(cylX, cylY),
        size = Size(cylWidth, cylHeight),
        style = strokeBorder
    )

    // Cylinder Flutes (Solid facets representing revolving notches)
    val fluteSpacing = cylHeight / 3.2f
    for (f in 0..2) {
        val fluteY = cylY + 5f * scale + f * fluteSpacing
        drawRect(
            color = steelLight,
            topLeft = Offset(cylX + 8f * scale, fluteY),
            size = Size(cylWidth - 16f * scale, 3.5f * scale)
        )
    }

    // Cylinder Front Face (3D Isometric Rim Oval with visible chambers)
    val faceCenter = Offset(cylX + 3f * scale, cylY + cylHeight / 2f)
    drawOval(
        color = steelBase,
        topLeft = Offset(cylX - 5f * scale, cylY),
        size = Size(14f * scale, cylHeight)
    )
    drawOval(
        color = steelOutline,
        topLeft = Offset(cylX - 5f * scale, cylY),
        size = Size(14f * scale, cylHeight),
        style = strokeDetail
    )

    // Cylinder chamber bores on front face
    val chamberRadX = 4f * scale
    val chamberRadY = 15f * scale
    for (c in 0 until 6) {
        val rad = (cylinderAngle + c * 60f) * PI / 180.0
        val cx = faceCenter.x + (chamberRadX * cos(rad)).toFloat()
        val cy = faceCenter.y + (chamberRadY * sin(rad)).toFloat()
        drawOval(
            color = Color(0xFF0F1216),
            topLeft = Offset(cx - 2.5f * scale, cy - 2.8f * scale),
            size = Size(5f * scale, 5.6f * scale)
        )
    }

    // 6. Barrel (Magnum Heavy Barrel with ventilated rib)
    val barrelLength = 135f * scale
    val barrelTopY = 40f * scale
    val barrelHeight = 22f * scale
    val barrelStartX = cylX - 4f * scale
    val muzzleX = barrelStartX - barrelLength

    // Main barrel tube (solid steel flat)
    drawRect(
        color = steelBase,
        topLeft = Offset(muzzleX, barrelTopY),
        size = Size(barrelLength, barrelHeight)
    )
    drawRect(
        color = steelOutline,
        topLeft = Offset(muzzleX, barrelTopY),
        size = Size(barrelLength, barrelHeight),
        style = strokeBorder
    )

    // Barrel Top Highlight Facet
    drawRect(
        color = steelHighlight,
        topLeft = Offset(muzzleX, barrelTopY),
        size = Size(barrelLength, 3f * scale)
    )

    // Full Underlug (Solid flat)
    drawRect(
        color = steelDark,
        topLeft = Offset(muzzleX + 12f * scale, barrelTopY + barrelHeight),
        size = Size(barrelLength - 12f * scale, 13f * scale)
    )
    drawRect(
        color = steelOutline,
        topLeft = Offset(muzzleX + 12f * scale, barrelTopY + barrelHeight),
        size = Size(barrelLength - 12f * scale, 13f * scale),
        style = strokeDetail
    )

    // Front Sight (Sharp silhouette with red target ramp)
    val sightPath = Path().apply {
        moveTo(muzzleX + 2f * scale, barrelTopY)
        lineTo(muzzleX + 18f * scale, barrelTopY)
        lineTo(muzzleX + 13f * scale, barrelTopY - 12f * scale)
        lineTo(muzzleX + 2f * scale, barrelTopY - 12f * scale)
        close()
    }
    drawPath(sightPath, steelDark)
    drawPath(sightPath, steelOutline, style = strokeDetail)

    // Red ramp insert
    drawRect(
        color = Color(0xFFFF3D00),
        topLeft = Offset(muzzleX + 3f * scale, barrelTopY - 10f * scale),
        size = Size(8f * scale, 2.5f * scale)
    )

    // Muzzle Bore
    drawOval(
        color = Color(0xFF0F1216),
        topLeft = Offset(muzzleX - 3f * scale, barrelTopY + 3f * scale),
        size = Size(6f * scale, barrelHeight - 6f * scale)
    )

    // 7. Muzzle Flash Burst (When BANG! occurs)
    if (muzzleFlash) {
        val flashCenterX = muzzleX - 25f * scale
        val flashCenterY = barrelTopY + barrelHeight / 2f

        // Crisp flat starburst polygon
        val burstPath = Path().apply {
            moveTo(muzzleX, flashCenterY)
            lineTo(flashCenterX - 65f * scale, flashCenterY - 32f * scale)
            lineTo(flashCenterX - 40f * scale, flashCenterY - 10f * scale)
            lineTo(flashCenterX - 85f * scale, flashCenterY)
            lineTo(flashCenterX - 40f * scale, flashCenterY + 10f * scale)
            lineTo(flashCenterX - 65f * scale, flashCenterY + 32f * scale)
            close()
        }
        drawPath(burstPath, Color(0xFFFFD600))
        drawPath(burstPath, Color(0xFFFF6D00), style = Stroke(width = 2.5f * scale))

        // Secondary flame dart
        val innerDart = Path().apply {
            moveTo(muzzleX, flashCenterY)
            lineTo(flashCenterX - 40f * scale, flashCenterY - 14f * scale)
            lineTo(flashCenterX - 60f * scale, flashCenterY)
            lineTo(flashCenterX - 40f * scale, flashCenterY + 14f * scale)
            close()
        }
        drawPath(innerDart, Color(0xFFFFFFFF))
    }
}

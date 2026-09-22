package com.briefclock.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.dp
import kotlin.math.*

@Composable
fun RevolverCanvas(
    modifier: Modifier = Modifier,
    cylinderAngle: Float = 0f,
    hammerCocked: Boolean = false,
    triggerPulled: Boolean = false,
    recoilAmount: Float = 0f, // 0f..1f kickback
    muzzleFlash: Boolean = false
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp)
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val centerX = canvasWidth * 0.52f
        val centerY = canvasHeight * 0.50f

        // Recoil transform: translation back and upward muzzle flip
        val recoilTx = -recoilAmount * 28f
        val recoilTy = -recoilAmount * 16f
        val recoilRotation = -recoilAmount * 14f

        translate(left = recoilTx, top = recoilTy) {
            rotate(degrees = recoilRotation, pivot = Offset(centerX + 80f, centerY + 60f)) {
                // Gun scale factor
                val scale = min(canvasWidth / 420f, canvasHeight / 260f) * 0.95f

                translate(left = centerX - 180f * scale, top = centerY - 60f * scale) {
                    drawRevolver(
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

private fun DrawScope.drawRevolver(
    scale: Float,
    cylinderAngle: Float,
    hammerCocked: Boolean,
    triggerPulled: Boolean,
    muzzleFlash: Boolean
) {
    // Coordinate anchor definitions
    // Grip anchor: ~ (260, 80)
    // Frame anchor: ~ (180, 50)
    // Cylinder anchor: ~ (150, 45)
    // Barrel anchor: ~ (30, 40)
    // Muzzle: ~ (-10, 42)

    val steelDark = Color(0xFF23272A)
    val steelMid = Color(0xFF3C434A)
    val steelLight = Color(0xFF6B7480)
    val steelHighlight = Color(0xFFB0B8C4)
    val woodDark = Color(0xFF3E2723)
    val woodMid = Color(0xFF5D4037)
    val woodLight = Color(0xFF8D6E63)
    val brassGold = Color(0xFFD4AF37)

    // 1. Grip (握把 - 2.5D Walnut wood with contour & screw)
    val gripPath = Path().apply {
        moveTo(230f * scale, 85f * scale)
        cubicTo(
            260f * scale, 100f * scale,
            300f * scale, 150f * scale,
            295f * scale, 210f * scale
        )
        // Butt of the grip
        cubicTo(
            290f * scale, 225f * scale,
            265f * scale, 230f * scale,
            245f * scale, 225f * scale
        )
        // Front strap of the grip
        cubicTo(
            235f * scale, 175f * scale,
            215f * scale, 130f * scale,
            205f * scale, 105f * scale
        )
        close()
    }

    val gripBrush = Brush.linearGradient(
        colors = listOf(woodLight, woodMid, woodDark),
        start = Offset(210f * scale, 90f * scale),
        end = Offset(295f * scale, 225f * scale)
    )
    drawPath(gripPath, gripBrush)
    drawPath(gripPath, steelDark, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5f * scale))

    // Grip screw medallion
    drawCircle(
        color = brassGold,
        radius = 7f * scale,
        center = Offset(260f * scale, 160f * scale)
    )
    drawCircle(
        color = Color(0xFF1E1612),
        radius = 3f * scale,
        center = Offset(260f * scale, 160f * scale)
    )

    // 2. Trigger & Trigger Guard (扳机与护圈)
    val guardPath = Path().apply {
        moveTo(160f * scale, 90f * scale)
        cubicTo(
            160f * scale, 135f * scale,
            225f * scale, 135f * scale,
            225f * scale, 92f * scale
        )
    }
    drawPath(guardPath, steelMid, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 5.5f * scale))
    drawPath(guardPath, steelDark, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f * scale))

    // Trigger
    val triggerOffset = if (triggerPulled) 14f * scale else 0f
    val triggerPath = Path().apply {
        moveTo((190f * scale) + triggerOffset, 90f * scale)
        cubicTo(
            (185f * scale) + triggerOffset, 105f * scale,
            (182f * scale) + triggerOffset, 115f * scale,
            (195f * scale) + triggerOffset, 120f * scale
        )
    }
    drawPath(triggerPath, steelHighlight, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f * scale, cap = StrokeCap.Round))

    // 3. Hammer (击锤 - 2.5D cocking action)
    val hammerPivot = Offset(230f * scale, 58f * scale)
    val hammerAngle = if (hammerCocked) 32f else 0f
    rotate(degrees = hammerAngle, pivot = hammerPivot) {
        val hammerPath = Path().apply {
            moveTo(230f * scale, 58f * scale)
            lineTo(245f * scale, 35f * scale) // Spur
            lineTo(252f * scale, 32f * scale)
            lineTo(250f * scale, 42f * scale)
            lineTo(238f * scale, 52f * scale)
            lineTo(228f * scale, 68f * scale)
            close()
        }
        drawPath(hammerPath, steelLight)
        drawPath(hammerPath, steelDark, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f * scale))
    }

    // 4. Main Frame (枪身机匣 - Solid metallic body)
    val framePath = Path().apply {
        moveTo(105f * scale, 40f * scale) // Cylinder window front top
        lineTo(235f * scale, 40f * scale) // Top strap & rear sight
        lineTo(235f * scale, 88f * scale) // Recoil shield & back strap
        lineTo(205f * scale, 90f * scale)
        lineTo(110f * scale, 90f * scale) // Bottom crane hinge
        close()
    }
    val frameBrush = Brush.linearGradient(
        colors = listOf(steelHighlight, steelMid, steelDark),
        start = Offset(105f * scale, 40f * scale),
        end = Offset(235f * scale, 90f * scale)
    )
    drawPath(framePath, frameBrush)

    // Rear sight notch
    drawRect(
        color = Color(0xFF15181B),
        topLeft = Offset(228f * scale, 36f * scale),
        size = Size(6f * scale, 6f * scale)
    )

    // 5. 2.5D Revolver Cylinder (转轮/弹巢 - 3D isometric perspective)
    val cylX = 115f * scale
    val cylY = 44f * scale
    val cylWidth = 78f * scale
    val cylHeight = 44f * scale

    // Cylinder outer body
    val cylBodyBrush = Brush.linearGradient(
        colors = listOf(steelHighlight, steelLight, steelMid, steelDark, steelMid),
        start = Offset(cylX, cylY),
        end = Offset(cylX, cylY + cylHeight)
    )
    drawRoundRect(
        brush = cylBodyBrush,
        topLeft = Offset(cylX, cylY),
        size = Size(cylWidth, cylHeight),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f * scale, 6f * scale)
    )

    // 2.5D Chamber Flutes & Longitudinal cuts (减重凹槽)
    val numFlutes = 4
    for (i in 0 until numFlutes) {
        val fluteY = cylY + 8f * scale + (i * 9.5f * scale)
        val flutePhase = sin((cylinderAngle * PI / 180.0) + (i * PI / 2.0)).toFloat()
        val fluteAlpha = (0.35f + 0.5f * (flutePhase + 1f) / 2f).coerceIn(0.1f, 0.9f)

        drawLine(
            color = Color.Black.copy(alpha = fluteAlpha * 0.7f),
            start = Offset(cylX + 8f * scale, fluteY),
            end = Offset(cylX + cylWidth - 8f * scale, fluteY),
            strokeWidth = 3.5f * scale,
            cap = StrokeCap.Round
        )
        drawLine(
            color = steelHighlight.copy(alpha = fluteAlpha * 0.5f),
            start = Offset(cylX + 8f * scale, fluteY + 1.5f * scale),
            end = Offset(cylX + cylWidth - 8f * scale, fluteY + 1.5f * scale),
            strokeWidth = 1.2f * scale,
            cap = StrokeCap.Round
        )
    }

    // Front Cylinder Face (2.5D Ellipse with visible chamber holes)
    val faceCenter = Offset(cylX + 4f * scale, cylY + cylHeight / 2f)
    drawOval(
        color = steelDark,
        topLeft = Offset(cylX - 4f * scale, cylY),
        size = Size(14f * scale, cylHeight)
    )

    // Cylinder chamber bores on the front rim
    val chamberRadiusX = 4f * scale
    val chamberRadiusY = 16f * scale
    for (c in 0 until 6) {
        val rad = (cylinderAngle + c * 60f) * PI / 180.0
        val cx = faceCenter.x + (chamberRadiusX * cos(rad)).toFloat()
        val cy = faceCenter.y + (chamberRadiusY * sin(rad)).toFloat()
        drawOval(
            color = Color(0xFF0D0F11),
            topLeft = Offset(cx - 2.5f * scale, cy - 2.8f * scale),
            size = Size(5f * scale, 5.6f * scale)
        )
    }

    // 6. Barrel (枪管 - Classic heavy underlug magnum barrel)
    val barrelLength = 135f * scale
    val barrelTopY = 40f * scale
    val barrelHeight = 22f * scale
    val barrelStartX = cylX - 5f * scale
    val muzzleX = barrelStartX - barrelLength

    // Main barrel tube
    val barrelBrush = Brush.linearGradient(
        colors = listOf(steelHighlight, steelLight, steelMid, steelDark),
        start = Offset(muzzleX, barrelTopY),
        end = Offset(muzzleX, barrelTopY + barrelHeight)
    )
    drawRect(
        brush = barrelBrush,
        topLeft = Offset(muzzleX, barrelTopY),
        size = Size(barrelLength, barrelHeight)
    )

    // Full underlug (退壳杆护套)
    val underlugBrush = Brush.linearGradient(
        colors = listOf(steelMid, steelDark),
        start = Offset(muzzleX, barrelTopY + barrelHeight),
        end = Offset(muzzleX, barrelTopY + barrelHeight + 14f * scale)
    )
    drawRect(
        brush = underlugBrush,
        topLeft = Offset(muzzleX + 12f * scale, barrelTopY + barrelHeight),
        size = Size(barrelLength - 12f * scale, 14f * scale)
    )

    // Top ventilated rib (散热肋条)
    drawLine(
        color = steelHighlight,
        start = Offset(muzzleX, barrelTopY + 1.5f * scale),
        end = Offset(barrelStartX, barrelTopY + 1.5f * scale),
        strokeWidth = 2.5f * scale
    )

    // Front Sight (准星)
    val sightPath = Path().apply {
        moveTo(muzzleX + 2f * scale, barrelTopY)
        lineTo(muzzleX + 16f * scale, barrelTopY)
        lineTo(muzzleX + 12f * scale, barrelTopY - 12f * scale)
        lineTo(muzzleX + 2f * scale, barrelTopY - 12f * scale)
        close()
    }
    drawPath(sightPath, steelDark)
    // Red ramp insert on front sight
    drawLine(
        color = Color(0xFFFF3D00),
        start = Offset(muzzleX + 3f * scale, barrelTopY - 10f * scale),
        end = Offset(muzzleX + 11f * scale, barrelTopY - 10f * scale),
        strokeWidth = 2.5f * scale
    )

    // Muzzle Crown & Bore (枪口与膛孔)
    drawOval(
        color = Color(0xFF14171A),
        topLeft = Offset(muzzleX - 3f * scale, barrelTopY),
        size = Size(7f * scale, barrelHeight)
    )
    drawOval(
        color = Color.Black,
        topLeft = Offset(muzzleX - 2f * scale, barrelTopY + 4f * scale),
        size = Size(5f * scale, barrelHeight - 8f * scale)
    )

    // 7. Muzzle Flash Animation (枪口爆燃火光与烟雾)
    if (muzzleFlash) {
        val flashCenterX = muzzleX - 20f * scale
        val flashCenterY = barrelTopY + barrelHeight / 2f

        // Starburst fire rays
        val flashBrush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFFFFFFF),
                Color(0xFFFFEA00),
                Color(0xFFFF6D00),
                Color(0xFFD50000),
                Color.Transparent
            ),
            center = Offset(flashCenterX, flashCenterY),
            radius = 85f * scale
        )
        drawCircle(
            brush = flashBrush,
            radius = 85f * scale,
            center = Offset(flashCenterX, flashCenterY)
        )

        // Explosive flame spikes
        val spikePath = Path().apply {
            moveTo(muzzleX, flashCenterY)
            lineTo(flashCenterX - 75f * scale, flashCenterY - 35f * scale)
            lineTo(flashCenterX - 45f * scale, flashCenterY - 10f * scale)
            lineTo(flashCenterX - 95f * scale, flashCenterY)
            lineTo(flashCenterX - 45f * scale, flashCenterY + 10f * scale)
            lineTo(flashCenterX - 75f * scale, flashCenterY + 35f * scale)
            close()
        }
        drawPath(spikePath, Color(0xFFFFD600).copy(alpha = 0.9f))

        // Smoke wisps
        drawCircle(
            color = Color(0x66BDBDBD),
            radius = 35f * scale,
            center = Offset(flashCenterX - 40f * scale, flashCenterY - 30f * scale)
        )
        drawCircle(
            color = Color(0x449E9E9E),
            radius = 45f * scale,
            center = Offset(flashCenterX - 60f * scale, flashCenterY + 25f * scale)
        )
    }
}

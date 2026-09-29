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
 * High-definition 3D Isometric Flat-Shaded (Unshaded) Revolver.
 * Rendered at a 3/4 axonometric perspective with distinct top, side, and front polygonal facets.
 * 100% vector-drawn with crisp mechanical linework, rotating 3D cylinder flutes & chambers,
 * 3D cocking hammer, trigger action, recoil kickback, and sharp geometric muzzle blast.
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
            .height(240.dp)
    ) {
        val w = size.width
        val h = size.height
        val cx = w * 0.50f
        val cy = h * 0.52f

        // Recoil transform in 3D space: backward translation + upward pitch rotation
        val recoilTx = -recoilAmount * 32f
        val recoilTy = -recoilAmount * 18f
        val recoilRot = -recoilAmount * 15f

        translate(left = recoilTx, top = recoilTy) {
            rotate(degrees = recoilRot, pivot = Offset(cx + 80f, cy + 60f)) {
                val scale = min(w / 440f, h / 240f) * 1.05f

                translate(left = cx - 180f * scale, top = cy - 65f * scale) {
                    drawIsometric3DRevolver(
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

private fun DrawScope.drawIsometric3DRevolver(
    scale: Float,
    cylinderAngle: Float,
    hammerCocked: Boolean,
    triggerPulled: Boolean,
    muzzleFlash: Boolean
) {
    // 3D Flat-Shaded Polygon Palette (Solid technical tones, zero gradients)
    val steelTop = Color(0xFF6B7280)       // Light top surfaces
    val steelSide = Color(0xFF404754)      // Primary vertical side surfaces
    val steelBottom = Color(0xFF232832)    // Dark recessed/under surfaces
    val steelDeepBore = Color(0xFF0F1116)  // Inner barrels & chambers
    val lineOutline = Color(0xFF14171E)    // Crisp technical drafting border
    val lineDetail = Color(0xFF262C38)

    // Wood Grip Tones (3D faceted walnut)
    val woodTop = Color(0xFFA0522D)
    val woodSide = Color(0xFF8B4513)
    val woodBack = Color(0xFF5C2C0B)
    val goldPin = Color(0xFFFFC107)

    val borderStroke = Stroke(width = 2.4f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val innerStroke = Stroke(width = 1.4f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)

    // ----------------------------------------------------
    // 1. 3D Faceted Walnut Grip (Handle)
    // ----------------------------------------------------
    // Grip Main Side Polygon
    val gripSidePath = Path().apply {
        moveTo(234f * scale, 86f * scale)
        cubicTo(262f * scale, 100f * scale, 298f * scale, 145f * scale, 295f * scale, 210f * scale)
        // Butt curve
        cubicTo(290f * scale, 226f * scale, 266f * scale, 230f * scale, 246f * scale, 226f * scale)
        // Front strap
        cubicTo(236f * scale, 178f * scale, 216f * scale, 132f * scale, 208f * scale, 106f * scale)
        close()
    }
    drawPath(gripSidePath, woodSide)
    drawPath(gripSidePath, lineOutline, style = borderStroke)

    // 3D Grip Backstrap Facet (Thickness plane along the spine)
    val backstrapPath = Path().apply {
        moveTo(246f * scale, 92f * scale)
        cubicTo(270f * scale, 108f * scale, 304f * scale, 148f * scale, 301f * scale, 212f * scale)
        lineTo(295f * scale, 210f * scale)
        cubicTo(298f * scale, 145f * scale, 262f * scale, 100f * scale, 234f * scale, 86f * scale)
        close()
    }
    drawPath(backstrapPath, woodBack)
    drawPath(backstrapPath, lineOutline, style = innerStroke)

    // Grip Medallion (Brass Emblem)
    drawCircle(color = goldPin, radius = 7f * scale, center = Offset(262f * scale, 162f * scale))
    drawCircle(color = lineOutline, radius = 2.8f * scale, center = Offset(262f * scale, 162f * scale))

    // ----------------------------------------------------
    // 2. 3D Trigger Guard & Trigger
    // ----------------------------------------------------
    // Trigger guard thickness (rear 3D offset)
    val guard3DPath = Path().apply {
        moveTo(164f * scale, 92f * scale)
        cubicTo(164f * scale, 138f * scale, 228f * scale, 138f * scale, 228f * scale, 94f * scale)
    }
    drawPath(guard3DPath, steelBottom, style = Stroke(width = 6f * scale, cap = StrokeCap.Round))

    val guardFrontPath = Path().apply {
        moveTo(160f * scale, 90f * scale)
        cubicTo(160f * scale, 134f * scale, 224f * scale, 134f * scale, 224f * scale, 92f * scale)
    }
    drawPath(guardFrontPath, steelSide, style = Stroke(width = 5.2f * scale, cap = StrokeCap.Round))
    drawPath(guardFrontPath, lineOutline, style = Stroke(width = 1.4f * scale, cap = StrokeCap.Round))

    // Trigger (Pivots when pulled)
    val triggerShift = if (triggerPulled) 13f * scale else 0f
    val triggerPath = Path().apply {
        moveTo((190f * scale) + triggerShift, 90f * scale)
        cubicTo(
            (186f * scale) + triggerShift, 104f * scale,
            (183f * scale) + triggerShift, 115f * scale,
            (196f * scale) + triggerShift, 120f * scale
        )
        lineTo((198f * scale) + triggerShift, 116f * scale)
        cubicTo(
            (189f * scale) + triggerShift, 112f * scale,
            (191f * scale) + triggerShift, 104f * scale,
            (195f * scale) + triggerShift, 90f * scale
        )
        close()
    }
    drawPath(triggerPath, steelBottom)
    drawPath(triggerPath, lineOutline, style = innerStroke)

    // ----------------------------------------------------
    // 3. 3D Cocking Hammer
    // ----------------------------------------------------
    val hammerPivot = Offset(232f * scale, 82f * scale)
    val hammerRot = if (hammerCocked) 34f else 0f
    rotate(degrees = hammerRot, pivot = hammerPivot) {
        // Hammer 3D side plane
        val hammerPath = Path().apply {
            moveTo(232f * scale, 82f * scale)
            lineTo(242f * scale, 56f * scale)
            lineTo(256f * scale, 50f * scale) // Serrated spur
            lineTo(258f * scale, 55f * scale)
            lineTo(246f * scale, 64f * scale)
            lineTo(240f * scale, 85f * scale)
            close()
        }
        drawPath(hammerPath, steelSide)
        drawPath(hammerPath, lineOutline, style = innerStroke)

        // Hammer top spur facet
        val spurTop = Path().apply {
            moveTo(242f * scale, 56f * scale)
            lineTo(256f * scale, 50f * scale)
            lineTo(254f * scale, 47f * scale)
            lineTo(240f * scale, 53f * scale)
            close()
        }
        drawPath(spurTop, steelTop)
        drawPath(spurTop, lineOutline, style = innerStroke)
    }

    // ----------------------------------------------------
    // 4. 3D Solid Frame & Top Strap (Bridging the Cylinder)
    // ----------------------------------------------------
    // Frame Recoil Shield & Side Plate
    val frameSidePath = Path().apply {
        moveTo(146f * scale, 42f * scale)
        lineTo(238f * scale, 42f * scale)
        cubicTo(245f * scale, 48f * scale, 248f * scale, 68f * scale, 242f * scale, 88f * scale)
        lineTo(228f * scale, 90f * scale)
        lineTo(146f * scale, 90f * scale)
        close()
    }
    drawPath(frameSidePath, steelSide)
    drawPath(frameSidePath, lineOutline, style = borderStroke)

    // Frame Top Strap Facet (3D horizontal roof plane)
    val topStrapFacet = Path().apply {
        moveTo(146f * scale, 42f * scale)
        lineTo(238f * scale, 42f * scale)
        lineTo(235f * scale, 37f * scale)
        lineTo(146f * scale, 37f * scale)
        close()
    }
    drawPath(topStrapFacet, steelTop)
    drawPath(topStrapFacet, lineOutline, style = innerStroke)

    // Rear Sight Notch (3D sight groove)
    drawRect(
        color = steelDeepBore,
        topLeft = Offset(230f * scale, 36f * scale),
        size = Size(6f * scale, 3f * scale)
    )

    // ----------------------------------------------------
    // 5. 3D Cylinder Drum (Isometric 6-Chamber Wheel)
    // ----------------------------------------------------
    val cylX = 144f * scale
    val cylY = 44f * scale
    val cylW = 76f * scale
    val cylH = 43f * scale

    // Cylinder Main Side Face
    drawRect(
        color = steelSide,
        topLeft = Offset(cylX, cylY),
        size = Size(cylW, cylH)
    )
    drawRect(
        color = lineOutline,
        topLeft = Offset(cylX, cylY),
        size = Size(cylW, cylH),
        style = borderStroke
    )

    // 3D Cylinder Top Facet (Cylindrical curvature bevel)
    val cylTopFacet = Path().apply {
        moveTo(cylX, cylY)
        lineTo(cylX + cylW, cylY)
        lineTo(cylX + cylW - 4f * scale, cylY - 4f * scale)
        lineTo(cylX - 4f * scale, cylY - 4f * scale)
        close()
    }
    drawPath(cylTopFacet, steelTop)
    drawPath(cylTopFacet, lineOutline, style = innerStroke)

    // Dynamic 3D Cylinder Flutes (Notches that revolve with cylinderAngle)
    val radAngle = (cylinderAngle * PI / 180.0)
    for (f in 0 until 6) {
        val flutePhase = radAngle + f * (2.0 * PI / 6.0)
        val sinVal = sin(flutePhase).toFloat()
        val cosVal = cos(flutePhase).toFloat()

        // Only flutes on the front-facing hemisphere are visible
        if (cosVal > -0.2f) {
            val yPos = cylY + cylH / 2f + sinVal * (cylH * 0.42f)
            val fluteColor = if (sinVal > 0) steelBottom else steelTop
            drawRect(
                color = fluteColor,
                topLeft = Offset(cylX + 8f * scale, yPos - 1.8f * scale),
                size = Size(cylW - 16f * scale, 3.6f * scale)
            )
            drawLine(
                color = lineOutline,
                start = Offset(cylX + 8f * scale, yPos),
                end = Offset(cylX + cylW - 8f * scale, yPos),
                strokeWidth = 1f * scale
            )
        }
    }

    // Cylinder Front Face (3D Oval Face with 6 Visible Chamber Bores)
    val faceCenter = Offset(cylX + 2f * scale, cylY + cylH / 2f)
    drawOval(
        color = steelSide,
        topLeft = Offset(cylX - 7f * scale, cylY - 2f * scale),
        size = Size(14f * scale, cylH + 4f * scale)
    )
    drawOval(
        color = lineOutline,
        topLeft = Offset(cylX - 7f * scale, cylY - 2f * scale),
        size = Size(14f * scale, cylH + 4f * scale),
        style = innerStroke
    )

    // 6 Chamber Bores revolving on the front face
    val chamberRadiusX = 4.2f * scale
    val chamberRadiusY = 16f * scale
    for (c in 0 until 6) {
        val chamberRad = (cylinderAngle + c * 60f) * PI / 180.0
        val cx = faceCenter.x + (chamberRadiusX * cos(chamberRad)).toFloat()
        val cy = faceCenter.y + (chamberRadiusY * sin(chamberRad)).toFloat()

        // Bore hole ellipse
        drawOval(
            color = steelDeepBore,
            topLeft = Offset(cx - 2.8f * scale, cy - 3.2f * scale),
            size = Size(5.6f * scale, 6.4f * scale)
        )
        drawOval(
            color = lineOutline,
            topLeft = Offset(cx - 2.8f * scale, cy - 3.2f * scale),
            size = Size(5.6f * scale, 6.4f * scale),
            style = Stroke(width = 0.8f * scale)
        )
    }

    // ----------------------------------------------------
    // 6. 3D Magnum Heavy Barrel (With Ventilated Rib & Underlug)
    // ----------------------------------------------------
    val barrelLen = 140f * scale
    val barrelTopY = 40f * scale
    val barrelHeight = 22f * scale
    val barrelStartX = cylX - 5f * scale
    val muzzleX = barrelStartX - barrelLen

    // Main Barrel Side Facet
    drawRect(
        color = steelSide,
        topLeft = Offset(muzzleX, barrelTopY),
        size = Size(barrelLen, barrelHeight)
    )
    drawRect(
        color = lineOutline,
        topLeft = Offset(muzzleX, barrelTopY),
        size = Size(barrelLen, barrelHeight),
        style = borderStroke
    )

    // Barrel 3D Top Facet (Upper angled plane)
    val barrelTopFacet = Path().apply {
        moveTo(muzzleX, barrelTopY)
        lineTo(barrelStartX, barrelTopY)
        lineTo(barrelStartX - 4f * scale, barrelTopY - 4f * scale)
        lineTo(muzzleX - 4f * scale, barrelTopY - 4f * scale)
        close()
    }
    drawPath(barrelTopFacet, steelTop)
    drawPath(barrelTopFacet, lineOutline, style = innerStroke)

    // Ventilated Rib Cutouts along barrel top (3D slots)
    for (s in 0..3) {
        val slotX = muzzleX + 22f * scale + s * 28f * scale
        drawRect(
            color = steelBottom,
            topLeft = Offset(slotX, barrelTopY - 3f * scale),
            size = Size(18f * scale, 2.2f * scale)
        )
    }

    // Full Magnum Underlug (Solid side & underside)
    drawRect(
        color = steelBottom,
        topLeft = Offset(muzzleX + 10f * scale, barrelTopY + barrelHeight),
        size = Size(barrelLen - 10f * scale, 14f * scale)
    )
    drawRect(
        color = lineOutline,
        topLeft = Offset(muzzleX + 10f * scale, barrelTopY + barrelHeight),
        size = Size(barrelLen - 10f * scale, 14f * scale),
        style = innerStroke
    )

    // Front Sight Ramp (3D Target Sight)
    val sightSide = Path().apply {
        moveTo(muzzleX + 3f * scale, barrelTopY - 4f * scale)
        lineTo(muzzleX + 20f * scale, barrelTopY - 4f * scale)
        lineTo(muzzleX + 15f * scale, barrelTopY - 16f * scale)
        lineTo(muzzleX + 3f * scale, barrelTopY - 16f * scale)
        close()
    }
    drawPath(sightSide, steelSide)
    drawPath(sightSide, lineOutline, style = innerStroke)

    // Red Target Ramp Insert
    drawRect(
        color = Color(0xFFFF3D00),
        topLeft = Offset(muzzleX + 4f * scale, barrelTopY - 14f * scale),
        size = Size(9f * scale, 3f * scale)
    )

    // 3D Muzzle Crown & Bore (Front perspective ellipse)
    drawOval(
        color = steelSide,
        topLeft = Offset(muzzleX - 5f * scale, barrelTopY - 4f * scale),
        size = Size(8f * scale, barrelHeight + 4f * scale)
    )
    drawOval(
        color = lineOutline,
        topLeft = Offset(muzzleX - 5f * scale, barrelTopY - 4f * scale),
        size = Size(8f * scale, barrelHeight + 4f * scale),
        style = innerStroke
    )

    // Deep Muzzle Caliber Bore
    drawOval(
        color = steelDeepBore,
        topLeft = Offset(muzzleX - 4f * scale, barrelTopY + 2f * scale),
        size = Size(6f * scale, barrelHeight - 8f * scale)
    )

    // ----------------------------------------------------
    // 7. 3D Geometric Muzzle Flash Burst (When BANG! occurs)
    // ----------------------------------------------------
    if (muzzleFlash) {
        val flashX = muzzleX - 25f * scale
        val flashY = barrelTopY + barrelHeight / 2f

        // Outer Sharp Geometric Starburst
        val outerStar = Path().apply {
            moveTo(muzzleX, flashY)
            lineTo(flashX - 70f * scale, flashY - 36f * scale)
            lineTo(flashX - 45f * scale, flashY - 12f * scale)
            lineTo(flashX - 90f * scale, flashY)
            lineTo(flashX - 45f * scale, flashY + 12f * scale)
            lineTo(flashX - 70f * scale, flashY + 36f * scale)
            close()
        }
        drawPath(outerStar, Color(0xFFFFD600))
        drawPath(outerStar, Color(0xFFFF6D00), style = Stroke(width = 2.5f * scale))

        // Inner White-Hot Core Spike
        val innerCore = Path().apply {
            moveTo(muzzleX, flashY)
            lineTo(flashX - 42f * scale, flashY - 15f * scale)
            lineTo(flashX - 65f * scale, flashY)
            lineTo(flashX - 42f * scale, flashY + 15f * scale)
            close()
        }
        drawPath(innerCore, Color.White)
    }
}

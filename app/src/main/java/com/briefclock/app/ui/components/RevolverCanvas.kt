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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.briefclock.app.R
import kotlin.math.*

/**
 * Authentic 2D Revolver Canvas Component.
 * Based on open-source classic revolver vector from Wikimedia Commons (CC BY-SA 3.0 / F l a n k e r).
 * Anatomically complete revolver:
 * - 6-chamber cylinder drum with flutes, swing yoke crane, and ejector rod.
 * - Thumb-cocking hammer, curved silver trigger, and textured grip handle.
 * - Absolutely NO slide (绝无套筒), NO box magazine (绝无弹匣).
 * - Upward muzzle-flip recoil, moving cylinder flutes on spin, and muzzle flash burst.
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
    val gunPainter = painterResource(R.drawable.revolver_authentic)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(230.dp)
    ) {
        val w = size.width
        val h = size.height

        // Gun image native aspect ratio: 1200 x 812
        val imgW = 1200f
        val imgH = 812f
        val scale = min((w * 0.94f) / imgW, (h * 0.94f) / imgH)
        val renderW = imgW * scale
        val renderH = imgH * scale

        // Center on screen
        val startX = (w - renderW) / 2f
        val startY = (h - renderH) / 2f

        // Recoil transform: Pivoting around the shooter's palm/grip handle (right side)
        // Upward muzzle flip (muzzle rises UPWARD) + backward kickback into hand
        val gripPivotX = startX + renderW * 0.82f
        val gripPivotY = startY + renderH * 0.65f

        val recoilRot = -recoilAmount * 16f // Clockwise rotation around right grip flips LEFT muzzle UPWARD!
        val recoilTx = recoilAmount * 22f   // Kickback into hand
        val recoilTy = -recoilAmount * 10f  // Upward lift

        translate(left = recoilTx, top = recoilTy) {
            rotate(degrees = recoilRot, pivot = Offset(gripPivotX, gripPivotY)) {
                // 1. Draw the authentic revolver gun
                translate(left = startX, top = startY) {
                    with(gunPainter) {
                        draw(size = Size(renderW, renderH))
                    }

                    // 2. Dynamic Cylinder Spin Flute Overlay
                    if (cylinderAngle % 360f != 0f) {
                        val cylLeft = renderW * (510f / 1200f)
                        val cylTop = renderH * (145f / 812f)
                        val cylW = renderW * (230f / 1200f)
                        val cylH = renderH * (205f / 812f)

                        // Draw moving cylindrical flutes reflection as cylinder turns
                        val rad = cylinderAngle * (PI.toFloat() / 180f)
                        for (i in 0 until 4) {
                            val phase = rad + i * (PI.toFloat() / 2f)
                            val sinP = sin(phase)
                            val cosP = cos(phase)
                            if (cosP > 0f) {
                                val fluteY = cylTop + cylH / 2f + sinP * (cylH * 0.38f)
                                drawRoundRect(
                                    color = Color.White.copy(alpha = 0.28f * cosP),
                                    topLeft = Offset(cylLeft + 12f * scale, fluteY - 4f * scale),
                                    size = Size(cylW - 24f * scale, 8f * scale),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f * scale)
                                )
                            }
                        }
                    }

                    // 3. Muzzle Flash Burst at the Leftmost Muzzle Tip (x=62, y=137 in 1200x812)
                    if (muzzleFlash) {
                        val muzzleX = 62f * scale
                        val muzzleY = 137f * scale

                        // Large multi-spiked explosive starburst erupting to the left
                        val outerFlame = Path().apply {
                            moveTo(muzzleX, muzzleY)
                            lineTo(muzzleX - 95f * scale, muzzleY - 48f * scale)
                            lineTo(muzzleX - 60f * scale, muzzleY - 18f * scale)
                            lineTo(muzzleX - 130f * scale, muzzleY)
                            lineTo(muzzleX - 60f * scale, muzzleY + 18f * scale)
                            lineTo(muzzleX - 95f * scale, muzzleY + 48f * scale)
                            close()
                        }
                        drawPath(outerFlame, Color(0xFFFFD600))
                        drawPath(outerFlame, Color(0xFFFF3D00), style = Stroke(width = 4f * scale))

                        // Inner white-hot blazing core
                        val innerFlame = Path().apply {
                            moveTo(muzzleX, muzzleY)
                            lineTo(muzzleX - 55f * scale, muzzleY - 22f * scale)
                            lineTo(muzzleX - 90f * scale, muzzleY)
                            lineTo(muzzleX - 55f * scale, muzzleY + 22f * scale)
                            close()
                        }
                        drawPath(innerFlame, Color.White)
                    }
                }
            }
        }
    }
}

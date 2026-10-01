package com.briefclock.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.briefclock.app.R
import kotlin.math.*

/**
 * Authentic 2D Revolver Canvas Component.
 * 100% minimalist solid flat colors (no slide, no box magazine).
 * Anatomically complete with individual moving parts:
 * - Movable cocking hammer (pivots back when cocked).
 * - Movable curved silver trigger (concave front welcomes finger, pulls back toward grip when fired).
 * - Dynamic rotating cylinder flutes on a 100% smooth base drum.
 * - Upward muzzle-flip recoil and leftmost muzzle flash starburst.
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
    val framePainter = painterResource(R.drawable.rev_layer_frame)
    val hammerPainter = painterResource(R.drawable.rev_layer_hammer)
    val triggerPainter = painterResource(R.drawable.rev_layer_trigger)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(230.dp)
    ) {
        val w = size.width
        val h = size.height

        // Native coordinate frame: 1200 x 750
        val imgW = 1200f
        val imgH = 750f
        val scale = min((w * 0.94f) / imgW, (h * 0.94f) / imgH)
        val renderW = imgW * scale
        val renderH = imgH * scale

        // Center on screen
        val startX = (w - renderW) / 2f
        val startY = (h - renderH) / 2f

        // Recoil transform: Pivoting around the shooter's palm/grip handle (right side)
        // Upward muzzle flip (muzzle rises UPWARD) + backward kickback into hand
        val gripPivotX = startX + 930f * scale
        val gripPivotY = startY + 480f * scale

        val recoilRot = recoilAmount * 15f // Clockwise rotation around right grip flips LEFT muzzle UPWARD!
        val recoilTx = recoilAmount * 24f  // Kickback into hand
        val recoilTy = -recoilAmount * 12f // Upward lift

        translate(left = recoilTx, top = recoilTy) {
            rotate(degrees = recoilRot, pivot = Offset(gripPivotX, gripPivotY)) {
                translate(left = startX, top = startY) {
                    val layerSize = Size(renderW, renderH)

                    // 1. Hammer Layer (Pivoting at base x=808*scale, y=194*scale)
                    val hammerPivotX = 808f * scale
                    val hammerPivotY = 194f * scale
                    val hammerAngle = if (hammerCocked) 22f else 0f
                    rotate(degrees = hammerAngle, pivot = Offset(hammerPivotX, hammerPivotY)) {
                        with(hammerPainter) {
                            draw(size = layerSize)
                        }
                    }

                    // 2. Trigger Layer (Pivoting at top x=580*scale, y=275*scale, pulls back towards grip)
                    // Note: Rotating counter-clockwise (-16f) pulls trigger tip towards the grip (to the right)
                    val triggerPivotX = 580f * scale
                    val triggerPivotY = 275f * scale
                    val triggerAngle = if (triggerPulled) -16f else 0f
                    rotate(degrees = triggerAngle, pivot = Offset(triggerPivotX, triggerPivotY)) {
                        with(triggerPainter) {
                            draw(size = layerSize)
                        }
                    }

                    // 3. Base Frame Layer (Frame, Barrel, Ejector Rod, Crane, Smooth Cylinder Base, Grip)
                    // Drawn on top of hammer and trigger hubs so seams are hidden inside the frame
                    with(framePainter) {
                        draw(size = layerSize)
                    }

                    // 4. Cylinder Flutes Layer (Dynamic 6 flutes rolling across the smooth cylinder drum)
                    val cylX1 = 524f * scale
                    val cylX2 = 672f * scale
                    val cylCenterY = 180f * scale
                    val cylRy = 76f * scale
                    val rad = cylinderAngle * (PI.toFloat() / 180f)

                    for (i in 0 until 6) {
                        val phase = rad + i * (2f * PI.toFloat() / 6f)
                        val sinP = sin(phase)
                        val cosP = cos(phase)

                        if (cosP > -0.15f) {
                            val fluteY = cylCenterY + sinP * cylRy
                            val halfH = max(2f * scale, 11f * scale * cosP)
                            val alphaDark = 0.86f * min(1f, cosP + 0.35f)
                            val alphaLight = 0.55f * min(1f, cosP + 0.35f)

                            // Recessed groove shadow
                            drawRoundRect(
                                color = Color(0xFF111827).copy(alpha = alphaDark),
                                topLeft = Offset(cylX1 + 6f * scale, fluteY - halfH),
                                size = Size(cylX2 - cylX1 - 12f * scale, halfH * 2f),
                                cornerRadius = CornerRadius(halfH, halfH)
                            )

                            // Bevel specular highlight inside groove
                            if (halfH >= 4f * scale) {
                                drawLine(
                                    color = Color.White.copy(alpha = alphaLight),
                                    start = Offset(cylX1 + 14f * scale, fluteY - halfH + 2f * scale),
                                    end = Offset(cylX2 - 14f * scale, fluteY - halfH + 2f * scale),
                                    strokeWidth = 2f * scale
                                )
                            }
                        }
                    }

                    // 5. Muzzle Flash Burst at the Leftmost Muzzle Tip (x=99*scale, y=130*scale)
                    if (muzzleFlash) {
                        val muzzleX = 99f * scale
                        val muzzleY = 130f * scale

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

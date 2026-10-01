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
 * Based on classic revolver vector from Wikimedia Commons (CC BY-SA 3.0 / F l a n k e r).
 * 100% minimalist solid flat colors (no slide, no box magazine).
 * Anatomically complete with individual moving parts:
 * - Movable cocking hammer (spurs back when cocked).
 * - Movable curved silver trigger (pulls back toward grip when fired).
 * - Dynamic rotating cylinder flutes on a smooth base drum.
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
    val flutesPainter = painterResource(R.drawable.rev_layer_flutes)

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
        val gripPivotX = startX + 880f * scale
        val gripPivotY = startY + 480f * scale

        val recoilRot = recoilAmount * 16f // Clockwise rotation around right grip flips LEFT muzzle UPWARD!
        val recoilTx = recoilAmount * 24f  // Kickback into hand
        val recoilTy = -recoilAmount * 12f // Upward lift

        translate(left = recoilTx, top = recoilTy) {
            rotate(degrees = recoilRot, pivot = Offset(gripPivotX, gripPivotY)) {
                translate(left = startX, top = startY) {
                    val layerSize = Size(renderW, renderH)

                    // 1. Hammer Layer (Pivoting at base x=760*scale, y=240*scale)
                    val hammerPivotX = 760f * scale
                    val hammerPivotY = 240f * scale
                    val hammerAngle = if (hammerCocked) 22f else 0f
                    rotate(degrees = hammerAngle, pivot = Offset(hammerPivotX, hammerPivotY)) {
                        with(hammerPainter) {
                            draw(size = layerSize)
                        }
                    }

                    // 2. Base Frame Layer (Frame, Barrel, Ejector Rod, Crane, Smooth Cylinder Base, Grip)
                    with(framePainter) {
                        draw(size = layerSize)
                    }

                    // 3. Trigger Layer (Pivoting at top x=640*scale, y=300*scale, pulls back towards grip)
                    val triggerPivotX = 640f * scale
                    val triggerPivotY = 300f * scale
                    val triggerAngle = if (triggerPulled) 18f else 0f
                    rotate(degrees = triggerAngle, pivot = Offset(triggerPivotX, triggerPivotY)) {
                        with(triggerPainter) {
                            draw(size = layerSize)
                        }
                    }

                    // 4. Cylinder Flutes Layer (The cut-out grooves that roll across the smooth cylinder drum)
                    val rad = cylinderAngle * (PI.toFloat() / 180f)
                    val fluteOffsetY = sin(rad) * 12f * scale
                    translate(left = 0f, top = fluteOffsetY) {
                        with(flutesPainter) {
                            draw(size = layerSize)
                        }
                    }

                    // 5. Muzzle Flash Burst at the Leftmost Muzzle Tip (x=98*scale, y=115*scale)
                    if (muzzleFlash) {
                        val muzzleX = 98f * scale
                        val muzzleY = 115f * scale

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

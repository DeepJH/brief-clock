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
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.dp
import kotlin.math.*

/**
 * Interactive 2D Vector Revolver Canvas.
 * Based on open-source Twemoji classic revolver vector (CC-BY 4.0 / MIT).
 * Features 2D vector drawing, cylinder spinning animation, cocking hammer,
 * trigger pull, recoil kickback, and crisp muzzle flash.
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
            .height(220.dp)
    ) {
        val w = size.width
        val h = size.height
        val cx = w * 0.50f
        val cy = h * 0.50f

        // Recoil transform: translation backward and upward muzzle tilt
        val recoilTx = -recoilAmount * 34f
        val recoilTy = -recoilAmount * 18f
        val recoilRot = -recoilAmount * 14f

        translate(left = recoilTx, top = recoilTy) {
            rotate(degrees = recoilRot, pivot = Offset(cx + 80f, cy + 60f)) {
                // Scale factor from Twemoji 36x36 coordinate space to Canvas size
                val s = min(w / 42f, h / 38f) * 0.96f

                translate(left = cx - 18f * s, top = cy - 18f * s) {
                    scale(scale = s, pivot = Offset.Zero) {
                        draw2DEmojiRevolver(
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
}

private fun DrawScope.draw2DEmojiRevolver(
    cylinderAngle: Float,
    hammerCocked: Boolean,
    triggerPulled: Boolean,
    muzzleFlash: Boolean
) {
    // Colors from Twemoji vector
    val colorSteelGrey = Color(0xFF999999)
    val colorSteelDark = Color(0xFF666666)
    val colorBodyCharcoal = Color(0xFF4C4C4C)
    val colorWoodBrown = Color(0xFF8B4513)
    val colorMetalBlack = Color(0xFF333333)
    val colorHammerGrey = Color(0xFF7F7F7F)
    val colorBrassGold = Color(0xFFFFCC4D)

    // 1. Hammer (Cocks back when cocked)
    val hammerPivot = Offset(32.9268f, 8.32343f)
    val hammerExtraAngle = if (hammerCocked) 32f else 0f
    rotate(degrees = -130.451f + hammerExtraAngle, pivot = hammerPivot) {
        val hammerPath = Path().apply {
            moveTo(31.92599f, 5.18198f)
            cubicTo(31.55425f, 5.03969f, 34.11429f, 8.27561f, 32.00131f, 7.82486f)
            cubicTo(29.88833f, 7.37411f, 33.71103f, 9.17402f, 32.68429f, 9.68663f)
            cubicTo(31.65755f, 10.19924f, 34.36903f, 13.62828f, 34.46945f, 9.33733f)
            cubicTo(34.56987f, 5.04638f, 34.53608f, 6.61744f, 32.52436f, 5.33934f)
            cubicTo(32.35912f, 5.23428f, 32.29772f, 5.32353f, 31.92598f, 5.18123f)
            close()
        }
        drawPath(hammerPath, colorHammerGrey)
    }

    // 2. Trigger Guard & Trigger
    // Trigger shift when pulled
    val triggerOffset = if (triggerPulled) 1.5f else 0f
    translate(left = triggerOffset, top = 0f) {
        val triggerPath = Path().apply {
            moveTo(25.11874f, 20.07974f)
            lineTo(18.80449f, 20.07974f)
            cubicTo(17.42529f, 20.07974f, 16.40323f, 19.34881f, 15.99971f, 18.07531f)
            lineTo(14.22877f, 13.12164f)
            lineTo(15.78543f, 12.54304f)
            lineTo(17.56667f, 17.52673f)
            cubicTo(17.75445f, 18.11809f, 18.15576f, 18.39049f, 18.80449f, 18.39049f)
            lineTo(25.11874f, 18.39049f)
            close()
        }
        drawPath(triggerPath, colorMetalBlack)
    }

    // Trigger Guard Frame
    val guardPath = Path().apply {
        moveTo(21.42017f, 12.65799f)
        lineTo(23.22778f, 12.11494f)
        lineTo(28.49343f, 25.4015f)
        cubicTo(27.49681f, 24.77636f, 21.53469f, 28.18199f, 25.50356f, 23.52608f)
        lineTo(21.42017f, 12.65799f)
        close()
    }
    drawPath(guardPath, colorMetalBlack)

    // 3. Main Revolver Body Frame (Charcoal Grey)
    val bodyPath = Path().apply {
        moveTo(35.279f, 29.37935f)
        cubicTo(35.279f, 29.37935f, 33.304f, 23.22535f, 32.548f, 20.61135f)
        cubicTo(31.792f, 17.99735f, 31.542f, 14.76535f, 33.625f, 13.23235f)
        cubicTo(34.76f, 12.39735f, 35.056f, 11.38835f, 34.829f, 10.33335f)
        cubicTo(34.452f, 8.58135f, 33.545f, 6.25735f, 33.109f, 5.40835f)
        cubicTo(32.92f, 5.03935f, 32.623f, 4.65035f, 32.036f, 4.48035f)
        cubicTo(32.036f, 4.48035f, 31.24515f, 4.4577f, 29.03868f, 4.43504f)
        cubicTo(28.48707f, 4.42937f, 22.44f, 4.25747f, 20.3795f, 4.25217f)
        cubicTo(18.31899f, 4.24686f, 19.79428f, 5.40561f, 18.84971f, 5.40136f)
        cubicTo(17.90513f, 5.39711f, 17.24922f, 5.38352f, 16.07888f, 5.38104f)
        cubicTo(14.90854f, 5.37857f, 16.18568f, 4.27762f, 14.77006f, 4.27762f)
        cubicTo(8.43706f, 4.27762f, 2.58067f, 3.70035f, 2.58067f, 6.00835f)
        lineTo(2.58067f, 9.62035f)
        cubicTo(2.58067f, 10.85035f, 3.57767f, 11.84735f, 4.80767f, 11.84735f)
        lineTo(18.50967f, 11.84735f)
        cubicTo(19.23667f, 11.84735f, 19.87967f, 12.32035f, 20.09667f, 13.01535f)
        lineTo(24.44867f, 26.80535f)
        cubicTo(24.87767f, 28.20735f, 23.53367f, 28.43835f, 23.53367f, 29.59235f)
        cubicTo(23.53367f, 30.30635f, 24.11767f, 30.88935f, 24.83067f, 30.88935f)
        lineTo(33.26367f, 30.88935f)
        cubicTo(33.97617f, 30.88935f, 34.58642f, 30.5436f, 34.9653f, 30.02785f)
        cubicTo(35.34417f, 29.5121f, 35.49167f, 28.82635f, 35.27867f, 28.14635f)
        close()
    }
    drawPath(bodyPath, colorBodyCharcoal)

    // 4. Walnut Wood Grip Plate
    val gripWoodPath = Path().apply {
        moveTo(21.55523f, 13.23653f)
        cubicTo(25.64907f, 13.1396f, 28.3105f, 12.21766f, 29.232f, 16.21722f)
        lineTo(32.492f, 27.45654f)
        cubicTo(31.84512f, 30.12079f, 30.92623f, 30.51257f, 26.9246f, 30.4326f)
        lineTo(21.55523f, 13.23653f)
        close()
    }
    drawPath(gripWoodPath, colorWoodBrown)

    // Grip Screws & Medallions
    drawCircle(color = colorBrassGold, radius = 0.535f, center = Offset(26.04441f, 15.06287f))
    drawCircle(color = colorBrassGold, radius = 0.535f, center = Offset(29.65937f, 29.69087f))
    drawOval(color = colorMetalBlack, topLeft = Offset(25.008f, 14.75f), size = Size(1.21f, 1.25f))
    drawOval(color = colorMetalBlack, topLeft = Offset(28.634f, 27.504f), size = Size(1.21f, 1.25f))

    // 5. Barrel & Underlug Details
    val barrelTopPath = Path().apply {
        moveTo(4.40613f, 5.80179f)
        lineTo(30.20511f, 5.80179f)
        cubicTo(30.67266f, 6.4418f, 31.14021f, 7.24683f, 31.23646f, 7.84558f)
        lineTo(7.94222f, 7.73195f)
        cubicTo(6.66334f, 7.65259f, 4.45245f, 8.10225f, 4.40613f, 6.65867f)
        close()
    }
    drawPath(barrelTopPath, colorSteelGrey)

    val barrelTip = Path().apply {
        moveTo(3.43311f, 6.80542f)
        lineTo(5.19411f, 6.80542f)
        lineTo(5.19411f, 9.40105f)
        lineTo(3.99841f, 10.03633f)
        cubicTo(3.12841f, 10.03633f, 2.41641f, 10.11691f, 2.08607f, 8.65845f)
        lineTo(1.85011f, 7.21169f)
        cubicTo(1.85011f, 6.76221f, 2.75088f, 7.10438f, 3.43311f, 6.80542f)
        close()
    }
    drawPath(barrelTip, colorSteelDark)

    drawOval(color = colorSteelGrey, topLeft = Offset(2.238f, 4.87f), size = Size(3.34f, 3.34f))

    // Underlug Bar
    val underlugPath = Path().apply {
        moveTo(4.869f, 12.94079f)
        cubicTo(4.869f, 14.10679f, 5.814f, 15.05179f, 7.528f, 15.05179f)
        lineTo(22.0f, 15.05179f)
        lineTo(22.0f, 12.94079f)
        close()
    }
    drawPath(underlugPath, colorSteelDark)

    // Thin barrel rib
    val barrelRib = Path().apply {
        moveTo(2.82089f, 10.41091f)
        lineTo(26.1283f, 10.41091f)
        cubicTo(26.5507f, 10.63018f, 26.97309f, 10.90599f, 27.06005f, 11.11113f)
        lineTo(6.01547f, 11.0722f)
        cubicTo(4.8601f, 11.04501f, 2.86273f, 11.19907f, 2.82088f, 10.70448f)
        close()
    }
    drawPath(barrelRib, colorSteelDark)

    // 6. Cylinder (With Dynamic Angle Spin Rotation)
    val cylPivot = Offset(20.3787f, 16.7921f)
    rotate(degrees = -6.42653f + cylinderAngle, pivot = cylPivot) {
        val cylChambersPath = Path().apply {
            moveTo(19.69705f, 14.11142f)
            cubicTo(19.46291f, 14.11142f, 19.22876f, 14.16425f, 19.05012f, 14.26888f)
            cubicTo(18.69283f, 14.47815f, 18.69283f, 14.81746f, 19.05012f, 15.02674f)
            cubicTo(20.51569f, 15.88415f, 20.51569f, 17.69955f, 19.05012f, 18.55798f)
            cubicTo(18.69283f, 18.76725f, 18.69283f, 19.10656f, 19.05012f, 19.31584f)
            cubicTo(19.40741f, 19.52511f, 19.9867f, 19.52511f, 20.34398f, 19.31584f)
            cubicTo(22.51892f, 18.04293f, 22.51892f, 15.5418f, 20.34398f, 14.26889f)
            cubicTo(20.16534f, 14.16425f, 19.93119f, 14.11142f, 19.69705f, 14.11142f)
            close()
        }
        drawPath(cylChambersPath, colorSteelGrey)
    }

    // 7. Muzzle Flash Burst (When BANG! occurs)
    if (muzzleFlash) {
        val muzzleTipX = 1.6f
        val muzzleTipY = 7.5f

        val flashPath = Path().apply {
            moveTo(muzzleTipX, muzzleTipY)
            lineTo(muzzleTipX - 9f, muzzleTipY - 5f)
            lineTo(muzzleTipX - 5.5f, muzzleTipY - 1.5f)
            lineTo(muzzleTipX - 12f, muzzleTipY)
            lineTo(muzzleTipX - 5.5f, muzzleTipY + 1.5f)
            lineTo(muzzleTipX - 9f, muzzleTipY + 5f)
            close()
        }
        drawPath(flashPath, Color(0xFFFFD600))
        drawPath(flashPath, Color(0xFFFF6D00), style = Stroke(width = 0.5f))

        val innerCore = Path().apply {
            moveTo(muzzleTipX, muzzleTipY)
            lineTo(muzzleTipX - 5f, muzzleTipY - 2f)
            lineTo(muzzleTipX - 7.5f, muzzleTipY)
            lineTo(muzzleTipX - 5f, muzzleTipY + 2f)
            close()
        }
        drawPath(innerCore, Color.White)
    }
}

package com.briefclock.app.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object AppIcons {
    val Mic: ImageVector by lazy {
        ImageVector.Builder(
            name = "Mic",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(12f, 14f)
                curveTo(13.66f, 14f, 15f, 12.66f, 15f, 11f)
                lineTo(15f, 5f)
                curveTo(15f, 3.34f, 13.66f, 2f, 12f, 2f)
                curveTo(10.34f, 2f, 9f, 3.34f, 9f, 5f)
                lineTo(9f, 11f)
                curveTo(9f, 12.66f, 10.34f, 14f, 12f, 14f)
                close()
            }
            path(
                fill = null,
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(19f, 10f)
                curveTo(19f, 13.87f, 15.87f, 17f, 12f, 17f)
                curveTo(8.13f, 17f, 5f, 13.87f, 5f, 10f)
            }
            path(
                fill = null,
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(12f, 17f)
                lineTo(12f, 21f)
                moveTo(8f, 21f)
                lineTo(16f, 21f)
            }
        }.build()
    }

    val Revolver: ImageVector by lazy {
        ImageVector.Builder(
            name = "Revolver",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(2f, 8f)
                lineTo(9f, 8f)
                lineTo(9f, 11.5f)
                lineTo(2f, 11.5f)
                close()

                moveTo(9f, 7f)
                lineTo(15f, 7f)
                lineTo(15f, 13f)
                lineTo(9f, 13f)
                close()

                moveTo(15f, 8f)
                lineTo(19f, 8f)
                lineTo(21.5f, 16f)
                lineTo(18f, 19.5f)
                lineTo(14.5f, 14f)
                lineTo(15f, 10.5f)
                close()
            }
        }.build()
    }

    val BarChart: ImageVector by lazy {
        ImageVector.Builder(
            name = "BarChart",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(4f, 13f)
                lineTo(8f, 13f)
                lineTo(8f, 20f)
                lineTo(4f, 20f)
                close()

                moveTo(10f, 7f)
                lineTo(14f, 7f)
                lineTo(14f, 20f)
                lineTo(10f, 20f)
                close()

                moveTo(16f, 10f)
                lineTo(20f, 10f)
                lineTo(20f, 20f)
                lineTo(16f, 20f)
                close()
            }
        }.build()
    }
}

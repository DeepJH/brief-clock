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
                moveTo(2f, 9f)
                lineTo(3f, 7f)
                lineTo(4f, 7f)
                lineTo(4f, 9f)
                lineTo(9f, 9f)
                lineTo(9f, 7.5f)
                curveTo(9f, 7f, 9.5f, 7f, 10f, 7f)
                lineTo(15f, 7f)
                curveTo(15.5f, 7f, 16f, 7.5f, 16f, 8f)
                lineTo(17f, 6.5f)
                lineTo(18.5f, 7.5f)
                lineTo(17.5f, 9f)
                curveTo(18f, 9.5f, 18f, 10f, 17.5f, 10.5f)
                lineTo(19.5f, 15.5f)
                curveTo(20.5f, 18f, 19f, 20.5f, 16.5f, 20.5f)
                curveTo(15f, 20.5f, 14f, 19.5f, 14f, 18.5f)
                lineTo(14.5f, 15f)
                curveTo(14.5f, 15f, 13.5f, 16.5f, 11.5f, 16.5f)
                curveTo(9.8f, 16.5f, 9f, 15f, 9f, 13.5f)
                lineTo(2f, 13.5f)
                lineTo(2f, 12.5f)
                lineTo(3.5f, 12.5f)
                lineTo(3.5f, 11f)
                lineTo(2f, 11f)
                close()

                moveTo(10f, 13.5f)
                curveTo(10f, 14.8f, 10.8f, 15.3f, 11.8f, 15.3f)
                curveTo(12.8f, 15.3f, 13.5f, 14.2f, 13.5f, 13.5f)
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

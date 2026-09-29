package com.briefclock.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.*

/**
 * Real-time 3D Unshaded Revolver Canvas.
 * Renders an open-source low-poly 3D mesh (CC0 1.0 Universal - Modbder/ThaumicBases)
 * in 3/4 axonometric perspective using Painter's depth sorting and faceted unshaded polygons.
 * Supports dynamic 3D cylinder rotation, hammer cocking, 3D recoil, and 3D muzzle flash.
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
    val vertexCount = Revolver3DModel.VERTEX_COUNT
    val baseVertices = Revolver3DModel.BASE_VERTICES
    val cylinderSet = remember { Revolver3DModel.CYLINDER_INDICES.toSet() }
    val hammerSet = remember { Revolver3DModel.HAMMER_INDICES.toSet() }
    val faces = Revolver3DModel.FACES

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp)
    ) {
        val w = size.width
        val h = size.height
        val cx = w * 0.48f
        val cy = h * 0.52f
        val scale = min(w / 14f, h / 7f) * 1.05f

        draw3DUnshadedRevolver(
            cx = cx,
            cy = cy,
            scale = scale,
            cylinderAngle = cylinderAngle,
            hammerCocked = hammerCocked,
            recoilAmount = recoilAmount,
            muzzleFlash = muzzleFlash,
            vertexCount = vertexCount,
            baseVertices = baseVertices,
            cylinderSet = cylinderSet,
            hammerSet = hammerSet,
            faces = faces
        )
    }
}

private class ProjectedVertex(val sx: Float, val sy: Float, val sz: Float, val normalY: Float)
private class RenderFace(val face: Revolver3DModel.Face, val avgZ: Float, val facing: Float)

private fun DrawScope.draw3DUnshadedRevolver(
    cx: Float,
    cy: Float,
    scale: Float,
    cylinderAngle: Float,
    hammerCocked: Boolean,
    recoilAmount: Float,
    muzzleFlash: Boolean,
    vertexCount: Int,
    baseVertices: FloatArray,
    cylinderSet: Set<Int>,
    hammerSet: Set<Int>,
    faces: Array<Revolver3DModel.Face>
) {
    // 3D Isometric View Angles
    val baseRotY = -76f * (PI.toFloat() / 180f) // Turn barrel towards left
    val baseRotX = 14f * (PI.toFloat() / 180f)  // Tilt down to see top details

    // Recoil dynamic transform (pitch gun up and push back)
    val recoilPitch = recoilAmount * 16f * (PI.toFloat() / 180f)
    val rotX = baseRotX + recoilPitch
    val recoilTx = -recoilAmount * 0.6f
    val recoilTy = -recoilAmount * 0.35f

    val cosY = cos(baseRotY)
    val sinY = sin(baseRotY)
    val cosX = cos(rotX)
    val sinX = sin(rotX)

    val cylRad = cylinderAngle * (PI.toFloat() / 180f)
    val cosCyl = cos(cylRad)
    val sinCyl = sin(cylRad)

    val hamRad = if (hammerCocked) 32f * (PI.toFloat() / 180f) else 0f
    val cosHam = cos(hamRad)
    val sinHam = sin(hamRad)

    // Center pivot of model
    val modelCenterX = 0.0f
    val modelCenterY = 2.1f
    val modelCenterZ = -5.1f

    val projected = arrayOfNulls<ProjectedVertex>(vertexCount)

    var muzzleScreenX = 0f
    var muzzleScreenY = 0f
    var minZVal = Float.MAX_VALUE

    for (i in 0 until vertexCount) {
        val baseIdx = i * 3
        var vx = baseVertices[baseIdx]
        var vy = baseVertices[baseIdx + 1]
        var vz = baseVertices[baseIdx + 2]

        // 1. Dynamic Cylinder Rotation around its Z axis (center at X=0, Y=1.76)
        if (i in cylinderSet) {
            val dx = vx - 0.0f
            val dy = vy - 1.76f
            vx = dx * cosCyl - dy * sinCyl
            vy = dx * sinCyl + dy * cosCyl + 1.76f
        }

        // 2. Dynamic Hammer Cocking around (X=0, Y=3.4, Z=-2.0)
        if (i in hammerSet && hammerCocked) {
            val dy = vy - 3.4f
            val dz = vz - (-2.0f)
            vy = dy * cosHam - dz * sinHam + 3.4f
            vz = dy * sinHam + dz * cosHam - 2.0f
        }

        // 3. Shift relative to model center
        val x0 = vx - modelCenterX + recoilTx
        val y0 = vy - modelCenterY + recoilTy
        val z0 = vz - modelCenterZ

        // 4. Rotate Y
        val x1 = x0 * cosY + z0 * sinY
        val y1 = y0
        val z1 = -x0 * sinY + z0 * cosY

        // 5. Rotate X
        val x2 = x1
        val y2 = y1 * cosX - z1 * sinX
        val z2 = y1 * sinX + z1 * cosX

        // 6. Project to screen
        val sx = cx + x2 * scale
        val sy = cy - y2 * scale
        projected[i] = ProjectedVertex(sx, sy, z2, y2)

        // Track muzzle tip position (most negative Z)
        if (vz < minZVal) {
            minZVal = vz
            muzzleScreenX = sx
            muzzleScreenY = sy
        }
    }

    // Depth Sorting using Painter's Algorithm
    val renderFaces = ArrayList<RenderFace>(faces.size)
    for (face in faces) {
        val indices = face.vertexIndices
        if (indices.size < 3) continue

        var sumZ = 0f
        var valid = true
        for (idx in indices) {
            val pv = projected[idx]
            if (pv == null) {
                valid = false
                break
            }
            sumZ += pv.sz
        }
        if (!valid) continue

        val avgZ = sumZ / indices.size

        // 2D Normal direction / Facing
        val p0 = projected[indices[0]]!!
        val p1 = projected[indices[1]]!!
        val p2 = projected[indices[2]]!!
        val crossZ = (p1.sx - p0.sx) * (p2.sy - p0.sy) - (p1.sy - p0.sy) * (p2.sx - p0.sx)

        renderFaces.add(RenderFace(face, avgZ, crossZ))
    }

    // Sort ascending by depth (farthest rendered first, nearest on top)
    renderFaces.sortBy { it.avgZ }

    // Color definitions for solid unshaded polygonal facets
    val edgeStroke = Stroke(width = 1.2f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val edgeColor = Color(0xFF13171F)

    val steelMain = Color(0xFF38404E)
    val steelTop = Color(0xFF5A6372)
    val steelDark = Color(0xFF222731)

    val woodMain = Color(0xFF8B4513)
    val woodTop = Color(0xFFA0522D)
    val woodDark = Color(0xFF5A2A08)

    val polygonPath = Path()

    for (rf in renderFaces) {
        val indices = rf.face.vertexIndices
        val part = rf.face.part
        val facing = rf.facing

        // Base color selection per part
        val (cTop, cSide, cBottom) = when (part) {
            Revolver3DModel.PART_GRIP -> Triple(woodTop, woodMain, woodDark)
            Revolver3DModel.PART_HAMMER -> Triple(Color(0xFF6B7280), Color(0xFF4B5563), Color(0xFF1F242F))
            Revolver3DModel.PART_CYLINDER -> Triple(Color(0xFF4C5565), Color(0xFF343B47), Color(0xFF1C2028))
            Revolver3DModel.PART_SIGHTS -> Triple(Color(0xFFFF3D00), Color(0xFFD50000), Color(0xFF900000))
            else -> Triple(steelTop, steelMain, steelDark)
        }

        // Unshaded flat facet lighting (discrete stepped shades, zero gradients)
        val faceColor = when {
            facing > 200f -> cTop
            facing < -200f -> cBottom
            else -> cSide
        }

        polygonPath.reset()
        val first = projected[indices[0]] ?: continue
        polygonPath.moveTo(first.sx, first.sy)
        for (k in 1 until indices.size) {
            val p = projected[indices[k]] ?: continue
            polygonPath.lineTo(p.sx, p.sy)
        }
        polygonPath.close()

        drawPath(polygonPath, faceColor)
        drawPath(polygonPath, edgeColor, style = edgeStroke)
    }

    // 7. 3D Muzzle Flash Burst
    if (muzzleFlash) {
        val flashPath = Path().apply {
            moveTo(muzzleScreenX, muzzleScreenY)
            lineTo(muzzleScreenX - 55f, muzzleScreenY - 28f)
            lineTo(muzzleScreenX - 35f, muzzleScreenY - 10f)
            lineTo(muzzleScreenX - 75f, muzzleScreenY)
            lineTo(muzzleScreenX - 35f, muzzleScreenY + 10f)
            lineTo(muzzleScreenX - 55f, muzzleScreenY + 28f)
            close()
        }
        drawPath(flashPath, Color(0xFFFFD600))
        drawPath(flashPath, Color(0xFFFF6D00), style = Stroke(width = 2.5f))

        val innerCore = Path().apply {
            moveTo(muzzleScreenX, muzzleScreenY)
            lineTo(muzzleScreenX - 35f, muzzleScreenY - 12f)
            lineTo(muzzleScreenX - 50f, muzzleScreenY)
            lineTo(muzzleScreenX - 35f, muzzleScreenY + 12f)
            close()
        }
        drawPath(innerCore, Color.White)
    }
}

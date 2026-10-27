package com.example.studyquest.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// ---------- Scene colours (only used in this file) ----------
private val NightTop = Color(0xFF07130F)
private val NightBottom = Color(0xFF123227)
private val MossHighlight = Color(0xFF3C7A5F)

private val MoonLight = Color(0xFFFFD166)
private val MoonDark = Color(0xFFF0B94A)

private val FarRidgeLight = Color(0xFF1B4535)
private val FarRidgeDark = Color(0xFF153A2C)
private val NearRidgeLight = Color(0xFF133324)
private val NearRidgeDark = Color(0xFF0F2A1E)

private val BackTreeLight = Color(0xFF1C4A38)
private val BackTreeDark = Color(0xFF143528)
private val FrontTreeLight = Color(0xFF245A44)
private val FrontTreeDark = Color(0xFF18402F)
private val TrunkColor = Color(0xFF0E2219)

// ---------- Scene data ----------
// All positions are stored as fractions (0..1) of the screen, so the scene
// scales to any screen size. It is generated once with a fixed seed.

private class Facet(val a: Offset, val b: Offset, val c: Offset, val shade: Float)

private class Tree(val x: Float, val baseY: Float, val height: Float, val isFront: Boolean)

private class Scene(
    val mesh: List<Facet>,
    val farRidge: List<Offset>,
    val nearRidge: List<Offset>,
    val trees: List<Tree>,        // back trees first, so they are drawn first
    val stars: List<Offset>,
    val starSizes: List<Float>
)

private fun buildScene(): Scene {
    val rnd = Random(11)   // same seed = the same forest every time

    // A grid of points, nudged randomly, then split into triangles = the low poly texture
    val cols = 6
    val rows = 12
    val grid = List(rows + 1) { r ->
        List(cols + 1) { c ->
            val jitterX = if (c == 0 || c == cols) 0f else (rnd.nextFloat() - 0.5f) * 0.7f / cols
            val jitterY = if (r == 0 || r == rows) 0f else (rnd.nextFloat() - 0.5f) * 0.7f / rows
            Offset(c / cols.toFloat() + jitterX, r / rows.toFloat() + jitterY)
        }
    }
    val mesh = ArrayList<Facet>()
    for (r in 0 until rows) {
        for (c in 0 until cols) {
            val p00 = grid[r][c]
            val p10 = grid[r][c + 1]
            val p01 = grid[r + 1][c]
            val p11 = grid[r + 1][c + 1]
            mesh.add(Facet(p00, p10, p01, rnd.nextFloat()))
            mesh.add(Facet(p10, p11, p01, rnd.nextFloat()))
        }
    }

    val farRidge = List(9) { i -> Offset(i / 8f, 0.52f + rnd.nextFloat() * 0.10f) }
    val nearRidge = List(7) { i -> Offset(i / 6f, 0.66f + rnd.nextFloat() * 0.10f) }

    val trees = ArrayList<Tree>()
    repeat(7) { i ->
        trees.add(
            Tree(
                x = (i + 0.3f + rnd.nextFloat() * 0.4f) / 7f,
                baseY = 0.90f,
                height = 0.09f + rnd.nextFloat() * 0.04f,
                isFront = false
            )
        )
    }
    repeat(5) { i ->
        trees.add(
            Tree(
                x = (i + rnd.nextFloat()) / 5f,
                baseY = 1.0f,
                height = 0.15f + rnd.nextFloat() * 0.07f,
                isFront = true
            )
        )
    }

    val stars = List(16) { Offset(rnd.nextFloat(), rnd.nextFloat() * 0.32f) }
    val starSizes = List(16) { 1f + rnd.nextFloat() * 1.8f }

    return Scene(mesh, farRidge, nearRidge, trees, stars, starSizes)
}

// ---------- The composable ----------

@Composable
fun LowPolyForestBackground(modifier: Modifier = Modifier) {
    val scene = remember { buildScene() }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        fun toPx(o: Offset) = Offset(o.x * w, o.y * h)

        // 1. Faceted background: dark at the top, greener towards the bottom
        for (f in scene.mesh) {
            val centerY = (f.a.y + f.b.y + f.c.y) / 3f
            val base = lerp(NightTop, NightBottom, centerY)
            drawFacet(
                toPx(f.a), toPx(f.b), toPx(f.c),
                lerp(base, MossHighlight, f.shade * 0.14f)
            )
        }

        // 2. Stars
        scene.stars.forEachIndexed { i, star ->
            drawCircle(
                color = Cream.copy(alpha = 0.5f),
                radius = scene.starSizes[i],
                center = toPx(star)
            )
        }

        // 3. Low poly moon (a hexagon made of 6 triangles) with a soft glow
        val moonCenter = Offset(w * 0.80f, h * 0.12f)
        val moonRadius = w * 0.07f
        drawCircle(Yellow.copy(alpha = 0.08f), radius = moonRadius * 2.4f, center = moonCenter)
        drawCircle(Yellow.copy(alpha = 0.10f), radius = moonRadius * 1.7f, center = moonCenter)
        val moonPoints = List(6) { i ->
            val angle = (PI / 3.0 * i).toFloat()
            Offset(
                moonCenter.x + moonRadius * cos(angle),
                moonCenter.y + moonRadius * sin(angle)
            )
        }
        for (i in 0 until 6) {
            drawFacet(
                moonCenter, moonPoints[i], moonPoints[(i + 1) % 6],
                if (i % 2 == 0) MoonLight else MoonDark
            )
        }

        // 4. Two layers of hills (far = lighter, near = darker)
        drawRidge(scene.farRidge, FarRidgeLight, FarRidgeDark)
        drawRidge(scene.nearRidge, NearRidgeLight, NearRidgeDark)

        // 5. Pine trees: back row first, then the bigger front row
        for (tree in scene.trees) {
            drawPine(
                centerX = tree.x * w,
                baseY = tree.baseY * h,
                height = tree.height * h,
                light = if (tree.isFront) FrontTreeLight else BackTreeLight,
                dark = if (tree.isFront) FrontTreeDark else BackTreeDark
            )
        }
    }
}

// ---------- Drawing helpers ----------

// One triangle. The thin outline in the same colour hides hairline gaps between neighbours.
private fun DrawScope.drawFacet(a: Offset, b: Offset, c: Offset, color: Color) {
    val path = Path().apply {
        moveTo(a.x, a.y)
        lineTo(b.x, b.y)
        lineTo(c.x, c.y)
        close()
    }
    drawPath(path, color)
    drawPath(path, color, style = Stroke(width = 1.2f))
}

// A jagged hill: every segment of the ridge line becomes two triangles down to the screen bottom
private fun DrawScope.drawRidge(points: List<Offset>, light: Color, dark: Color) {
    for (i in 0 until points.size - 1) {
        val p0 = Offset(points[i].x * size.width, points[i].y * size.height)
        val p1 = Offset(points[i + 1].x * size.width, points[i + 1].y * size.height)
        val b0 = Offset(p0.x, size.height)
        val b1 = Offset(p1.x, size.height)
        val flip = i % 2 == 0
        drawFacet(p0, p1, b0, if (flip) light else dark)
        drawFacet(p1, b1, b0, if (flip) dark else light)
    }
}

// A pine tree: 3 stacked triangles, each split into a lighter and a darker half
private fun DrawScope.drawPine(
    centerX: Float,
    baseY: Float,
    height: Float,
    light: Color,
    dark: Color
) {
    val topY = baseY - height
    val maxHalfWidth = height * 0.28f
    val trunkTop = topY + height * 0.96f

    drawRect(
        color = TrunkColor,
        topLeft = Offset(centerX - height * 0.025f, trunkTop),
        size = Size(height * 0.05f, baseY - trunkTop)
    )

    for (i in 0..2) {
        val apexY = topY + i * height * 0.27f
        val tierBaseY = apexY + height * 0.42f
        val halfWidth = maxHalfWidth * (0.55f + 0.225f * i)

        val apex = Offset(centerX, apexY)
        val left = Offset(centerX - halfWidth, tierBaseY)
        val middle = Offset(centerX, tierBaseY)
        val right = Offset(centerX + halfWidth, tierBaseY)

        drawFacet(apex, left, middle, light)
        drawFacet(apex, middle, right, dark)
    }
}
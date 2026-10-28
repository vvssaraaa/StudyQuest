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
// Sky: dark purple at the top, fading into forest green at the bottom
private val NightTop = Color(0xFF0F0919)
private val NightMid = Color(0xFF1C1232)
private val NightBottom = Color(0xFF123227)
private val PurpleHighlight = Color(0xFF5B3F8F)
private val MossHighlight = Color(0xFF3C7A5F)

private val MoonLight = Color(0xFFFFD166)
private val MoonDark = Color(0xFFF0B94A)

private val BatColor = Color(0xF20B0614)

private val CastleLight = Color(0xFF2E2352)
private val CastleDark = Color(0xFF22193F)
private val RoofLight = Color(0xFF3A2C66)
private val RoofDark = Color(0xFF2B2050)

private val FarRidgeLight = Color(0xFF2C2450)   // purple hills in the distance
private val FarRidgeDark = Color(0xFF241D42)
private val NearRidgeLight = Color(0xFF133324)  // green hills in front
private val NearRidgeDark = Color(0xFF0F2A1E)

private val BackTreeLight = Color(0xFF1C4A38)
private val BackTreeDark = Color(0xFF143528)
private val FrontTreeLight = Color(0xFF245A44)
private val FrontTreeDark = Color(0xFF18402F)
private val TrunkColor = Color(0xFF0E2219)

// ---------- Scene data ----------
// All positions are stored as fractions (0..1) of the screen, so the scene
// scales to any screen size. The random parts use a fixed seed.

private class Facet(val a: Offset, val b: Offset, val c: Offset, val shade: Float)

private class Tree(val x: Float, val baseY: Float, val height: Float, val isFront: Boolean)

private class Bat(val x: Float, val y: Float, val size: Float)

private class Scene(
    val mesh: List<Facet>,
    val farRidge: List<Offset>,
    val nearRidge: List<Offset>,
    val trees: List<Tree>,        // back trees first, so they are drawn first
    val stars: List<Offset>,
    val starSizes: List<Float>
)

// Bats flying around the moon (x, y as fractions of the screen, size as a fraction of the width)
private val bats = listOf(
    Bat(0.60f, 0.17f, 0.040f),
    Bat(0.70f, 0.075f, 0.030f),
    Bat(0.88f, 0.21f, 0.034f),
    Bat(0.50f, 0.10f, 0.024f),
    Bat(0.93f, 0.08f, 0.022f)
)

// The outline of one bat, relative to its centre (the left half mirrors the right half)
private val batOutline = listOf(
    Offset(0f, -0.15f), Offset(0.10f, -0.30f), Offset(0.16f, -0.14f),
    Offset(0.45f, -0.34f), Offset(1.0f, -0.12f), Offset(0.72f, 0.05f),
    Offset(0.5f, 0.0f), Offset(0.28f, 0.16f), Offset(0f, 0.26f),
    Offset(-0.28f, 0.16f), Offset(-0.5f, 0.0f), Offset(-0.72f, 0.05f),
    Offset(-1.0f, -0.12f), Offset(-0.45f, -0.34f), Offset(-0.16f, -0.14f),
    Offset(-0.10f, -0.30f)
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

    val farRidge = List(9) { i -> Offset(i / 8f, 0.46f + rnd.nextFloat() * 0.10f) }
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

        // 1. Faceted sky: purple at the top, green at the bottom
        for (f in scene.mesh) {
            val centerY = (f.a.y + f.b.y + f.c.y) / 3f
            val base = if (centerY < 0.55f) {
                lerp(NightTop, NightMid, centerY / 0.55f)
            } else {
                lerp(NightMid, NightBottom, (centerY - 0.55f) / 0.45f)
            }
            val highlight = lerp(PurpleHighlight, MossHighlight, centerY)
            drawFacet(
                toPx(f.a), toPx(f.b), toPx(f.c),
                lerp(base, highlight, f.shade * 0.14f)
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
        val moonRadius = w * 0.09f
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

        // 4. Bats flying across the sky
        for (bat in bats) {
            drawBat(center = Offset(bat.x * w, bat.y * h), s = bat.size * w)
        }

        // 5. Vampire castle. It is drawn BEFORE the far hills, so the hills hide its base
        drawCastle(cx = w * 0.74f, baseY = h * 0.58f, u = w * 0.34f)

        // 6. Two layers of hills (far = purple, near = green)
        drawRidge(scene.farRidge, FarRidgeLight, FarRidgeDark)
        drawRidge(scene.nearRidge, NearRidgeLight, NearRidgeDark)

        // 7. Pine trees: back row first, then the bigger front row
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

// A bat silhouette: the outline points scaled by s and moved to the centre
private fun DrawScope.drawBat(center: Offset, s: Float) {
    val path = Path()
    batOutline.forEachIndexed { i, p ->
        val x = center.x + p.x * s
        val y = center.y + p.y * s
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path, BatColor)
}

// A tower: two-tone body (light left, dark right) with a pointed two-tone roof
private fun DrawScope.drawTower(
    centerX: Float,
    baseY: Float,
    width: Float,
    height: Float,
    roofHeight: Float
) {
    val left = centerX - width / 2
    val top = baseY - height
    drawRect(CastleLight, topLeft = Offset(left, top), size = Size(width / 2 + 1f, height))
    drawRect(CastleDark, topLeft = Offset(centerX, top), size = Size(width / 2, height))

    val overhang = width * 0.12f
    val apex = Offset(centerX, top - roofHeight)
    val roofLeft = Offset(left - overhang, top)
    val roofMiddle = Offset(centerX, top)
    val roofRight = Offset(centerX + width / 2 + overhang, top)
    drawFacet(apex, roofLeft, roofMiddle, RoofLight)
    drawFacet(apex, roofMiddle, roofRight, RoofDark)
}

// The castle: a wall with battlements and three towers, with a few lit windows.
// u is the castle's width unit; everything else is sized relative to it.
private fun DrawScope.drawCastle(cx: Float, baseY: Float, u: Float) {
    // Wall between the towers
    val wallWidth = 0.9f * u
    val wallTop = baseY - 0.7f * u
    val wallLeft = cx - wallWidth / 2
    drawRect(CastleDark, topLeft = Offset(wallLeft, wallTop), size = Size(wallWidth, 0.7f * u + 2f))
    val merlon = wallWidth / 11f
    for (i in 0 until 6) {
        drawRect(
            CastleDark,
            topLeft = Offset(wallLeft + i * 2 * merlon, wallTop - 0.06f * u),
            size = Size(merlon, 0.06f * u)
        )
    }

    // Towers: left, tall middle, right
    drawTower(cx - 0.32f * u, baseY, width = 0.22f * u, height = 1.2f * u, roofHeight = 0.45f * u)
    drawTower(cx, baseY, width = 0.26f * u, height = 1.55f * u, roofHeight = 0.55f * u)
    drawTower(cx + 0.34f * u, baseY, width = 0.20f * u, height = 1.0f * u, roofHeight = 0.40f * u)

    // Lit windows
    val window = Yellow.copy(alpha = 0.9f)
    drawRect(window, Offset(cx - 0.02f * u, baseY - 1.2f * u), Size(0.045f * u, 0.08f * u))
    drawRect(window, Offset(cx - 0.34f * u, baseY - 0.9f * u), Size(0.04f * u, 0.07f * u))
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
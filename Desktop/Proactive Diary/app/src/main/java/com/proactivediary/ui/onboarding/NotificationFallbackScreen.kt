package com.proactivediary.ui.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proactivediary.analytics.AnalyticsService
import com.proactivediary.ui.theme.InstrumentSerif
import kotlin.math.cos
import kotlin.math.sin

private val AccentBlue = Color(0xFF3897F0)
private val FlowerPink = Color(0xFFF48FB1)
private val FlowerLavender = Color(0xFFCE93D8)
private val FlowerPeach = Color(0xFFFFAB91)
private val FlowerYellow = Color(0xFFFFD54F)
private val FlowerRose = Color(0xFFEF9A9A)
private val StemGreen = Color(0xFF81C784)
private val LeafGreen = Color(0xFF66BB6A)
private val VaseTerracotta = Color(0xFF8D6E63)

@Composable
fun NotificationFallbackScreen(
    onTurnOn: () -> Unit,
    onBack: () -> Unit,
    onNotNow: () -> Unit,
    analyticsService: AnalyticsService
) {
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            analyticsService.logOnboardingNotifGranted()
        }
        onTurnOn()
    }

    // Staggered drawing animation — stems, leaves, then flowers
    val drawProgress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        drawProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(2500, easing = FastOutSlowInEasing)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(0.06f))

        // Header
        Text(
            text = "You made the first step!",
            style = TextStyle(
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Big title
        Text(
            text = "Stay Inspired",
            style = TextStyle(
                fontFamily = InstrumentSerif,
                fontSize = 28.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Turn On Notifications button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(AccentBlue)
                .clickable {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        onTurnOn()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Turn On Notifications",
                style = TextStyle(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 4 bullet benefits
        val benefits = listOf(
            "Gentle reminders to write",
            "Make writing a habit",
            "Reflect and analyse",
            "Become a better writer"
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            benefits.forEach { benefit ->
                BenefitRow(text = benefit)
            }
        }

        Spacer(modifier = Modifier.weight(0.05f))

        // Animated flower bouquet — drawn with lines
        BouquetIllustration(
            modifier = Modifier
                .weight(0.45f)
                .fillMaxWidth(),
            drawProgress = drawProgress.value
        )

        // Bottom: Back (left) + Not Now (right)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Back",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onBack
                    )
                    .padding(vertical = 6.dp, horizontal = 4.dp)
            )
            Text(
                text = "Not Now",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            analyticsService.logOnboardingNotifDenied()
                            onNotNow()
                        }
                    )
                    .padding(vertical = 6.dp, horizontal = 4.dp)
            )
        }
    }
}

@Composable
private fun BenefitRow(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .width(6.dp)
                .height(6.dp)
                .clip(CircleShape)
                .background(AccentBlue.copy(alpha = 0.6f))
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            style = TextStyle(
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground,
                lineHeight = 18.sp
            )
        )
    }
}

@Composable
private fun BouquetIllustration(
    modifier: Modifier = Modifier,
    drawProgress: Float
) {
    // Gentle sway
    val infiniteTransition = rememberInfiniteTransition(label = "sway")
    val swayAngle by infiniteTransition.animateFloat(
        initialValue = -1.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sway"
    )

    // Phase breakpoints: stems 0-0.4, leaves 0.3-0.7, flowers 0.5-1.0
    val stemProgress = (drawProgress / 0.4f).coerceIn(0f, 1f)
    val leafProgress = ((drawProgress - 0.3f) / 0.4f).coerceIn(0f, 1f)
    val flowerProgress = ((drawProgress - 0.5f) / 0.5f).coerceIn(0f, 1f)

    val lineStroke = Stroke(
        width = 2f,
        cap = StrokeCap.Round,
        join = StrokeJoin.Round
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2

        rotate(swayAngle, pivot = Offset(cx, h)) {

            // ── Vase ──
            val vaseTop = h * 0.72f
            val vaseBottom = h * 0.94f
            val vaseMidY = (vaseTop + vaseBottom) / 2
            val vaseTopW = w * 0.14f
            val vaseMidW = w * 0.12f
            val vaseBotW = w * 0.15f

            val vasePath = Path().apply {
                moveTo(cx - vaseTopW, vaseTop)
                // Left edge curves in then out
                quadraticTo(cx - vaseMidW, vaseMidY, cx - vaseBotW, vaseBottom)
                lineTo(cx + vaseBotW, vaseBottom)
                quadraticTo(cx + vaseMidW, vaseMidY, cx + vaseTopW, vaseTop)
                close()
            }
            drawPath(vasePath, color = VaseTerracotta.copy(alpha = 0.2f * stemProgress))
            drawPath(vasePath, color = VaseTerracotta.copy(alpha = 0.5f * stemProgress), style = lineStroke)

            // Vase rim
            val rimPath = Path().apply {
                moveTo(cx - vaseTopW - 4, vaseTop)
                quadraticTo(cx, vaseTop - 6, cx + vaseTopW + 4, vaseTop)
            }
            drawPath(rimPath, color = VaseTerracotta.copy(alpha = 0.4f * stemProgress), style = lineStroke)

            // ── 5 Stems (drawn as lines that grow upward) ──
            data class StemDef(val topX: Float, val topY: Float, val ctrlX: Float, val ctrlY: Float)

            val stems = listOf(
                StemDef(cx, h * 0.18f, cx, h * 0.45f),                          // center tall
                StemDef(cx - w * 0.12f, h * 0.22f, cx - w * 0.04f, h * 0.48f), // left
                StemDef(cx + w * 0.12f, h * 0.24f, cx + w * 0.05f, h * 0.46f), // right
                StemDef(cx - w * 0.08f, h * 0.28f, cx - w * 0.06f, h * 0.50f), // inner left
                StemDef(cx + w * 0.09f, h * 0.26f, cx + w * 0.03f, h * 0.48f)  // inner right
            )

            stems.forEach { stem ->
                val stemPath = Path().apply {
                    moveTo(cx, vaseTop)
                    quadraticTo(stem.ctrlX, stem.ctrlY, stem.topX, stem.topY)
                }
                drawPathAnimated(stemPath, StemGreen.copy(alpha = 0.7f), stemProgress, lineStroke)
            }

            // ── Leaves (small curved shapes on stems) ──
            data class LeafDef(val baseX: Float, val baseY: Float, val dir: Float) // dir: -1 left, +1 right

            val leaves = listOf(
                LeafDef(cx - w * 0.03f, h * 0.52f, -1f),
                LeafDef(cx + w * 0.04f, h * 0.50f, 1f),
                LeafDef(cx - w * 0.09f, h * 0.42f, -1f),
                LeafDef(cx + w * 0.08f, h * 0.44f, 1f),
                LeafDef(cx, h * 0.40f, -1f),
                LeafDef(cx - w * 0.05f, h * 0.58f, 1f),
                LeafDef(cx + w * 0.02f, h * 0.56f, -1f)
            )

            leaves.forEach { leaf ->
                val leafSize = w * 0.06f
                val leafPath = Path().apply {
                    moveTo(leaf.baseX, leaf.baseY)
                    quadraticTo(
                        leaf.baseX + leaf.dir * leafSize,
                        leaf.baseY - leafSize * 0.5f,
                        leaf.baseX + leaf.dir * leafSize * 0.4f,
                        leaf.baseY - leafSize
                    )
                    quadraticTo(
                        leaf.baseX,
                        leaf.baseY - leafSize * 0.4f,
                        leaf.baseX, leaf.baseY
                    )
                }
                drawPath(
                    leafPath,
                    color = LeafGreen.copy(alpha = 0.35f * leafProgress),
                )
                drawPathAnimated(leafPath, LeafGreen.copy(alpha = 0.6f * leafProgress), leafProgress, lineStroke)
            }

            // ── 5 Flowers (line-drawn petals radiating from stem tops) ──
            data class FlowerDef(val cx: Float, val cy: Float, val color: Color, val petals: Int, val size: Float)

            val flowers = listOf(
                FlowerDef(cx, h * 0.18f, FlowerPink, 6, w * 0.07f),
                FlowerDef(cx - w * 0.12f, h * 0.22f, FlowerLavender, 5, w * 0.055f),
                FlowerDef(cx + w * 0.12f, h * 0.24f, FlowerPeach, 5, w * 0.06f),
                FlowerDef(cx - w * 0.08f, h * 0.28f, FlowerRose, 7, w * 0.05f),
                FlowerDef(cx + w * 0.09f, h * 0.26f, FlowerYellow, 6, w * 0.055f)
            )

            flowers.forEach { flower ->
                val petalR = flower.size * flowerProgress
                if (petalR > 0.5f) {
                    // Draw each petal as a line-drawn teardrop
                    for (i in 0 until flower.petals) {
                        val angle = (i * 360f / flower.petals) * (Math.PI / 180.0)
                        val tipX = flower.cx + (petalR * cos(angle)).toFloat()
                        val tipY = flower.cy + (petalR * sin(angle)).toFloat()
                        val perpAngle = angle + Math.PI / 2
                        val bulge = petalR * 0.35f
                        val midX = (flower.cx + tipX) / 2
                        val midY = (flower.cy + tipY) / 2

                        val petalPath = Path().apply {
                            moveTo(flower.cx, flower.cy)
                            quadraticTo(
                                midX + (bulge * cos(perpAngle)).toFloat(),
                                midY + (bulge * sin(perpAngle)).toFloat(),
                                tipX, tipY
                            )
                            quadraticTo(
                                midX - (bulge * cos(perpAngle)).toFloat(),
                                midY - (bulge * sin(perpAngle)).toFloat(),
                                flower.cx, flower.cy
                            )
                        }
                        // Soft fill
                        drawPath(petalPath, color = flower.color.copy(alpha = 0.25f * flowerProgress))
                        // Line-drawn outline
                        drawPath(
                            petalPath,
                            color = flower.color.copy(alpha = 0.7f * flowerProgress),
                            style = Stroke(width = 1.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                    }
                    // Center dot
                    drawCircle(
                        color = FlowerYellow.copy(alpha = 0.8f * flowerProgress),
                        radius = petalR * 0.25f,
                        center = Offset(flower.cx, flower.cy)
                    )
                    // Center dot outline
                    drawCircle(
                        color = FlowerYellow.copy(alpha = 0.5f * flowerProgress),
                        radius = petalR * 0.25f,
                        center = Offset(flower.cx, flower.cy),
                        style = Stroke(width = 1f)
                    )
                }
            }
        }
    }
}

/**
 * Draws a path with animated stroke progress (line-drawing effect).
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawPathAnimated(
    path: Path,
    color: Color,
    progress: Float,
    stroke: Stroke
) {
    if (progress <= 0f) return
    val measure = PathMeasure()
    measure.setPath(path, false)
    val length = measure.length
    val drawnLength = length * progress

    drawPath(
        path = path,
        color = color,
        style = Stroke(
            width = stroke.width,
            cap = stroke.cap,
            join = stroke.join,
            pathEffect = PathEffect.dashPathEffect(
                floatArrayOf(drawnLength, length - drawnLength),
                0f
            )
        )
    )
}

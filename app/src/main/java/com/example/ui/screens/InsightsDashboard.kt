package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SocialViewModel
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

// Representation of a historical user engagement data point
data class EngagementMetrics(
    val label: String,
    val likes: Int,
    val comments: Int,
    val reach: Int,
    val impressions: Int
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun InsightsDashboard(
    viewModel: SocialViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val posts by viewModel.posts.collectAsState()
    val reels by viewModel.reels.collectAsState()
    val followersCount by viewModel.followersCount.collectAsState()
    val followingCount by viewModel.followingCount.collectAsState()

    var selectedInterval by remember { mutableStateOf("7 Days") } // "7 Days" or "30 Days"
    var activeMetricType by remember { mutableStateOf("Engagement") } // "Engagement", "Reach & Growth", "Audience"

    // Sajiur's actual database details to calculate base values
    val originalMePosts = posts.filter { it.userId == "user_me" }
    val totalMyLikes = originalMePosts.sumOf { it.likesCount }
    val totalMyComments = originalMePosts.sumOf { it.commentsCount }
    val totalMyReelsCount = reels.filter { it.userId == "user_me" }.size

    // Generate high-density historical data for realistic intelligence dashboard
    val sevenDaysData = remember(totalMyLikes, totalMyComments, followersCount) {
        listOf(
            EngagementMetrics("Mon", (totalMyLikes * 0.15).roundToInt().coerceAtLeast(35), (totalMyComments * 0.1).roundToInt().coerceAtLeast(10), 1200, 1800),
            EngagementMetrics("Tue", (totalMyLikes * 0.12).roundToInt().coerceAtLeast(28), (totalMyComments * 0.15).roundToInt().coerceAtLeast(15), 1450, 2100),
            EngagementMetrics("Wed", (totalMyLikes * 0.18).roundToInt().coerceAtLeast(42), (totalMyComments * 0.08).roundToInt().coerceAtLeast(8), 1680, 2400),
            EngagementMetrics("Thu", (totalMyLikes * 0.22).roundToInt().coerceAtLeast(55), (totalMyComments * 0.22).roundToInt().coerceAtLeast(22), 2100, 3100),
            EngagementMetrics("Fri", (totalMyLikes * 0.25).roundToInt().coerceAtLeast(60), (totalMyComments * 0.28).roundToInt().coerceAtLeast(26), 2840, 4200),
            EngagementMetrics("Sat", (totalMyLikes * 0.30).roundToInt().coerceAtLeast(72), (totalMyComments * 0.35).roundToInt().coerceAtLeast(32), 3500, 5600),
            EngagementMetrics("Sun", (totalMyLikes * 0.20).roundToInt().coerceAtLeast(48), (totalMyComments * 0.18).roundToInt().coerceAtLeast(18), 2410, 3900)
        )
    }

    val thirtyDaysData = remember(totalMyLikes, totalMyComments) {
        listOf(
            EngagementMetrics("Wk 1", (totalMyLikes * 0.7 + 120).roundToInt(), (totalMyComments * 0.8 + 35).roundToInt(), 8400, 12000),
            EngagementMetrics("Wk 2", (totalMyLikes * 0.9 + 150).roundToInt(), (totalMyComments * 0.6 + 45).roundToInt(), 9800, 14500),
            EngagementMetrics("Wk 3", (totalMyLikes * 1.2 + 190).roundToInt(), (totalMyComments * 1.1 + 80).roundToInt(), 12500, 19200),
            EngagementMetrics("Wk 4", (totalMyLikes * 1.5 + 240).roundToInt(), (totalMyComments * 1.4 + 95).roundToInt(), 15400, 23800)
        )
    }

    val currentData = if (selectedInterval == "7 Days") sevenDaysData else thirtyDaysData

    Scaffold(
        topBar = {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(0.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("dashboard_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Return to Profile",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Engagement Recharts",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            // Pulsing live intelligence badge
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2ECC71))
                            )
                        }
                        Text(
                            text = "Real-time feed, message, & reel intelligence metrics",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
        ) {
            // Executive summary metrics (Row of cards)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SummaryMetricCard(
                    title = "Profile Reach",
                    value = if (selectedInterval == "7 Days") "15.2K" else "46.1K",
                    percentage = "+18.4%",
                    isPositive = true,
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    modifier = Modifier.weight(1f)
                )

                SummaryMetricCard(
                    title = "Interactions",
                    value = "${totalMyLikes + totalMyComments + (totalMyReelsCount * 12)}",
                    percentage = "+9.1%",
                    isPositive = true,
                    icon = Icons.Default.OfflineBolt,
                    modifier = Modifier.weight(1f)
                )
            }

            // Tabs / Control Filter Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp)
                ) {
                    // Control filter tags
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Focus Dimension",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Duration switcher (7 Days / 30 Days)
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(2.dp)
                        ) {
                            listOf("7 Days", "30 Days").forEach { range ->
                                val isSelected = selectedInterval == range
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                        .clickable { selectedInterval = range }
                                        .padding(horizontal = 14.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = range,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Secondary Sub-filter (Engagement vs Reach & Growth vs Audience Activity)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Engagement", "Reach & Growth", "Audience").forEach { metric ->
                            val isChosen = activeMetricType == metric
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isChosen) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .border(
                                        width = 1.dp,
                                        color = if (isChosen) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { activeMetricType = metric }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = metric,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isChosen) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Recharts AreaChart Canvas
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Engagement Trend Flow (Recharts)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Drag or press chart plot to display interactive values",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Recharts Line & Area visualizer
                    RechartsInteractiveAreaLine(
                        data = currentData,
                        metricType = activeMetricType,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Chart legend keys
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (activeMetricType == "Reach & Growth") "Reach Metrics" else "Likes Flow",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.width(20.dp))

                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.tertiary)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (activeMetricType == "Reach & Growth") "Impressions Metrics" else "Comments Flow",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Split metrics (Interaction Shares + Audience Distribution)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Left Column: Donut Breakdown (Engagement Distribution)
                Card(
                    modifier = Modifier.weight(1.1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Interaction Share",
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )
                        Text(
                            text = "Feed vs Reels vs DM activity",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        RechartsDonutChart(
                            likesPct = 0.55f,
                            commentsPct = 0.30f,
                            messagesPct = 0.15f,
                            modifier = Modifier.size(110.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Custom Labels Grid
                        DonutLegendItem(color = MaterialTheme.colorScheme.primary, label = "Likes Flow (55%)")
                        DonutLegendItem(color = MaterialTheme.colorScheme.tertiary, label = "Comments (30%)")
                        DonutLegendItem(color = MaterialTheme.colorScheme.secondary, label = "Direct DMs (15%)")
                    }
                }

                // Right Column: Weekly Reach (Bar Graph Widget)
                Card(
                    modifier = Modifier.weight(0.9f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Text(
                            text = "Reach Radar",
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Active Weekly reach velocity",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        RechartsWeeklyBarChart(
                            data = currentData,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom insights commentary & Top-performing asset card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Engagement Recommendation",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Peak reach was captured on Sat during high Reels sharing loops. We recommend releasing your upcoming media posts around 3:00 PM for 2.4x visibility amplification.",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun SummaryMetricCard(
    title: String,
    value: String,
    percentage: String,
    isPositive: Boolean,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            if (isPositive) Color(0xFF2ECC71).copy(alpha = 0.15f)
                            else Color(0xFFE74C3C).copy(alpha = 0.15f)
                        )
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = percentage,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isPositive) Color(0xFF27AE60) else Color(0xFFC0392B)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "vs yesterday",
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Recharts Interactive Line + Area Plot Custom Canvas
@Composable
fun RechartsInteractiveAreaLine(
    data: List<EngagementMetrics>,
    metricType: String,
    modifier: Modifier = Modifier
) {
    var activeIndex by remember(data) { mutableStateOf<Int?>(null) }

    val graphPrimaryColor = MaterialTheme.colorScheme.primary
    val graphSecondaryColor = MaterialTheme.colorScheme.tertiary
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant

    BoxWithConstraints(modifier = modifier) {
        val width = constraints.maxWidth.toFloat()
        val height = constraints.maxHeight.toFloat()

        // Padding calculations matching exact layouts
        val paddingLeft = 60f
        val paddingRight = 40f
        val paddingTop = 40f
        val paddingBottom = 60f

        val chartWidth = width - paddingLeft - paddingRight
        val chartHeight = height - paddingTop - paddingBottom

        val values1 = remember(data, metricType) {
            when (metricType) {
                "Reach & Growth" -> data.map { it.reach.toFloat() }
                "Audience" -> data.map { (it.likes * 1.5 + 40).toFloat() }
                else -> data.map { it.likes.toFloat() } // Engagement
            }
        }

        val values2 = remember(data, metricType) {
            when (metricType) {
                "Reach & Growth" -> data.map { it.impressions.toFloat() }
                "Audience" -> data.map { (it.comments * 1.8 + 15).toFloat() }
                else -> data.map { it.comments.toFloat() } // Engagement
            }
        }

        val maxVal = remember(values1, values2) {
            (values1 + values2).maxOrNull()?.coerceAtLeast(1f) ?: 100f
        }
        val minVal = 0f

        // Handle gestures inside Box
        val gestureModifier = Modifier
            .fillMaxSize()
            .pointerInput(data) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val relativeX = offset.x - paddingLeft
                        val fraction = relativeX / chartWidth
                        val index = (fraction * (data.size - 1)).roundToInt().coerceIn(data.indices)
                        activeIndex = index
                    },
                    onDrag = { change, _ ->
                        val relativeX = change.position.x - paddingLeft
                        val fraction = relativeX / chartWidth
                        val index = (fraction * (data.size - 1)).roundToInt().coerceIn(data.indices)
                        activeIndex = index
                    },
                    onDragEnd = { activeIndex = null },
                    onDragCancel = { activeIndex = null }
                )
            }
            .pointerInput(data) {
                detectTapGestures(
                    onTap = { offset ->
                        val relativeX = offset.x - paddingLeft
                        val fraction = relativeX / chartWidth
                        val index = (fraction * (data.size - 1)).roundToInt().coerceIn(data.indices)
                        activeIndex = if (activeIndex == index) null else index
                    }
                )
            }

        Box(modifier = gestureModifier) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // 1. Draw horizontal grid lines (recharts standard)
                val gridLines = 4
                for (i in 0..gridLines) {
                    val y = paddingTop + (i.toFloat() / gridLines) * chartHeight
                    drawLine(
                        color = surfaceVariantColor,
                        start = Offset(paddingLeft, y),
                        end = Offset(width - paddingRight, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Calculate control points coordinates
                val points1 = values1.indices.map { i ->
                    val x = paddingLeft + (i.toFloat() / (values1.size - 1)) * chartWidth
                    val y = paddingTop + chartHeight - ((values1[i] - minVal) / (maxVal - minVal)) * chartHeight
                    Offset(x, y)
                }

                val points2 = values2.indices.map { i ->
                    val x = paddingLeft + (i.toFloat() / (values2.size - 1)) * chartWidth
                    val y = paddingTop + chartHeight - ((values2[i] - minVal) / (maxVal - minVal)) * chartHeight
                    Offset(x, y)
                }

                // Draw Primary AREA under Line 1 (recharts Gradient Fill)
                if (points1.isNotEmpty()) {
                    val areaPath = Path().apply {
                        moveTo(points1[0].x, points1[0].y)
                        for (i in 1 until points1.size) {
                            val p0 = points1[i - 1]
                            val p1 = points1[i]
                            val cpX1 = p0.x + (p1.x - p0.x) / 2f
                            cubicTo(cpX1, p0.y, cpX1, p1.y, p1.x, p1.y)
                        }
                        lineTo(points1.last().x, paddingTop + chartHeight)
                        lineTo(points1.first().x, paddingTop + chartHeight)
                        close()
                    }

                    drawPath(
                        path = areaPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(graphPrimaryColor.copy(alpha = 0.25f), Color.Transparent),
                            startY = paddingTop,
                            endY = paddingTop + chartHeight
                        )
                    )
                }

                // Draw curve Line 1
                if (points1.isNotEmpty()) {
                    val linePath = Path().apply {
                        moveTo(points1[0].x, points1[0].y)
                        for (i in 1 until points1.size) {
                            val p0 = points1[i - 1]
                            val p1 = points1[i]
                            val cpX1 = p0.x + (p1.x - p0.x) / 2f
                            cubicTo(cpX1, p0.y, cpX1, p1.y, p1.x, p1.y)
                        }
                    }

                    drawPath(
                        path = linePath,
                        color = graphPrimaryColor,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Draw curve Line 2 (dotted or thinner line)
                if (points2.isNotEmpty()) {
                    val linePath2 = Path().apply {
                        moveTo(points2[0].x, points2[0].y)
                        for (i in 1 until points2.size) {
                            val p0 = points2[i - 1]
                            val p1 = points2[i]
                            val cpX1 = p0.x + (p1.x - p0.x) / 2f
                            cubicTo(cpX1, p0.y, cpX1, p1.y, p1.x, p1.y)
                        }
                    }

                    drawPath(
                        path = linePath2,
                        color = graphSecondaryColor,
                        style = Stroke(width = 2.0.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Draw active index guide vertical line
                activeIndex?.let { index ->
                    if (index in points1.indices) {
                        val activeX = points1[index].x

                        // Draw Vertical dashed indicator line
                        drawLine(
                            color = graphPrimaryColor.copy(alpha = 0.5f),
                            start = Offset(activeX, paddingTop),
                            end = Offset(activeX, paddingTop + chartHeight),
                            strokeWidth = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )

                        // Highlight dots
                        drawCircle(
                            color = graphPrimaryColor,
                            radius = 6.dp.toPx(),
                            center = points1[index]
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 3.dp.toPx(),
                            center = points1[index]
                        )

                        if (index in points2.indices) {
                            drawCircle(
                                color = graphSecondaryColor,
                                radius = 6.dp.toPx(),
                                center = points2[index]
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 3.dp.toPx(),
                                center = points2[index]
                            )
                        }
                    }
                }
            }

            // Draw Bottom Labels (X-Axis names)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(start = 24.dp, end = 16.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                data.forEach { d ->
                    Text(
                        text = d.label,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Floating Custom Recharts Tooltip
            activeIndex?.let { index ->
                if (index in data.indices) {
                    val activeItem = data[index]
                    val isFirstHalf = index < data.size / 2
                    val alignment = if (isFirstHalf) Alignment.TopEnd else Alignment.TopStart

                    Card(
                        modifier = Modifier
                            .align(alignment)
                            .padding(12.dp)
                            .widthIn(max = 140.dp)
                            .testTag("chart_recharts_tooltip"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Text(
                                text = "Period: ${activeItem.label}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Divider(modifier = Modifier.padding(vertical = 4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (metricType == "Reach & Growth") "Reach" else "Likes",
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (metricType == "Reach & Growth") "${activeItem.reach}" else "${activeItem.likes}",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = graphPrimaryColor
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (metricType == "Reach & Growth") "Impress." else "Comments",
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (metricType == "Reach & Growth") "${activeItem.impressions}" else "${activeItem.comments}",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = graphSecondaryColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Custom Draw Circle Arcs for Donut Graph
@Composable
fun RechartsDonutChart(
    likesPct: Float,
    commentsPct: Float,
    messagesPct: Float,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    Canvas(modifier = modifier) {
        val diameter = size.minDimension
        val strokeWidth = 14.dp.toPx()
        val radiusSize = Size(diameter - strokeWidth, diameter - strokeWidth)
        val offsetPos = Offset(strokeWidth / 2, strokeWidth / 2)

        val likesSweep = 360f * likesPct
        val commentsSweep = 360f * commentsPct
        val messagesSweep = 360f * messagesPct

        // Likes segment
        drawArc(
            color = primaryColor,
            startAngle = -90f,
            sweepAngle = likesSweep,
            useCenter = false,
            topLeft = offsetPos,
            size = radiusSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Comments segment
        drawArc(
            color = tertiaryColor,
            startAngle = -90f + likesSweep,
            sweepAngle = commentsSweep,
            useCenter = false,
            topLeft = offsetPos,
            size = radiusSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Messages segment
        drawArc(
            color = secondaryColor,
            startAngle = -90f + likesSweep + commentsSweep,
            sweepAngle = messagesSweep,
            useCenter = false,
            topLeft = offsetPos,
            size = radiusSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun DonutLegendItem(
    color: Color,
    label: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Recharts Style Weekly Bar Chart Panel
@Composable
fun RechartsWeeklyBarChart(
    data: List<EngagementMetrics>,
    modifier: Modifier = Modifier
) {
    val barColor = MaterialTheme.colorScheme.primary
    val errorColor = MaterialTheme.colorScheme.error
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    var selectedBarIndex by remember { mutableStateOf<Int?>(null) }

    BoxWithConstraints(modifier = modifier) {
        val width = constraints.maxWidth.toFloat()
        val height = constraints.maxHeight.toFloat()

        val paddingLeft = 10f
        val paddingRight = 10f
        val paddingTop = 10f
        val paddingBottom = 40f

        val chartWidth = width - paddingLeft - paddingRight
        val chartHeight = height - paddingTop - paddingBottom

        val barSpaceRatio = 0.4f
        val totalBars = data.size
        val barGroupWidth = chartWidth / totalBars
        val singleBarWidth = barGroupWidth * (1 - barSpaceRatio)

        val maxReach = data.maxOf { it.reach }.toFloat().coerceAtLeast(1f)

        // Handle touch state inside Canvas bounds
        val tapModifier = Modifier
            .fillMaxSize()
            .pointerInput(data) {
                detectTapGestures(
                    onTap = { offset ->
                        val colIndex = ((offset.x - paddingLeft) / barGroupWidth).toInt()
                        if (colIndex in data.indices) {
                            selectedBarIndex = if (selectedBarIndex == colIndex) null else colIndex
                        }
                    }
                )
            }

        Box(modifier = tapModifier) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                data.forEachIndexed { i, d ->
                    val barHeightFraction = d.reach.toFloat() / maxReach
                    val barHeight = chartHeight * barHeightFraction

                    val left = paddingLeft + (i * barGroupWidth) + (barGroupWidth * barSpaceRatio / 2)
                    val top = paddingTop + chartHeight - barHeight

                    val isSelected = selectedBarIndex == i

                    // Draw back translucent shadow bar
                    drawRoundRect(
                        color = surfaceVariant.copy(alpha = 0.25f),
                        topLeft = Offset(left, paddingTop),
                        size = Size(singleBarWidth, chartHeight),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )

                    // Draw actual value rounded bar
                    drawRoundRect(
                        color = if (isSelected) errorColor else barColor,
                        topLeft = Offset(left, top),
                        size = Size(singleBarWidth, barHeight),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )
                }
            }

            // Labels grid below bars
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(start = 4.dp, top = 0.dp, end = 4.dp, bottom = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                data.forEachIndexed { idx, d ->
                    Text(
                        text = d.label,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedBarIndex == idx) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(28.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Tiny Bubble details
            selectedBarIndex?.let { index ->
                if (index in data.indices) {
                    val activeReach = data[index].reach
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.inverseSurface)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Reach: $activeReach",
                            fontSize = 8.sp,
                            color = MaterialTheme.colorScheme.inverseOnSurface,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

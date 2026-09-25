package com.example.core.health

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.entity.WeightRecordEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

data class BirdWeightAnalysis(
    val currentWeightGrams: Double?,
    val previousWeightGrams: Double?,
    val weightChangeGrams: Double?,
    val weightChangePercentage: Double?,
    val trend: WeightTrend,
    val recordsSortedChronologically: List<WeightRecordEntity>
)

enum class WeightTrend {
    GAIN,
    LOSS,
    STABLE,
    CRITICAL_LOSS,
    NO_DATA
}

object WeightTrackerHelper {

    fun analyzeWeights(records: List<WeightRecordEntity>): BirdWeightAnalysis {
        val validRecords = records.filter { !it.isDeleted && it.weightGrams > 0 }
        if (validRecords.isEmpty()) {
            return BirdWeightAnalysis(
                currentWeightGrams = null,
                previousWeightGrams = null,
                weightChangeGrams = null,
                weightChangePercentage = null,
                trend = WeightTrend.NO_DATA,
                recordsSortedChronologically = emptyList()
            )
        }

        val sortedAsc = validRecords.sortedBy { it.recordedDate }
        val current = sortedAsc.last().weightGrams
        val previous = if (sortedAsc.size > 1) sortedAsc[sortedAsc.size - 2].weightGrams else null

        val delta = if (previous != null) current - previous else null
        val percentage = if (previous != null && previous > 0) {
            ((current - previous) / previous) * 100.0
        } else null

        val trend = when {
            previous == null -> WeightTrend.STABLE
            percentage != null && percentage < -8.0 -> WeightTrend.CRITICAL_LOSS
            percentage != null && percentage < -1.5 -> WeightTrend.LOSS
            percentage != null && percentage > 1.5 -> WeightTrend.GAIN
            else -> WeightTrend.STABLE
        }

        return BirdWeightAnalysis(
            currentWeightGrams = current,
            previousWeightGrams = previous,
            weightChangeGrams = delta,
            weightChangePercentage = percentage,
            trend = trend,
            recordsSortedChronologically = sortedAsc
        )
    }
}

/**
 * Growth & Weight Trend Chart Composable
 */
@Composable
fun WeightGrowthTrendChart(
    weightRecords: List<WeightRecordEntity>,
    modifier: Modifier = Modifier,
    isPersian: Boolean = false,
    idealMinGrams: Float = 30f,
    idealMaxGrams: Float = 45f
) {
    val analysis = WeightTrackerHelper.analyzeWeights(weightRecords)
    val records = analysis.recordsSortedChronologically

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("weight_growth_chart_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isPersian) "روند رشد و وزن‌کشی" else "Weight & Growth Trend",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (isPersian) "محدوده نرمال مرغ عشق: ۳۰ تا ۴۵ گرم" else "Standard budgie norm: 30g - 45g",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                if (analysis.currentWeightGrams != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Balance,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "%.1fg".format(Locale.US, analysis.currentWeightGrams),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Current, Previous, and Delta summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Current Weight Card
                WeightMetricBox(
                    title = if (isPersian) "وزن فعلی" else "Current",
                    value = analysis.currentWeightGrams?.let { "%.1f g".format(Locale.US, it) } ?: "--",
                    subtitle = if (records.isNotEmpty()) {
                        SimpleDateFormat("MM/dd", Locale.US).format(Date(records.last().recordedDate))
                    } else null,
                    modifier = Modifier.weight(1f)
                )

                // Previous Weight Card
                WeightMetricBox(
                    title = if (isPersian) "وزن قبلی" else "Previous",
                    value = analysis.previousWeightGrams?.let { "%.1f g".format(Locale.US, it) } ?: "--",
                    subtitle = if (records.size > 1) {
                        SimpleDateFormat("MM/dd", Locale.US).format(Date(records[records.size - 2].recordedDate))
                    } else null,
                    modifier = Modifier.weight(1f)
                )

                // Delta / Change Card
                val deltaColor = when (analysis.trend) {
                    WeightTrend.GAIN -> Color(0xFF2E7D32)
                    WeightTrend.STABLE -> MaterialTheme.colorScheme.primary
                    WeightTrend.LOSS -> Color(0xFFF57C00)
                    WeightTrend.CRITICAL_LOSS -> MaterialTheme.colorScheme.error
                    WeightTrend.NO_DATA -> MaterialTheme.colorScheme.outline
                }

                val deltaText = analysis.weightChangeGrams?.let {
                    val sign = if (it > 0) "+" else ""
                    "$sign%.1f g".format(Locale.US, it)
                } ?: "--"

                val deltaPercent = analysis.weightChangePercentage?.let {
                    val sign = if (it > 0) "+" else ""
                    "$sign%.1f%%".format(Locale.US, it)
                } ?: ""

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = deltaColor.copy(alpha = 0.12f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isPersian) "تغییر وزن" else "Weight Change",
                            style = MaterialTheme.typography.labelSmall,
                            color = deltaColor
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            when (analysis.trend) {
                                WeightTrend.GAIN -> Icon(
                                    imageVector = Icons.Filled.ArrowUpward,
                                    contentDescription = null,
                                    tint = deltaColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                WeightTrend.LOSS, WeightTrend.CRITICAL_LOSS -> Icon(
                                    imageVector = Icons.Filled.ArrowDownward,
                                    contentDescription = null,
                                    tint = deltaColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                else -> {}
                            }
                            Text(
                                text = deltaText,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = deltaColor
                            )
                        }
                        if (deltaPercent.isNotBlank()) {
                            Text(
                                text = deltaPercent,
                                style = MaterialTheme.typography.labelSmall,
                                color = deltaColor
                            )
                        }
                    }
                }
            }

            if (analysis.trend == WeightTrend.CRITICAL_LOSS) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isPersian)
                                "هشدار: کاهش وزن بیش از ۸٪ نیازمند بررسی دقیق تغذیه و سلامت پرنده است."
                            else
                                "Alert: Weight loss >8% requires veterinary/health monitoring.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Canvas Chart
            if (records.size >= 2) {
                val lineColor = MaterialTheme.colorScheme.primary
                val pointColor = MaterialTheme.colorScheme.secondary
                val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                val bandColor = Color(0xFF4CAF50).copy(alpha = 0.1f)

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                        .testTag("weight_canvas_chart")
                ) {
                    val width = size.width
                    val height = size.height
                    val leftPadding = 30f
                    val rightPadding = 20f
                    val bottomPadding = 30f
                    val topPadding = 20f

                    val chartWidth = width - leftPadding - rightPadding
                    val chartHeight = height - topPadding - bottomPadding

                    val minWeight = (records.minOf { it.weightGrams } - 2.0).coerceAtLeast(15.0).toFloat()
                    val maxWeight = (records.maxOf { it.weightGrams } + 2.0).coerceAtLeast(50.0).toFloat()
                    val range = (maxWeight - minWeight).coerceAtLeast(1f)

                    fun getY(weight: Float): Float {
                        val norm = (weight - minWeight) / range
                        return topPadding + chartHeight * (1f - norm)
                    }

                    // Draw ideal healthy range band
                    val idealTop = getY(idealMaxGrams).coerceIn(topPadding, topPadding + chartHeight)
                    val idealBottom = getY(idealMinGrams).coerceIn(topPadding, topPadding + chartHeight)
                    if (idealBottom > idealTop) {
                        drawRect(
                            color = bandColor,
                            topLeft = Offset(leftPadding, idealTop),
                            size = androidx.compose.ui.geometry.Size(chartWidth, idealBottom - idealTop)
                        )
                    }

                    // Grid lines (3 horizontal lines)
                    val steps = 3
                    for (i in 0..steps) {
                        val lineVal = minWeight + (range / steps) * i
                        val lineY = getY(lineVal)
                        drawLine(
                            color = gridColor,
                            start = Offset(leftPadding, lineY),
                            end = Offset(width - rightPadding, lineY),
                            strokeWidth = 1f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                        )
                        // Label
                        drawContext.canvas.nativeCanvas.drawText(
                            "${lineVal.roundToInt()}g",
                            4f,
                            lineY + 4f,
                            android.graphics.Paint().apply {
                                color = android.graphics.Color.GRAY
                                textSize = 22f
                                textAlign = android.graphics.Paint.Align.LEFT
                            }
                        )
                    }

                    // Calculate X coordinates for records
                    val xStep = chartWidth / (records.size - 1).coerceAtLeast(1)
                    val points = records.mapIndexed { index, rec ->
                        val x = leftPadding + index * xStep
                        val y = getY(rec.weightGrams.toFloat())
                        Offset(x, y)
                    }

                    // Draw Line Path
                    val path = Path()
                    points.forEachIndexed { index, pt ->
                        if (index == 0) path.moveTo(pt.x, pt.y)
                        else path.lineTo(pt.x, pt.y)
                    }
                    drawPath(
                        path = path,
                        color = lineColor,
                        style = Stroke(width = 4f)
                    )

                    // Draw Points and values
                    val dateFormat = SimpleDateFormat("MM/dd", Locale.US)
                    points.forEachIndexed { index, pt ->
                        val rec = records[index]
                        // Outer halo
                        drawCircle(
                            color = Color.White,
                            radius = 7f,
                            center = pt
                        )
                        // Core point
                        drawCircle(
                            color = pointColor,
                            radius = 5f,
                            center = pt
                        )

                        // Weight label above point
                        drawContext.canvas.nativeCanvas.drawText(
                            "%.1f".format(Locale.US, rec.weightGrams),
                            pt.x,
                            pt.y - 12f,
                            android.graphics.Paint().apply {
                                color = android.graphics.Color.DKGRAY
                                textSize = 22f
                                textAlign = android.graphics.Paint.Align.CENTER
                                isFakeBoldText = true
                            }
                        )

                        // Date label below axis
                        drawContext.canvas.nativeCanvas.drawText(
                            dateFormat.format(Date(rec.recordedDate)),
                            pt.x,
                            height - 4f,
                            android.graphics.Paint().apply {
                                color = android.graphics.Color.GRAY
                                textSize = 20f
                                textAlign = android.graphics.Paint.Align.CENTER
                            }
                        )
                    }
                }
            } else if (records.size == 1) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isPersian)
                            "یک رکورد وزن ثبت شده است. برای نمایش نمودار حداقل ۲ رکورد نیاز است."
                        else
                            "1 weight record available. Add at least 2 records to view the trend line.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isPersian) "هنوز رکورد وزنی ثبت نشده است." else "No weight records logged yet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun WeightMetricBox(
    title: String,
    value: String,
    subtitle: String?,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

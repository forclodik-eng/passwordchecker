package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AttackMethod
import com.example.model.BruteForceProgress
import com.example.model.MethodResult
import com.example.model.MethodStatus
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberGreen
import com.example.ui.theme.CyberMint
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.CyberRed
import com.example.ui.theme.CyberSurfaceDark
import com.example.ui.theme.CyberSurfaceVariantDark
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark

@Composable
fun AttackVisualizerCard(
    isSimulating: Boolean,
    methodResults: Map<AttackMethod, MethodResult>,
    bruteForceProgress: BruteForceProgress,
    onStartSimulation: () -> Unit,
    onStopSimulation: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("attack_visualizer_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CyberSurfaceDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header with simulation action button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = null,
                            tint = CyberCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Симулятор атак",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                    }
                    Text(
                        text = "От легких к сложным + параллельный перебор",
                        fontSize = 12.sp,
                        color = TextSecondaryDark
                    )
                }

                if (isSimulating) {
                    OutlinedButton(
                        onClick = onStopSimulation,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = CyberRed
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyberRed),
                        modifier = Modifier.testTag("stop_simulation_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Стоп", fontSize = 12.sp)
                    }
                } else {
                    Button(
                        onClick = onStartSimulation,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyberCyan,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier.testTag("start_simulation_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Тест взлома", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // PARALLEL RANDOM BRUTE-FORCE STREAM (Live Matrix Terminal Box)
            BruteForceTerminalBox(bruteForceProgress = bruteForceProgress)

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "ПОСЛЕДОВАТЕЛЬНЫЕ МЕТОДЫ ПОДБОРА:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondaryDark,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Pipeline of Methods (from easiest to hardest)
            AttackMethod.values().sortedBy { it.order }.forEach { method ->
                val result = methodResults[method] ?: MethodResult(method = method)
                MethodRowItem(result = result)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun BruteForceTerminalBox(bruteForceProgress: BruteForceProgress) {
    val infiniteTransition = rememberInfiniteTransition(label = "terminalBlink")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursorAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF06090F))
            .border(1.dp, CyberCyan.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(14.dp)
            .testTag("brute_force_terminal")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                if (bruteForceProgress.isRunning) CyberMint else TextMutedDark,
                                CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ПАРАЛЛЕЛЬНЫЙ СЛУЧАЙНЫЙ ПЕРЕБОР",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberCyan,
                        fontFamily = FontFamily.Monospace
                    )
                }

                if (bruteForceProgress.isRunning) {
                    Text(
                        text = "${bruteForceProgress.attemptsPerSecond} попыток/сек",
                        fontSize = 11.sp,
                        color = CyberMint,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Current guess terminal stream
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0C1322), RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "> ",
                    color = CyberMint,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = if (bruteForceProgress.currentGuess.isNotEmpty()) bruteForceProgress.currentGuess else "ожидание запуска потока...",
                    color = if (bruteForceProgress.isRunning) CyberMint else TextMutedDark,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                if (bruteForceProgress.isRunning) {
                    Box(
                        modifier = Modifier
                            .size(8.dp, 16.dp)
                            .background(CyberMint)
                            .alpha(alphaAnim)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Всего попыток:",
                        fontSize = 11.sp,
                        color = TextSecondaryDark
                    )
                    Text(
                        text = String.format("%,d", bruteForceProgress.totalAttempts),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Пространство поиска:",
                        fontSize = 11.sp,
                        color = TextSecondaryDark
                    )
                    Text(
                        text = bruteForceProgress.theoreticalCombinationsFormatted.ifEmpty { "N/A" } + " вар.",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberPurple,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
private fun MethodRowItem(result: MethodResult) {
    val status = result.status

    val statusColor by animateColorAsState(
        targetValue = when (status) {
            MethodStatus.WAITING -> TextMutedDark
            MethodStatus.RUNNING -> CyberCyan
            MethodStatus.CRACKED -> CyberRed
            MethodStatus.RESISTED -> CyberGreen
        },
        label = "statusColor"
    )

    val backgroundColor = when (status) {
        MethodStatus.WAITING -> Color(0xFF131B2E)
        MethodStatus.RUNNING -> Color(0xFF102738)
        MethodStatus.CRACKED -> Color(0xFF2B141E)
        MethodStatus.RESISTED -> Color(0xFF102B22)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(backgroundColor)
            .border(1.dp, statusColor.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
            .padding(12.dp)
            .testTag("method_row_${result.method.order}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Icon & Name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                when (status) {
                    MethodStatus.WAITING -> {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(TextMutedDark, CircleShape)
                        )
                    }
                    MethodStatus.RUNNING -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            color = CyberCyan,
                            strokeWidth = 2.dp
                        )
                    }
                    MethodStatus.CRACKED -> {
                        Icon(
                            imageVector = Icons.Default.Dangerous,
                            contentDescription = "Взломан",
                            tint = CyberRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    MethodStatus.RESISTED -> {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Устойчив",
                            tint = CyberGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = result.method.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimaryDark
                    )

                    if (result.details.isNotEmpty()) {
                        Text(
                            text = result.details,
                            fontSize = 11.sp,
                            color = statusColor,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Text(
                            text = result.method.description,
                            fontSize = 11.sp,
                            color = TextSecondaryDark
                        )
                    }
                }
            }

            // Status Badge
            Box(
                modifier = Modifier
                    .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = when (status) {
                        MethodStatus.WAITING -> "В очереди"
                        MethodStatus.RUNNING -> "Тест..."
                        MethodStatus.CRACKED -> "ВЗЛОМАН"
                        MethodStatus.RESISTED -> "ЗАЩИЩЕН"
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

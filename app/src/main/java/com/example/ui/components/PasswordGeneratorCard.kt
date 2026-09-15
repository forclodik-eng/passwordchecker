package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberMint
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.CyberSurfaceDark
import com.example.ui.theme.CyberSurfaceVariantDark
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.viewmodel.GenerationMode

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PasswordGeneratorCard(
    generatedPassword: String,
    generationMode: GenerationMode,
    length: Int,
    includeUpper: Boolean,
    includeDigits: Boolean,
    includeSymbols: Boolean,
    onModeSelected: (GenerationMode) -> Unit,
    onLengthChanged: (Int) -> Unit,
    onToggleUpper: () -> Unit,
    onToggleDigits: () -> Unit,
    onToggleSymbols: () -> Unit,
    onRegenerate: () -> Unit,
    onApplyToAuditor: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("password_generator_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CyberSurfaceDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoFixHigh,
                        contentDescription = null,
                        tint = CyberMint,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Генератор надежных вариантов",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                }

                IconButton(
                    onClick = onRegenerate,
                    modifier = Modifier.testTag("regenerate_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Сгенерировать другой",
                        tint = CyberCyan
                    )
                }
            }

            Text(
                text = "Создайте устойчивый пароль с максимальным рейтингом 10.0",
                fontSize = 12.sp,
                color = TextSecondaryDark
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Mode Selection Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ModePill(
                    title = "Усилить свой",
                    isSelected = generationMode == GenerationMode.UPGRADE,
                    onClick = { onModeSelected(GenerationMode.UPGRADE) },
                    modifier = Modifier.weight(1f)
                )
                ModePill(
                    title = "Случайный",
                    isSelected = generationMode == GenerationMode.RANDOM,
                    onClick = { onModeSelected(GenerationMode.RANDOM) },
                    modifier = Modifier.weight(1f)
                )
                ModePill(
                    title = "Фраза",
                    isSelected = generationMode == GenerationMode.PASSPHRASE,
                    onClick = { onModeSelected(GenerationMode.PASSPHRASE) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Generated Password Display Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF070C18))
                    .border(1.dp, CyberMint.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = generatedPassword.ifEmpty { "Генерация..." },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberMint,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("generated_password_text")
                    )

                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Generated Password", generatedPassword)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Пароль скопирован!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("copy_password_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Скопировать пароль",
                            tint = CyberCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Options for Random Mode
            if (generationMode == GenerationMode.RANDOM) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Длина пароля:",
                            fontSize = 12.sp,
                            color = TextSecondaryDark
                        )
                        Text(
                            text = "$length символов",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberCyan,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Slider(
                        value = length.toFloat(),
                        onValueChange = { onLengthChanged(it.toInt()) },
                        valueRange = 10f..32f,
                        steps = 21,
                        colors = SliderDefaults.colors(
                            thumbColor = CyberCyan,
                            activeTrackColor = CyberCyan,
                            inactiveTrackColor = CyberSurfaceVariantDark
                        ),
                        modifier = Modifier.testTag("length_slider")
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = includeUpper,
                            onClick = onToggleUpper,
                            label = { Text("A-Z", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyberCyan.copy(alpha = 0.2f),
                                selectedLabelColor = CyberCyan
                            )
                        )
                        FilterChip(
                            selected = includeDigits,
                            onClick = onToggleDigits,
                            label = { Text("0-9", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyberCyan.copy(alpha = 0.2f),
                                selectedLabelColor = CyberCyan
                            )
                        )
                        FilterChip(
                            selected = includeSymbols,
                            onClick = onToggleSymbols,
                            label = { Text("!@#$%", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyberCyan.copy(alpha = 0.2f),
                                selectedLabelColor = CyberCyan
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (generationMode == GenerationMode.UPGRADE) {
                Text(
                    text = "Берет введенный вами пароль и добавляет скрытую энтропию, спецсимволы и длину, сохраняя удобство запоминания.",
                    fontSize = 11.sp,
                    color = TextSecondaryDark
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            if (generationMode == GenerationMode.PASSPHRASE) {
                Text(
                    text = "Метод Diceware: последовательность случайных слов с дефисами. Легко читается человеком, но требует триллионы лет для перебора.",
                    fontSize = 11.sp,
                    color = TextSecondaryDark
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Action: Test in Auditor
            Button(
                onClick = onApplyToAuditor,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyberMint,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("apply_and_audit_button")
            ) {
                Icon(
                    imageVector = Icons.Default.PlayCircle,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Проверить этот вариант в аудите",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ModePill(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isSelected) CyberCyan.copy(alpha = 0.2f) else CyberSurfaceVariantDark
    val textColor = if (isSelected) CyberCyan else TextSecondaryDark
    val borderColor = if (isSelected) CyberCyan else Color.Transparent

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = textColor
        )
    }
}

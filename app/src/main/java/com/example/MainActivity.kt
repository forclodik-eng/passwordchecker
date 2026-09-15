package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AttackVisualizerCard
import com.example.ui.components.HardwareResistanceCard
import com.example.ui.components.PasswordGeneratorCard
import com.example.ui.components.RecommendationsCard
import com.example.ui.components.ScoreGauge
import com.example.ui.theme.CyberBgDark
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberMint
import com.example.ui.theme.CyberSurfaceDark
import com.example.ui.theme.CyberSurfaceVariantDark
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.viewmodel.PasswordAuditorViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                PasswordAuditorApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PasswordAuditorApp(
    viewModel: PasswordAuditorViewModel = viewModel()
) {
    val password by viewModel.password.collectAsState()
    val isVisible by viewModel.isPasswordVisible.collectAsState()
    val analysis by viewModel.analysis.collectAsState()
    val methodResults by viewModel.methodResults.collectAsState()
    val bruteForceProgress by viewModel.bruteForceProgress.collectAsState()
    val isSimulating by viewModel.isSimulating.collectAsState()

    val generatedPassword by viewModel.generatedPassword.collectAsState()
    val generationMode by viewModel.generationMode.collectAsState()
    val generatorLength by viewModel.generatorLength.collectAsState()
    val includeUpper by viewModel.includeUpper.collectAsState()
    val includeDigits by viewModel.includeDigits.collectAsState()
    val includeSymbols by viewModel.includeSymbols.collectAsState()

    val focusManager = LocalFocusManager.current

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = CyberBgDark,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CyberSurfaceDark,
                    titleContentColor = TextPrimaryDark
                ),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CyberCyan.copy(alpha = 0.15f))
                                .border(1.dp, CyberCyan.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = CyberCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Password Auditor",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark
                            )
                            Text(
                                text = "Оценка стойкости и симуляция взлома",
                                fontSize = 11.sp,
                                color = TextSecondaryDark
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Password Input Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CyberSurfaceDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Text(
                            text = "Введите пароль для проверки:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimaryDark
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = { viewModel.onPasswordChanged(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("password_input_field"),
                            placeholder = {
                                Text(
                                    text = "Например: P@ssw0rd2024!",
                                    color = TextMutedDark,
                                    fontSize = 14.sp
                                )
                            },
                            visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { focusManager.clearFocus() }
                            ),
                            trailingIcon = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (password.isNotEmpty()) {
                                        IconButton(
                                            onClick = { viewModel.onPasswordChanged("") },
                                            modifier = Modifier.testTag("clear_password_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Clear,
                                                contentDescription = "Очистить",
                                                tint = TextSecondaryDark
                                            )
                                        }
                                    }
                                    IconButton(
                                        onClick = { viewModel.togglePasswordVisibility() },
                                        modifier = Modifier.testTag("toggle_visibility_button")
                                    ) {
                                        Icon(
                                            imageVector = if (isVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = if (isVisible) "Скрыть пароль" else "Показать пароль",
                                            tint = CyberCyan
                                        )
                                    }
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberCyan,
                                unfocusedBorderColor = CyberCardBorder,
                                focusedTextColor = TextPrimaryDark,
                                unfocusedTextColor = TextPrimaryDark,
                                cursorColor = CyberCyan,
                                focusedContainerColor = Color(0xFF0C1322),
                                unfocusedContainerColor = Color(0xFF0C1322)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Quick Test Presets
                        Text(
                            text = "Быстрые примеры для проверки:",
                            fontSize = 11.sp,
                            color = TextSecondaryDark
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            PresetChip(
                                label = "123456",
                                onClick = { viewModel.onPasswordChanged("123456") }
                            )
                            PresetChip(
                                label = "qwerty",
                                onClick = { viewModel.onPasswordChanged("qwerty") }
                            )
                            PresetChip(
                                label = "Admin2024!",
                                onClick = { viewModel.onPasswordChanged("Admin2024!") }
                            )
                            PresetChip(
                                label = "P@ssw0rd1",
                                onClick = { viewModel.onPasswordChanged("P@ssw0rd1") }
                            )
                            PresetChip(
                                label = "сокол-орбита-72!",
                                onClick = { viewModel.onPasswordChanged("сокол-орбита-72!") }
                            )
                            PresetChip(
                                label = "K9#mQ!8z\$vL2^xR",
                                onClick = { viewModel.onPasswordChanged("K9#mQ!8z\$vL2^xR") }
                            )
                        }
                    }
                }
            }

            // 10.0 Rating & Entropy Gauge
            item {
                ScoreGauge(analysis = analysis)
            }

            // Cracking Simulator (Smart Methods 1-6 + Parallel Brute Force)
            item {
                AttackVisualizerCard(
                    isSimulating = isSimulating,
                    methodResults = methodResults,
                    bruteForceProgress = bruteForceProgress,
                    onStartSimulation = { viewModel.runAttackSimulation() },
                    onStopSimulation = { viewModel.stopAttackSimulation() }
                )
            }

            // Hardware Crack Time Breakdown
            item {
                HardwareResistanceCard(analysis = analysis)
            }

            // Actionable Recommendations
            item {
                RecommendationsCard(analysis = analysis)
            }

            // Strong Password Auto-Generator
            item {
                PasswordGeneratorCard(
                    generatedPassword = generatedPassword,
                    generationMode = generationMode,
                    length = generatorLength,
                    includeUpper = includeUpper,
                    includeDigits = includeDigits,
                    includeSymbols = includeSymbols,
                    onModeSelected = { viewModel.setGenerationMode(it) },
                    onLengthChanged = { viewModel.setGeneratorLength(it) },
                    onToggleUpper = { viewModel.toggleIncludeUpper() },
                    onToggleDigits = { viewModel.toggleIncludeDigits() },
                    onToggleSymbols = { viewModel.toggleIncludeSymbols() },
                    onRegenerate = { viewModel.generateNewVariant() },
                    onApplyToAuditor = { viewModel.applyGeneratedPasswordToAuditor() }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun PresetChip(
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(CyberSurfaceVariantDark)
            .border(1.dp, CyberCardBorder, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = CyberCyan,
            fontFamily = FontFamily.Monospace
        )
    }
}

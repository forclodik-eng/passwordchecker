package com.example.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PasswordAnalysis
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberMint
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.CyberRed
import com.example.ui.theme.CyberSurfaceDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark

@Composable
fun HardwareResistanceCard(
    analysis: PasswordAnalysis,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("hardware_resistance_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CyberSurfaceDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Dns,
                    contentDescription = null,
                    tint = CyberCyan,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Время взлома по оборудованию",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )
            }
            Text(
                text = "Сколько времени потребуется разным вычислительным мощностям",
                fontSize = 12.sp,
                color = TextSecondaryDark
            )

            Spacer(modifier = Modifier.height(14.dp))

            HardwareItem(
                icon = Icons.Default.Language,
                tierName = "Онлайн-сервер (с лимитом)",
                speedLabel = "100 попыток/сек",
                timeEstimate = analysis.crackTimeOnline,
                accentColor = CyberMint
            )

            Spacer(modifier = Modifier.height(8.dp))

            HardwareItem(
                icon = Icons.Default.Computer,
                tierName = "Обычный ПК (CPU перебор)",
                speedLabel = "10 млн хэшей/сек",
                timeEstimate = analysis.crackTimeCpu,
                accentColor = CyberCyan
            )

            Spacer(modifier = Modifier.height(8.dp))

            HardwareItem(
                icon = Icons.Default.Memory,
                tierName = "GPU-ферма (RTX 4090 Hashcat)",
                speedLabel = "100 млрд хэшей/сек",
                timeEstimate = analysis.crackTimeGpu,
                accentColor = CyberAmber
            )

            Spacer(modifier = Modifier.height(8.dp))

            HardwareItem(
                icon = Icons.Default.Devices,
                tierName = "Суперкомпьютер / Ботнет",
                speedLabel = "100 трлн хэшей/сек",
                timeEstimate = analysis.crackTimeSupercomputer,
                accentColor = CyberPurple
            )
        }
    }
}

@Composable
private fun HardwareItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tierName: String,
    speedLabel: String,
    timeEstimate: String,
    accentColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0D1424), RoundedCornerShape(10.dp))
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = tierName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimaryDark
                    )
                    Text(
                        text = speedLabel,
                        fontSize = 11.sp,
                        color = TextSecondaryDark,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Text(
                text = timeEstimate,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

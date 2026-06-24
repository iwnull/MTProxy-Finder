package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProxyEntity
import com.example.ui.Localization

@Composable
fun DashboardCard(
    proxies: List<ProxyEntity>,
    lang: String,
    modifier: Modifier = Modifier
) {
    val totalCount = proxies.size
    val onlineCount = proxies.count { it.isOnline }

    // Compute average ping
    val onlineProxies = proxies.filter { it.isOnline }
    val avgPing = if (onlineProxies.isNotEmpty()) {
        onlineProxies.map { it.ping }.average().toLong()
    } else {
        0L
    }

    // Stability Score calculation based on current online vs total ratio
    val stabilityRatio = if (totalCount > 0) onlineCount.toFloat() / totalCount.toFloat() else 0.0f
    val animatedRatio by animateFloatAsState(
        targetValue = stabilityRatio,
        animationSpec = tween(durationMillis = 1000),
        label = "stability_anim"
    )

    val alignEnd = if (lang == "fa") Alignment.End else Alignment.Start

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Bento Part 1: Stability Score (Primary Blue-Indigo Gradient)
        Card(
            modifier = Modifier
                .weight(1.2f)
                .height(82.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF2563EB), Color(0xFF4F46E5))
                        )
                    )
                    .padding(10.dp)
            ) {
                // Background icon decoration
                Icon(
                    imageVector = Icons.Default.Analytics,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.08f),
                    modifier = Modifier
                        .size(45.dp)
                        .align(Alignment.CenterStart)
                        .offset(x = (-5).dp, y = 5.dp)
                )

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = alignEnd
                ) {
                    Text(
                        text = Localization.get("overall_stability", lang),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.85f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        // Small animated sparkline
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            modifier = Modifier.padding(bottom = 2.dp)
                        ) {
                            val barWeights = listOf(0.4f, 0.7f, 0.5f, 0.9f, 0.6f)
                            barWeights.forEach { weight ->
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .height((16 * weight * animatedRatio).dp)
                                        .clip(RoundedCornerShape(percent = 50))
                                        .background(Color.White.copy(alpha = 0.3f + (0.7f * weight)))
                                )
                            }
                        }

                        val percentSign = if (lang == "fa") "٪" else "%"
                        Text(
                            text = "${(stabilityRatio * 100).toInt()}$percentSign",
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        // Bento Part 2: Active Proxies
        Card(
            modifier = Modifier
                .weight(1f)
                .height(82.dp)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
                    shape = RoundedCornerShape(18.dp)
                ),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = Localization.get("active_proxies", lang),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center
                )
                
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(18.dp)
                )

                val ratioText = if (lang == "fa") {
                    "$onlineCount از $totalCount"
                } else if (lang == "ru") {
                    "$onlineCount из $totalCount"
                } else {
                    "$onlineCount of $totalCount"
                }

                Text(
                    text = ratioText,
                    style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Bento Part 3: Avg Ping
        Card(
            modifier = Modifier
                .weight(1f)
                .height(82.dp)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
                    shape = RoundedCornerShape(18.dp)
                ),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = Localization.get("avg_ping", lang),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center
                )

                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(18.dp)
                )

                Text(
                    text = if (avgPing > 0) "$avgPing ms" else "—",
                    style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.sp),
                    color = if (avgPing > 0) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

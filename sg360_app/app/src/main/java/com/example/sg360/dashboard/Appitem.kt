package com.example.sg360.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.example.sg360.models.AppItemState

@Composable
fun AppItem(
    appItemState: AppItemState,
    isSelected: Boolean,
    onSelect: () -> Unit,
    scanApp: (String) -> String // Add scanApp as a parameter
) {
    val expandedState = remember { mutableStateOf(isSelected) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp, vertical = 8.dp)
                .clickable { onSelect() },
            colors = CardDefaults.cardColors(containerColor = when (appItemState.result) {
                "Benign" -> Color(0xFFDFF0D8)
                "Unknown" -> Color(0xFFD3D3D3)
                "Malware" -> Color(0xFFF2DEDE)
                else -> Color.White
            }),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 8.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when (appItemState.result) {
                            "Benign" -> Icons.Default.CheckCircle
                            "Unknown" -> Icons.Default.Info
                            "Malware" -> Icons.Default.Warning
                            else -> Icons.Default.Refresh
                        },
                        contentDescription = null,
                        tint = when (appItemState.result) {
                            "Benign" -> Color(0xFF3C763D)
                            "Unknown" -> Color(0xFF8A6D3B)
                            "Malware" -> Color(0xFFA94442)
                            else -> Color.Gray
                        },
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = appItemState.result ?: "Scan",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            color = when (appItemState.result) {
                                "Benign" -> Color(0xFF3C763D)
                                "Unknown" -> Color(0xFF8A6D3B)
                                "Malware" -> Color(0xFFA94442)
                                else -> Color.Gray
                            }
                        ),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Image(
                    bitmap = appItemState.icon,
                    contentDescription = null,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = appItemState.appName,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )

                // Content visible only when expanded
                if (expandedState.value) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            // Trigger scan using scanApp function
                            val prediction = scanApp(appItemState.packageName)
                            appItemState.result = prediction // Update result in state
                            expandedState.value = false // Collapse after scan
                        },
                        modifier = Modifier.fillMaxWidth().height(40.dp).padding(0.dp)
                    ) {
                        Text("Scan", style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 10.sp
                        ),
                            modifier = Modifier.padding(0.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if (appItemState.result != null) {
                        Text(
                            text = "Static Analysis: ${appItemState.result}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Normal,
                                fontSize = 12.sp
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
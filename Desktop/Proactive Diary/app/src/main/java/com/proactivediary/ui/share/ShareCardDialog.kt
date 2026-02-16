package com.proactivediary.ui.share

import android.graphics.Bitmap
import android.graphics.Picture
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.draw
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.proactivediary.ui.theme.DiaryColors
import com.proactivediary.ui.theme.PlusJakartaSans

@Composable
fun ShareCardDialog(
    data: ShareCardData,
    onDismiss: () -> Unit,
    onShare: (Bitmap) -> Unit
) {
    val picture = remember { Picture() }
    var selectedRatio by remember { mutableStateOf(ShareAspectRatio.STORY) }
    var selectedStyle by remember { mutableStateOf(ShareCardStyle.SUNSET) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Share your moment",
                    style = TextStyle(
                        fontFamily = PlusJakartaSans,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )

                Spacer(Modifier.height(16.dp))

                // Style selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ShareCardStyle.entries.forEach { style ->
                        val isSelected = style == selectedStyle
                        Text(
                            text = style.label,
                            style = TextStyle(
                                fontFamily = PlusJakartaSans,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                letterSpacing = 0.5.sp
                            ),
                            modifier = Modifier
                                .clickable { selectedStyle = style }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Aspect ratio selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ShareAspectRatio.entries.forEach { ratio ->
                        val isSelected = ratio == selectedRatio
                        Text(
                            text = ratio.label,
                            style = TextStyle(
                                fontFamily = PlusJakartaSans,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                            ),
                            modifier = Modifier
                                .clickable { selectedRatio = ratio }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Preview card
                Box(
                    modifier = Modifier
                        .drawWithCache {
                            val width = size.width.toInt()
                            val height = size.height.toInt()
                            onDrawWithContent {
                                val pictureCanvas = Canvas(
                                    picture.beginRecording(width, height)
                                )
                                draw(this, this.layoutDirection, pictureCanvas, this.size) {
                                    this@onDrawWithContent.drawContent()
                                }
                                picture.endRecording()
                                drawIntoCanvas { canvas ->
                                    canvas.nativeCanvas.drawPicture(picture)
                                }
                            }
                        }
                ) {
                    ShareCardPreview(
                        data = data,
                        aspectRatio = selectedRatio,
                        style = selectedStyle
                    )
                }

                Spacer(Modifier.height(20.dp))

                // Share button — gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                listOf(DiaryColors.ElectricIndigo, DiaryColors.NeonPink)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                Bitmap.createBitmap(picture)
                            } else {
                                val bmp = Bitmap.createBitmap(
                                    picture.width.coerceAtLeast(1),
                                    picture.height.coerceAtLeast(1),
                                    Bitmap.Config.ARGB_8888
                                )
                                val canvas = android.graphics.Canvas(bmp)
                                picture.draw(canvas)
                                bmp
                            }
                            onShare(bitmap)
                        }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Share",
                        style = TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                    )
                }

                Spacer(Modifier.height(8.dp))

                TextButton(onClick = onDismiss) {
                    Text(
                        text = "Cancel",
                        style = TextStyle(
                            fontFamily = PlusJakartaSans,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    )
                }
            }
        }
    }
}

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
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.drawscope.draw
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.proactivediary.ui.theme.CormorantGaramond

@Composable
fun ShareCardDialog(
    data: ShareCardData,
    onDismiss: () -> Unit,
    onShare: (Bitmap) -> Unit
) {
    val picture = remember { Picture() }
    var selectedRatio by remember { mutableStateOf(ShareAspectRatio.SQUARE) }
    val pencilColor = MaterialTheme.colorScheme.secondary
    val inkColor = MaterialTheme.colorScheme.onBackground

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Share your page",
                    style = TextStyle(
                        fontFamily = CormorantGaramond,
                        fontSize = 22.sp,
                        color = inkColor
                    )
                )

                Spacer(Modifier.height(12.dp))

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
                                fontSize = 12.sp,
                                color = if (isSelected) inkColor else pencilColor.copy(alpha = 0.5f),
                                letterSpacing = 0.5.sp
                            ),
                            modifier = Modifier
                                .clickable { selectedRatio = ratio }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Preview card with picture recording for bitmap capture
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
                    ShareCardPreview(data = data, aspectRatio = selectedRatio)
                }

                Spacer(Modifier.height(20.dp))

                // Share button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = inkColor,
                            shape = RoundedCornerShape(4.dp)
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
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "SHARE",
                        style = TextStyle(
                            fontSize = 13.sp,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.surface
                        )
                    )
                }

                Spacer(Modifier.height(8.dp))

                TextButton(onClick = onDismiss) {
                    Text(
                        text = "Cancel",
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = pencilColor
                        )
                    )
                }
            }
        }
    }
}

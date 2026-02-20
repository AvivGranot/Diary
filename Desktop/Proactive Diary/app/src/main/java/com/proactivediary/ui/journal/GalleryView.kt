package com.proactivediary.ui.journal

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.proactivediary.data.db.entities.EntryEntity
import com.proactivediary.data.media.ImageMetadata
import com.proactivediary.data.media.ImageStorageManager
import com.proactivediary.data.repository.EntryRepository
import com.proactivediary.ui.theme.CormorantGaramond
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

data class GalleryImage(
    val entryId: String,
    val imageMetadata: ImageMetadata
)

@HiltViewModel
class GalleryViewModel @Inject constructor(
    private val entryRepository: EntryRepository,
    val imageStorageManager: ImageStorageManager
) : androidx.lifecycle.ViewModel() {

    private val gson = Gson()

    val galleryImages: Flow<List<GalleryImage>> = entryRepository.getEntriesWithImages()
        .map { entries ->
            entries.flatMap { entry ->
                parseImages(entry.images).map { imageMetadata ->
                    GalleryImage(entry.id, imageMetadata)
                }
            }
        }

    private fun parseImages(json: String): List<ImageMetadata> {
        return try {
            val type = object : TypeToken<List<ImageMetadata>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }
}

@Composable
fun GalleryView(
    onImageClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GalleryViewModel = hiltViewModel()
) {
    val galleryImages by viewModel.galleryImages.collectAsState(initial = emptyList())

    if (galleryImages.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "No photos yet",
                    style = TextStyle(
                        fontFamily = CormorantGaramond,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Photos from your entries will appear here",
                    style = TextStyle(
                        fontFamily = FontFamily.Default,
                        fontSize = 14.sp,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(galleryImages, key = { it.imageMetadata.id }) { galleryImage ->
                val thumbnailFile = viewModel.imageStorageManager.getThumbnailFile(
                    galleryImage.entryId,
                    galleryImage.imageMetadata.filename
                )

                AsyncImage(
                    model = thumbnailFile,
                    contentDescription = "Entry photo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onImageClick(galleryImage.entryId) },
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

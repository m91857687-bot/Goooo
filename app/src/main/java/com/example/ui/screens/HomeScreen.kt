package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.border
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import com.example.viewmodel.GalleryUiModel
import com.example.viewmodel.MainViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: MainViewModel, onMediaClick: (Long) -> Unit) {
    val pagingItems = viewModel.mediaPagingData.collectAsLazyPagingItems()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("المعرض", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                ),
                actions = {
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(Icons.Default.Search, contentDescription = "بحث")
                    }
                    IconButton(onClick = { /* TODO */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "المزيد")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(100.dp),
            contentPadding = paddingValues,
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp)
        ) {
            items(
                count = pagingItems.itemCount,
                span = { index ->
                    val item = pagingItems[index]
                    if (item is GalleryUiModel.Header) {
                        GridItemSpan(maxLineSpan)
                    } else {
                        GridItemSpan(1)
                    }
                }
            ) { index ->
                val uiModel = pagingItems[index]
                when (uiModel) {
                    is GalleryUiModel.Header -> {
                        Text(
                            text = uiModel.title,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = androidx.compose.ui.graphics.Color.Gray,
                                letterSpacing = 1.sp
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp)
                        )
                    }
                    is GalleryUiModel.Item -> {
                        val mediaItem = uiModel.mediaItem
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.dp, androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                                .clickable { onMediaClick(mediaItem.id) }
                        ) {
                            AsyncImage(
                                model = mediaItem.uri,
                                contentDescription = mediaItem.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            if (mediaItem.isVideo) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(8.dp)
                                        .background(
                                            androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.4f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    androidx.compose.foundation.layout.Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayCircleOutline,
                                            contentDescription = "فيديو",
                                            tint = androidx.compose.ui.graphics.Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(
                                            text = formatDuration(mediaItem.duration),
                                            color = androidx.compose.ui.graphics.Color.White,
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    null -> {
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                    }
                }
            }
        }
    }
}

fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}

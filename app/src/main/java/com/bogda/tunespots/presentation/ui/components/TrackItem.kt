package com.bogda.tunespots.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.bogda.tunespots.R
import com.bogda.tunespots.data.model.Track

@Composable
fun TrackItem(
    track: Track,
    modifier: Modifier = Modifier,
    isSelected: Boolean? = null,
    onToggleSelect: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(MaterialTheme.shapes.medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(track.coverArtUrl)
                .crossfade(true)
                .build(),
            placeholder = painterResource(R.drawable.ic_music_placeholder),
            error = painterResource(R.drawable.ic_music_placeholder),
            contentDescription = "Cover for ${track.title}",
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(80.dp).clip(MaterialTheme.shapes.medium)
        )
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(track.title, maxLines = 1, style = MaterialTheme.typography.titleMedium)
            Text(track.artist, maxLines = 1, style = MaterialTheme.typography.bodyMedium)
        }
        if (isSelected != null && onToggleSelect != null) {
            IconButton(onClick = onToggleSelect) {
                Icon(
                    imageVector = if (isSelected) Icons.Default.Remove else Icons.Default.Add,
                    contentDescription = if (isSelected) "Remove song" else "Add song"
                )
            }
        }
    }
}

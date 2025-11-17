package com.bogda.tunespots.presentation.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
    ListItem(
        modifier = modifier,
        headlineContent = { Text(track.title, maxLines = 1) },
        supportingContent = { Text(track.artist, maxLines = 1) },
        leadingContent = {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(track.coverArtUrl)
                    .crossfade(true)
                    .build(),
                placeholder = painterResource(R.drawable.ic_music_placeholder),
                error = painterResource(R.drawable.ic_music_placeholder),
                contentDescription = "Cover for ${track.title}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(56.dp).clip(MaterialTheme.shapes.small)
            )
        },
        trailingContent = {
            if (isSelected != null && onToggleSelect != null) {
                IconButton(onClick = onToggleSelect) {
                    Icon(
                        imageVector = if (isSelected) Icons.Default.Remove else Icons.Default.Add,
                        contentDescription = if (isSelected) "Remove song" else "Add song"
                    )
                }
            }
        }
    )
}

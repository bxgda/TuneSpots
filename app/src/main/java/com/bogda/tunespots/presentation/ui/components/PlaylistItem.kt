package com.bogda.tunespots.presentation.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.bogda.tunespots.R
import com.bogda.tunespots.domain.model.Playlist

@Composable
fun PlaylistItem(
    playlist: Playlist,
    authorName: String,
    authorImageUrl: String?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (playlist.coverImageUrl?.isNotEmpty() == true) {
                AsyncImage(
                    model = playlist.coverImageUrl,
                    contentDescription = "Playlist image",
                    modifier = Modifier.size(100.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = "No playlist image",
                    modifier = Modifier.size(100.dp)
                )
            }
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(playlist.name, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = authorImageUrl,
                        contentDescription = "Author image",
                        placeholder = painterResource(id = R.drawable.ic_music_placeholder),
                        error = painterResource(id = R.drawable.ic_music_placeholder),
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                    )
                    Text(" by $authorName", style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic))
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("# ${playlist.genre}", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(4.dp))
                Text("${playlist.tracks.size} tracks", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

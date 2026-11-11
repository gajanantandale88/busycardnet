package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.ui.components.BcnCardHyperlink
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.FeedPostEntity
import com.example.ui.viewmodel.BusyCardNetViewModel

@Composable
fun VideoFeedScreen(
  viewModel: BusyCardNetViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val videoPosts by viewModel.videoPosts.collectAsStateWithLifecycle()
  val connectedBridges by viewModel.connectedBridges.collectAsStateWithLifecycle()

  var selectedFilter by remember { mutableStateOf("ALL") } // "ALL" vs "CONNECTED_BRIDGES"
  var showPostDialog by remember { mutableStateOf(false) }

  val filteredPosts = remember(videoPosts, selectedFilter, connectedBridges) {
    if (selectedFilter == "CONNECTED_BRIDGES") {
      val connectedUserIds = connectedBridges.map { it.requesterUserId }.toSet() +
          connectedBridges.map { it.targetUserId }.toSet()
      videoPosts.filter { it.authorUserId in connectedUserIds || it.postScope == "CONNECTED_BRIDGES" }
    } else {
      videoPosts
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFF070C15))
      .testTag("video_feed_screen")
  ) {
    // Top Bar
    Surface(
      modifier = Modifier.fillMaxWidth(),
      color = Color(0xFF0F172A),
      shadowElevation = 4.dp
    ) {
      Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(Color(0xFFEF4444)),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.Videocam, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "BCN VIDEO BROADCAST",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.2.sp,
                color = Color(0xFFEF4444)
              )
              Text(
                text = "Business Pages & Video Posts",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            }
          }

          // Button: Post Video
          Button(
            onClick = { showPostDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            modifier = Modifier.testTag("post_video_button")
          ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Post Video", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Audience Filter: [All Video Posts] | [Connected Bridges Only]
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          FilterChip(
            selected = selectedFilter == "ALL",
            onClick = { selectedFilter = "ALL" },
            label = { Text("All Broadcasts (${videoPosts.size})", fontSize = 11.sp, fontWeight = if (selectedFilter == "ALL") FontWeight.Bold else FontWeight.Medium) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = Color(0xFF0284C7),
              selectedLabelColor = Color.White,
              containerColor = Color(0xFF1E293B),
              labelColor = Color(0xFFCBD5E1)
            )
          )

          FilterChip(
            selected = selectedFilter == "CONNECTED_BRIDGES",
            onClick = { selectedFilter = "CONNECTED_BRIDGES" },
            label = { Text("🤝 Connected Bridges Only", fontSize = 11.sp, fontWeight = if (selectedFilter == "CONNECTED_BRIDGES") FontWeight.Bold else FontWeight.Medium) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = Color(0xFF10B981),
              selectedLabelColor = Color.White,
              containerColor = Color(0xFF1E293B),
              labelColor = Color(0xFFCBD5E1)
            )
          )
        }
      }
    }

    // Video Feed Stream
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 14.dp),
      contentPadding = PaddingValues(vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      items(filteredPosts, key = { it.id }) { post ->
        VideoFeedItemCard(
          post = post,
          onLike = { viewModel.likePost(post.id) },
          onConnect = {
            viewModel.sendConnectRequest(
              targetUserId = post.authorUserId,
              targetCardId = "card-${post.authorUserId}",
              targetName = post.authorName,
              targetBusyCardNetId = post.authorBusyCardNetId,
              targetProfession = post.authorProfession,
              note = "Connected through Video Post"
            )
            Toast.makeText(context, "🤝 Bridge connected with ${post.authorName}!", Toast.LENGTH_SHORT).show()
          }
        )
      }
    }
  }

  // Dialog to Post Video / Business Page
  if (showPostDialog) {
    PostVideoDialog(
      onDismiss = { showPostDialog = false },
      onSubmit = { caption, videoUrl, ctaType, ctaPhone ->
        viewModel.postVideo(
          content = caption,
          videoUrl = videoUrl,
          ctaType = ctaType,
          ctaPhone = ctaPhone
        )
        showPostDialog = false
        Toast.makeText(context, "🎉 Video Broadcast published to your Network!", Toast.LENGTH_SHORT).show()
      }
    )
  }
}

@Composable
fun VideoFeedItemCard(
  post: FeedPostEntity,
  onLike: () -> Unit,
  onConnect: () -> Unit
) {
  val context = LocalContext.current
  var isPlaying by remember { mutableStateOf(true) }
  var isLiked by remember { mutableStateOf(false) }
  var likeCount by remember(post.likesCount) { mutableStateOf(post.likesCount) }

  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("video_card_${post.id}")
  ) {
    Column {
      // 1. Author Header
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(CircleShape)
              .background(Color(0xFFF59E0B).copy(alpha = 0.2f))
              .border(1.dp, Color(0xFFF59E0B), CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = if (post.authorProfession.contains("Elec", true)) "⚡" else if (post.authorProfession.contains("Plum", true)) "🔧" else "💼",
              fontSize = 18.sp
            )
          }

          Spacer(modifier = Modifier.width(10.dp))

          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = post.authorName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
              Spacer(modifier = Modifier.width(4.dp))
              Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(14.dp))
            }
            Text(
              text = "${post.authorProfession} • ${post.locationName}",
              fontSize = 11.sp,
              color = Color(0xFF94A3B8)
            )
            BcnCardHyperlink(
              bcnId = post.authorBusyCardNetId,
              color = Color(0xFF38BDF8),
              fontSize = 10.sp,
              modifier = Modifier.padding(top = 2.dp)
            )
          }
        }

        // BCN ID Badge (Mandatory Position & Branding - Clickable Hyperlink)
        Surface(
          shape = RoundedCornerShape(6.dp),
          color = Color(0xFF38BDF8).copy(alpha = 0.15f),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f))
        ) {
          BcnCardHyperlink(
            bcnId = post.authorBusyCardNetId,
            color = Color(0xFF38BDF8),
            prefix = "",
            fontSize = 10.sp,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
          )
        }
      }

      // 2. Video Player Frame (Rich interactive simulated player container)
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(240.dp)
          .background(
            Brush.verticalGradient(
              listOf(Color(0xFF030712), Color(0xFF111827), Color(0xFF030712))
            )
          )
          .clickable { isPlaying = !isPlaying }
      ) {
        // Simulated Video Visualizer / Backdrop
        Column(
          modifier = Modifier.fillMaxSize(),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
        ) {
          Box(
            modifier = Modifier
              .size(60.dp)
              .clip(CircleShape)
              .background(Color.Black.copy(alpha = 0.6f))
              .border(2.dp, if (isPlaying) Color(0xFF10B981) else Color(0xFFEF4444), CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = if (isPlaying) Icons.Default.PlayArrow else Icons.Default.Stop,
              contentDescription = "Video playback",
              tint = Color.White,
              modifier = Modifier.size(36.dp)
            )
          }
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = if (isPlaying) "Playing Business Showcase • Tap to Pause" else "Paused • Tap to Play",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White.copy(alpha = 0.8f)
          )
        }

        // Top badges: "HD VIDEO", "LIVE", Speaker
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            shape = RoundedCornerShape(4.dp),
            color = Color(0xFFEF4444)
          ) {
            Text(
              text = "● VIDEO AD",
              fontSize = 9.sp,
              fontWeight = FontWeight.Black,
              color = Color.White,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }

          Surface(
            shape = CircleShape,
            color = Color.Black.copy(alpha = 0.5f)
          ) {
            Icon(
              imageVector = Icons.Default.VolumeUp,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier
                .padding(6.dp)
                .size(16.dp)
            )
          }
        }

        // Floating Engagement Controls on Right (Like, Comment, Share, Connect)
        Column(
          modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(12.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // Like
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
              modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.55f))
                .clickable {
                  isLiked = !isLiked
                  likeCount += if (isLiked) 1 else -1
                  onLike()
                },
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "Like",
                tint = if (isLiked) Color(0xFFEF4444) else Color.White,
                modifier = Modifier.size(20.dp)
              )
            }
            Text(text = "$likeCount", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
          }

          // Share
          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(CircleShape)
              .background(Color.Black.copy(alpha = 0.55f))
              .clickable {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                  type = "text/plain"
                  putExtra(Intent.EXTRA_TEXT, "Watch business video from ${post.authorName} on BizCardNet (${post.authorBusyCardNetId})")
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share Video"))
              },
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White, modifier = Modifier.size(18.dp))
          }
        }
      }

      // 3. Caption & Details
      Column(modifier = Modifier.padding(14.dp)) {
        Text(
          text = post.content,
          fontSize = 13.sp,
          color = Color(0xFFF1F5F9),
          lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Actions: [Direct Call] [WhatsApp] [🤝 Connect Bridge]
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          if (post.ctaPhone.isNotBlank()) {
            Button(
              onClick = {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${post.ctaPhone}"))
                context.startActivity(intent)
              },
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.weight(1f)
            ) {
              Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Call Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Button(
              onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=${post.ctaPhone}"))
                try { context.startActivity(intent) } catch (e: Exception) {}
              },
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.weight(1f)
            ) {
              Text("💬", fontSize = 13.sp)
              Spacer(modifier = Modifier.width(4.dp))
              Text("WhatsApp", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
          }

          // Build Bridge Button
          Button(
            onClick = onConnect,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.weight(1.2f)
          ) {
            Text("🤝 Connect", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
          }
        }
      }
    }
  }
}

@Composable
fun PostVideoDialog(
  onDismiss: () -> Unit,
  onSubmit: (caption: String, videoUrl: String, ctaType: String, ctaPhone: String) -> Unit
) {
  var caption by remember { mutableStateOf("") }
  var ctaPhone by remember { mutableStateOf("+91 98765 43210") }
  var ctaType by remember { mutableStateOf("CALL") }
  var selectedVideoDemo by remember { mutableStateOf("Wiring & MCB Overhaul") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = "Post Video & Page Broadcast",
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White
      )
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
          text = "Share video demonstrations, visiting card showcases, or trade advertisements with your connected cardholders.",
          fontSize = 12.sp,
          color = Color(0xFF94A3B8)
        )

        OutlinedTextField(
          value = caption,
          onValueChange = { caption = it },
          label = { Text("Video Caption & Business Details") },
          placeholder = { Text("Describe your services, current discounts, or latest projects...") },
          modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
          colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFF1E293B),
            unfocusedContainerColor = Color(0xFF1E293B),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
          )
        )

        OutlinedTextField(
          value = ctaPhone,
          onValueChange = { ctaPhone = it },
          label = { Text("Contact Phone / WhatsApp for CTA") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFF1E293B),
            unfocusedContainerColor = Color(0xFF1E293B),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
          )
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
          onSubmit(
            caption.ifEmpty { "New business video showcase and services announcement on BizCardNet!" },
            url,
            ctaType,
            ctaPhone
          )
        },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
        shape = RoundedCornerShape(8.dp)
      ) {
        Text("Publish Broadcast", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      Button(
        onClick = onDismiss,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
        shape = RoundedCornerShape(8.dp)
      ) {
        Text("Cancel")
      }
    },
    containerColor = Color(0xFF0F172A)
  )
}

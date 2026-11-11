package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

/**
 * Local Radius Advertisement Creator (Prompt Sections 18-20)
 */
@Composable
fun CreateAdDialog(
  onDismiss: () -> Unit,
  onSubmit: (content: String, radiusMeters: Int, durationDays: Int, budgetRupees: Int, ctaType: String) -> Unit
) {
  var content by remember { mutableStateOf("Grand Weekend Offer! Flat 20% off on all services/products. Visit our shop or contact us directly on WhatsApp.") }
  var selectedRadius by remember { mutableIntStateOf(3000) } // 3km default
  var selectedDuration by remember { mutableIntStateOf(7) } // 7 days
  var selectedCta by remember { mutableStateOf("WHATSAPP") }
  val scrollState = rememberScrollState()

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = MaterialTheme.colorScheme.surface,
      modifier = Modifier
        .fillMaxWidth()
        .testTag("create_ad_dialog")
    ) {
      Column(
        modifier = Modifier
          .padding(20.dp)
          .verticalScroll(scrollState)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Campaign,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Create Local Advertisement",
              fontSize = 17.sp,
              fontWeight = FontWeight.Bold
            )
          }

          IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(text = "Ad Description & Message:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
          value = content,
          onValueChange = { content = it },
          placeholder = { Text("What are you promoting?") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp),
          minLines = 3
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Target Area / Radius Selection (Prompt Section 18: 500m, 1km, 2km, 5km, 10km, 25km)
        Text(text = "Target Area Radius:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          listOf(500 to "500m", 1000 to "1 km", 3000 to "3 km", 5000 to "5 km", 10000 to "10 km").forEach { (meters, label) ->
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (selectedRadius == meters) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
              modifier = Modifier
                .weight(1f)
                .clickable { selectedRadius = meters }
            ) {
              Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(vertical = 7.dp)
              ) {
                Text(
                  text = label,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (selectedRadius == meters) Color.White else MaterialTheme.colorScheme.onSurface
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Duration Selection (Prompt Section 19: 1 day, 3 days, 7 days, 30 days)
        Text(text = "Campaign Duration:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          listOf(1, 3, 7, 30).forEach { days ->
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (selectedDuration == days) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
              modifier = Modifier
                .weight(1f)
                .clickable { selectedDuration = days }
            ) {
              Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(vertical = 7.dp)
              ) {
                Text(
                  text = "$days Days",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (selectedDuration == days) Color.White else MaterialTheme.colorScheme.onSurface
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Action Button CTA (Prompt Section 19: Call, WhatsApp, View Card)
        Text(text = "Direct Call-to-Action Button:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          listOf("WHATSAPP" to "💬 WhatsApp", "CALL" to "📞 Phone Call", "VIEW_CARD" to "📇 View Card").forEach { (cta, label) ->
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (selectedCta == cta) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
              modifier = Modifier
                .weight(1f)
                .clickable { selectedCta = cta }
            ) {
              Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(vertical = 8.dp)
              ) {
                Text(
                  text = label,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (selectedCta == cta) Color.White else MaterialTheme.colorScheme.onSurface
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
          onClick = {
            if (content.isNotBlank()) {
              onSubmit(content, selectedRadius, selectedDuration, selectedDuration * 120, selectedCta)
            }
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("submit_ad_button"),
          shape = RoundedCornerShape(12.dp)
        ) {
          Text(text = "PUBLISH ADVERTISEMENT (₹${selectedDuration * 120})", fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

/**
 * Sell Anything Modal Dialog (Prompt Section 21)
 */
@Composable
fun SellProductDialog(
  onDismiss: () -> Unit,
  onSubmit: (title: String, description: String, price: Double, condition: String, category: String, pickup: String, imageUrl: String) -> Unit
) {
  var title by remember { mutableStateOf("Wooden Dining Table (4-Seater)") }
  var description by remember { mutableStateOf("Well maintained solid teak wood table with 4 cushioned chairs. Moving out.") }
  var priceStr by remember { mutableStateOf("3500") }
  var condition by remember { mutableStateOf("USED") }
  var category by remember { mutableStateOf("FURNITURE") }
  var pickupOption by remember { mutableStateOf("PICKUP") }
  var imageUrl by remember { mutableStateOf("https://images.unsplash.com/photo-1577140917170-285929fb55b7?w=600") }
  val scrollState = rememberScrollState()

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = MaterialTheme.colorScheme.surface,
      modifier = Modifier
        .fillMaxWidth()
        .testTag("sell_product_dialog")
    ) {
      Column(
        modifier = Modifier
          .padding(20.dp)
          .verticalScroll(scrollState)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Sell,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Sell Something Locally",
              fontSize = 17.sp,
              fontWeight = FontWeight.Bold
            )
          }

          IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(text = "Item Title:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          placeholder = { Text("e.g. Used Office Chair, Mobile Phone...") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(text = "Price (₹):", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
          value = priceStr,
          onValueChange = { priceStr = it },
          placeholder = { Text("e.g. 2500") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(text = "Condition:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(4.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          listOf("USED" to "Used Item", "NEW" to "Brand New").forEach { (cnd, label) ->
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (condition == cnd) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
              modifier = Modifier
                .weight(1f)
                .clickable { condition = cnd }
            ) {
              Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(vertical = 8.dp)
              ) {
                Text(
                  text = label,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (condition == cnd) Color.White else MaterialTheme.colorScheme.onSurface
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(text = "Category:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(4.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          listOf("FURNITURE" to "Furniture", "ELECTRONICS" to "Gadgets", "TOOLS" to "Tools", "HOME" to "Home").forEach { (cat, label) ->
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (category == cat) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
              modifier = Modifier
                .weight(1f)
                .clickable { category = cat }
            ) {
              Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(vertical = 7.dp)
              ) {
                Text(
                  text = label,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (category == cat) Color.White else MaterialTheme.colorScheme.onSurface
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(text = "Photo of Item (Image URL):", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
          value = imageUrl,
          onValueChange = { imageUrl = it },
          placeholder = { Text("https://example.com/product_photo.jpg") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(6.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          listOf(
            "Furniture" to "https://images.unsplash.com/photo-1577140917170-285929fb55b7?w=600",
            "Gadget" to "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=600",
            "Drill" to "https://images.unsplash.com/photo-1504148455328-c376907d081c?w=600"
          ).forEach { (lbl, url) ->
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = if (imageUrl == url) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
              modifier = Modifier.clickable { imageUrl = url }
            ) {
              Text(
                text = "Sample $lbl",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(text = "Description:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
          value = description,
          onValueChange = { description = it },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(10.dp),
          minLines = 2
        )

        Spacer(modifier = Modifier.height(18.dp))

        Button(
          onClick = {
            val price = priceStr.toDoubleOrNull() ?: 1000.0
            if (title.isNotBlank()) {
              onSubmit(title, description, price, condition, category, pickupOption, imageUrl)
            }
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("submit_product_button"),
          shape = RoundedCornerShape(12.dp)
        ) {
          Text("LIST ITEM ON MARKETPLACE", fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.ChatMessage
import com.example.data.model.Conversation
import com.example.data.model.NotificationItem
import com.example.ui.theme.BeityGold
import com.example.ui.theme.BeityNavy
import com.example.ui.theme.BeityWhite
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun MessagesNotificationsScreen(
    currentUserId: String,
    conversations: List<Conversation>,
    notifications: List<NotificationItem>,
    initialTab: Int = 0,
    onOpenConversation: (Conversation) -> Unit,
    onMarkNotificationRead: (NotificationItem) -> Unit
) {
    var selectedTab by remember(initialTab) { mutableIntStateOf(initialTab) }
    val unreadCount = remember(notifications) { notifications.count { !it.read } }

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(color = BeityNavy, contentColor = BeityWhite) {
            Column {
                Text(
                    text = "المحادثات والإشعارات",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = BeityWhite,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
                )
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = BeityNavy,
                    contentColor = BeityGold,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = BeityGold
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("المحادثات (${conversations.size})") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("الإشعارات ($unreadCount)") }
                    )
                }
            }
        }

        if (selectedTab == 0) {
            if (conversations.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = null, tint = BeityGold, modifier = Modifier.size(56.dp))
                        Text("لا توجد محادثات بعد", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("تواصل مع أي معلن من صفحة تفاصيل العقار وستظهر محادثاتك هنا.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(conversations, key = { it.id }) { conv ->
                        val otherName = conv.participantNames.entries
                            .firstOrNull { it.key != currentUserId }?.value ?: "مستخدم بيتي"
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onOpenConversation(conv) },
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(BeityNavy),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = otherName.take(1),
                                        color = BeityGold,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text(otherName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        text = conv.propertyTitle,
                                        color = BeityGold,
                                        style = MaterialTheme.typography.labelSmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = conv.lastMessage,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            if (notifications.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Outlined.NotificationsNone, contentDescription = null, tint = BeityGold, modifier = Modifier.size(56.dp))
                        Text("لا توجد إشعارات حالياً", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(notifications, key = { it.id }) { notif ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onMarkNotificationRead(notif) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (!notif.read) BeityGold.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.NotificationsActive,
                                    contentDescription = null,
                                    tint = if (!notif.read) BeityGold else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(notif.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                    Text(notif.body, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatThreadScreen(
    conversation: Conversation,
    currentUserId: String,
    messages: List<ChatMessage>,
    onBack: () -> Unit,
    onSendMessage: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }
    val otherName = conversation.participantNames.entries
        .firstOrNull { it.key != currentUserId }?.value ?: "مستخدم بيتي"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(otherName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(conversation.propertyTitle, style = MaterialTheme.typography.labelSmall, color = BeityGold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "رجوع")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BeityNavy,
                    titleContentColor = BeityWhite,
                    navigationIconContentColor = BeityWhite
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    val isMine = msg.senderId == currentUserId
                    val timeLabel = msg.createdAt?.toDate()?.let {
                        SimpleDateFormat("HH:mm", Locale.forLanguageTag("ar")).format(it)
                    }.orEmpty()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isMine) Arrangement.Start else Arrangement.End
                    ) {
                        Surface(
                            color = if (isMine) BeityNavy else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (isMine) BeityWhite else MaterialTheme.colorScheme.onSurface,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.widthIn(max = 290.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(msg.text, style = MaterialTheme.typography.bodyMedium)
                                if (timeLabel.isNotEmpty()) {
                                    Text(
                                        text = timeLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isMine) BeityGold else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text("اكتب رسالتك...") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = {
                        if (text.trim().isNotEmpty()) {
                            onSendMessage(text.trim())
                            text = ""
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(BeityNavy)
                ) {
                    Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = "إرسال", tint = BeityGold)
                }
            }
        }
    }
}

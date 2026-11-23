package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PauseCircle
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material.icons.outlined.ReportProblem
import androidx.compose.material.icons.rounded.AddHome
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AccountType
import com.example.data.model.PropertyItem
import com.example.data.model.PropertyStatus
import com.example.data.model.SearchHistoryItem
import com.example.data.model.UiState
import com.example.data.model.UserProfile
import com.example.data.model.formatPriceAr
import com.example.ui.components.BeityPropertyImage
import com.example.ui.components.compressImageUriToDataUrl
import com.example.ui.theme.BeityError
import com.example.ui.theme.BeityGold
import com.example.ui.theme.BeityNavy
import com.example.ui.theme.BeitySuccess
import com.example.ui.theme.BeityWarning
import com.example.ui.theme.BeityWhite

@Composable
fun ProfileScreen(
    userProfile: UserProfile?,
    fallbackName: String,
    fallbackEmail: String?,
    isAdmin: Boolean,
    isDarkTheme: Boolean,
    myPropertiesState: UiState<List<PropertyItem>>,
    searchHistory: List<SearchHistoryItem>,
    onToggleDarkTheme: (Boolean) -> Unit,
    onUpdateProfile: (String, String?, String?, AccountType, String?) -> Unit,
    onAddProperty: () -> Unit,
    onOpenPropertyDetail: (PropertyItem) -> Unit,
    onEditProperty: (PropertyItem) -> Unit,
    onTogglePauseProperty: (String, Boolean) -> Unit,
    onDeleteProperty: (String) -> Unit,
    onApplySearchHistory: (SearchHistoryItem) -> Unit,
    onOpenAdminDashboard: () -> Unit,
    onOpenInfoPage: (String) -> Unit,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    var showEditDialog by remember { mutableStateOf(false) }

    val avatarPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            compressImageUriToDataUrl(context, uri, 320)?.let { dataUrl ->
                onUpdateProfile(
                    userProfile?.displayName ?: fallbackName,
                    userProfile?.phone,
                    userProfile?.city,
                    AccountType.fromKey(userProfile?.accountType),
                    dataUrl
                )
            }
        }
    }

    val displayName = userProfile?.displayName?.ifBlank { fallbackName } ?: fallbackName
    val accountRoleLabel = if (isAdmin) {
        "مسؤول إدارة النظام"
    } else {
        AccountType.fromKey(userProfile?.accountType).labelAr
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Profile Header
        item {
            Surface(color = BeityNavy, contentColor = BeityWhite) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(modifier = Modifier.size(84.dp)) {
                        val avatar = userProfile?.avatarUrl
                        if (!avatar.isNullOrBlank()) {
                            BeityPropertyImage(
                                imageSource = avatar,
                                contentDescription = "الصورة الشخصية",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(BeityGold),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Rounded.Person, contentDescription = null, tint = BeityNavy, modifier = Modifier.size(44.dp))
                            }
                        }
                        IconButton(
                            onClick = {
                                avatarPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(BeityWhite)
                        ) {
                            Icon(Icons.Rounded.CameraAlt, contentDescription = "تغيير الصورة", tint = BeityNavy, modifier = Modifier.size(16.dp))
                        }
                    }

                    Text(displayName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = BeityWhite)
                    if (!fallbackEmail.isNullOrBlank()) {
                        Text(fallbackEmail, style = MaterialTheme.typography.bodySmall, color = BeityWhite.copy(alpha = 0.78f))
                    }
                    Surface(color = BeityGold, shape = RoundedCornerShape(14.dp)) {
                        Text(
                            text = accountRoleLabel,
                            color = BeityNavy,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        )
                    }
                    OutlinedButton(
                        onClick = { showEditDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BeityGold)
                    ) {
                        Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("تعديل البيانات الشخصية")
                    }
                }
            }
        }

        // Admin Dashboard Entry
        if (isAdmin) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .clickable(onClick = onOpenAdminDashboard)
                        .testTag("open_admin_dashboard_card"),
                    colors = CardDefaults.cardColors(containerColor = BeityGold)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(Icons.Rounded.AdminPanelSettings, contentDescription = null, tint = BeityNavy, modifier = Modifier.size(32.dp))
                            Column {
                                Text("لوحة تحكم الإدارة", color = BeityNavy, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text("مراجعة الإعلانات، إدارة المستخدمين، والتقارير", color = BeityNavy.copy(alpha = 0.85f), style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }

        // My Properties Manager
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("عقاراتي المعلنة", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        TextButton(onClick = onAddProperty) {
                            Icon(Icons.Rounded.AddHome, contentDescription = null, tint = BeityGold, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("إضافة عقار", color = BeityGold)
                        }
                    }

                    when (myPropertiesState) {
                        is UiState.Loading -> Text("جاري تحميل عقاراتك...", style = MaterialTheme.typography.bodySmall)
                        is UiState.Error -> Text(myPropertiesState.message, color = BeityError, style = MaterialTheme.typography.bodySmall)
                        is UiState.Success -> {
                            val list = myPropertiesState.data
                            if (list.isEmpty()) {
                                Text("لم تضف أي عقارات بعد.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            } else {
                                list.forEach { prop ->
                                    val statusEnum = prop.statusEnum()
                                    val statusColor = when (statusEnum) {
                                        PropertyStatus.APPROVED -> BeitySuccess
                                        PropertyStatus.PENDING -> BeityWarning
                                        PropertyStatus.REJECTED -> BeityError
                                        PropertyStatus.PAUSED -> MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                                            .clickable { onOpenPropertyDetail(prop) }
                                            .padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(prop.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                                            Surface(color = statusColor, shape = RoundedCornerShape(8.dp)) {
                                                Text(statusEnum.labelAr, color = BeityWhite, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                                            }
                                        }
                                        Text("${formatPriceAr(prop.price)} • ${prop.city}", style = MaterialTheme.typography.bodySmall, color = BeityGold)
                                        if (statusEnum == PropertyStatus.REJECTED && !prop.rejectionReason.isNullOrBlank()) {
                                            Text("سبب الرفض: ${prop.rejectionReason}", color = BeityError, style = MaterialTheme.typography.bodySmall)
                                        }
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            OutlinedButton(onClick = { onEditProperty(prop) }) {
                                                Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("تعديل", style = MaterialTheme.typography.labelSmall)
                                            }
                                            if (statusEnum == PropertyStatus.APPROVED || statusEnum == PropertyStatus.PAUSED) {
                                                val isPaused = statusEnum == PropertyStatus.PAUSED
                                                OutlinedButton(onClick = { onTogglePauseProperty(prop.id, !isPaused) }) {
                                                    Icon(
                                                        if (isPaused) Icons.Outlined.PlayCircle else Icons.Outlined.PauseCircle,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(if (isPaused) "إعادة نشر" else "إيقاف", style = MaterialTheme.typography.labelSmall)
                                                }
                                            }
                                            IconButton(onClick = { onDeleteProperty(prop.id) }) {
                                                Icon(Icons.Outlined.Delete, contentDescription = "حذف الإعلان", tint = BeityError)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Recent Search History
        if (searchHistory.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("سجل عمليات البحث", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        searchHistory.take(5).forEach { hist ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onApplySearchHistory(hist) }
                                    .padding(vertical = 8.dp, horizontal = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Outlined.History, contentDescription = null, tint = BeityGold, modifier = Modifier.size(18.dp))
                                Text(hist.querySummary, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }

        // Settings & Legal / Info Pages
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column {
                    ListItem(
                        headlineContent = { Text("الوضع الليلي") },
                        supportingContent = { Text("التبديل بين المظهر النهاري والليلي الفاخر") },
                        leadingContent = { Icon(Icons.Outlined.DarkMode, contentDescription = null, tint = BeityGold) },
                        trailingContent = {
                            Switch(
                                checked = isDarkTheme,
                                onCheckedChange = onToggleDarkTheme,
                                modifier = Modifier.testTag("dark_theme_switch")
                            )
                        }
                    )
                    HorizontalDivider()
                    InfoLinkRow("من نحن", Icons.Outlined.Info) { onOpenInfoPage("about") }
                    InfoLinkRow("اتصل بنا", Icons.AutoMirrored.Outlined.HelpOutline) { onOpenInfoPage("contact") }
                    InfoLinkRow("الأسئلة الشائعة", Icons.AutoMirrored.Outlined.HelpOutline) { onOpenInfoPage("faq") }
                    InfoLinkRow("سياسة الخصوصية", Icons.Outlined.Policy) { onOpenInfoPage("privacy") }
                    InfoLinkRow("الشروط والأحكام", Icons.Outlined.Policy) { onOpenInfoPage("terms") }
                    InfoLinkRow("سياسة نشر الإعلانات", Icons.Outlined.Policy) { onOpenInfoPage("publishing") }
                    InfoLinkRow("الإبلاغ عن مشكلة", Icons.Outlined.ReportProblem) { onOpenInfoPage("report_issue") }
                }
            }
        }

        // Sign Out
        item {
            Button(
                onClick = onSignOut,
                colors = ButtonDefaults.buttonColors(containerColor = BeityError.copy(alpha = 0.12f), contentColor = BeityError),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(50.dp)
                    .testTag("sign_out_button")
            ) {
                Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("تسجيل الخروج", fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showEditDialog) {
        var nameInput by remember(userProfile) { mutableStateOf(displayName) }
        var phoneInput by remember(userProfile) { mutableStateOf(userProfile?.phone.orEmpty()) }
        var cityInput by remember(userProfile) { mutableStateOf(userProfile?.city.orEmpty()) }
        var selectedType by remember(userProfile) { mutableStateOf(AccountType.fromKey(userProfile?.accountType)) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("تعديل الملف الشخصي") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = nameInput, onValueChange = { nameInput = it }, label = { Text("الاسم الكامل") }, singleLine = true)
                    OutlinedTextField(value = phoneInput, onValueChange = { phoneInput = it }, label = { Text("رقم الهاتف") }, singleLine = true)
                    OutlinedTextField(value = cityInput, onValueChange = { cityInput = it }, label = { Text("المدينة") }, singleLine = true)
                    Text("نوع الحساب", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AccountType.entries.forEach { at ->
                            FilterChip(
                                selected = selectedType == at,
                                onClick = { selectedType = at },
                                label = { Text(at.labelAr) }
                            )
                        }
                    }
                    Text(
                        text = "ملاحظة: كلمة المرور والتحقق الأمني يُداران تلقائياً عبر حساب Google الخاص بك.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nameInput.trim().isNotEmpty()) {
                            onUpdateProfile(nameInput.trim(), phoneInput.trim(), cityInput.trim(), selectedType, userProfile?.avatarUrl)
                            showEditDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BeityNavy, contentColor = BeityGold)
                ) { Text("حفظ") }
            },
            dismissButton = { TextButton(onClick = { showEditDialog = false }) { Text("إلغاء") } }
        )
    }
}

@Composable
private fun InfoLinkRow(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        leadingContent = { Icon(icon, contentDescription = null, tint = BeityNavy) },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

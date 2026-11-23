package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AccountStatus
import com.example.data.model.AccountType
import com.example.data.model.CategoryItem
import com.example.data.model.LocationItem
import com.example.data.model.PropertyItem
import com.example.data.model.PropertyStatus
import com.example.data.model.PropertyType
import com.example.data.model.ReportItem
import com.example.data.model.UiState
import com.example.data.model.UserProfile
import com.example.data.model.formatPriceAr
import com.example.ui.theme.BeityError
import com.example.ui.theme.BeityGold
import com.example.ui.theme.BeityNavy
import com.example.ui.theme.BeitySuccess
import com.example.ui.theme.BeityWarning
import com.example.ui.theme.BeityWhite

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AdminDashboardScreen(
    allPropertiesState: UiState<List<PropertyItem>>,
    allUsersState: UiState<List<UserProfile>>,
    allReportsState: UiState<List<ReportItem>>,
    categories: List<CategoryItem>,
    locations: List<LocationItem>,
    onBack: () -> Unit,
    onApproveProperty: (PropertyItem) -> Unit,
    onRejectProperty: (PropertyItem, String) -> Unit,
    onToggleFeatured: (String, Boolean) -> Unit,
    onDeleteProperty: (String) -> Unit,
    onUpdateUser: (String, AccountType, AccountStatus) -> Unit,
    onResolveReport: (String, String) -> Unit,
    onUpsertCategory: (CategoryItem) -> Unit,
    onUpsertLocation: (String, String, List<String>, Boolean) -> Unit,
    onSeedDemoProperties: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var rejectTarget by remember { mutableStateOf<PropertyItem?>(null) }
    var resolveTarget by remember { mutableStateOf<ReportItem?>(null) }

    val properties = (allPropertiesState as? UiState.Success)?.data ?: emptyList()
    val users = (allUsersState as? UiState.Success)?.data ?: emptyList()
    val reports = (allReportsState as? UiState.Success)?.data ?: emptyList()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("لوحة تحكم الإدارة", fontWeight = FontWeight.Bold) },
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
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("الإحصائيات") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("العقارات (${properties.size})") })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("المستخدمون (${users.size})") })
                Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }, text = { Text("المحتوى والبلاغات") })
            }

            when (selectedTab) {
                0 -> AdminAnalyticsTab(
                    properties = properties,
                    usersCount = users.size,
                    reportsCount = reports.count { it.status == "open" },
                    onSeedDemoProperties = onSeedDemoProperties
                )
                1 -> AdminPropertiesTab(
                    state = allPropertiesState,
                    onApprove = onApproveProperty,
                    onRejectClick = { rejectTarget = it },
                    onToggleFeatured = onToggleFeatured,
                    onDelete = onDeleteProperty
                )
                2 -> AdminUsersTab(
                    state = allUsersState,
                    onUpdateUser = onUpdateUser
                )
                3 -> AdminContentAndReportsTab(
                    reports = reports,
                    categories = categories,
                    locations = locations,
                    onResolveClick = { resolveTarget = it },
                    onUpsertCategory = onUpsertCategory,
                    onUpsertLocation = onUpsertLocation
                )
            }
        }
    }

    rejectTarget?.let { prop ->
        var reason by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { rejectTarget = null },
            title = { Text("رفض الإعلان") },
            text = {
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("سبب الرفض") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRejectProperty(prop, reason.ifBlank { "البيانات غير مكتملة" })
                        rejectTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BeityError, contentColor = BeityWhite)
                ) { Text("تأكيد الرفض") }
            },
            dismissButton = { TextButton(onClick = { rejectTarget = null }) { Text("إلغاء") } }
        )
    }

    resolveTarget?.let { rep ->
        var notes by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { resolveTarget = null },
            title = { Text("معالجة البلاغ") },
            text = {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات الإدارة") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResolveReport(rep.id, notes)
                        resolveTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BeityNavy, contentColor = BeityGold)
                ) { Text("إغلاق البلاغ") }
            },
            dismissButton = { TextButton(onClick = { resolveTarget = null }) { Text("إلغاء") } }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AdminAnalyticsTab(
    properties: List<PropertyItem>,
    usersCount: Int,
    reportsCount: Int,
    onSeedDemoProperties: () -> Unit
) {
    val forSale = properties.count { it.listingType == "sale" }
    val forRent = properties.count { it.listingType == "rent" }
    val pending = properties.count { it.status == PropertyStatus.PENDING.key }
    val approved = properties.count { it.status == PropertyStatus.APPROVED.key }
    val rejected = properties.count { it.status == PropertyStatus.REJECTED.key }
    val totalViews = properties.sumOf { it.viewsCount }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                maxItemsInEachRow = 2
            ) {
                KpiCard("إجمالي المستخدمين", "$usersCount", Modifier.weight(1f))
                KpiCard("إجمالي العقارات", "${properties.size}", Modifier.weight(1f))
                KpiCard("عقارات للبيع", "$forSale", Modifier.weight(1f))
                KpiCard("عقارات للإيجار", "$forRent", Modifier.weight(1f))
                KpiCard("قيد المراجعة", "$pending", Modifier.weight(1f), BeityWarning)
                KpiCard("مقبولة ومنشورة", "$approved", Modifier.weight(1f), BeitySuccess)
                KpiCard("مرفوضة", "$rejected", Modifier.weight(1f), BeityError)
                KpiCard("إجمالي المشاهدات", "$totalViews", Modifier.weight(1f), BeityGold)
                KpiCard("بلاغات مفتوحة", "$reportsCount", Modifier.weight(1f), BeityError)
            }
        }

        // Category Bar Chart
        item {
            Card(shape = RoundedCornerShape(18.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("توزيع العقارات حسب النوع", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    val maxCount = PropertyType.entries.maxOf { pt -> properties.count { it.propertyType == pt.key } }.coerceAtLeast(1)
                    PropertyType.entries.forEach { pt ->
                        val count = properties.count { it.propertyType == pt.key }
                        val ratio = count.toFloat() / maxCount.toFloat()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(pt.labelAr, style = MaterialTheme.typography.labelMedium, modifier = Modifier.width(108.dp))
                            Canvas(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(14.dp)
                            ) {
                                drawRoundRect(
                                    color = BeityNavy.copy(alpha = 0.1f),
                                    size = size,
                                    cornerRadius = CornerRadius(8f, 8f)
                                )
                                drawRoundRect(
                                    color = BeityGold,
                                    size = Size(size.width * ratio, size.height),
                                    cornerRadius = CornerRadius(8f, 8f)
                                )
                            }
                            Text("$count", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Admin Demo Seeder Tool (Clearly labeled as demo/testing data)
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = BeityNavy)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("تهيئة بيانات تجريبية للاختبار (Demo)", color = BeityGold, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        "يمكن للمسؤول إدراج 5 عقارات نموذجية بالإضافة إلى الأقسام والمحافظات في قاعدة بيانات Firestore لتجربة العرض والخريطة والفلترة مباشرة.",
                        color = BeityWhite.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Button(
                        onClick = onSeedDemoProperties,
                        colors = ButtonDefaults.buttonColors(containerColor = BeityGold, contentColor = BeityNavy),
                        modifier = Modifier.testTag("admin_seed_demo_button")
                    ) {
                        Icon(Icons.Rounded.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("إدراج عقارات نموذجية تجريبية", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun KpiCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    accent: androidx.compose.ui.graphics.Color = BeityNavy
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = accent)
        }
    }
}

@Composable
private fun AdminPropertiesTab(
    state: UiState<List<PropertyItem>>,
    onApprove: (PropertyItem) -> Unit,
    onRejectClick: (PropertyItem) -> Unit,
    onToggleFeatured: (String, Boolean) -> Unit,
    onDelete: (String) -> Unit
) {
    var statusFilter by remember { mutableStateOf<PropertyStatus?>(null) }

    when (state) {
        is UiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = BeityGold) }
        is UiState.Error -> Box(Modifier.fillMaxSize().padding(16.dp)) { Text(state.message, color = BeityError) }
        is UiState.Success -> {
            val filtered = if (statusFilter == null) state.data else state.data.filter { it.status == statusFilter?.key }
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(selected = statusFilter == null, onClick = { statusFilter = null }, label = { Text("الكل") })
                    PropertyStatus.entries.forEach { st ->
                        FilterChip(selected = statusFilter == st, onClick = { statusFilter = st }, label = { Text(st.labelAr) })
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filtered, key = { it.id }) { prop ->
                        Card(shape = RoundedCornerShape(16.dp)) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(prop.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                                    Surface(color = BeityNavy, shape = RoundedCornerShape(8.dp)) {
                                        Text(prop.statusEnum().labelAr, color = BeityGold, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                    }
                                }
                                Text("${formatPriceAr(prop.price)} • ${prop.governorate} • المعلن: ${prop.ownerName}", style = MaterialTheme.typography.bodySmall)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (prop.status != PropertyStatus.APPROVED.key) {
                                        Button(
                                            onClick = { onApprove(prop) },
                                            colors = ButtonDefaults.buttonColors(containerColor = BeitySuccess, contentColor = BeityWhite)
                                        ) {
                                            Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("قبول")
                                        }
                                    }
                                    if (prop.status != PropertyStatus.REJECTED.key) {
                                        OutlinedButton(onClick = { onRejectClick(prop) }) {
                                            Icon(Icons.Outlined.Close, contentDescription = null, modifier = Modifier.size(16.dp), tint = BeityError)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("رفض", color = BeityError)
                                        }
                                    }
                                    IconButton(onClick = { onToggleFeatured(prop.id, !prop.featured) }) {
                                        Icon(
                                            if (prop.featured) Icons.Outlined.Star else Icons.Outlined.StarBorder,
                                            contentDescription = "تمييز العقار",
                                            tint = BeityGold
                                        )
                                    }
                                    IconButton(onClick = { onDelete(prop.id) }) {
                                        Icon(Icons.Outlined.Delete, contentDescription = "حذف", tint = BeityError)
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

@Composable
private fun AdminUsersTab(
    state: UiState<List<UserProfile>>,
    onUpdateUser: (String, AccountType, AccountStatus) -> Unit
) {
    when (state) {
        is UiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = BeityGold) }
        is UiState.Error -> Box(Modifier.fillMaxSize().padding(16.dp)) { Text(state.message, color = BeityError) }
        is UiState.Success -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.data, key = { it.userId }) { user ->
                    val type = AccountType.fromKey(user.accountType)
                    val status = AccountStatus.fromKey(user.status)
                    Card(shape = RoundedCornerShape(16.dp)) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(user.displayName, fontWeight = FontWeight.Bold)
                                Surface(
                                    color = if (status == AccountStatus.ACTIVE) BeitySuccess else BeityError,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(status.labelAr, color = BeityWhite, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                                }
                            }
                            user.email?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                val nextType = if (type == AccountType.SEEKER) AccountType.OWNER else AccountType.SEEKER
                                OutlinedButton(onClick = { onUpdateUser(user.userId, nextType, status) }) {
                                    Text("تحويل إلى ${nextType.labelAr}", style = MaterialTheme.typography.labelSmall)
                                }
                                val nextStatus = if (status == AccountStatus.ACTIVE) AccountStatus.SUSPENDED else AccountStatus.ACTIVE
                                Button(
                                    onClick = { onUpdateUser(user.userId, type, nextStatus) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (nextStatus == AccountStatus.SUSPENDED) BeityError else BeitySuccess,
                                        contentColor = BeityWhite
                                    )
                                ) {
                                    Text(if (nextStatus == AccountStatus.SUSPENDED) "تعطيل الحساب" else "تفعيل الحساب", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminContentAndReportsTab(
    reports: List<ReportItem>,
    categories: List<CategoryItem>,
    locations: List<LocationItem>,
    onResolveClick: (ReportItem) -> Unit,
    onUpsertCategory: (CategoryItem) -> Unit,
    onUpsertLocation: (String, String, List<String>, Boolean) -> Unit
) {
    var newGovName by remember { mutableStateOf("") }
    var newGovCities by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("البلاغات والشكاوى (${reports.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        items(reports, key = { it.id }) { rep ->
            Card(shape = RoundedCornerShape(14.dp)) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(rep.reason, fontWeight = FontWeight.Bold)
                        Surface(
                            color = if (rep.status == "open") BeityError else BeitySuccess,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (rep.status == "open") "مفتوح" else "تمت المعالجة", color = BeityWhite, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                        }
                    }
                    Text("المرسل: ${rep.reporterName}", style = MaterialTheme.typography.bodySmall)
                    Text(rep.details, style = MaterialTheme.typography.bodySmall)
                    if (rep.status == "open") {
                        Button(
                            onClick = { onResolveClick(rep) },
                            colors = ButtonDefaults.buttonColors(containerColor = BeityNavy, contentColor = BeityGold)
                        ) { Text("معالجة وإغلاق") }
                    }
                }
            }
        }

        item {
            Text("إدارة أقسام العقارات", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Card(shape = RoundedCornerShape(14.dp)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    categories.forEach { cat ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(cat.nameAr, style = MaterialTheme.typography.bodyMedium)
                            Switch(
                                checked = cat.enabled,
                                onCheckedChange = { onUpsertCategory(cat.copy(enabled = it)) }
                            )
                        }
                    }
                }
            }
        }

        item {
            Text("إضافة أو تحديث محافظة", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Card(shape = RoundedCornerShape(14.dp)) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = newGovName, onValueChange = { newGovName = it }, label = { Text("اسم المحافظة") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = newGovCities, onValueChange = { newGovCities = it }, label = { Text("المدن (مفصولة بفاصلة)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Button(
                        onClick = {
                            if (newGovName.trim().isNotEmpty()) {
                                val id = "gov_${System.currentTimeMillis()}"
                                val citiesList = newGovCities.split(",", "،").map { it.trim() }.filter { it.isNotEmpty() }
                                onUpsertLocation(id, newGovName.trim(), citiesList, true)
                                newGovName = ""
                                newGovCities = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BeityNavy, contentColor = BeityGold)
                    ) { Text("حفظ المحافظة (${locations.size})") }
                }
            }
        }
    }
}

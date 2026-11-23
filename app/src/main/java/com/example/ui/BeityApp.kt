package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.R
import com.example.data.model.ListingType
import com.example.data.model.PropertyItem
import com.example.data.model.UiState
import com.example.data.repository.AdminRepository
import com.example.data.repository.AuthManager
import com.example.data.repository.BeityRepository
import com.example.data.repository.PreferencesRepository
import com.example.ui.screens.AddEditPropertyScreen
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.ChatThreadScreen
import com.example.ui.screens.ExploreScreen
import com.example.ui.screens.FavoritesCompareScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.InfoPagesScreen
import com.example.ui.screens.MessagesNotificationsScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.PropertyDetailScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.BeityGold
import com.example.ui.theme.BeityNavy
import com.example.ui.theme.BeityTheme
import com.example.ui.theme.BeityWhite
import com.example.ui.viewmodel.AdminViewModel
import com.example.ui.viewmodel.MainViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

enum class BeityTab(
    val labelAr: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("الرئيسية", Icons.Filled.Home, Icons.Outlined.Home),
    EXPLORE("استكشف", Icons.Filled.Explore, Icons.Outlined.Explore),
    FAVORITES("المفضلة", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder),
    MESSAGES("الرسائل", Icons.AutoMirrored.Filled.Chat, Icons.AutoMirrored.Outlined.Chat),
    PROFILE("حسابي", Icons.Filled.Person, Icons.Outlined.Person)
}

sealed interface BeitySubScreen {
    data class PropertyDetail(val property: PropertyItem) : BeitySubScreen
    data class AddEditProperty(val property: PropertyItem? = null) : BeitySubScreen
    data class ChatThread(val conversationId: String) : BeitySubScreen
    data object AdminDashboard : BeitySubScreen
    data class InfoPage(val pageKey: String) : BeitySubScreen
}

@Composable
fun BeityApp(preferencesRepository: PreferencesRepository) {
    val isDarkTheme by preferencesRepository.darkModeFlow.collectAsStateWithLifecycle(initialValue = false)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    var showSplash by remember { mutableStateOf(true) }
    var currentUser by remember { mutableStateOf<FirebaseUser?>(Firebase.auth.currentUser) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose { Firebase.auth.removeAuthStateListener(listener) }
    }

    LaunchedEffect(Unit) {
        AuthManager.attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            scope = scope,
            onAuthSuccess = { currentUser = Firebase.auth.currentUser },
            onUnauthenticated = { currentUser = Firebase.auth.currentUser }
        )
    }

    BeityTheme(darkTheme = isDarkTheme) {
        if (showSplash) {
            SplashScreen(onFinished = { showSplash = false })
        } else {
            val user = currentUser
            if (user == null) {
                AuthScreen(onAuthSuccess = { currentUser = Firebase.auth.currentUser })
            } else {
                val uid = user.uid
                val isAdmin = AuthManager.isBootstrappedAdmin(user)
                key(uid) {
                    AuthenticatedBeityShell(
                        uid = uid,
                        displayName = user.displayName ?: "مستخدم بيتي",
                        email = user.email,
                        photoUrl = user.photoUrl?.toString(),
                        isAdmin = isAdmin,
                        isDarkTheme = isDarkTheme,
                        onToggleDarkTheme = { enabled ->
                            scope.launch { preferencesRepository.setDarkMode(enabled) }
                        },
                        onSignOut = {
                            AuthManager.signOut(credentialManager, scope) {
                                currentUser = null
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AuthenticatedBeityShell(
    uid: String,
    displayName: String,
    email: String?,
    photoUrl: String?,
    isAdmin: Boolean,
    isDarkTheme: Boolean,
    onToggleDarkTheme: (Boolean) -> Unit,
    onSignOut: () -> Unit
) {
    val mainViewModel: MainViewModel = viewModel(
        key = uid,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY])
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                MainViewModel(
                    repository = BeityRepository(db),
                    currentUserId = uid,
                    initialDisplayName = displayName,
                    initialEmail = email,
                    initialPhotoUrl = photoUrl,
                    isAdmin = isAdmin
                )
            }
        }
    )

    val adminViewModel: AdminViewModel = viewModel(
        key = "admin_$uid",
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY])
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                AdminViewModel(
                    adminRepository = AdminRepository(db),
                    beityRepository = BeityRepository(db),
                    currentUserId = uid,
                    isAdmin = isAdmin
                )
            }
        }
    )

    val approvedState by mainViewModel.approvedPropertiesState.collectAsStateWithLifecycle()
    val filteredProperties by mainViewModel.filteredProperties.collectAsStateWithLifecycle()
    val myPropertiesState by mainViewModel.myPropertiesState.collectAsStateWithLifecycle()
    val favorites by mainViewModel.favorites.collectAsStateWithLifecycle()
    val notifications by mainViewModel.notifications.collectAsStateWithLifecycle()
    val conversations by mainViewModel.conversations.collectAsStateWithLifecycle()
    val searchHistory by mainViewModel.searchHistory.collectAsStateWithLifecycle()
    val categories by mainViewModel.categories.collectAsStateWithLifecycle()
    val locations by mainViewModel.locations.collectAsStateWithLifecycle()
    val userProfile by mainViewModel.userProfile.collectAsStateWithLifecycle()
    val filter by mainViewModel.filter.collectAsStateWithLifecycle()
    val compareIds by mainViewModel.compareIds.collectAsStateWithLifecycle()
    val activeMessages by mainViewModel.activeMessages.collectAsStateWithLifecycle()
    val activeReviews by mainViewModel.activePropertyReviews.collectAsStateWithLifecycle()

    val adminAllProps by adminViewModel.allProperties.collectAsStateWithLifecycle()
    val adminAllUsers by adminViewModel.allUsers.collectAsStateWithLifecycle()
    val adminAllReports by adminViewModel.allReports.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(mainViewModel) {
        mainViewModel.feedback.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }
    LaunchedEffect(adminViewModel) {
        adminViewModel.feedback.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    var currentTab by remember { mutableStateOf(BeityTab.HOME) }
    var exploreOnMap by remember { mutableStateOf(false) }
    var messagesInitialTab by remember { mutableIntStateOf(0) }
    val subScreenStack = remember { mutableStateListOf<BeitySubScreen>() }

    val favoriteIds = remember(favorites) { favorites.map { it.propertyId }.toSet() }
    val allApproved = (approvedState as? UiState.Success)?.data ?: emptyList()
    val unreadNotifs = remember(notifications) { notifications.count { !it.read } }

    val activeSub = subScreenStack.lastOrNull()
    if (activeSub != null) {
        BackHandler {
            subScreenStack.removeAt(subScreenStack.lastIndex)
        }
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { innerPadding ->
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (activeSub) {
                    is BeitySubScreen.PropertyDetail -> {
                        val liveProp = allApproved.firstOrNull { it.id == activeSub.property.id }
                            ?: (myPropertiesState as? UiState.Success)?.data?.firstOrNull { it.id == activeSub.property.id }
                            ?: activeSub.property
                        val similar = allApproved
                            .filter { it.id != liveProp.id && (it.propertyType == liveProp.propertyType || it.governorate == liveProp.governorate) }
                            .take(5)
                        PropertyDetailScreen(
                            property = liveProp,
                            currentUserId = uid,
                            reviews = activeReviews,
                            similarProperties = similar,
                            isFavorite = favoriteIds.contains(liveProp.id),
                            isCompared = compareIds.contains(liveProp.id),
                            favoriteIds = favoriteIds,
                            compareIds = compareIds,
                            onBack = { subScreenStack.removeAt(subScreenStack.lastIndex) },
                            onToggleFavorite = { mainViewModel.toggleFavorite(liveProp) },
                            onToggleCompare = { mainViewModel.toggleCompare(liveProp.id) },
                            onEditOwnProperty = { subScreenStack.add(BeitySubScreen.AddEditProperty(liveProp)) },
                            onSendMessage = { msgText ->
                                mainViewModel.sendMessage(
                                    propertyId = liveProp.id,
                                    propertyTitle = liveProp.title,
                                    recipientId = liveProp.ownerId,
                                    recipientName = liveProp.ownerName,
                                    text = msgText
                                ) { convId ->
                                    subScreenStack.add(BeitySubScreen.ChatThread(convId))
                                }
                            },
                            onAddReview = { rating, comment ->
                                mainViewModel.addReview(liveProp.id, rating, comment) {}
                            },
                            onSubmitReport = { reason, details ->
                                mainViewModel.submitReport(
                                    propertyId = liveProp.id,
                                    propertyTitle = liveProp.title,
                                    reportType = "property_report",
                                    reason = reason,
                                    details = details
                                ) {}
                            },
                            onOpenProperty = { next ->
                                mainViewModel.setActivePropertyForDetails(next.id)
                                subScreenStack.add(BeitySubScreen.PropertyDetail(next))
                            }
                        )
                    }
                    is BeitySubScreen.AddEditProperty -> {
                        AddEditPropertyScreen(
                            existingProperty = activeSub.property,
                            defaultOwnerName = userProfile?.displayName ?: displayName,
                            defaultPhone = userProfile?.phone.orEmpty(),
                            locations = locations,
                            isAdmin = isAdmin,
                            onBack = { subScreenStack.removeAt(subScreenStack.lastIndex) },
                            onSubmit = { propPayload ->
                                if (activeSub.property == null) {
                                    mainViewModel.createProperty(propPayload) {
                                        subScreenStack.removeAt(subScreenStack.lastIndex)
                                    }
                                } else {
                                    mainViewModel.updateOwnProperty(propPayload) {
                                        subScreenStack.removeAt(subScreenStack.lastIndex)
                                    }
                                }
                            }
                        )
                    }
                    is BeitySubScreen.ChatThread -> {
                        val conv = conversations.firstOrNull { it.id == activeSub.conversationId }
                        if (conv != null) {
                            ChatThreadScreen(
                                conversation = conv,
                                currentUserId = uid,
                                messages = activeMessages,
                                onBack = { subScreenStack.removeAt(subScreenStack.lastIndex) },
                                onSendMessage = { txt ->
                                    val otherUid = conv.participantIds.firstOrNull { it != uid } ?: uid
                                    val otherName = conv.participantNames[otherUid] ?: "مستخدم بيتي"
                                    mainViewModel.sendMessage(
                                        propertyId = conv.propertyId,
                                        propertyTitle = conv.propertyTitle,
                                        recipientId = otherUid,
                                        recipientName = otherName,
                                        text = txt
                                    )
                                }
                            )
                        } else {
                            InfoPagesScreen(
                                pageKey = "404",
                                onBack = { subScreenStack.removeAt(subScreenStack.lastIndex) },
                                onGoHome = {
                                    subScreenStack.clear()
                                    currentTab = BeityTab.HOME
                                },
                                onSubmitTicket = { _, _, _, _ -> }
                            )
                        }
                    }
                    is BeitySubScreen.AdminDashboard -> {
                        AdminDashboardScreen(
                            allPropertiesState = adminAllProps,
                            allUsersState = adminAllUsers,
                            allReportsState = adminAllReports,
                            categories = categories,
                            locations = locations,
                            onBack = { subScreenStack.removeAt(subScreenStack.lastIndex) },
                            onApproveProperty = { adminViewModel.approveProperty(it) },
                            onRejectProperty = { prop, reason -> adminViewModel.rejectProperty(prop, reason) },
                            onToggleFeatured = { id, feat -> adminViewModel.toggleFeatured(id, feat) },
                            onDeleteProperty = { adminViewModel.deleteProperty(it) },
                            onUpdateUser = { userId, type, status -> adminViewModel.updateUserAccount(userId, type, status) },
                            onResolveReport = { repId, notes -> adminViewModel.resolveReport(repId, notes) },
                            onUpsertCategory = { adminViewModel.upsertCategory(it) },
                            onUpsertLocation = { id, name, cities, en -> adminViewModel.upsertLocation(id, name, cities, en) },
                            onSeedDemoProperties = { adminViewModel.seedDemoProperties() }
                        )
                    }
                    is BeitySubScreen.InfoPage -> {
                        InfoPagesScreen(
                            pageKey = activeSub.pageKey,
                            onBack = { subScreenStack.removeAt(subScreenStack.lastIndex) },
                            onGoHome = {
                                subScreenStack.clear()
                                currentTab = BeityTab.HOME
                            },
                            onSubmitTicket = { type, reason, details, onDone ->
                                mainViewModel.submitReport(null, null, type, reason, details, onDone)
                            }
                        )
                    }
                }
            }
        }
    } else {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val useRail = maxWidth >= 700.dp

            val openProperty: (PropertyItem) -> Unit = { prop ->
                mainViewModel.setActivePropertyForDetails(prop.id)
                subScreenStack.add(BeitySubScreen.PropertyDetail(prop))
            }

            val handleQuickNav: (String) -> Unit = { key ->
                when (key) {
                    "home" -> currentTab = BeityTab.HOME
                    "explore" -> {
                        exploreOnMap = false
                        currentTab = BeityTab.EXPLORE
                    }
                    "sell" -> {
                        mainViewModel.updateFilter { it.copy(listingType = ListingType.SALE) }
                        exploreOnMap = false
                        currentTab = BeityTab.EXPLORE
                    }
                    "rent" -> {
                        mainViewModel.updateFilter { it.copy(listingType = ListingType.RENT) }
                        exploreOnMap = false
                        currentTab = BeityTab.EXPLORE
                    }
                    "map" -> {
                        exploreOnMap = true
                        currentTab = BeityTab.EXPLORE
                    }
                    "about", "contact" -> subScreenStack.add(BeitySubScreen.InfoPage(key))
                }
            }

            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                bottomBar = {
                    if (!useRail) {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.navigationBarsPadding()
                        ) {
                            BeityTab.entries.forEach { tab ->
                                val selected = currentTab == tab
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = {
                                        if (tab == BeityTab.EXPLORE) exploreOnMap = false
                                        currentTab = tab
                                    },
                                    icon = {
                                        if (tab == BeityTab.MESSAGES && unreadNotifs > 0) {
                                            BadgedBox(badge = { Badge(containerColor = BeityGold, contentColor = BeityNavy) { Text("$unreadNotifs") } }) {
                                                Icon(if (selected) tab.selectedIcon else tab.unselectedIcon, contentDescription = tab.labelAr)
                                            }
                                        } else {
                                            Icon(if (selected) tab.selectedIcon else tab.unselectedIcon, contentDescription = tab.labelAr)
                                        }
                                    },
                                    label = { Text(tab.labelAr) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = BeityNavy,
                                        selectedTextColor = BeityNavy,
                                        indicatorColor = BeityGold
                                    ),
                                    modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (useRail) {
                        NavigationRail(
                            containerColor = BeityNavy,
                            contentColor = BeityWhite,
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            BeityTab.entries.forEach { tab ->
                                val selected = currentTab == tab
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { currentTab = tab },
                                    icon = {
                                        Icon(
                                            if (selected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = tab.labelAr,
                                            tint = if (selected) BeityNavy else BeityWhite
                                        )
                                    },
                                    label = { Text(tab.labelAr, color = if (selected) BeityGold else BeityWhite) }
                                )
                            }
                        }
                    }

                    androidx.compose.foundation.layout.Box(modifier = Modifier.weight(1f)) {
                        when (currentTab) {
                            BeityTab.HOME -> HomeScreen(
                                propertiesState = approvedState,
                                categories = categories,
                                locations = locations,
                                favoriteIds = favoriteIds,
                                compareIds = compareIds,
                                unreadNotifications = unreadNotifs,
                                isAdmin = isAdmin,
                                onQuickNav = handleQuickNav,
                                onSearchSubmit = { gov, city, dist, type, listing, maxP ->
                                    mainViewModel.updateFilter {
                                        it.copy(
                                            governorate = gov,
                                            city = city,
                                            district = dist,
                                            propertyType = type,
                                            listingType = listing,
                                            maxPrice = maxP
                                        )
                                    }
                                    mainViewModel.saveCurrentSearch()
                                    exploreOnMap = false
                                    currentTab = BeityTab.EXPLORE
                                },
                                onSelectCategory = { type ->
                                    mainViewModel.updateFilter { it.copy(propertyType = type) }
                                    exploreOnMap = false
                                    currentTab = BeityTab.EXPLORE
                                },
                                onPropertyClick = openProperty,
                                onToggleFavorite = { mainViewModel.toggleFavorite(it) },
                                onToggleCompare = { mainViewModel.toggleCompare(it) },
                                onAddProperty = { subScreenStack.add(BeitySubScreen.AddEditProperty()) },
                                onOpenNotifications = {
                                    messagesInitialTab = 1
                                    currentTab = BeityTab.MESSAGES
                                },
                                onOpenAdmin = {
                                    if (isAdmin) subScreenStack.add(BeitySubScreen.AdminDashboard)
                                }
                            )
                            BeityTab.EXPLORE -> ExploreScreen(
                                properties = filteredProperties,
                                filter = filter,
                                locations = locations,
                                searchHistory = searchHistory,
                                favoriteIds = favoriteIds,
                                compareIds = compareIds,
                                startOnMap = exploreOnMap,
                                onUpdateFilter = { mainViewModel.updateFilter(it) },
                                onResetFilter = { mainViewModel.resetFilter() },
                                onSaveSearch = { mainViewModel.saveCurrentSearch() },
                                onApplyHistory = { mainViewModel.applySearchHistoryItem(it) },
                                onPropertyClick = openProperty,
                                onToggleFavorite = { mainViewModel.toggleFavorite(it) },
                                onToggleCompare = { mainViewModel.toggleCompare(it) }
                            )
                            BeityTab.FAVORITES -> FavoritesCompareScreen(
                                allApprovedProperties = allApproved,
                                favorites = favorites,
                                compareIds = compareIds,
                                onPropertyClick = openProperty,
                                onToggleFavorite = { mainViewModel.toggleFavorite(it) },
                                onToggleCompare = { mainViewModel.toggleCompare(it) },
                                onExploreClick = { currentTab = BeityTab.EXPLORE }
                            )
                            BeityTab.MESSAGES -> MessagesNotificationsScreen(
                                currentUserId = uid,
                                conversations = conversations,
                                notifications = notifications,
                                initialTab = messagesInitialTab,
                                onOpenConversation = { conv ->
                                    mainViewModel.setActiveConversation(conv.id)
                                    subScreenStack.add(BeitySubScreen.ChatThread(conv.id))
                                },
                                onMarkNotificationRead = { notif ->
                                    mainViewModel.markNotificationRead(notif.id)
                                }
                            )
                            BeityTab.PROFILE -> ProfileScreen(
                                userProfile = userProfile,
                                fallbackName = displayName,
                                fallbackEmail = email,
                                isAdmin = isAdmin,
                                isDarkTheme = isDarkTheme,
                                myPropertiesState = myPropertiesState,
                                searchHistory = searchHistory,
                                onToggleDarkTheme = onToggleDarkTheme,
                                onUpdateProfile = { name, phone, city, type, avatar ->
                                    mainViewModel.updateProfile(name, phone, city, type, avatar)
                                },
                                onAddProperty = { subScreenStack.add(BeitySubScreen.AddEditProperty()) },
                                onOpenPropertyDetail = openProperty,
                                onEditProperty = { prop -> subScreenStack.add(BeitySubScreen.AddEditProperty(prop)) },
                                onTogglePauseProperty = { id, pause -> mainViewModel.togglePropertyPause(id, pause) },
                                onDeleteProperty = { id -> mainViewModel.deleteProperty(id) },
                                onApplySearchHistory = { hist ->
                                    mainViewModel.applySearchHistoryItem(hist)
                                    currentTab = BeityTab.EXPLORE
                                },
                                onOpenAdminDashboard = {
                                    if (isAdmin) subScreenStack.add(BeitySubScreen.AdminDashboard)
                                },
                                onOpenInfoPage = { pageKey ->
                                    subScreenStack.add(BeitySubScreen.InfoPage(pageKey))
                                },
                                onSignOut = onSignOut
                            )
                        }
                    }
                }
            }
        }
    }
}

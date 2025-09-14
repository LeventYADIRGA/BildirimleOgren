package com.lyadirga.bildirimleogren.ui_compose

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lyadirga.bildirimleogren.R
import com.lyadirga.bildirimleogren.data.PrefData
import com.lyadirga.bildirimleogren.model.LanguageSetSummary
import com.lyadirga.bildirimleogren.notification.NotificationWorker
import com.lyadirga.bildirimleogren.ui.MainActivity.Companion.UNIQUE_WORK_NAME
import com.lyadirga.bildirimleogren.ui.MainActivity.Companion.intervalsInMinutes
import com.lyadirga.bildirimleogren.ui.MainViewModel
import com.lyadirga.bildirimleogren.ui.isInternetAvailable
import com.lyadirga.bildirimleogren.ui.showAlert
import com.lyadirga.bildirimleogren.ui.showToast
import com.lyadirga.bildirimleogren.ui_compose.theme.BildirimleOgrenTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import com.lyadirga.bildirimleogren.util.Toast as AppToast


@AndroidEntryPoint
class MainActivityCompose : ComponentActivity() {

    @Inject
    lateinit var prefData: PrefData

    private val viewModel: MainViewModel by viewModels()

    private val choices: Array<CharSequence> by lazy {
        resources.getStringArray(R.array.notification_intervals).map { it as CharSequence }.toTypedArray()
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val mainViewModel: MainViewModel = hiltViewModel()
            BildirimleOgrenTheme {
                MainScreenWithNavigation(mainViewModel, prefData)
            }
        }
    } // end onCreate

    fun updateNotification(isEnabled: Boolean, enabledSets: List<Long>) {
        lifecycleScope.launch {
            val intervalIndex = prefData.getNotificationIntervalIndexOnce()
            if (isEnabled && intervalIndex != PrefData.NOTIFICATION_DISABLED_INDEX && enabledSets.size == 1) {
                // 🇹🇷Türkçe: Bildirime açık hiçbir set yokken bu set bildirime açılıyor. Bildirimi başlat
                // 🇬🇧English: When no set has notifications enabled, this set will be enabled. Start the notification.
                val notificationInterval  = intervalsInMinutes[intervalIndex]
                scheduleNotificationsFromSetDetail(notificationInterval)
                AppToast.showSuccessToast(this@MainActivityCompose, R.string.notification_set_enabled)
            } else if (isEnabled && intervalIndex == PrefData.NOTIFICATION_DISABLED_INDEX && enabledSets.size == 1){
                // 🇹🇷Türkçe: Bildirime açık hiçbir set yokken bu set bildirime açılıyor ama bildirim sıklığı ayarlarından seçim yapılmamış. Bildirim sıklığı dialog unu aç.
                // 🇬🇧English: When no set has notifications enabled, this set is enabled but no frequency is selected. Open the notification frequency dialog.
                openNotificationIntervalSettings()
            }
            else if (intervalIndex != PrefData.NOTIFICATION_DISABLED_INDEX && enabledSets.isEmpty()){
                // 🇹🇷Türkçe: Bildirim kapat, çünkü enabledSets boş
                // 🇬🇧English: Turn off notification because enabledSets is empty
                scheduleNotificationsFromSetDetail(null)
                prefData.resetIndex()
            }else if (isEnabled.not()){
                AppToast.showSuccessToast(this@MainActivityCompose, R.string.notification_set_disabled)
            }
        }
    }

    fun openNotificationIntervalSettings() {

        lifecycleScope.launch {
            var currentIntervalIndex = prefData.getNotificationIntervalIndexOnce()
            val oldIndex = currentIntervalIndex

            val builder = MaterialAlertDialogBuilder(this@MainActivityCompose, R.style.Theme_BildirimleOgren_MaterialAlertDialog).apply {
                setTitle(R.string.notification_interval_title)
                setPositiveButton(R.string.generic_ok) { _, _ ->
                    lifecycleScope.launch {
                        if (currentIntervalIndex != oldIndex) {
                            prefData.setNotificationIntervalIndex(currentIntervalIndex)
                            val enabledSets = prefData.getNotificationSetIdsOnce()
                            viewModel.getAllSetSummariesOnce { summaries ->
                                if (summaries.isEmpty()){
                                    showAlert(R.string.notification_no_sets_message)
                                }
                                else if (enabledSets.isEmpty()) {
                                    showAlert(R.string.notification_no_enabled_sets_message)
                                } else {
                                    val notificationInterval = intervalsInMinutes[currentIntervalIndex]
                                    scheduleNotifications(notificationInterval, choices[currentIntervalIndex])
                                }
                            }
                        }

                    }
                }
                setSingleChoiceItems(choices, currentIntervalIndex) { _, which ->
                    currentIntervalIndex = which
                }
            }

            val dialog = builder.create()
            dialog.show()

        }
    }

    private fun scheduleNotifications(notificationInterval: Int?, intervalLabel: CharSequence) {

        val workManager = WorkManager.getInstance(this)
        notificationInterval?.let {
            val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(it.toLong(), TimeUnit.MINUTES)
                .build()

            workManager.enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE, // Cancel previous if any and replace with the new one
                workRequest
            )
            showToast(getString(R.string.notification_scheduled, intervalLabel))

        } ?:run {
            workManager.cancelUniqueWork(UNIQUE_WORK_NAME)
            AppToast.showSuccessToast(this@MainActivityCompose, R.string.notification_all_disabled)
        }

    }


    fun scheduleNotificationsFromSetDetail(notificationInterval: Int?) {

        val workManager = WorkManager.getInstance(this)
        notificationInterval?.let {
            val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(it.toLong(), TimeUnit.MINUTES)
                .build()

            workManager.enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE, // Cancel previous if any and replace with the new one
                workRequest
            )

        } ?:run {
            workManager.cancelUniqueWork(UNIQUE_WORK_NAME)
            AppToast.showSuccessToast(this@MainActivityCompose, R.string.notifications_disabled_no_enabled_set)
        }

    }

} // end MainActivityCompose

@Composable
fun MainScreenWithNavigation(viewModel: MainViewModel, prefData: PrefData) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "main"
    ) {
        composable("main") {
            MainScreen(
                viewModel = viewModel,
                prefData = prefData,
                onSetClick = { setId, setTitle ->
                    navController.navigate("detail/$setId/$setTitle")
                }
            )
        }
        composable(
            route = "detail/{setId}/{setTitle}",
            arguments = listOf(
                navArgument("setId") { type = NavType.LongType },
                navArgument("setTitle") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val setId = backStackEntry.arguments?.getLong("setId") ?: 0L
            val setTitle = backStackEntry.arguments?.getString("setTitle") ?: ""
            DetailScreen(setId = setId, setTitle = setTitle, navController = navController)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel,
               prefData: PrefData,
               onSetClick: (Long, String) -> Unit
) {

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        if (context.isInternetAvailable()) {
            viewModel.fetchSheetsFromDbUrls()
        }
    }

    val isLoading by viewModel.isLoading.collectAsState()

    // PrefData'daki bildirimli set id'lerini state olarak takip et
    val enabledSetIds by prefData.observeNotificationSetIds()
        .collectAsState(initial = emptySet())


    LaunchedEffect(Unit) {
        viewModel.errorEvent.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    val sets by viewModel.setAllSetSummariesFlow.collectAsState(initial = emptyList())

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showSheet by remember { mutableStateOf(false) }

    var expanded by remember { mutableStateOf(false) }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.app_name)) },
                actions = {
                    // Bilgi
                    IconButton(onClick = {
                        //TODO: navigate bilgi screen
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_help_outline_24),
                            contentDescription = "Bilgi"
                        )
                    }

                    // Bildirim ayarları
                    IconButton(onClick = {
                        //TODO: open dialog
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.notification_settings),
                            contentDescription = "Bildirim ayarları"
                        )
                    }

                    // Overflow (üç nokta) menü
                    IconButton(onClick = { expanded = true }) {
                        Icon(
                            painter = painterResource(id = R.drawable.outline_more_vert_24),
                            contentDescription = "Daha fazla"
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        containerColor = MaterialTheme.colorScheme.surface // Menü arka plan rengi
                    ) {
                        DropdownMenuItem(
                            text = { Text("Paylaş") },
                            onClick = {
                                expanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Oy ver") },
                            onClick = {
                                expanded = false
                            }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showSheet = true  },
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_add_24),
                    contentDescription = stringResource(R.string.add_workout_set)
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            // Liste her zaman gösteriliyor
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 6.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                items(sets) { set ->
                    SetListItem(
                        set = set,
                        notificationEnabled = set.id in enabledSetIds,
                        onClick = {
                            onSetClick(set.id, set.title)
                        }
                    )
                }
            }

            // Rehber içerik, liste boşsa
            if (sets.isEmpty() && !isLoading) {
                GuideContent(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // Loading göstergesi liste veya rehberin üstüne
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.Center),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
            // 🔽 Scaffold’un hemen altına BottomSheet’i ekliyoruz
            if (showSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showSheet = false },
                    sheetState = sheetState,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    BottomSheetContent(
                        onAddClicked = { url ->
                            viewModel.fetchSingleSheet(url)
                            showSheet = false
                        }
                    )
                }
            }
        } // end Box
    }
}

@Composable
fun BottomSheetContent(onAddClicked: (String) -> Unit) {
    var url by remember { mutableStateOf("") }

    Column(Modifier.padding(16.dp)) {
        Text("Yeni Set Ekle", style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = url,
            onValueChange = { url = it },
            label = { Text("Google Sheet URL") },
            modifier = Modifier.fillMaxWidth()
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Button(onClick = { onAddClicked(url) }) {
                Text("Ekle")
            }
        }
    }
}

@Composable
fun GuideContent(modifier: Modifier) {
   Text("Guide", modifier = modifier)
}


@Composable
fun SetListItem(
    set: LanguageSetSummary,
    modifier: Modifier = Modifier,
    notificationEnabled: Boolean = false,
    onClick: (LanguageSetSummary) -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable { onClick(set) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = set.title,
                    style = MaterialTheme.typography.titleMedium
                )

                set.url?.let { url ->
                    Text(
                        text = url,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.outline,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (notificationEnabled) {
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.notification_enable),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = stringResource(id = R.string.set_added_to_notifications),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }

            Icon(
                painter = painterResource(id = R.drawable.baseline_arrow_forward_ios_24),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}



@Preview(showBackground = true)
@Composable
fun PreviewSetListItem() {
    BildirimleOgrenTheme {
        SetListItem(
            set = LanguageSetSummary(
                id = 1L,
                title = "Çalışma Seti",
                url = "https://docs.google.com/forms/d/e/1FAIpQLSdJU8qyKU3szHgVxxlZnaTqBdggME3YaBfFq0lb"
            ),
            notificationEnabled = true,
            onClick = {}
        )
    }
}

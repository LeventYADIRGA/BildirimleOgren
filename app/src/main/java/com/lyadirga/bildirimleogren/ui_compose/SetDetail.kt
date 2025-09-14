package com.lyadirga.bildirimleogren.ui_compose

import android.R.attr.contentDescription
import android.util.Log.d
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.lyadirga.bildirimleogren.R
import com.lyadirga.bildirimleogren.data.PrefData
import com.lyadirga.bildirimleogren.ui.MainActivity
import com.lyadirga.bildirimleogren.ui.MainActivity.Companion.intervalsInMinutes
import com.lyadirga.bildirimleogren.ui.MainViewModel
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.jvm.java
import com.lyadirga.bildirimleogren.util.Toast as AppToast



@EntryPoint
@InstallIn(SingletonComponent::class)
interface PrefDataEntryPoint {
    val prefData: PrefData
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    setId: Long,
    setTitle: String,
    navController: NavController,
    viewModel: MainViewModel = hiltViewModel(),
) {

    val context = LocalContext.current

    val setDetails by viewModel.currentSet.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.errorEvent.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    // Bildirim durumu
    var notificationEnabled by remember { mutableStateOf(false) }

    var showDeleteDialog by remember { mutableStateOf(false) }


    val prefData = EntryPointAccessors.fromApplication(
        context.applicationContext,
        PrefDataEntryPoint::class.java
    ).prefData

    LaunchedEffect(setId) {
        viewModel.getSetDetails(setId) // ViewModel üzerinden set detaylarını al
        val enabledSets = prefData.getNotificationSetIdsOnce()
        notificationEnabled = setId in enabledSets
    }

    val coroutineScope = rememberCoroutineScope()

    var showDialog by remember { mutableStateOf(false) }
    val choices = LocalResources.current.getStringArray(R.array.notification_intervals)

    // Bildirim ayarı dialogu
    if (showDialog) {
        NotificationIntervalDialog(
            currentIndex = runBlocking { prefData.getNotificationIntervalIndexOnce() },
            choices = choices,
            onConfirm = { selectedIndex ->
                coroutineScope.launch {
                    prefData.setNotificationIntervalIndex(selectedIndex)
                    val enabledSets = prefData.getNotificationSetIdsOnce()
                    context
                    if (enabledSets.isEmpty()) {
                        Toast.makeText(context, R.string.notification_no_enabled_sets_message, Toast.LENGTH_SHORT).show()
                    } else {
                        val notificationInterval = intervalsInMinutes[selectedIndex]
                        (context as MainActivityCompose).scheduleNotifications(
                            notificationInterval,
                            choices[selectedIndex]
                        )
                    }
                }
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }


    // Silme dialogu
    if (showDeleteDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(text = context.getString(R.string.delete_set_title)) },
            text = { Text(text = context.getString(R.string.delete_set_message)) },
            containerColor = MaterialTheme.colorScheme.surface,
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    showDeleteDialog = false
                    // Silme işlemi
                    coroutineScope.launch {
                        viewModel.deleteSet(setId)

                        // Bildirim listesinde varsa çıkar
                        val enabledSets = prefData.getNotificationSetIdsOnce().toMutableSet()
                        if (setId in enabledSets) {
                            enabledSets.remove(setId)
                            prefData.saveNotificationSetIds(enabledSets.toList())
                        }

                        Toast.makeText(context, R.string.set_deleted, Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    }
                }) {
                    Text(text = context.getString(R.string.generic_yes))
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = {
                    showDeleteDialog = false
                }) {
                    Text(text = context.getString(R.string.generic_no))
                }
            }
        )
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(
                    text = setTitle,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis // Sığmazsa üç nokta
                ) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        // Toggle
                        coroutineScope.launch {
                            prefData.toggleNotificationSetId(setId)
                            val enabledSets = prefData.getNotificationSetIdsOnce()
                            notificationEnabled = setId in enabledSets
                            Toast.makeText(
                                context,
                                if (notificationEnabled) R.string.notification_set_enabled
                                else R.string.notification_set_disabled,
                                Toast.LENGTH_SHORT
                            ).show()
                            val intervalIndex = prefData.getNotificationIntervalIndexOnce()
                            val activity = context as MainActivityCompose

                            if (notificationEnabled && intervalIndex != PrefData.NOTIFICATION_DISABLED_INDEX && enabledSets.size == 1) {
                                // 🇹🇷Türkçe: Bildirime açık hiçbir set yokken bu set bildirime açılıyor. Bildirimi başlat
                                // 🇬🇧English: When no set has notifications enabled, this set will be enabled. Start the notification.
                                val notificationInterval  = intervalsInMinutes[intervalIndex]
                                activity.scheduleNotificationsFromSetDetail(notificationInterval)
                                AppToast.showSuccessToast(activity, R.string.notification_set_enabled)
                            } else if (notificationEnabled && intervalIndex == PrefData.NOTIFICATION_DISABLED_INDEX && enabledSets.size == 1){
                                // 🇹🇷Türkçe: Bildirime açık hiçbir set yokken bu set bildirime açılıyor ama bildirim sıklığı ayarlarından seçim yapılmamış. Bildirim sıklığı dialog unu aç.
                                // 🇬🇧English: When no set has notifications enabled, this set is enabled but no frequency is selected. Open the notification frequency dialog.
                                showDialog = true
                            }
                            else if (intervalIndex != PrefData.NOTIFICATION_DISABLED_INDEX && enabledSets.isEmpty()){
                                // 🇹🇷Türkçe: Bildirim kapat, çünkü enabledSets boş
                                // 🇬🇧English: Turn off notification because enabledSets is empty
                                activity.scheduleNotificationsFromSetDetail(null)
                                prefData.resetIndex()
                            }else if (notificationEnabled.not()){
                                AppToast.showSuccessToast(activity, R.string.notification_set_disabled)
                            }


                        } // end coroutineScope.launch
                    }) {
                        Icon(
                            painter = painterResource(
                                id = if (notificationEnabled)
                                    R.drawable.notification_enable
                                else
                                    R.drawable.notification_disable
                            ),
                            contentDescription = "Bildirim"
                        )
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_delete_outline_24),
                            contentDescription = "Sil"
                        )
                    }
                }
            )
        },
        content = { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(setDetails?.items ?: emptyList()) {
                    DetailListItem(
                        sentence = it.wordOrSentence,
                        mean = it.meaning
                    )
                }
            }
        }
    )
}


@Composable
fun DetailListItem(
    sentence: String,
    mean: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(horizontal = 16.dp, vertical = 5.dp)
    ) {
        Text(
            text = sentence,
            fontSize = 17.sp,
            fontWeight = FontWeight.Normal, // sans-serif
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = mean,
            fontSize = 14.sp,
            fontWeight = FontWeight.Light, // sans-serif-light
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
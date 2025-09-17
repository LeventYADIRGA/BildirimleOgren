package com.lyadirga.bildirimleogren.ui

import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.lyadirga.bildirimleogren.R
import com.lyadirga.bildirimleogren.data.PrefData
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Locale
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
                        AppToast.showSuccessToast(context, R.string.notification_no_enabled_sets_message)
                    } else {
                        val notificationInterval = MainActivity.Companion.intervalsInMinutes[selectedIndex]
                        (context as MainActivity).scheduleNotifications(
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
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(text = context.getString(R.string.delete_set_title)) },
            text = { Text(text = context.getString(R.string.delete_set_message)) },
            containerColor = MaterialTheme.colorScheme.surface,
            confirmButton = {
                TextButton(onClick = {
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
                TextButton(onClick = {
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
                            contentDescription = "Back"
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
                            val intervalIndex = prefData.getNotificationIntervalIndexOnce()
                            val activity = context as MainActivity

                            if (notificationEnabled && intervalIndex != PrefData.NOTIFICATION_DISABLED_INDEX && enabledSets.size == 1) {
                                // 🇹🇷Türkçe: Bildirime açık hiçbir set yokken bu set bildirime açılıyor. Bildirimi başlat
                                // 🇬🇧English: When no set has notifications enabled, this set will be enabled. Start the notification.
                                val notificationInterval  = MainActivity.Companion.intervalsInMinutes[intervalIndex]
                                activity.scheduleNotificationsFromSetDetail(notificationInterval)
                                AppToast.showSuccessToast(activity, R.string.notification_set_enabled)
                            } else if (notificationEnabled && intervalIndex == PrefData.NOTIFICATION_DISABLED_INDEX && enabledSets.size == 1){
                                // 🇹🇷Türkçe: Bildirime açık hiçbir set yokken bu set bildirime açılıyor ama bildirim sıklığı ayarlarından seçim yapılmamış. Bildirim sıklığı dialog unu aç.
                                // 🇬🇧English: When no set has notifications enabled, this set is enabled but no frequency is selected. Open the notification frequency dialog.
                                showDialog = true
                                AppToast.showSuccessToast(activity, R.string.notification_set_enabled)
                            }
                            else if (intervalIndex != PrefData.NOTIFICATION_DISABLED_INDEX && enabledSets.isEmpty()){
                                // 🇹🇷Türkçe: Bildirim kapat, çünkü enabledSets boş
                                // 🇬🇧English: Turn off notification because enabledSets is empty
                                activity.scheduleNotificationsFromSetDetail(null)
                                AppToast.showSuccessToast(activity, R.string.notifications_disabled_no_enabled_set)
                                prefData.resetIndex()
                            }else if (notificationEnabled.not()){
                                AppToast.showSuccessToast(activity, R.string.notification_set_disabled)
                            } else if (notificationEnabled) {
                                AppToast.showSuccessToast(activity, R.string.notification_set_enabled)
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
                            contentDescription = stringResource(R.string.menu_notification)
                        )
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_delete_outline_24),
                            contentDescription = stringResource(R.string.menu_delete)
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
                val itemsList = setDetails?.items ?: emptyList()
                itemsIndexed(itemsList) { index, item ->
                    DetailListItem(
                        sentence = item.wordOrSentence,
                        mean = item.meaning
                    )
                    if (index < itemsList.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier
                                .padding(horizontal = 16.dp), // sola/sağa boşluk
                            thickness = 0.8.dp,
                            color = MaterialTheme.colorScheme.outlineVariant // tema rengi
                        )
                    }
                }
            }
        }
    )
}


@Composable
fun DetailListItem(
    sentence: String,
    mean: String,
    tts: TextToSpeech = rememberTextToSpeech()
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                tts.speak(sentence, TextToSpeech.QUEUE_FLUSH, null, null)
            }
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



@Composable
fun rememberTextToSpeech(): TextToSpeech {
    val context = LocalContext.current

    val tts = remember {
        var tmpTts: TextToSpeech? = null

        tmpTts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tmpTts?.setLanguage(Locale.getDefault())

                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    // fallback
                    tmpTts?.language = Locale.US
                    val message = when (result) {
                        TextToSpeech.LANG_MISSING_DATA -> context.getString(R.string.tts_lang_missing_data)
                        TextToSpeech.LANG_NOT_SUPPORTED -> context.getString(R.string.tts_lang_not_supported)
                        else -> context.getString(R.string.tts_unknown_error, result)
                    }
                    context.showToast(message)
                }
            } else {
                val message = context.getString(R.string.tts_unknown_error, status)
                context.showToast(message)
            }
        }

        tmpTts
    }

    // Activity/Composable lifecycleda temizleme
    DisposableEffect(Unit) {
        onDispose {
            try {
                tts.stop()
                tts.shutdown()
            } catch (throwable: Throwable) {
                Log.e("DetailScreen -> TextToSpeech: ", throwable.message.toString())
            }
        }
    }

    return tts
}

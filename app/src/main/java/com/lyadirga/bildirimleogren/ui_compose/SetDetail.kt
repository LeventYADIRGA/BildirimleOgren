package com.lyadirga.bildirimleogren.ui_compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.lyadirga.bildirimleogren.R
import com.lyadirga.bildirimleogren.data.PrefData
import com.lyadirga.bildirimleogren.ui.MainViewModel
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlin.jvm.java


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
    mainViewModel: MainViewModel = hiltViewModel(),
    onNotificationClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {}
) {

    val context = LocalContext.current

    val setDetails by mainViewModel.currentSet.collectAsState()

    // Bildirim durumu
    var notificationEnabled by remember { mutableStateOf(false) }

    val prefData = EntryPointAccessors.fromApplication(
        context.applicationContext,
        PrefDataEntryPoint::class.java
    ).prefData

    LaunchedEffect(setId) {
        mainViewModel.getSetDetails(setId) // ViewModel üzerinden set detaylarını al
        val enabledSets = prefData.getNotificationSetIdsOnce()
        notificationEnabled = setId in enabledSets
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
                            painter = painterResource(id = R.drawable.outline_arrow_back_24),
                            contentDescription = "Geri"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNotificationClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.notification_disable),
                            contentDescription = "Bildirim"
                        )
                    }
                    IconButton(onClick = onDeleteClick) {
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
            .clickable {  }
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
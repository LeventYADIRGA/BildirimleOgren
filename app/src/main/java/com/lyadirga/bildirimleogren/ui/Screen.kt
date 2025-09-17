package com.lyadirga.bildirimleogren.ui

import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed interface Screen {
    val route: String

    data object Main : Screen {
        override val route = "main"
    }

    data object Info : Screen {
        override val route = "info"
    }

    data class Detail(val setId: Long, val setTitle: String) : Screen {
        override val route = "detail/$setId/$setTitle"

        companion object {
            const val ARG_SET_ID = "setId"
            const val ARG_SET_TITLE = "setTitle"
            const val baseRoute = "detail/{$ARG_SET_ID}/{$ARG_SET_TITLE}"
            val arguments = listOf(
                navArgument(ARG_SET_ID) { type = NavType.LongType },
                navArgument(ARG_SET_TITLE) { type = NavType.StringType }
            )
        }
    }
}

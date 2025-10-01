package io.getsafenow.libraries.designcomponents.utils.snackbar

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

//@Composable
//fun SnackbarHost(hostState: SnackbarHostState, modifier: Modifier = Modifier) {
//    androidx.compose.material3.SnackbarHost(hostState, modifier) { data ->
//        Snackbar(
//            // Add default padding
//            modifier = Modifier.padding(12.dp),
//            message = data.visuals.message,
//            action = data.visuals.actionLabel?.let { ButtonVisuals.Text(it, data::performAction) },
//            dismissAction = if (data.visuals.withDismissAction) {
//                ButtonVisuals.Icon(
//                    IconSource.Vector(CompoundIcons.Close()),
//                    data::dismiss
//                )
//            } else {
//                null
//            },
//        )
//    }
//}
package com.plumedours.mire.ui

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.plumedours.mire.R

enum class MaterialAction {
    PLAY,
    RESTORE,
    DELETE,
}

/** Official Material SVG paths, rendered by Android as vector drawables. Button owns the label. */
@Composable
fun MaterialActionIcon(action: MaterialAction, modifier: Modifier = Modifier.size(24.dp)) {
    val resource =
        when (action) {
            MaterialAction.PLAY -> R.drawable.ic_material_play_arrow
            MaterialAction.RESTORE -> R.drawable.ic_material_restore
            MaterialAction.DELETE -> R.drawable.ic_material_delete_outline
        }
    Icon(painterResource(resource), contentDescription = null, modifier = modifier)
}

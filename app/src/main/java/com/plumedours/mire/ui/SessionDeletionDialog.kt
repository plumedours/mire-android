package com.plumedours.mire.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import com.plumedours.mire.domain.Session

@Composable
internal fun SessionDeletionDialog(session: Session, onDismiss: () -> Unit, onDelete: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(tr("Supprimer cette séance ?", "Delete this session?")) },
        text = {
            Text(
                tr(
                    "« ${session.name} »\nSes flèches, son journal et ses repères de parcours seront supprimés définitivement.",
                    "“${session.name}”\nIts arrows, journal and course markers will be permanently deleted.",
                )
            )
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(tr("Annuler", "Cancel")) } },
        confirmButton = {
            TextButton(
                onClick = onDelete,
                colors =
                    ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
            ) {
                Text(tr("Supprimer", "Delete"))
            }
        },
    )
}

@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.plumedours.mire.ui

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.plumedours.mire.MireViewModel
import com.plumedours.mire.data.*
import com.plumedours.mire.domain.*
import com.plumedours.mire.export.Exports
import com.plumedours.mire.ui.theme.MireTheme
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private data class ExportFile(val bytes: ByteArray, val name: String, val mime: String)

@Composable
fun MireApp(vm: MireViewModel) {
    val data by vm.data.collectAsStateWithLifecycle()
    val fatal by vm.fatal.collectAsStateWithLifecycle()
    var introComplete by rememberSaveable { mutableStateOf(false) }
    val d = data
    val theme = d?.settings?.theme ?: "sport"
    val en =
        when (d?.settings?.language) {
            "en" -> true
            "system" -> Locale.getDefault().language == "en"
            else -> false
        }
    val splashVisible = fatal == null && (!introComplete || d == null)
    val dark =
        splashVisible ||
            theme in listOf("sport", "dark", "forest", "plum", "graphite") ||
            (theme == "system" && isSystemInDarkTheme())
    val view = LocalView.current
    val context = LocalContext.current
    SideEffect {
        (context as? Activity)?.window?.let { window ->
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !dark
                isAppearanceLightNavigationBars = !dark
            }
        }
    }
    MireTheme(theme) {
        CompositionLocalProvider(LocalEnglish provides en) {
            Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                Crossfade(
                    targetState = splashVisible,
                    animationSpec = tween(200),
                    label = "launch",
                ) { opening ->
                    if (opening) BrandSplash(animate = !introComplete) { introComplete = true }
                    else if (d == null)
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            if (fatal != null)
                                Column(
                                    Modifier.safeDrawingPadding().padding(28.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp),
                                ) {
                                    Text(
                                        tr(
                                            "Impossible d’ouvrir le carnet",
                                            "Unable to open journal",
                                        ),
                                        style = MaterialTheme.typography.titleLarge,
                                    )
                                    Text(fatal!!)
                                    Button(onClick = { vm.load() }) {
                                        Text(tr("Réessayer", "Retry"))
                                    }
                                    Text(
                                        tr(
                                            "Aucune donnée n’a été effacée.",
                                            "No data has been deleted.",
                                        )
                                    )
                                }
                        }
                    else AppContent(d, vm)
                }
            }
        }
    }
}

@Composable
private fun AppContent(data: AppData, vm: MireViewModel) {
    var route by rememberSaveable { mutableStateOf("home") }
    var tab by rememberSaveable { mutableStateOf("home") }
    var formOpen by rememberSaveable { mutableStateOf(false) }
    var formInitial by remember { mutableStateOf<Session?>(null) }
    var preview by remember { mutableStateOf<ImportPreview?>(null) }
    var restore by remember { mutableStateOf(false) }
    var pendingExport by remember { mutableStateOf<ExportFile?>(null) }
    var exporting by remember { mutableStateOf(false) }
    val busy by vm.busy.collectAsStateWithLifecycle()
    val message by vm.message.collectAsStateWithLifecycle()
    val snack = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val savedMessage = tr("Fichier enregistré", "File saved")
    LaunchedEffect(message) {
        message?.let {
            snack.showSnackbar(it)
            vm.dismissMessage()
        }
    }
    val saveLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result
            ->
            val file = pendingExport
            pendingExport = null
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data
                if (file != null && uri != null)
                    scope.launch {
                        exporting = true
                        try {
                            withContext(Dispatchers.IO) {
                                context.contentResolver.openOutputStream(uri, "wt")?.use {
                                    it.write(file.bytes)
                                } ?: error("Impossible d’écrire le fichier / Cannot write file")
                            }
                            vm.tell(savedMessage)
                        } catch (e: Exception) {
                            vm.tell(e.message ?: "Export error")
                        } finally {
                            exporting = false
                        }
                    }
                else
                    vm.tell(
                        "Export interrompu : relance l’export / Export interrupted: please try again"
                    )
            }
        }
    val importLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null)
                scope.launch {
                    exporting = true
                    try {
                        val text =
                            withContext(Dispatchers.IO) {
                                context.contentResolver.openInputStream(uri)?.use { stream ->
                                    val out = ByteArrayOutputStream()
                                    val buffer = ByteArray(8192)
                                    var total = 0
                                    while (true) {
                                        val n = stream.read(buffer)
                                        if (n < 0) break
                                        total += n
                                        require(total <= 50 * 1024 * 1024) {
                                            "Fichier trop volumineux (50 Mo) / File too large"
                                        }
                                        out.write(buffer, 0, n)
                                    }
                                    out.toString(Charsets.UTF_8.name()).removePrefix("\uFEFF")
                                } ?: error("Fichier inaccessible / Cannot read file")
                            }
                        vm.prepareImport(text) { preview = it }
                    } catch (e: Exception) {
                        vm.tell(e.message ?: "Import error")
                    } finally {
                        exporting = false
                    }
                }
        }
    fun startImport() {
        importLauncher.launch(arrayOf("application/json", "text/*", "application/octet-stream"))
    }
    fun newSession(initial: Session? = null) {
        formInitial = initial
        formOpen = true
    }
    fun openSession(s: Session) {
        route = if (s.completed) "summary:${s.id}" else "score:${s.id}"
    }
    fun deleteListedSession(s: Session) {
        vm.deleteSession(s.id) {}
    }
    fun reopenListedSession(s: Session) {
        vm.complete(s.id, false) { route = "score:${s.id}" }
    }
    fun export(kind: String, id: String?) {
        if (exporting || pendingExport != null) return
        scope.launch {
            exporting = true
            try {
                val snapshot =
                    if (id == null) data
                    else
                        data.copy(
                            sessions = data.sessions.filter { it.id == id },
                            impacts = data.impacts.filter { it.sessionId == id },
                        )
                val stamp =
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                val file =
                    withContext(Dispatchers.Default) {
                        when (kind) {
                            "backup" ->
                                ExportFile(
                                    Codec.backup(data).toByteArray(Charsets.UTF_8),
                                    "mire_sauvegarde_$stamp.json",
                                    "application/json",
                                )
                            "pdf" ->
                                ExportFile(
                                    Exports.pdf(snapshot, id),
                                    "mire_rapport_$stamp.pdf",
                                    "application/pdf",
                                )
                            "xlsx" ->
                                ExportFile(
                                    Exports.xlsx(snapshot),
                                    "mire_statistiques_$stamp.xlsx",
                                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                )
                            "csv" ->
                                ExportFile(
                                    Exports.csv(snapshot),
                                    "mire_impacts_$stamp.csv",
                                    "text/csv",
                                )
                            else ->
                                ExportFile(
                                    Exports.card(data, data.sessions.first { it.id == id }),
                                    "mire_resultat_$stamp.png",
                                    "image/png",
                                )
                        }
                    }
                if (kind == "card") Exports.share(context, file.bytes, file.name, file.mime)
                else {
                    pendingExport = file
                    saveLauncher.launch(
                        Intent(Intent.ACTION_CREATE_DOCUMENT)
                            .addCategory(Intent.CATEGORY_OPENABLE)
                            .setType(file.mime)
                            .putExtra(Intent.EXTRA_TITLE, file.name)
                    )
                }
            } catch (e: Exception) {
                vm.tell(e.message ?: "Export error")
            } finally {
                exporting = false
            }
        }
    }
    val detail = route.contains(':')
    val sid = route.substringAfter(':', "")
    val session = data.sessions.find { it.id == sid }
    fun back() {
        route = if (route.startsWith("score:") && session != null) "summary:$sid" else tab
    }
    BackHandler(detail || route != "home") {
        if (detail) back()
        else {
            route = "home"
            tab = "home"
        }
    }
    val tabs =
        listOf(
            "home" to tr("Accueil", "Home"),
            "sessions" to tr("Séances", "Sessions"),
            "progress" to tr("Suivi", "Progress"),
            "equipment" to tr("Matériel", "Equipment"),
            "settings" to tr("Réglages", "Settings"),
        )
    val icons =
        mapOf(
            "home" to "home",
            "sessions" to "target",
            "progress" to "chart",
            "equipment" to "equipment",
            "settings" to "settings",
        )
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val rail = maxWidth >= 840.dp && !route.startsWith("score:")
        Row(Modifier.fillMaxSize()) {
            if (rail)
                NavigationRail(
                    Modifier.fillMaxHeight().safeDrawingPadding(),
                    header = {
                        Column(
                            Modifier.padding(vertical = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Glyph("target", Modifier.size(36.dp))
                            Text(
                                "MIRE",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    },
                ) {
                    tabs.forEach { (key, label) ->
                        NavigationRailItem(
                            selected = tab == key,
                            onClick = {
                                tab = key
                                route = key
                            },
                            icon = { Glyph(icons[key]!!) },
                            label = { Text(label) },
                        )
                    }
                }
            Scaffold(
                modifier = Modifier.weight(1f),
                containerColor = MaterialTheme.colorScheme.background,
                snackbarHost = { SnackbarHost(snack) },
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                if (detail)
                                    if (route.startsWith("score:"))
                                        tr("Séance de tir", "Shooting session")
                                    else tr("Résumé", "Summary")
                                else tabs.find { it.first == route }?.second ?: "Mire",
                                fontWeight = FontWeight.Bold,
                            )
                        },
                        navigationIcon = {
                            if (detail) IconButton(onClick = { back() }) { Glyph("back") }
                        },
                        actions = {
                            if (!detail && route != "settings")
                                IconButton(onClick = { newSession() }) { Glyph("plus") }
                        },
                    )
                },
                bottomBar = {
                    if (!rail && !detail)
                        NavigationBar {
                            tabs.forEach { (key, label) ->
                                NavigationBarItem(
                                    selected = tab == key,
                                    onClick = {
                                        tab = key
                                        route = key
                                    },
                                    icon = { Glyph(icons[key]!!) },
                                    label = { Text(label, maxLines = 1) },
                                    alwaysShowLabel = true,
                                )
                            }
                        }
                },
            ) { padding ->
                Box(Modifier.fillMaxSize().padding(padding)) {
                    when {
                        route.startsWith("score:") && session != null ->
                            ScoringScreen(data, session, vm) { route = "summary:$sid" }
                        route.startsWith("summary:") && session != null ->
                            SummaryScreen(
                                data,
                                session,
                                vm,
                                { route = "score:$sid" },
                                { newSession(it) },
                                { route = tab },
                                ::export,
                            )
                        detail ->
                            EmptyState(
                                tr("Séance introuvable", "Session not found"),
                                tr(
                                    "Elle a peut-être été supprimée ou remplacée par une restauration.",
                                    "It may have been deleted or replaced by a restore.",
                                ),
                                tr("Retour", "Back"),
                            ) {
                                route = tab
                            }
                        route == "home" ->
                            HomeScreen(
                                data,
                                { newSession() },
                                ::openSession,
                                {
                                    route = "sessions"
                                    tab = "sessions"
                                },
                                ::startImport,
                                { target ->
                                    newSession(
                                        Session(
                                            name = "",
                                            target = target,
                                            arrowsPerEnd = if (target.rule == Rule.NATURE) 2 else 3,
                                        )
                                    )
                                },
                                onDelete = ::deleteListedSession,
                                onReopen = ::reopenListedSession,
                            )
                        route == "sessions" ->
                            HistoryScreen(
                                data,
                                { newSession() },
                                ::openSession,
                                ::export,
                                ::deleteListedSession,
                                ::reopenListedSession,
                            )
                        route == "progress" ->
                            ProgressScreen(data, vm, ::openSession, { newSession(it) })
                        route == "equipment" -> EquipmentScreen(data, vm)
                        else -> SettingsScreen(data, vm, ::startImport, ::export)
                    }
                }
            }
        }
        if (busy || exporting)
            Surface(
                Modifier.align(Alignment.TopCenter).safeDrawingPadding().padding(top = 65.dp),
                shape = MaterialTheme.shapes.large,
                shadowElevation = 8.dp,
            ) {
                Row(
                    Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                    Text(tr("Traitement…", "Processing…"))
                }
            }
    }
    if (formOpen)
        SessionForm(data, formInitial, { formOpen = false }) { s ->
            vm.saveSession(s) {
                formOpen = false
                route = "score:${s.id}"
            }
        }
    preview?.let { p ->
        FormDialog(
            tr("Importer depuis ${p.source}", "Import from ${p.source}"),
            { preview = null },
            {
                vm.import(p, false) {
                    preview = null
                    route = "sessions"
                    tab = "sessions"
                }
            },
            saveText = tr("Fusionner", "Merge"),
            enabled = p.fingerprint !in data.importKeys,
        ) {
            Stats(
                "${p.data.sessions.size}" to tr("Séances", "Sessions"),
                "${p.data.impacts.size}" to tr("Impacts", "Arrows"),
                "${p.data.equipment.size}" to tr("Équipements", "Equipment"),
            )
            if (p.fingerprint in data.importKeys)
                Text(
                    tr("Ce fichier a déjà été importé.", "This file has already been imported."),
                    color = MaterialTheme.colorScheme.error,
                )
            Text(
                tr(
                    "La fusion conserve les enregistrements déjà présents avec le même identifiant.",
                    "Merge keeps existing records with the same identifier.",
                )
            )
            p.warnings.forEach { Text(it, style = MaterialTheme.typography.bodySmall) }
            HorizontalDivider()
            TextButton(onClick = { restore = true }) {
                Text(
                    tr("Restaurer en remplaçant le carnet…", "Restore and replace journal…"),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
        if (restore)
            Confirm(
                tr("Remplacer tout le carnet ?", "Replace the entire journal?"),
                tr(
                    "Les séances, le matériel, le profil et les objectifs actuels seront remplacés par cette sauvegarde. Crée une sauvegarde avant de continuer si tu veux les conserver.",
                    "Current sessions, equipment, profile and goals will be replaced by this backup. Create a backup first if you want to keep them.",
                ),
                { restore = false },
                {
                    vm.import(p, true) {
                        restore = false
                        preview = null
                        route = "sessions"
                        tab = "sessions"
                    }
                },
            )
    }
}

package com.example.fourthofficial.ui.summary

import com.example.fourthofficial.export.PdfMatchPeriod
import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.fourthofficial.domain.event.Discipline
import com.example.fourthofficial.domain.event.Score
import com.example.fourthofficial.domain.event.Substitution
import com.example.fourthofficial.domain.id.PlayerId
import com.example.fourthofficial.domain.id.TeamId
import com.example.fourthofficial.domain.match.MatchState
import com.example.fourthofficial.domain.rules.EventEditResult
import com.example.fourthofficial.domain.team.Team
import com.example.fourthofficial.export.PdfEventType
import com.example.fourthofficial.export.TeamPdfExportOptions
import com.example.fourthofficial.export.TeamPdfExporter
import com.example.fourthofficial.ui.common.DataTable
import com.example.fourthofficial.ui.common.TableColumn
import com.example.fourthofficial.ui.theme.AppButtonShape
import com.example.fourthofficial.ui.viewmodel.MatchViewModel

enum class SummaryTab {
    Scores, Substitutions, Disciplines, Export
}

@Composable
fun SummaryScreen(modifier: Modifier = Modifier, vm: MatchViewModel) {
    var currentTab by rememberSaveable { mutableStateOf(SummaryTab.Scores) }
    var selectedTeam by rememberSaveable { mutableIntStateOf(1) }
    var selectedHalf by rememberSaveable { mutableIntStateOf(1) }
    val team = if (selectedTeam == 1) vm.team1 else vm.team2

    Column(modifier = modifier.fillMaxSize()) {
        PrimaryTabRow(
            selectedTabIndex = currentTab.ordinal
        ) {
            SummaryTab.entries.forEachIndexed { index, tab ->
                Tab(
                    selected = currentTab.ordinal == index,
                    onClick = { currentTab = tab },
                    text = {
                        Text(
                            tab.name,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                )
            }
        }
        if (currentTab != SummaryTab.Export) {
            SummaryFilters(
                team = team,
                halfIndex = selectedHalf,
                onSwitchTeam = {
                    selectedTeam = switchTeams(selectedTeam)
                },
                onSwitchHalf = {
                    selectedHalf = switchHalf(selectedHalf)
                }
            )
        }

        when (currentTab) {
            SummaryTab.Scores -> ScoresTab(vm = vm, team = team, halfIndex = selectedHalf)
            SummaryTab.Substitutions -> SubstitutionsTab(
                vm = vm,
                team = team,
                halfIndex = selectedHalf
            )

            SummaryTab.Disciplines -> DisciplinesTab(vm = vm, team = team, halfIndex = selectedHalf)
            SummaryTab.Export -> ExportTab(vm = vm)
        }
    }
}

@Composable
private fun ScoresTab(modifier: Modifier = Modifier, vm: MatchViewModel, team: Team, halfIndex: Int)
{
    var selectedScore by remember { mutableStateOf<Score?>(null) }
    var editError by remember { mutableStateOf<String?>(null) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Match Scores",
            style = MaterialTheme.typography.headlineMedium
        )

        val events = vm.scoreEvents
            .filter { it.teamId == team.id && it.halfIndex == halfIndex }
            .sortedBy { it.timeMs / 1000L}

        val columns = listOf(
            TableColumn(header = "Type", weight = 1.5f) { e ->
                e.type.label },
            TableColumn(header = "Player", weight = 1.5f) { e ->
                playerLabel(team, e.playerId) },
            TableColumn<Score>(header = "Time", weight = 0.8f) { e ->
                vm.formatClock(e.timeMs, false) }
        )

        DataTable(
            events = events,
            columns = columns,
            Modifier.fillMaxWidth().weight(1f),
            keySelector = { it.id.value },
            onRowClick = { score ->
                editError = null
                selectedScore = score }
        )

        val scoreToEdit = selectedScore

        if (scoreToEdit != null) {
            EditScoreDialog(
                event = scoreToEdit,
                players = team.players,
                initialTimeText = vm.formatClock(scoreToEdit.timeMs, false),
                minTimeMs = if (scoreToEdit.halfIndex == 2) { vm.halfDurationMs } else { 0L },
                maxTimeMs = vm.maxEditableEventTimeMs(scoreToEdit.halfIndex),
                errorMessage = editError,
                onSave = { playerId, scoreType, timeMs ->
                    when (val result = vm.updateScore(
                        eventId = scoreToEdit.id,
                        playerId = playerId,
                        scoreType = scoreType,
                        timeMs = timeMs)
                    ) {
                        EventEditResult.Success -> {
                            editError = null
                            selectedScore = null
                        }

                        is EventEditResult.Failure -> {
                            editError = result.message
                        }
                    }
                },
                onCancel = {
                    editError = null
                    selectedScore = null },
                onDelete = {
                    when (val result = vm.deleteScore(scoreToEdit.id)
                    ) {
                        EventEditResult.Success -> {
                            editError = null
                            selectedScore = null
                        }

                        is EventEditResult.Failure -> {
                            editError = result.message
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun SubstitutionsTab(modifier: Modifier = Modifier, vm: MatchViewModel, team: Team, halfIndex: Int) {
    var selectedSubstitution by remember { mutableStateOf<Substitution?>(null) }
    var editError by remember { mutableStateOf<String?>(null) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = modifier.fillMaxSize().padding(16.dp)
    ) {
        Text("Match Substitutions", style = MaterialTheme.typography.headlineMedium)

        val events = vm.subEvents
            .filter { it.teamId == team.id && it.halfIndex == halfIndex }
            .sortedBy { it.timeMs / 1000L}

        val columns = listOf(
            TableColumn(header = "Off", weight = 1.5f) { e ->
                playerLabel(team, e.playerOffId)
            },
            TableColumn(header = "Reason", weight = 1.5f) { e ->
                e.type.label
            },
            TableColumn(header = "On", weight = 1.5f) { e ->
                playerLabel(team, e.playerOnId)
            },
            TableColumn<Substitution>(header = "Time", weight = 0.8f) { e ->
                vm.formatClock(e.timeMs, false)
            }
        )

        DataTable(
            events = events,
            columns = columns,
            modifier = Modifier.fillMaxWidth().weight(1f),
            keySelector = { it.id.value },
            onRowClick = { substitution ->
                editError = null
                selectedSubstitution = substitution
            }
        )

        val substitutionToEdit = selectedSubstitution

        if (substitutionToEdit != null) {
            EditSubstitutionDialog(
                event = substitutionToEdit,
                players = team.players,
                initialTimeText = vm.formatClock(substitutionToEdit.timeMs, false),
                minTimeMs = if (substitutionToEdit.halfIndex == 2) { vm.halfDurationMs } else { 0L },
                maxTimeMs = vm.maxEditableEventTimeMs(substitutionToEdit.halfIndex),
                errorMessage = editError,
                onSave = { playerOffId, playerOnId, type, timeMs ->
                    when (val result = vm.updateSubstitution(
                        eventId = substitutionToEdit.id,
                        playerOffId = playerOffId,
                        playerOnId = playerOnId,
                        type = type,
                        timeMs = timeMs)
                    ) {
                        EventEditResult.Success -> {
                            editError = null
                            selectedSubstitution = null
                        }

                        is EventEditResult.Failure -> {
                            editError = result.message
                        }
                    }
                },
                onCancel = {
                    editError = null
                    selectedSubstitution = null
                },

                onDelete = {when (val result = vm.deleteSubstitution(substitutionToEdit.id)
                ) {
                    EventEditResult.Success -> {
                        editError = null
                        selectedSubstitution = null
                    }

                    is EventEditResult.Failure -> {
                        editError = result.message
                    }
                }
                }
            )
        }
    }
}

@Composable
private fun DisciplinesTab(modifier: Modifier = Modifier, vm: MatchViewModel, team: Team, halfIndex: Int) {
    var selectedDiscipline by remember { mutableStateOf<Discipline?>(null) }
    var editError by remember { mutableStateOf<String?>(null) }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = modifier.fillMaxSize().padding(16.dp)
    ) {
        Text("Match Disciplines", style = MaterialTheme.typography.headlineMedium)

        val events = vm.discEvents
            .filter { it.teamId == team.id && it.halfIndex == halfIndex }
            .sortedBy { it.timeMs / 1000L}

        val columns = listOf(
            TableColumn(header = "Type", weight = 1.5f) { e ->
                if (e.isSecondYellow) "Second Yellow (Red Card)" else e.type.label
            },
            TableColumn(header = "Reason", weight = 1.5f) { e ->
                e.reason.label
            },
            TableColumn(header = "Player", weight = 1.5f) { e ->
                playerLabel(team, e.playerId)
            },
            TableColumn<Discipline>(header = "Time", weight = 0.8f) { e ->
                vm.formatClock(e.timeMs, false)
            }
        )

        DataTable(
            events = events,
            columns = columns,
            modifier = Modifier.fillMaxWidth().weight(1f),
            keySelector = { it.id.value },
            onRowClick = { discipline ->
                editError = null
                selectedDiscipline = discipline
            }
        )
    }

    val disciplineToEdit = selectedDiscipline

    if (disciplineToEdit != null) {
        EditDisciplineDialog(
            event = disciplineToEdit,
            players = team.players,
            initialTimeText = vm.formatClock(disciplineToEdit.timeMs, false),
            minTimeMs = if (disciplineToEdit.halfIndex == 2) { vm.halfDurationMs } else { 0L },
            maxTimeMs = vm.maxEditableEventTimeMs(disciplineToEdit.halfIndex),
            errorMessage = editError,
            onSave = { playerId, type, reason, timeMs ->
                when (val result = vm.updateDiscipline(
                    eventId = disciplineToEdit.id,
                    playerId = playerId,
                    type = type,
                    reason = reason,
                    timeMs = timeMs)
                ) {
                    EventEditResult.Success -> {
                        editError = null
                        selectedDiscipline = null
                    }

                    is EventEditResult.Failure -> {
                        editError = result.message
                    }
                }
            },

            onCancel = {
                editError = null
                selectedDiscipline = null
            },

            onDelete = {
                when (val result = vm.deleteDiscipline(disciplineToEdit.id)
                ) {
                    EventEditResult.Success -> {
                        editError = null
                        selectedDiscipline = null
                    }

                    is EventEditResult.Failure -> {
                        editError = result.message
                    }
                }
            }
        )
    }
}

@Composable
private fun ExportTab(modifier: Modifier = Modifier, vm: MatchViewModel) {
    val context = LocalContext.current
    val exporter = remember { TeamPdfExporter() }
    var includeScores by rememberSaveable { mutableStateOf(true) }
    var includeTeam1 by rememberSaveable { mutableStateOf(true) }
    var includeTeam2 by rememberSaveable { mutableStateOf(true) }
    var includeSubstitutions by rememberSaveable { mutableStateOf(true) }
    var includeDiscipline by rememberSaveable { mutableStateOf(true) }
    var selectedPeriod by rememberSaveable { mutableStateOf(PdfMatchPeriod.FULL_MATCH) }
    var pendingExport by remember { mutableStateOf<PendingPdfExport?>(null) }
    var exportMessage by remember { mutableStateOf<String?>(null) }
    val team1 = vm.team1
    val team2 = vm.team2
    val includedTeamIds =
        buildSet {
            if (includeTeam1) { add(team1.id) }
            if (includeTeam2) { add(team2.id) }
        }

    val includedEventTypes =
        buildSet {
            if (includeScores) {
                add(PdfEventType.SCORE)
            }

            if (includeSubstitutions) {
                add(PdfEventType.SUBSTITUTION)
            }

            if (includeDiscipline) {
                add(PdfEventType.DISCIPLINE)
            }
        }

    val exportOptions = TeamPdfExportOptions(
        includedEventTypes = includedEventTypes,
        period = selectedPeriod
    )

    val createPdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf"))
    { uri ->
        if (uri == null) {
            pendingExport = null
            return@rememberLauncherForActivityResult
        }

        val export = pendingExport ?: return@rememberLauncherForActivityResult

        try {
            val outputStream = context.contentResolver.openOutputStream(uri)
                ?: throw IllegalStateException("Could not open the selected file.")

            outputStream.use { stream ->
                exporter.export(
                    matchState = export.matchState,
                    teamIds = export.teamIds,
                    options = export.options,
                    outputStream = stream
                )
            }

            exportMessage = "PDF exported successfully."
        } catch (exception: Exception) {
            exportMessage = "Export failed: " + (exception.message ?: "Unknown error.")
        } finally {
            pendingExport = null
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = modifier.fillMaxSize().padding(16.dp)
    ) {
        Text("Export Match Report", style = MaterialTheme.typography.headlineMedium)
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Teams",
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Checkbox(
                        checked = includeTeam1,
                        onCheckedChange = {
                            includeTeam1 = it
                            exportMessage = null
                        }
                    )

                    Text(team1.name.ifBlank { "Team ${team1.index}" })
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Checkbox(
                        checked = includeTeam2,
                        onCheckedChange = {
                            includeTeam2 = it
                            exportMessage = null
                        }
                    )

                    Text(team2.name.ifBlank { "Team ${team2.index}" })
                }
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Period",
                style = MaterialTheme.typography.titleMedium
            )

            PdfMatchPeriod.entries.forEach { period ->
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedPeriod == period,
                        onClick = {
                            selectedPeriod = period
                            exportMessage = null
                        }
                    )
                    Text(period.label)
                }
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Include events", style = MaterialTheme.typography.titleMedium)

            Row(verticalAlignment = Alignment.CenterVertically)
            {
                Checkbox(
                    checked = includeScores,
                    onCheckedChange = {
                        includeScores = it
                        exportMessage = null
                    }
                )

                Text("Scores")
            }

            Row(verticalAlignment = Alignment.CenterVertically)
            {
                Checkbox(
                    checked = includeSubstitutions,
                    onCheckedChange = {
                        includeSubstitutions = it
                        exportMessage = null
                    }
                )

                Text("Substitutions")
            }

            Row(verticalAlignment = Alignment.CenterVertically)
            {
                Checkbox(
                    checked = includeDiscipline,
                    onCheckedChange = {
                        includeDiscipline = it
                        exportMessage = null
                    }
                )

                Text("Discipline")
            }
        }

        Button(
            enabled = includedTeamIds.isNotEmpty() && exportOptions.hasSelectedEventTypes,
            onClick = {
                exportMessage = null
                pendingExport = PendingPdfExport(
                    matchState = vm.matchState,
                    teamIds = includedTeamIds,
                    options = exportOptions
                )

                createPdfLauncher.launch(
                    buildPdfFileName(
                        selectedTeamIds = includedTeamIds,
                        team1 = team1,
                        team2 = team2,
                        period = selectedPeriod
                    )
                )
            },
            shape = AppButtonShape
        ) {
            Text("Export PDF")
        }

        exportMessage?.let { Text(it) }
    }
}

private data class PendingPdfExport(
    val matchState: MatchState,
    val teamIds: Set<TeamId>,
    val options: TeamPdfExportOptions
)

private fun buildPdfFileName(selectedTeamIds: Set<TeamId>, team1: Team,
                             team2: Team, period: PdfMatchPeriod): String
{
    val team1Name = team1.name.ifBlank { "Team ${team1.index}" }
    val team2Name = team2.name.ifBlank { "Team ${team2.index}" }

    val reportName =
        when {
            team1.id in selectedTeamIds && team2.id in selectedTeamIds -> "Match_Report"
            team1.id in selectedTeamIds -> "${team1Name}_Report"
            else -> "${team2Name}_Report"
        }

    val rawName = "${team1Name}_vs_${team2Name}_${period.label}_$reportName"

    val safeName =
        rawName
            .replace(Regex("[^\\p{L}\\p{N}._-]+"), "_")
            .trim('_')

    return "$safeName.pdf"
}

@Composable
private fun SummaryFilters(
    team: Team,
    halfIndex: Int,
    onSwitchTeam: () -> Unit,
    onSwitchHalf: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Button(onClick = onSwitchTeam, shape = AppButtonShape) {
                Text(
                    team.name.ifBlank {
                        "Team ${team.index}"
                    }
                )
            }
        }

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Button(onClick = onSwitchHalf, shape = AppButtonShape) {
                Text("Half $halfIndex")
            }
        }
    }
}

private fun switchTeams(index: Int) =
    if (index == 1) 2 else 1

private fun switchHalf(index: Int) =
    if (index == 1) 2 else 1

private fun playerLabel(team: Team, playerId: PlayerId?): String {
    if (playerId == null) {
        return "-"
    }
    return team.players
        .find { it.id == playerId }
        ?.let { "${it.number}. ${it.name.ifBlank { "(Unnamed)" }}" }
        ?: "Unknown player"
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true)
@Composable
private fun SummaryScreenPreview() {
    SummaryScreen(vm = MatchViewModel())
}
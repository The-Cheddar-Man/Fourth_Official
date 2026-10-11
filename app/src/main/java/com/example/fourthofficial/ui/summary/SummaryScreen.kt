package com.example.fourthofficial.ui.summary

import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.semantics.Role
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
import com.example.fourthofficial.export.PdfMatchPeriod
import com.example.fourthofficial.export.TeamPdfExportOptions
import com.example.fourthofficial.export.TeamPdfExporter
import com.example.fourthofficial.ui.common.DataTable
import com.example.fourthofficial.ui.common.TableColumn
import com.example.fourthofficial.ui.theme.AppButtonShape
import com.example.fourthofficial.ui.theme.AppCardShape
import com.example.fourthofficial.ui.theme.SummaryScreenHeaderHeight
import com.example.fourthofficial.ui.viewmodel.MatchViewModel

enum class SummaryTab(val label: String) {
    Scores("Scores"),
    Substitutions("Subs"),
    Discipline("Discipline"),
    Export("Export")
}

@Composable
fun SummaryScreen(modifier: Modifier = Modifier, vm: MatchViewModel, onShowMessage: (String) -> Unit) {
    var currentTab by rememberSaveable { mutableStateOf(SummaryTab.Scores) }
    var selectedTeam by rememberSaveable { mutableIntStateOf(1) }
    var selectedHalf by rememberSaveable { mutableIntStateOf(1) }
    val team = if (selectedTeam == 1) vm.team1 else vm.team2

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (currentTab != SummaryTab.Export) {
            SummaryEventHeader(
                currentTab = currentTab,
                onTabSelected = { currentTab = it },
                team = team,
                halfIndex = selectedHalf,
                onSwitchTeam = { selectedTeam = switchTeams(selectedTeam) },
                onSwitchHalf = { selectedHalf = switchHalf(selectedHalf) }
            )
        }

        when (currentTab) {
            SummaryTab.Scores -> ScoresTab(
                modifier = Modifier.weight(1f),
                vm = vm,
                team = team,
                halfIndex = selectedHalf
            )
            SummaryTab.Substitutions -> SubstitutionsTab(
                modifier = Modifier.weight(1f),
                vm = vm,
                team = team,
                halfIndex = selectedHalf
            )
            SummaryTab.Discipline -> DisciplinesTab(
                modifier = Modifier.weight(1f),
                vm = vm,
                team = team,
                halfIndex = selectedHalf
            )
            SummaryTab.Export -> ExportTab(
                modifier = Modifier.weight(1f),
                vm = vm,
                currentTab = currentTab,
                onTabSelected = { currentTab = it },
                onShowMessage = onShowMessage
            )
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
        modifier = modifier.fillMaxSize().padding(horizontal = 16.dp)
    ) {
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
            modifier = Modifier.fillMaxWidth().weight(1f),
            keySelector = { it.id.value },
            onRowClick = { score ->
                editError = null
                selectedScore = score
            },
            emptyMessage = "No scores recorded for " +
                "${team.name.ifBlank { "Team ${team.index}" }} - " +
                if (halfIndex == 1) { "First Half" } else { "Second Half" }
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
        modifier = modifier.fillMaxSize().padding(16.dp)
    ) {
        val events = vm.subEvents
            .filter { it.teamId == team.id && it.halfIndex == halfIndex }
            .sortedWith(compareBy<Substitution> { it.timeMs / 1000L }.thenBy {
                if (it.occurredAtHalfTime) { 0 } else { 1 }
            })

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
            TableColumn<Substitution>(header = "Time", weight = 1.1f) { e ->
                if (e.occurredAtHalfTime) {
                    "Half Time"
                } else {
                    vm.formatClock(e.timeMs, false)
                }
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
            },
            emptyMessage = "No substitutions recorded for " +
                "${team.name.ifBlank { "Team ${team.index}" }} - " +
                if (halfIndex == 1) { "First Half" } else { "Second Half" }
        )

        val substitutionToEdit = selectedSubstitution

        if (substitutionToEdit != null) {
            EditSubstitutionDialog(
                event = substitutionToEdit,
                players = team.players,
                initialTimeText = vm.formatClock(substitutionToEdit.timeMs, false),
                availablePeriods = vm.availableSubstitutionEditPeriods(),
                halfDurationMs = vm.halfDurationMs,
                firstHalfMaxTimeMs = vm.maxEditableEventTimeMs(1),
                secondHalfMaxTimeMs = vm.maxEditableEventTimeMs(2),
                errorMessage = editError,
                onSave = { playerOffId, playerOnId, type, period, timeMs ->
                    when (val result = vm.updateSubstitution(
                        eventId = substitutionToEdit.id,
                        playerOffId = playerOffId,
                        playerOnId = playerOnId,
                        type = type,
                        period = period,
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
        modifier = modifier.fillMaxSize().padding(16.dp)
    ) {
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
            },
            emptyMessage = "No discipline recorded for " +
                "${team.name.ifBlank { "Team ${team.index}" }} - " +
                if (halfIndex == 1) { "First Half" } else { "Second Half" }
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
private fun ExportTab(modifier: Modifier = Modifier, vm: MatchViewModel,
                      currentTab: SummaryTab, onTabSelected: (SummaryTab) -> Unit,
                      onShowMessage: (String) -> Unit) {
    val context = LocalContext.current
    val exporter = remember { TeamPdfExporter() }
    var includeScores by rememberSaveable { mutableStateOf(true) }
    var includeTeam1 by rememberSaveable { mutableStateOf(true) }
    var includeTeam2 by rememberSaveable { mutableStateOf(true) }
    var includeSubstitutions by rememberSaveable { mutableStateOf(true) }
    var includeDiscipline by rememberSaveable { mutableStateOf(true) }
    var selectedPeriod by rememberSaveable { mutableStateOf(PdfMatchPeriod.FULL_MATCH) }
    var pendingExport by remember { mutableStateOf<PendingPdfExport?>(null) }
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

            onShowMessage("PDF exported successfully.")
        } catch (exception: Exception) {
            onShowMessage("Export failed: " + (exception.message ?: "Unknown error."))
        } finally {
            pendingExport = null
        }
    }

    val canExport = includedTeamIds.isNotEmpty() && exportOptions.hasSelectedEventTypes
    val exportValidationMessage = when {
        includedTeamIds.isEmpty() -> "Select at least one team."
        !exportOptions.hasSelectedEventTypes -> "Select at least one event type."
        else -> null
    }
    val startExport: () -> Unit = {
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
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SummaryExportHeader(
            enabled = canExport,
            onExport = startExport,
            currentTab = currentTab,
            onTabSelected = onTabSelected
        )

        exportValidationMessage?.let { message ->
            Text(
                text = message,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
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
                        modifier = Modifier
                            .weight(1f)
                            .toggleable(
                                value = includeTeam1,
                                role = Role.Checkbox,
                                onValueChange = { includeTeam1 = it }
                            )
                    ) {
                        Checkbox(
                            checked = includeTeam1,
                            onCheckedChange = null
                        )

                        Text(
                            text = team1.name.ifBlank { "Team ${team1.index}" },
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .toggleable(
                                value = includeTeam2,
                                role = Role.Checkbox,
                                onValueChange = { includeTeam2 = it }
                            )
                    ) {
                        Checkbox(
                            checked = includeTeam2,
                            onCheckedChange = null
                        )

                        Text(
                            text = team2.name.ifBlank { "Team ${team2.index}" },
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
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
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selectedPeriod == period,
                                role = Role.RadioButton,
                                onClick = {
                                    selectedPeriod = period
                                }
                            )
                    ) {
                        RadioButton(
                            selected = selectedPeriod == period,
                            onClick = null
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

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .toggleable(
                            value = includeScores,
                            role = Role.Checkbox,
                            onValueChange = {
                                includeScores = it
                            }
                        )
                ) {
                    Checkbox(
                        checked = includeScores,
                        onCheckedChange = null
                    )

                    Text("Scores")
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .toggleable(
                            value = includeSubstitutions,
                            role = Role.Checkbox,
                            onValueChange = {
                                includeSubstitutions = it
                            }
                        )
                ) {
                    Checkbox(
                        checked = includeSubstitutions,
                        onCheckedChange = null
                    )

                    Text("Substitutions")
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .toggleable(
                            value = includeDiscipline,
                            role = Role.Checkbox,
                            onValueChange = {
                                includeDiscipline = it
                            }
                        )
                ) {
                    Checkbox(
                        checked = includeDiscipline,
                        onCheckedChange = null
                    )

                    Text("Discipline")
                }
            }
        }
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
private fun SummaryHeader(currentTab: SummaryTab, onTabSelected: (SummaryTab) -> Unit,
                          content: @Composable RowScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().height(SummaryScreenHeaderHeight),
        shape = AppCardShape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column {
            PrimaryTabRow(selectedTabIndex = currentTab.ordinal) {
                SummaryTab.entries.forEachIndexed { index, tab ->
                    Tab(
                        selected = currentTab.ordinal == index,
                        onClick = { onTabSelected(tab) },
                        text = {
                            Text(
                                text = tab.label,
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                content = content
            )
        }
    }
}

@Composable
private fun SummaryEventHeader(
    currentTab: SummaryTab,
    onTabSelected: (SummaryTab) -> Unit,
    team: Team,
    halfIndex: Int,
    onSwitchTeam: () -> Unit,
    onSwitchHalf: () -> Unit
) {
    SummaryHeader(
        currentTab = currentTab,
        onTabSelected = onTabSelected
    ) {
        OutlinedButton(
            onClick = onSwitchTeam,
            modifier = Modifier.weight(1f),
            shape = AppButtonShape,
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Text(
                text = team.name.ifBlank { "Team ${team.index}" },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        OutlinedButton(
            onClick = onSwitchHalf,
            modifier = Modifier.weight(1f),
            shape = AppButtonShape,
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Text(
                text = if (halfIndex == 1) "First Half" else "Second Half",
                maxLines = 1
            )
        }
    }
}

@Composable
private fun SummaryExportHeader(
    currentTab: SummaryTab,
    onTabSelected: (SummaryTab) -> Unit,
    enabled: Boolean,
    onExport: () -> Unit) {
    SummaryHeader(
        currentTab = currentTab,
        onTabSelected = onTabSelected
    ) {
        Button(
            onClick = onExport,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
            shape = AppButtonShape
        ) {
            Text("Export PDF")
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
    SummaryScreen(
        vm = MatchViewModel(),
        onShowMessage = {}
    )
}
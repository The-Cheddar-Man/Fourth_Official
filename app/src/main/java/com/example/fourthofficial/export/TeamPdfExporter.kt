package com.example.fourthofficial.export

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.fourthofficial.domain.event.Discipline
import com.example.fourthofficial.domain.event.MatchEvent
import com.example.fourthofficial.domain.event.Score
import com.example.fourthofficial.domain.event.Substitution
import com.example.fourthofficial.domain.id.PlayerId
import com.example.fourthofficial.domain.id.TeamId
import com.example.fourthofficial.domain.match.MatchPhase
import com.example.fourthofficial.domain.match.MatchState
import com.example.fourthofficial.domain.rules.calculateScore
import com.example.fourthofficial.domain.team.Team
import java.io.OutputStream
import kotlin.math.max

class TeamPdfExporter {
    companion object {
        private const val PAGE_WIDTH = 595
        private const val PAGE_HEIGHT = 842
        private const val MARGIN = 40f
        private const val BOTTOM_MARGIN = 50f
        private const val HALF_X = MARGIN
        private const val TIME_X = MARGIN + 45f
        private const val EVENT_X = MARGIN + 105f
        private const val DETAILS_X = MARGIN + 210f
        private const val DETAILS_WIDTH = PAGE_WIDTH - MARGIN - DETAILS_X
        private const val LINE_HEIGHT = 14f
        private const val ROW_VERTICAL_PADDING = 7f
    }

    fun export(matchState: MatchState, teamId: TeamId,
               options: TeamPdfExportOptions, outputStream: OutputStream)
    {
        require(options.hasSelectedEventTypes) { "At least one event type must be selected." }

        val selectedTeam = when (teamId) {
            matchState.team1.team.id -> matchState.team1.team
            matchState.team2.team.id -> matchState.team2.team
            else -> throw IllegalArgumentException("Selected team does not belong to this match.")
        }

        val orderedEvents = getOrderedEvents(
            matchState = matchState,
            teamId = teamId,
            options = options
        )

        val pdf = PdfDocument()

        try {
            var pageNumber = 1
            var page = startPage(pdf = pdf, pageNumber = pageNumber)
            var canvas = page.canvas

            var y = drawReportHeader(
                canvas = canvas,
                matchState = matchState,
                selectedTeam = selectedTeam,
                options = options
            )

            y = drawTableHeader(canvas = canvas, y = y)

            if (orderedEvents.isEmpty()) {
                val bodyPaint = bodyPaint()

                canvas.drawText(
                    "No matching events recorded.",
                    MARGIN,
                    y + 10f, bodyPaint
                )
            } else {
                for (event in orderedEvents) {
                    val detailLines =
                        wrapText(
                            text = eventDetails(event = event, team = selectedTeam),
                            paint = bodyPaint(),
                        )

                    val rowHeight = calculateRowHeight(detailLines)

                    if (y + rowHeight > PAGE_HEIGHT - BOTTOM_MARGIN)
                    {
                        drawFooter(canvas = canvas, pageNumber = pageNumber)
                        pdf.finishPage(page)
                        pageNumber++
                        page = startPage(pdf = pdf, pageNumber = pageNumber)
                        canvas = page.canvas
                        y = drawContinuationHeader(canvas = canvas, selectedTeam = selectedTeam)
                        y = drawTableHeader(canvas = canvas, y = y)
                    }

                    y = drawEventRow(
                        canvas = canvas,
                        event = event,
                        detailLines = detailLines,
                        y = y
                    )
                }
            }

            drawFooter(canvas = canvas, pageNumber = pageNumber)
            pdf.finishPage(page)
            pdf.writeTo(outputStream)
        }
        finally {
            pdf.close()
        }
    }

    private fun getOrderedEvents(matchState: MatchState, teamId: TeamId,
                                 options: TeamPdfExportOptions): List<MatchEvent>
    {
        return matchState.events
            .withIndex()
            .filter { indexedEvent ->
                val event = indexedEvent.value
                event.teamId == teamId && eventType(event) in options.includedEventTypes
            }.sortedWith(
                compareBy<IndexedValue<MatchEvent>> { it.value.halfIndex }
                    .thenBy { it.value.timeMs / 1000L }
                    .thenBy { it.index }
            ).map { it.value }
    }

    private fun eventType(event: MatchEvent): PdfEventType
    {
        return when (event) {
            is Score -> PdfEventType.SCORE
            is Substitution -> PdfEventType.SUBSTITUTION
            is Discipline -> PdfEventType.DISCIPLINE
        }
    }

    private fun startPage(pdf: PdfDocument, pageNumber: Int): PdfDocument.Page
    {
        val pageInfo =
            PdfDocument.PageInfo.Builder(
                PAGE_WIDTH,
                PAGE_HEIGHT,
                pageNumber
            ).create()

        return pdf.startPage(pageInfo)
    }

    private fun drawReportHeader(canvas: Canvas, matchState: MatchState,
                                 selectedTeam: Team, options: TeamPdfExportOptions): Float
    {
        var y = MARGIN
        canvas.drawText(
            "Fourth Official Match Report",
            MARGIN,
            y,
            titlePaint()
        )

        y += 32f

        val team1 = matchState.team1.team
        val team2 = matchState.team2.team

        canvas.drawText("${teamName(team1)} vs ${teamName(team2)}",
            MARGIN,
            y,
            headingPaint())

        y += 24f

        canvas.drawText(
            "Team report: ${teamName(selectedTeam)}",
            MARGIN,
            y,
            bodyBoldPaint()
        )

        y += 22f

        val team1Score = scoreForTeam(matchState = matchState, teamId = team1.id)
        val team2Score = scoreForTeam(matchState = matchState, teamId = team2.id)

        val scoreLabel =
            if (matchState.phase == MatchPhase.FINISHED) {
                "Final score"
            } else {
                "Current score"
            }

        canvas.drawText(
            "$scoreLabel: " + "${teamName(team1)} $team1Score - " + "$team2Score ${teamName(team2)}",
            MARGIN,
            y,
            bodyPaint()
        )

        y += 20f

        if (matchState.phase == MatchPhase.HALF_TIME ||
            matchState.phase == MatchPhase.SECOND_HALF ||
            matchState.phase == MatchPhase.FINISHED
        ) {
            val team1HalfTimeScore = firstHalfScoreForTeam(matchState = matchState, teamId = team1.id)
            val team2HalfTimeScore = firstHalfScoreForTeam(matchState = matchState, teamId = team2.id)

            canvas.drawText(
                "Half-time: " + "${teamName(team1)} $team1HalfTimeScore - " + "$team2HalfTimeScore ${teamName(team2)}",
                MARGIN,
                y,
                bodyPaint()
            )

            y += 20f
        }

        val includedTypes = PdfEventType.entries
            .filter { it in options.includedEventTypes }
            .joinToString(", ") { it.label }

        canvas.drawText(
            "Included events: $includedTypes",
            MARGIN,
            y,
            bodyPaint()
        )

        return y + 30f
    }

    private fun drawContinuationHeader(canvas: Canvas, selectedTeam: Team): Float
    {
        canvas.drawText(
            "${teamName(selectedTeam)} - Match Events",
            MARGIN,
            MARGIN,
            headingPaint()
        )

        return MARGIN + 28f
    }

    private fun drawTableHeader(canvas: Canvas, y: Float): Float
    {
        val paint = tableHeaderPaint()

        canvas.drawText("Half", HALF_X, y, paint)
        canvas.drawText("Time", TIME_X, y, paint)
        canvas.drawText("Event", EVENT_X, y, paint)
        canvas.drawText("Details", DETAILS_X, y, paint)
        canvas.drawLine(MARGIN, y + 7f, PAGE_WIDTH - MARGIN, y + 7f, paint)

        return y + 25f
    }

    private fun drawEventRow(canvas: Canvas, event: MatchEvent,
        detailLines: List<String>, y: Float): Float
    {
        val rowHeight = calculateRowHeight(detailLines)
        val textY = y + ROW_VERTICAL_PADDING + 9f

        canvas.drawText("H${event.halfIndex}", HALF_X, textY, bodyPaint())
        canvas.drawText(formatMatchTime(event.timeMs), TIME_X, textY, bodyPaint())
        canvas.drawText(eventLabel(event), EVENT_X, textY, bodyBoldPaint())

        detailLines.forEachIndexed { index, line ->
            canvas.drawText(
                line,
                DETAILS_X,
                textY + (index * LINE_HEIGHT),
                bodyPaint()
            )
        }

        canvas.drawLine(
            MARGIN,
            y + rowHeight,
            PAGE_WIDTH - MARGIN,
            y + rowHeight,
            dividerPaint()
        )

        return y + rowHeight
    }

    private fun eventLabel(event: MatchEvent): String
    {
        return when (event) {
            is Score -> event.type.label
            is Substitution -> "Substitution"
            is Discipline ->
                if (event.isSecondYellow) {
                    "Second Yellow"
                } else {
                    event.type.label
                }
        }
    }

    private fun eventDetails(event: MatchEvent, team: Team): String
    {
        return when (event) {
            is Score -> { playerLabel(team = team, playerId = event.playerId) }
            is Substitution -> {
                "${playerLabel(team, event.playerOffId)} off, " +
                "${playerLabel(team, event.playerOnId)} on " +
                "(${event.type.label})"
            }

            is Discipline -> { "${playerLabel(team, event.playerId)} - " + event.reason.label }
        }
    }

    private fun scoreForTeam(matchState: MatchState, teamId: TeamId): Int
    {
        return calculateScore(matchState.events.filterIsInstance<Score>().filter {
            it.teamId == teamId
        })
    }

    private fun firstHalfScoreForTeam(matchState: MatchState, teamId: TeamId): Int
    {
        return calculateScore(
            matchState.events
                .filterIsInstance<Score>()
                .filter { it.teamId == teamId && it.halfIndex == 1 }
        )
    }

    private fun playerLabel(team: Team, playerId: PlayerId): String
    {
        return team.players
            .find { it.id == playerId }
            ?.let { "${it.number}. " + it.name.ifBlank { "(Unnamed)" } }
            ?: "Unknown player"
    }

    private fun teamName(team: Team): String
    {
        return team.name.ifBlank { "Team ${team.index}" }
    }

    private fun formatMatchTime(timeMs: Long): String
    {
        val totalSeconds = timeMs / 1000L
        val minutes = totalSeconds / 60L
        val seconds = totalSeconds % 60L
        return "%02d:%02d".format(minutes, seconds)
    }

    private fun calculateRowHeight(detailLines: List<String>): Float
    {
        val textHeight = max(1, detailLines.size) * LINE_HEIGHT
        return textHeight + (ROW_VERTICAL_PADDING * 2)
    }

    private fun wrapText(text: String, paint: Paint): List<String>
    {
        if (text.isBlank()) { return listOf("") }

        val words = text.split(Regex("\\s+"))
        val lines = mutableListOf<String>()
        var currentLine = ""

        for (word in words)
        {
            val candidate =
                if (currentLine.isEmpty()) {
                    word
                } else {
                    "$currentLine $word"
                }

            if (paint.measureText(candidate) <= DETAILS_WIDTH) {
                currentLine = candidate
            } else {
                if (currentLine.isNotEmpty()) { lines += currentLine }
                if (paint.measureText(word) <= DETAILS_WIDTH) {
                    currentLine = word
                } else {
                    val brokenWordLines = breakLongWord(word = word, paint = paint)
                    if (brokenWordLines.isNotEmpty()) {
                        lines += brokenWordLines.dropLast(1)
                        currentLine = brokenWordLines.last()
                    }
                }
            }
        }

        if (currentLine.isNotEmpty()) { lines += currentLine }
        return lines.ifEmpty { listOf("") }
    }

    private fun breakLongWord(word: String, paint: Paint): List<String> {
        val lines = mutableListOf<String>()
        var remaining = word
        while (remaining.isNotEmpty()) {
            val characterCount =
                paint.breakText(
                    remaining,
                    true,
                    DETAILS_WIDTH,
                    null
                )

            if (characterCount <= 0) { break }

            lines += remaining.take(characterCount)
            remaining = remaining.substring(characterCount)
        }

        return lines
    }

    private fun drawFooter(canvas: Canvas, pageNumber: Int)
    {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 9f }
        canvas.drawText("Fourth Official", MARGIN, PAGE_HEIGHT - 25f, paint)

        val pageText = "Page $pageNumber"

        canvas.drawText(pageText,
            PAGE_WIDTH - MARGIN - paint.measureText(pageText),
            PAGE_HEIGHT - 25f, paint
        )
    }

    private fun titlePaint() =
        Paint(Paint.ANTI_ALIAS_FLAG).apply{
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

    private fun headingPaint() =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

    private fun bodyPaint() = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 10f }

    private fun bodyBoldPaint() =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

    private fun tableHeaderPaint() =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

    private fun dividerPaint() = Paint(Paint.ANTI_ALIAS_FLAG).apply { strokeWidth = 0.5f }
}
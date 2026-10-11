# File Structure

The main application source is under:

```text
app/src/main/java/com/thecheddarman/fourthofficial/
├─ AppRoot.kt
├─ MainActivity.kt
│
├─ dev/
│  └─ DevTestData.kt
│
├─ domain/
│  ├─ event/
│  │  ├─ DisciplineType.kt
│  │  ├─ MatchEvent.kt
│  │  ├─ ScoreType.kt
│  │  └─ SubstitutionType.kt
│  │
│  ├─ id/
│  │  └─ Ids.kt
│  │
│  ├─ match/
│  │  ├─ MatchClock.kt
│  │  ├─ MatchPhase.kt
│  │  ├─ MatchPlayerState.kt
│  │  ├─ MatchState.kt
│  │  ├─ MatchTeamState.kt
│  │  └─ PreparedSubstitutionBatch.kt
│  │
│  ├─ rules/
│  │  ├─ DisciplineRules.kt
│  │  ├─ EventEditResult.kt
│  │  ├─ MatchEventReplay.kt
│  │  ├─ MatchRules.kt
│  │  ├─ ScoringRules.kt
│  │  └─ SubstitutionRules.kt
│  │
│  └─ team/
│     ├─ Player.kt
│     └─ Team.kt
│
├─ export/
│  ├─ TeamPdfExportOptions.kt
│  └─ TeamPdfExporter.kt
│
└─ ui/
   ├─ common/
   │  ├─ AppAlertDialog.kt
   │  ├─ DataTable.kt
   │  ├─ PlayerTile.kt
   │  ├─ SingleChoiceDialog.kt
   │  └─ TeamPanel.kt
   │
   ├─ match/
   │  ├─ MatchScreen.kt
   │  ├─ MatchScreenUiState.kt
   │  └─ components/
   │     ├─ MatchContent.kt
   │     ├─ MatchDialogs.kt
   │     ├─ SubstitutionPreparationContent.kt
   │     └─ TeamColumn.kt
   │
   ├─ setup/
   │  └─ MatchSetupScreen.kt
   │
   ├─ summary/
   │  ├─ SummaryEditDialogs.kt
   │  └─ SummaryScreen.kt
   │
   ├─ theme/
   │  ├─ Color.kt
   │  ├─ Dimensions.kt
   │  ├─ Shape.kt
   │  ├─ Theme.kt
   │  └─ Type.kt
   │
   └─ viewmodel/
      └─ MatchViewModel.kt
```

## Structure Overview

### Root

`MainActivity.kt` is the Android entry point for the application.

`AppRoot.kt` contains the main Compose application structure and top-level navigation between the current Setup, Match and Summary areas.

---

### `dev/`

Contains development and testing utilities.

`DevTestData.kt` contains synthetic match and team data used to quickly populate the app during development.

This code is intended to be moved to a debug-only source set before the public release.

---

### `domain/`

Contains the core match data and rules independently of the user interface.

#### `domain/event/`

Defines the different events which can occur during a match.

This includes:

- Scoring events
- Substitutions
- Disciplinary events

#### `domain/id/`

Contains strongly typed identifiers used by domain objects.

#### `domain/match/`

Contains the current state of a match.

This includes:

- Match clock
- Match phase
- Team state
- Player state
- Prepared substitution batches

#### `domain/rules/`

Contains rugby match logic and event-processing rules.

This includes:

- Scoring rules
- Substitution rules
- Discipline rules
- Match-event replay
- Event validation and editing

#### `domain/team/`

Contains the basic team and player models.

---

### `export/`

Contains match export functionality.

`TeamPdfExportOptions.kt` defines the configuration used when exporting match information.

`TeamPdfExporter.kt` generates the PDF match report.

---

### `ui/`

Contains the Jetpack Compose user interface.

#### `ui/common/`

Contains reusable UI components shared between multiple screens.

This includes:

- Alert dialogs
- Data tables
- Player tiles
- Selection dialogs
- Team panels

#### `ui/match/`

Contains the main live-match interface.

`MatchScreen.kt` defines the main match screen.

`MatchScreenUiState.kt` contains UI-specific state used by the match screen.

The `components/` directory contains the smaller composables used to build the match screen, including team displays, match content, dialogs and substitution preparation.

#### `ui/setup/`

Contains the match setup interface used to enter teams and matchday squads before a match begins.

#### `ui/summary/`

Contains the match summary interface.

This area is responsible for:

- Reviewing recorded match events
- Filtering scores, substitutions and disciplinary events
- Editing recorded events
- Accessing match export

#### `ui/theme/`

Contains the Jetpack Compose theme configuration.

This includes:

- Colours
- Dimensions
- Shapes
- Typography
- Overall application theme configuration

#### `ui/viewmodel/`

Contains UI-facing state and application logic.

`MatchViewModel.kt` currently coordinates most match operations and exposes match state to the Compose UI.
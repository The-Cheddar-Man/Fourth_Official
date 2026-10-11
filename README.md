# Fourth Official

Fourth Official is an Android rugby match-management app designed to help track games from the sidelines, from setup through to post match reporting.

Fourth Official was conceived of by my father, a humble and occasional fourth official for our local team. 
He was growing weary of holding multiple stop watches and a notepad on the side of the pitch and yearned for a more efficient way to fulfil his duties.
In dismay at his lack of free and simple digital solutions, I began putting together this app with the hopes of making his life easier.

Currently the app is fully offline. So no accounts, login or pulling data from the internet. That does mean you have to enter teams manually every time although that is planned for the next alpha release.

## Current release

**Alpha v1 — `v0.1.0-alpha.1`**

Current functionality includes:

- Match setup and squad entry (2 teams of 23)
- Match clock management
- Tracking of scores
- Queueing, editing and mass processing of substitutes
- Tracking of disciplinaries
- Hopefully accurate summaries of the game which can be edited
- PDF export of the match

The current release is intended for development and testing only and is **not intended for production match-day use**. Use at your own peril!

## Download

The latest build can be found in the repository's **Releases** section.

Current release:

`Fourth Official 0.1.0 Alpha 1`

## Development roadmap

### Alpha v2

The next major development phase will focus on the following:

- Home screen
- Saveable teams which can be reused (from your local device only)
- Match day squad selection from said saved teams
- Auto saving games if you close the app and resuming them
- Saving games after the match
- Viewable match history of previous games

### Beta

Beta will focus primarily on reliability and getting us ready for release:

- Persistence and recovery testing
- Remaining V1 functionality
- UX/UI changes to make it work good and look good

### Release Candidate

Final tests before we release to squash those pesky bugs

### v1.0

First proper release onto the google play store to share with friends

## Technology

- Kotlin
- Jetpack Compose
- Android
- Gradle

## Repository structure

The project is broadly organised into:

- `domain/` - match, team, rules and event models
- `export/` - match export functionality
- `ui/` - application screens and reusable UI components
- `dev/` - development test data and tooling

## Contributing

The project is currently primarily developed as a personal project.

Issues may be used to track bugs, features and planned development work.

Get in touch or leave an issue if you have something to add!

## License

Licensed under the Apache License 2.0.

See [LICENSE](LICENSE) for details.

# Fourth Official Roadmap

This roadmap outlines the planned development of Fourth Official.

Plans may change as the app is tested and developed.

## Alpha v1 — Complete

Initial working version of the app.

Includes:

- Match setup and squad entry
- Match clock management
- Score tracking
- Substitution management
- Discipline tracking
- Editable match summaries
- PDF export

Release:
`v0.1.0-alpha.1`

---

## Alpha v2

Focus: persistence, reusable data and navigation.

Planned work:

- Add a home screen
- Save and reuse teams
- Store larger team rosters
- Select a 23-player matchday squad
- Automatically save active matches
- Recover matches after the app is closed
- Save completed matches
- Add match history
- Reopen previous match summaries
- Preserve historical match data independently from later team edits

All data will remain stored locally on the device.

---

## Beta

Focus: reliability, testing and refinement.

Planned work:

- Test persistence and recovery thoroughly
- Test app behaviour during:
    - app closure
    - backgrounding
    - device rotation
    - longer matches
    - interrupted matches
- Complete any remaining V1 functionality
- Review navigation and user experience
- UI cleanup and visual consistency
- Improve accessibility where needed
- Expand automated testing
- Review project dependencies and Android toolchain
- Move development-only tooling into debug-only source sets
- Test release builds without development features

---

## Release Candidate

Focus: final release preparation.

Development should be feature-frozen at this point.

Work will focus on:

- Fixing release-blocking bugs
- Regression testing
- Final release-build testing
- Signing and packaging checks
- Privacy and data-storage review
- Final Google Play preparation

---

## v1.0

First public Android release.

The goal for V1 is a stable, reliable offline match-management app suitable for normal match-day use.

---

# Post-v1

## Non-local team and historical match storage

Improve how saved teams and historical matches are stored and managed.

Possible areas include:

- Better team management
- Import/export of teams
- Easier transfer of data between devices
- Improved historical match browsing

## RFU Integration

Investigate possible integration with RFU systems or data sources where appropriate.

Potential areas could include:

- Team data
- Fixtures
- Player information
- Match reporting

Any integration would depend on available APIs, permissions and data-access requirements.

## Convenience Features

Continue improving the app based on real-world use.

Possible examples:

- Faster match setup
- Additional shortcuts
- Improved exports
- Additional statistics
- Quality-of-life improvements

## iOS / Apple Support

Investigate an iOS version in the future.
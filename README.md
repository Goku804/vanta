# VANTA

A futuristic music and media app for Android, built with Kotlin, Jetpack Compose,
Coroutines/Flow, Room, and Media3.

## Status: Phases 1–6 of 10

- **Phase 1 — Project foundation.** Full Gradle/Kotlin project, GitHub Actions
  build workflow, manifest, permissions, dark futuristic theme.
- **Phase 2 — Navigation shell.** Home, Music, Morning, Playlists, Videos,
  Files, Settings, and the full-screen Player are all real, reachable
  Compose destinations with bottom navigation and a persistent mini player.
- **Phase 3 — Music library & playback.** MediaStore scanning into a Room
  cache, search, playlist create/delete/add/remove, and full transport
  control (play/pause/seek/skip/queue) via a Media3 `MediaSessionService`
  running as a foreground service, plus a dedicated immersive single-song
  player screen with a waveform-style seek bar.
- **Phase 4 — Morning scheduling.** Config UI + `AlarmManager`-backed
  persistence that survives reboot, built directly on the Phase 3 playback
  engine since the spec treats this as core rather than optional.
- **Phase 5 — Video library & playback.** MediaStore video scanning into its
  own Room cache, a thumbnail grid (Coil decodes each video's first frame
  directly — no separate thumbnail files), search, and a dedicated
  full-screen video player using Media3's `PlayerView`. Deliberately built
  as its own layout and its own local `ExoPlayer` instance rather than
  reusing the music screen or the shared background playback service, since
  video isn't meant to keep playing once you leave the screen.
- **Phase 6 — Video-to-audio conversion.** Real extraction via Media3
  `Transformer` (not a fake progress animation) — pick a video, tap
  "Convert", watch real percentage progress, and the resulting audio track
  is written to app storage and registered straight into the music library.
  Failure states are shown honestly rather than being reported as success.

**Not yet built** — and honestly labeled as such in the app itself rather
than faked — is the media file manager and the floating overlay widget
(Phases 7–9). The Files screen and the overlay-permission row in Settings
say plainly that they're coming later.

## A note on Phase 6's build risk

The Media3 `Transformer` API (`app/src/main/kotlin/com/vanta/app/conversion/VideoAudioConverter.kt`)
has shifted method signatures across minor Media3 releases more than the
rest of the library. Everything here matches the documented 1.4.1 API as
best I can verify without a live compiler, but if CI fails specifically in
that file, it's the most likely spot — send the `e: file://` compiler error
and it's a quick, contained fix.

## Getting the APK

You do not need Android Studio, the Android SDK, or a local Gradle install.

1. Push this project to a GitHub repository (see below).
2. Open the repo's **Actions** tab. The "Build VANTA debug APK" workflow runs
   automatically on push, or trigger it manually via "Run workflow".
3. When it finishes, download the `vanta-debug-apk` artifact from the run
   summary — that's a zip containing `app-debug.apk`.
4. Install it on your phone (you'll need to allow installs from your file
   manager / browser the first time).

### Pushing this project

```bash
cd VANTA
git init
git add .
git commit -m "VANTA: phases 1-3"
git branch -M main
git remote add origin <your-repo-url>
git push -u origin main
```

## A note on the Gradle wrapper

`gradlew`, `gradlew.bat`, and `gradle/wrapper/gradle-wrapper.properties` are
included and correct, but the small binary `gradle-wrapper.jar` that normally
sits alongside them is not — it can't be produced as text, and this project
was generated in an environment with no network access to fetch it. The
GitHub Actions workflow installs Gradle 8.7 directly and runs `gradle wrapper`
as its first build step, which generates that jar so `./gradlew` works for
the rest of the CI build. If you ever build locally and hit a missing-jar
error, run `gradle wrapper --gradle-version 8.7` once (with any local Gradle
install) to generate it yourself — after that, `./gradlew` works normally on
its own.

## Architecture

- **UI:** Jetpack Compose, single-Activity, Navigation-Compose for routing.
- **State:** Each screen has a `ViewModel` exposing a `StateFlow` of its UI
  state; `PlaybackManager` is the one shared source of truth for transport
  state across every screen (mini player, song rows, full player all read
  the same `StateFlow`).
- **Playback:** `PlaybackService` (`MediaSessionService`) hosts a single
  `ExoPlayer`; `PlaybackManager` wraps a `MediaController` connected to it.
  This is what the floating widget (Phase 8) will also connect to.
- **Data:** Room caches MediaStore metadata (`MusicRepository`) and persists
  the morning configuration (`MorningRepository`). `MorningScheduler` keeps a
  single `AlarmManager` alarm in sync with that persisted config.
- **DI:** A small hand-rolled `AppContainer` + `ViewModelProvider.Factory`
  rather than Hilt/Koin, to keep the build simple while the project is this
  size. Swappable later without touching call sites much.

## Requesting the next phases

Phases 7 (file manager) and 8–9 (floating widget + music-reactive animation)
build directly on this foundation. Ask for the next phase and it'll arrive
as an update to this same project rather than a restart.

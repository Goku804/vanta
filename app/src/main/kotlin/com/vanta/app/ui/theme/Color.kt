package com.vanta.app.ui.theme

import androidx.compose.ui.graphics.Color

// Deep, near-black surfaces rather than pure black, so layered panels can
// read as distinct depth rather than flat cutouts.
val VoidBase = Color(0xFF0A0B10)
val VoidSurface = Color(0xFF12141F)
val VoidSurfaceRaised = Color(0xFF191C2B)
val VoidSurfaceOverlay = Color(0xFF20243A)

// Two accent hues used deliberately for different meanings: Ion for active/
// live states (currently playing, live progress), Signal for
// selection/emphasis (selected items, primary actions).
val Ion = Color(0xFF5EE7FF)
val IonDim = Color(0xFF2C7C8C)
val Signal = Color(0xFFB98CFF)
val SignalDim = Color(0xFF5B4A85)
val Ember = Color(0xFFFF7A59)

val TextPrimary = Color(0xFFE8EAF6)
val TextSecondary = Color(0xFFA9AEC7)
val TextTertiary = Color(0xFF6E7390)

val DividerLine = Color(0xFF262A3E)
val ErrorRed = Color(0xFFFF5C7A)

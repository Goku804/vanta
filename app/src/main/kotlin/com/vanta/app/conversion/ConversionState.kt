package com.vanta.app.conversion

import com.vanta.app.data.model.Song

/**
 * State of a single video-to-audio conversion. Spec section 8 requires that
 * a failed conversion never be presented as if it succeeded, so [Error] and
 * [Success] are strictly separate terminal states — nothing here defaults
 * to "probably fine".
 */
sealed interface ConversionState {
    data object Idle : ConversionState
    data class Converting(val progressPercent: Int) : ConversionState
    data class Success(val song: Song) : ConversionState
    data class Error(val message: String) : ConversionState
}

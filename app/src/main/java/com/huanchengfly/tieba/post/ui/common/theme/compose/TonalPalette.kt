package com.huanchengfly.tieba.post.ui.common.theme.compose

import androidx.compose.ui.graphics.Color

/**
 * Tonal Palette structure in Material.
 *
 * A tonal palette is comprised of 5 tonal ranges. Each tonal range includes the 13 stops, or
 * tonal swatches.
 *
 * Tonal range names are:
 * - Neutral (N)
 * - Neutral variant (NV)
 * - Primary (P)
 * - Secondary (S)
 * - Tertiary (T)
 */
internal class TonalPalette(
    // The neutral tonal range from the generated dynamic color palette.
    // Ordered from the lightest shade [neutral100] to the darkest shade [neutral0].
    val neutral100: Color,
    val neutral99: Color,
    val neutral95: Color,
    val neutral90: Color,
    val neutral80: Color,
    val neutral70: Color,
    val neutral60: Color,
    val neutral50: Color,
    val neutral40: Color,
    val neutral30: Color,
    val neutral20: Color,
    val neutral10: Color,
    val neutral0: Color,

    // The neutral variant tonal range, sometimes called "neutral 2",  from the
    // generated dynamic color palette.
    // Ordered from the lightest shade [neutralVariant100] to the darkest shade [neutralVariant0].
    val neutralVariant100: Color,
    val neutralVariant99: Color,
    val neutralVariant95: Color,
    val neutralVariant90: Color,
    val neutralVariant80: Color,
    val neutralVariant70: Color,
    val neutralVariant60: Color,
    val neutralVariant50: Color,
    val neutralVariant40: Color,
    val neutralVariant30: Color,
    val neutralVariant20: Color,
    val neutralVariant10: Color,
    val neutralVariant0: Color,

    // The primary tonal range from the generated dynamic color palette.
    // Ordered from the lightest shade [primary100] to the darkest shade [primary0].
    val primary100: Color,
    val primary99: Color,
    val primary95: Color,
    val primary90: Color,
    val primary80: Color,
    val primary70: Color,
    val primary60: Color,
    val primary50: Color,
    val primary40: Color,
    val primary30: Color,
    val primary20: Color,
    val primary10: Color,
    val primary0: Color,

    // The secondary tonal range from the generated dynamic color palette.
    // Ordered from the lightest shade [secondary100] to the darkest shade [secondary0].
    val secondary100: Color,
    val secondary99: Color,
    val secondary95: Color,
    val secondary90: Color,
    val secondary80: Color,
    val secondary70: Color,
    val secondary60: Color,
    val secondary50: Color,
    val secondary40: Color,
    val secondary30: Color,
    val secondary20: Color,
    val secondary10: Color,
    val secondary0: Color,

    // The tertiary tonal range from the generated dynamic color palette.
    // Ordered from the lightest shade [tertiary100] to the darkest shade [tertiary0].
    val tertiary100: Color,
    val tertiary99: Color,
    val tertiary95: Color,
    val tertiary90: Color,
    val tertiary80: Color,
    val tertiary70: Color,
    val tertiary60: Color,
    val tertiary50: Color,
    val tertiary40: Color,
    val tertiary30: Color,
    val tertiary20: Color,
    val tertiary10: Color,
    val tertiary0: Color,
)
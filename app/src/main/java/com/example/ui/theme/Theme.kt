package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val DarkColorScheme =
  darkColorScheme(
    primary = OrangePrimary,
    onPrimary = PureWhite,
    primaryContainer = OrangePrimaryContainer,
    onPrimaryContainer = OrangeOnPrimaryContainer,
    secondary = OrangeSecondary,
    onSecondary = PureWhite,
    secondaryContainer = WarmPeachSecondaryContainer,
    onSecondaryContainer = WarmPeachOnSecondaryContainer,
    tertiary = WarmAmberTertiary,
    onTertiary = PureWhite,
    tertiaryContainer = WarmAmberTertiaryContainer,
    onTertiaryContainer = WarmAmberOnTertiaryContainer,
    background = WarmOffWhite,
    onBackground = InputTextBlack,
    surface = PureWhite,
    onSurface = InputTextBlack,
    surfaceVariant = WarmSurfaceVariant,
    onSurfaceVariant = InputLabelDark,
    outline = WarmBorder
  )

private val LightColorScheme =
  lightColorScheme(
    primary = OrangePrimary,
    onPrimary = PureWhite,
    primaryContainer = OrangePrimaryContainer,
    onPrimaryContainer = OrangeOnPrimaryContainer,
    secondary = OrangeSecondary,
    onSecondary = PureWhite,
    secondaryContainer = WarmPeachSecondaryContainer,
    onSecondaryContainer = WarmPeachOnSecondaryContainer,
    tertiary = WarmAmberTertiary,
    onTertiary = PureWhite,
    tertiaryContainer = WarmAmberTertiaryContainer,
    onTertiaryContainer = WarmAmberOnTertiaryContainer,
    background = WarmOffWhite,
    onBackground = InputTextBlack,
    surface = PureWhite,
    onSurface = InputTextBlack,
    surfaceVariant = WarmSurfaceVariant,
    onSurfaceVariant = InputLabelDark,
    outline = WarmBorder
  )

val AppInputTextStyle = TextStyle(
    color = InputTextBlack,
    fontSize = 15.sp,
    fontWeight = FontWeight.Medium
)

@Composable
fun appOutlinedTextFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedTextColor = InputTextBlack,
    unfocusedTextColor = InputTextBlack,
    disabledTextColor = Color(0xFF6B7280),
    errorTextColor = DangerRed,
    focusedContainerColor = PureWhite,
    unfocusedContainerColor = PureWhite,
    disabledContainerColor = PureWhite,
    focusedBorderColor = OrangePrimary,
    unfocusedBorderColor = InputBorderDefault,
    focusedLabelColor = OrangePrimaryDark,
    unfocusedLabelColor = InputLabelDark,
    focusedPlaceholderColor = InputPlaceholderMuted,
    unfocusedPlaceholderColor = InputPlaceholderMuted,
    focusedLeadingIconColor = OrangePrimary,
    unfocusedLeadingIconColor = Color(0xFF6B7280),
    focusedTrailingIconColor = OrangePrimary,
    unfocusedTrailingIconColor = Color(0xFF6B7280),
    cursorColor = OrangePrimary
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = false,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}


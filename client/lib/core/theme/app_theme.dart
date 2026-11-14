import 'package:flutter/material.dart';

import '../../core/theme/tokens.dart';
import '../../core/theme/typography.dart';
import 'motion.dart';

abstract final class AppTheme {
  static ThemeData light() {
    const scheme = ColorScheme.light(
      primary: AppColors.ink,
      onPrimary: Colors.white,
      secondary: AppColors.mintDeep,
      onSecondary: Colors.white,
      surface: AppColors.card,
      onSurface: AppColors.ink,
      error: AppColors.red,
      onError: Colors.white,
      outline: AppColors.line,
      outlineVariant: AppColors.line2,
      surfaceContainerHighest: AppColors.wash,
    );

    final base = ThemeData(
      useMaterial3: true,
      colorScheme: scheme,
      scaffoldBackgroundColor: AppColors.bg,
      splashFactory: InkRipple.splashFactory,
      visualDensity: VisualDensity.standard,
    );

    return base.copyWith(
      textTheme: AppText.theme(base.textTheme),
      appBarTheme: const AppBarTheme(
        backgroundColor: AppColors.bg,
        surfaceTintColor: Colors.transparent,
        elevation: 0,
        scrolledUnderElevation: 0,
        foregroundColor: AppColors.ink,
        centerTitle: false,
        toolbarHeight: 56,
        titleTextStyle: AppText.h2,
      ),
      dividerTheme: const DividerThemeData(
        color: AppColors.line2,
        thickness: 1,
        space: 1,
      ),
      inputDecorationTheme: InputDecorationTheme(
        filled: true,
        fillColor: AppColors.card,
        isDense: true,
        contentPadding:
            const EdgeInsets.symmetric(horizontal: 14, vertical: 14),
        hintStyle: AppText.bodySm.copyWith(color: AppColors.ink3),
        helperStyle: AppText.caption,
        errorStyle: AppText.caption.copyWith(color: AppColors.red),
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(AppRadius.field),
          borderSide: const BorderSide(color: AppColors.line),
        ),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(AppRadius.field),
          borderSide: const BorderSide(color: AppColors.line),
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(AppRadius.field),
          borderSide: const BorderSide(color: AppColors.mintDeep, width: 1.5),
        ),
        errorBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(AppRadius.field),
          borderSide: const BorderSide(color: AppColors.red),
        ),
        focusedErrorBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(AppRadius.field),
          borderSide: const BorderSide(color: AppColors.red, width: 1.5),
        ),
      ),
      snackBarTheme: SnackBarThemeData(
        backgroundColor: AppColors.ink,
        contentTextStyle: AppText.bodySm.copyWith(color: Colors.white),
        behavior: SnackBarBehavior.floating,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(AppRadius.button),
        ),
        insetPadding: const EdgeInsets.all(AppSpacing.page),
      ),
      textSelectionTheme: const TextSelectionThemeData(
        cursorColor: AppColors.mintDeep,
        selectionColor: AppColors.selection,
        selectionHandleColor: AppColors.mintDeep,
      ),
      progressIndicatorTheme: const ProgressIndicatorThemeData(
        color: AppColors.mintDeep,
        linearTrackColor: AppColors.sunken,
      ),
      switchTheme: SwitchThemeData(
        thumbColor: WidgetStateProperty.resolveWith(
          (states) => states.contains(WidgetState.selected)
              ? Colors.white
              : AppColors.card,
        ),
        trackColor: WidgetStateProperty.resolveWith(
          (states) => states.contains(WidgetState.selected)
              ? AppColors.mintDeep
              : AppColors.sunken,
        ),
        trackOutlineColor: WidgetStateProperty.resolveWith(
          (states) => states.contains(WidgetState.selected)
              ? AppColors.mintDeep
              : AppColors.line,
        ),
      ),
      textButtonTheme: TextButtonThemeData(
        style: TextButton.styleFrom(
          foregroundColor: AppColors.mintDeep,
          textStyle: AppText.label.copyWith(fontWeight: FontWeight.w600),
          minimumSize: const Size(AppSizes.touch, AppSizes.touch),
        ),
      ),
      pageTransitionsTheme: const PageTransitionsTheme(
        builders: {
          TargetPlatform.android: SharedAxisPageTransitionsBuilder(),
          TargetPlatform.iOS: SharedAxisPageTransitionsBuilder(),
          TargetPlatform.windows: SharedAxisPageTransitionsBuilder(),
          TargetPlatform.macOS: SharedAxisPageTransitionsBuilder(),
          TargetPlatform.linux: SharedAxisPageTransitionsBuilder(),
          TargetPlatform.fuchsia: SharedAxisPageTransitionsBuilder(),
        },
      ),
    );
  }
}

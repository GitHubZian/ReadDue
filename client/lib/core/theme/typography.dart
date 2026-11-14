import 'package:flutter/material.dart';

import 'tokens.dart';

/// 字号阶梯来自 AGENT §4.2，只在这里定义一次。
abstract final class AppText {
  static const _tabular =
      TextStyle(fontFeatures: [FontFeature.tabularFigures()]);

  static const display = TextStyle(
    fontSize: 34,
    fontWeight: FontWeight.w700,
    height: 1.2,
    letterSpacing: -0.6,
    color: AppColors.ink,
  );

  static const h1 = TextStyle(
    fontSize: 26,
    fontWeight: FontWeight.w700,
    height: 1.25,
    letterSpacing: -0.4,
    color: AppColors.ink,
  );

  static const h2 = TextStyle(
    fontSize: 22,
    fontWeight: FontWeight.w600,
    height: 1.3,
    letterSpacing: -0.2,
    color: AppColors.ink,
  );

  static const h3 = TextStyle(
    fontSize: 17,
    fontWeight: FontWeight.w600,
    height: 1.35,
    color: AppColors.ink,
  );

  static const body = TextStyle(
    fontSize: 15,
    fontWeight: FontWeight.w400,
    height: 1.65,
    color: AppColors.ink,
  );

  static const bodySm = TextStyle(
    fontSize: 13.5,
    fontWeight: FontWeight.w400,
    height: 1.65,
    color: AppColors.ink,
  );

  static const label = TextStyle(
    fontSize: 12.5,
    fontWeight: FontWeight.w500,
    height: 1.5,
    color: AppColors.ink2,
  );

  static const caption = TextStyle(
    fontSize: 11.5,
    fontWeight: FontWeight.w400,
    height: 1.5,
    color: AppColors.ink3,
  );

  static const micro = TextStyle(
    fontSize: 10.5,
    fontWeight: FontWeight.w400,
    height: 1.4,
    color: AppColors.ink3,
  );

  static const number = TextStyle(
    fontSize: 30,
    fontWeight: FontWeight.w700,
    height: 1.1,
    color: AppColors.ink,
    fontFeatures: [FontFeature.tabularFigures()],
  );

  static const numberSmall = TextStyle(
    fontSize: 20,
    fontWeight: FontWeight.w700,
    height: 1.2,
    color: AppColors.ink,
    fontFeatures: [FontFeature.tabularFigures()],
  );

  /// 取书码、单号等需要等宽字符的场景。
  static const mono = TextStyle(
    fontSize: 15,
    fontWeight: FontWeight.w600,
    height: 1.3,
    letterSpacing: 0.4,
    color: AppColors.ink,
    fontFeatures: [FontFeature.tabularFigures()],
  );

  static const tabular = _tabular;

  static TextTheme theme(TextTheme base) {
    return base.copyWith(
      displayLarge: display,
      displayMedium: display,
      headlineLarge: h1,
      headlineMedium: h2,
      headlineSmall: h2,
      titleLarge: h3,
      titleMedium: AppText.h3,
      bodyLarge: body,
      bodyMedium: bodySm,
      bodySmall: caption,
      labelLarge: const TextStyle(
        fontSize: 13.5,
        fontWeight: FontWeight.w600,
        height: 1.4,
        color: AppColors.ink,
      ),
      labelMedium: label,
      labelSmall: micro,
    );
  }
}

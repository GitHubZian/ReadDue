import 'package:flutter/widgets.dart';

/// 唯一视觉来源：颜色、圆角、间距、尺寸。禁止在页面里自造颜色。
abstract final class AppColors {
  static const bg = Color(0xFFFBFCF8);
  static const card = Color(0xFFFFFFFF);
  static const ink = Color(0xFF334155);
  static const ink2 = Color(0xFF515D6F);
  static const ink3 = Color(0xFF6A7486);
  static const mint = Color(0xFF64C2A6);
  static const mintDeep = Color(0xFF2A7159);
  static const mintSoft = Color(0xFFE7F5EF);
  static const mintWash = Color(0xFFF2FAF6);
  static const wash = Color(0xFFF6F8F3);
  static const sunken = Color(0xFFF0F2EC);
  static const line = Color(0xFFE6E9E3);
  static const line2 = Color(0xFFEFF1EC);
  static const deep = Color(0xFF26303F);
  static const deepDivider = Color(0xFF45536B);
  static const amber = Color(0xFF8C5C16);
  static const amberSoft = Color(0xFFFBF2E1);
  static const red = Color(0xFFA3492F);
  static const redSoft = Color(0xFFFBEDE8);
  static const slateSoft = Color(0xFFEDF1F4);
  static const selection = Color(0xFFCFEDE2);
}

abstract final class AppRadius {
  static const card = 14.0;
  static const panel = 14.0;
  static const button = 12.0;
  static const field = 12.0;
  static const pill = 99.0;
  static const cover = 8.0;
}

abstract final class AppSpacing {
  static const xs = 4.0;
  static const sm = 8.0;
  static const md = 12.0;
  static const lg = 16.0;
  static const page = 20.0;
  static const xl = 26.0;
}

abstract final class AppSizes {
  static const touch = 44.0;
  static const button = 48.0;
  static const tabBar = 56.0;
  static const pill = 24.0;
  static const coverWidth = 56.0;
  static const coverHeight = 76.0;
}

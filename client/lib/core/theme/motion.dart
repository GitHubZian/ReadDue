import 'package:flutter/material.dart';

/// 动效时长与曲线：只用于 transform / opacity，尊重系统减少动态效果。
abstract final class Motion {
  static const Duration press = Duration(milliseconds: 90);
  static const Duration release = Duration(milliseconds: 140);
  static const Duration fast = Duration(milliseconds: 120);
  static const Duration base = Duration(milliseconds: 180);
  static const Duration page = Duration(milliseconds: 260);

  static const Cubic emphasized = Cubic(0.2, 0, 0, 1);
  static const Curve standard = Curves.easeOutCubic;

  static bool reduced(BuildContext context) =>
      MediaQuery.maybeOf(context)?.disableAnimations ?? false;
}

/// 二级页横向 shared-axis：新页 6% 位移 + 淡入，返回反向收回。
class SharedAxisPageTransitionsBuilder extends PageTransitionsBuilder {
  const SharedAxisPageTransitionsBuilder();

  @override
  Widget buildTransitions<T>(
    PageRoute<T> route,
    BuildContext context,
    Animation<double> animation,
    Animation<double> secondaryAnimation,
    Widget child,
  ) {
    if (Motion.reduced(context)) return child;
    final curved = CurvedAnimation(
      parent: animation,
      curve: Motion.emphasized,
      reverseCurve: Curves.easeInCubic,
    );
    return FadeTransition(
      opacity: curved,
      child: SlideTransition(
        position: Tween<Offset>(
          begin: const Offset(0.06, 0),
          end: Offset.zero,
        ).animate(curved),
        child: child,
      ),
    );
  }
}

import 'dart:async';

import 'package:flutter/material.dart';

import '../theme/motion.dart';

/// 列表首屏入场：淡入 + 上移 8dp，按 index 错峰，最多 8 项，只播一次。
class EnterFade extends StatefulWidget {
  const EnterFade({
    super.key,
    required this.child,
    this.index = 0,
  });

  final Widget child;
  final int index;

  @override
  State<EnterFade> createState() => _EnterFadeState();
}

class _EnterFadeState extends State<EnterFade>
    with SingleTickerProviderStateMixin {
  late final AnimationController _controller = AnimationController(
    vsync: this,
    duration: const Duration(milliseconds: 180),
  );
  Timer? _timer;

  @override
  void initState() {
    super.initState();
    final reduced = WidgetsBinding
        .instance.platformDispatcher.accessibilityFeatures.disableAnimations;
    if (reduced) {
      _controller.value = 1;
      return;
    }
    final delay = Duration(milliseconds: 35 * widget.index.clamp(0, 8));
    _timer = Timer(delay, () {
      if (mounted) _controller.forward();
    });
  }

  @override
  void dispose() {
    _timer?.cancel();
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final curved = CurvedAnimation(
      parent: _controller,
      curve: Motion.standard,
    );
    return AnimatedBuilder(
      animation: curved,
      child: widget.child,
      builder: (context, child) {
        final t = curved.value;
        return Opacity(
          opacity: t.clamp(0, 1),
          child: Transform.translate(
            offset: Offset(0, (1 - t) * 8),
            child: child,
          ),
        );
      },
    );
  }
}

import 'package:flutter/material.dart';

import '../theme/motion.dart';

/// 按压反馈：按下 0.97，松开回弹。用 Listener 不拦截子组件点击。
class PressScale extends StatefulWidget {
  const PressScale({
    super.key,
    required this.child,
    this.enabled = true,
    this.scale = 0.97,
  });

  final Widget child;
  final bool enabled;
  final double scale;

  @override
  State<PressScale> createState() => _PressScaleState();
}

class _PressScaleState extends State<PressScale> {
  bool _pressed = false;

  void _set(bool value) {
    if (widget.enabled && _pressed != value) {
      setState(() => _pressed = value);
    }
  }

  @override
  Widget build(BuildContext context) {
    final active = _pressed && widget.enabled && !Motion.reduced(context);
    return Listener(
      onPointerDown: (_) => _set(true),
      onPointerUp: (_) => _set(false),
      onPointerCancel: (_) => _set(false),
      child: AnimatedScale(
        scale: active ? widget.scale : 1,
        duration: active ? Motion.press : Motion.release,
        curve: Curves.easeOutCubic,
        child: widget.child,
      ),
    );
  }
}

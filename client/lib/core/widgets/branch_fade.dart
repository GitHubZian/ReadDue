import 'package:flutter/material.dart';

import '../theme/motion.dart';

/// 底栏切换：内容 0.94→1 淡入，保留各分支状态。
class BranchFade extends StatefulWidget {
  const BranchFade({super.key, required this.index, required this.child});

  final int index;
  final Widget child;

  @override
  State<BranchFade> createState() => _BranchFadeState();
}

class _BranchFadeState extends State<BranchFade>
    with SingleTickerProviderStateMixin {
  late final AnimationController _controller = AnimationController(
    vsync: this,
    duration: Motion.base,
    value: 1,
  );

  @override
  void didUpdateWidget(covariant BranchFade oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (oldWidget.index != widget.index && !Motion.reduced(context)) {
      _controller.forward(from: 0);
    }
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    if (Motion.reduced(context)) return widget.child;
    return FadeTransition(
      opacity: Tween<double>(begin: 0.94, end: 1).animate(
        CurvedAnimation(parent: _controller, curve: Motion.standard),
      ),
      child: widget.child,
    );
  }
}

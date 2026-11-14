import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../network/api_exception.dart';
import '../theme/motion.dart';
import 'states.dart';

/// 统一四态：加载（800ms 后骨架）/ 空 / 错误（可重试）/ 成功。
class AsyncBody<T> extends StatelessWidget {
  const AsyncBody({
    super.key,
    required this.value,
    required this.builder,
    required this.onRetry,
    this.skeleton,
    this.isEmpty,
    this.empty,
  });

  final AsyncValue<T> value;
  final Widget Function(T data) builder;
  final VoidCallback onRetry;
  final Widget? skeleton;
  final bool Function(T data)? isEmpty;
  final Widget? empty;

  @override
  Widget build(BuildContext context) {
    final content = value.when(
      data: (data) {
        if (isEmpty?.call(data) ?? false) {
          return empty ?? const SizedBox.shrink();
        }
        return builder(data);
      },
      error: (error, _) => ErrorState(
        message: messageForError(error),
        onRetry: onRetry,
      ),
      loading: () => _DelayedSkeleton(
        child: skeleton ?? const SkeletonList(),
      ),
    );
    final key = value.isLoading
        ? 'loading'
        : value.hasError
            ? 'error'
            : 'data';
    return AnimatedSwitcher(
      duration: Motion.reduced(context) ? Duration.zero : Motion.fast,
      switchInCurve: Motion.standard,
      switchOutCurve: Curves.easeOut,
      child: KeyedSubtree(key: ValueKey(key), child: content),
    );
  }
}

class _DelayedSkeleton extends StatefulWidget {
  const _DelayedSkeleton({required this.child});

  final Widget child;

  @override
  State<_DelayedSkeleton> createState() => _DelayedSkeletonState();
}

class _DelayedSkeletonState extends State<_DelayedSkeleton> {
  bool _visible = false;

  @override
  void initState() {
    super.initState();
    final reduceMotion = WidgetsBinding
        .instance.platformDispatcher.accessibilityFeatures.disableAnimations;
    if (reduceMotion) {
      _visible = true;
      return;
    }
    Future<void>.delayed(const Duration(milliseconds: 800), () {
      if (mounted) setState(() => _visible = true);
    });
  }

  @override
  Widget build(BuildContext context) {
    return _visible ? widget.child : const SizedBox.shrink();
  }
}

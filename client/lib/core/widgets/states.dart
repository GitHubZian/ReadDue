import 'package:flutter/material.dart';

import '../theme/tokens.dart';
import '../theme/typography.dart';
import 'buttons.dart';

class EmptyState extends StatelessWidget {
  const EmptyState({
    super.key,
    required this.title,
    this.message,
    this.icon = Icons.inbox_outlined,
    this.actionLabel,
    this.onAction,
  });

  final String title;
  final String? message;
  final IconData icon;
  final String? actionLabel;
  final VoidCallback? onAction;

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 32, vertical: 40),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Container(
              width: 64,
              height: 64,
              decoration: const BoxDecoration(
                color: AppColors.sunken,
                shape: BoxShape.circle,
              ),
              child: Icon(icon, size: 28, color: AppColors.ink3),
            ),
            const SizedBox(height: 14),
            Text(title, style: AppText.h3, textAlign: TextAlign.center),
            if (message != null) ...[
              const SizedBox(height: 6),
              Text(
                message!,
                textAlign: TextAlign.center,
                style: AppText.bodySm.copyWith(color: AppColors.ink3),
              ),
            ],
            if (actionLabel != null) ...[
              const SizedBox(height: 14),
              TextButton(
                onPressed: onAction,
                child: Text(
                  actionLabel!,
                  style: AppText.bodySm.copyWith(
                    fontWeight: FontWeight.w600,
                    color: AppColors.mintDeep,
                  ),
                ),
              ),
            ],
          ],
        ),
      ),
    );
  }
}

class ErrorState extends StatelessWidget {
  const ErrorState({
    super.key,
    required this.message,
    required this.onRetry,
    this.title = '暂时无法加载',
    this.compact = false,
  });

  final String message;
  final VoidCallback onRetry;
  final String title;
  final bool compact;

  @override
  Widget build(BuildContext context) {
    final content = Column(
      mainAxisSize: MainAxisSize.min,
      children: [
        const Icon(Icons.error_outline, size: 30, color: AppColors.ink3),
        const SizedBox(height: 12),
        Text(title, style: AppText.h3, textAlign: TextAlign.center),
        const SizedBox(height: 6),
        Text(
          message,
          textAlign: TextAlign.center,
          style: AppText.bodySm.copyWith(color: AppColors.ink3),
        ),
        const SizedBox(height: 16),
        SizedBox(
          width: 132,
          child: PrimaryButton(
            label: '重试',
            ghost: true,
            height: 44,
            onPressed: onRetry,
          ),
        ),
      ],
    );
    if (compact) {
      return Padding(
        padding: const EdgeInsets.all(AppSpacing.page),
        child: content,
      );
    }
    return Center(
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 32, vertical: 40),
        child: content,
      ),
    );
  }
}

/// 骨架行高与真实卡片一致，≥800ms 才显示，避免闪烁。
class SkeletonList extends StatefulWidget {
  const SkeletonList({
    super.key,
    this.count = 3,
    this.itemHeight = 96,
    this.padding = const EdgeInsets.fromLTRB(20, 8, 20, 20),
  });

  final int count;
  final double itemHeight;
  final EdgeInsetsGeometry padding;

  @override
  State<SkeletonList> createState() => _SkeletonListState();
}

class _SkeletonListState extends State<SkeletonList>
    with SingleTickerProviderStateMixin {
  late final AnimationController _controller = AnimationController(
    vsync: this,
    duration: const Duration(milliseconds: 1100),
  )..repeat(reverse: true);

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final reduceMotion = MediaQuery.of(context).disableAnimations;
    final list = ListView.separated(
      physics: const NeverScrollableScrollPhysics(),
      padding: widget.padding,
      itemCount: widget.count,
      separatorBuilder: (_, __) => const SizedBox(height: 12),
      itemBuilder: (_, __) => Container(
        height: widget.itemHeight,
        padding: const EdgeInsets.all(AppSpacing.lg),
        decoration: BoxDecoration(
          color: AppColors.card,
          borderRadius: BorderRadius.circular(AppRadius.card),
          border: Border.all(color: AppColors.line),
        ),
        child: const Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _Bar(width: 120),
            SizedBox(height: 10),
            _Bar(width: double.infinity, height: 12),
            SizedBox(height: 8),
            _Bar(width: 180, height: 12),
          ],
        ),
      ),
    );
    if (reduceMotion) return list;
    return FadeTransition(
      opacity: Tween<double>(begin: .55, end: 1).animate(
        CurvedAnimation(parent: _controller, curve: Curves.easeInOut),
      ),
      child: list,
    );
  }
}

class _Bar extends StatelessWidget {
  const _Bar({required this.width, this.height = 14});

  final double width;
  final double height;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: width,
      height: height,
      decoration: BoxDecoration(
        color: AppColors.sunken,
        borderRadius: BorderRadius.circular(6),
      ),
    );
  }
}

class OfflineBanner extends StatelessWidget {
  const OfflineBanner({super.key, this.text = '离线 · 显示上次同步的数据'});

  final String text;

  @override
  Widget build(BuildContext context) {
    return Container(
      margin: const EdgeInsets.fromLTRB(20, 0, 20, 12),
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
      decoration: BoxDecoration(
        color: AppColors.wash,
        borderRadius: BorderRadius.circular(10),
        border: Border.all(color: AppColors.line),
      ),
      child: Row(
        children: [
          const Icon(Icons.wifi_off, size: 14, color: AppColors.amber),
          const SizedBox(width: 8),
          Expanded(
            child: Text(
              text,
              style: AppText.caption.copyWith(color: AppColors.amber),
            ),
          ),
        ],
      ),
    );
  }
}

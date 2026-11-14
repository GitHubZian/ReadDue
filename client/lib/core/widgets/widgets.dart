import 'package:flutter/material.dart';

import '../../core/theme/tokens.dart';
import '../../core/theme/typography.dart';

/// 白色卡片：1px line 描边、radius 14、不使用投影。
class AppCard extends StatelessWidget {
  const AppCard({
    super.key,
    required this.child,
    this.padding = const EdgeInsets.all(AppSpacing.lg),
    this.onTap,
    this.color = AppColors.card,
    this.borderColor = AppColors.line,
    this.radius = AppRadius.card,
  });

  final Widget child;
  final EdgeInsetsGeometry padding;
  final VoidCallback? onTap;
  final Color color;
  final Color borderColor;
  final double radius;

  @override
  Widget build(BuildContext context) {
    final shape = RoundedRectangleBorder(
      borderRadius: BorderRadius.circular(radius),
      side: BorderSide(color: borderColor),
    );
    final content = Padding(padding: padding, child: child);
    return Material(
      color: color,
      shape: shape,
      clipBehavior: Clip.antiAlias,
      child: onTap == null ? content : InkWell(onTap: onTap, child: content),
    );
  }
}

enum PillTone { mint, amber, red, slate }

/// 高 24、radius 99 的语义胶囊，颜色只取 token。
class StatusPill extends StatelessWidget {
  const StatusPill({
    super.key,
    required this.label,
    this.tone = PillTone.mint,
    this.icon,
  });

  final String label;
  final PillTone tone;
  final IconData? icon;

  @override
  Widget build(BuildContext context) {
    final (bg, fg) = switch (tone) {
      PillTone.mint => (AppColors.mintSoft, AppColors.mintDeep),
      PillTone.amber => (AppColors.amberSoft, AppColors.amber),
      PillTone.red => (AppColors.redSoft, AppColors.red),
      PillTone.slate => (AppColors.slateSoft, AppColors.ink2),
    };
    return Container(
      height: AppSizes.pill,
      padding: const EdgeInsets.symmetric(horizontal: 10),
      decoration: BoxDecoration(
        color: bg,
        borderRadius: BorderRadius.circular(AppRadius.pill),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          if (icon != null) ...[
            Icon(icon, size: 12, color: fg),
            const SizedBox(width: 4),
          ],
          Text(
            label,
            style: TextStyle(
              fontSize: 11.5,
              fontWeight: FontWeight.w600,
              height: 1.2,
              color: fg,
            ),
          ),
        ],
      ),
    );
  }
}

enum CoverTone { mint, amber, slate }

/// 72×96 的封面占位，左侧 3px 书脊，书名落在左下角。
class BookCover extends StatelessWidget {
  const BookCover({
    super.key,
    required this.label,
    this.tone = CoverTone.mint,
    this.width = 72,
    this.height = 96,
  });

  final String label;
  final CoverTone tone;
  final double width;
  final double height;

  @override
  Widget build(BuildContext context) {
    final (bg, spine, fg) = switch (tone) {
      CoverTone.mint => (
          AppColors.mintSoft,
          AppColors.mint,
          AppColors.mintDeep,
        ),
      CoverTone.amber => (
          AppColors.amberSoft,
          AppColors.amber,
          AppColors.amber
        ),
      CoverTone.slate => (AppColors.slateSoft, AppColors.ink3, AppColors.ink2),
    };
    return ClipRRect(
      borderRadius: BorderRadius.circular(AppRadius.cover),
      child: Container(
        width: width,
        height: height,
        color: bg,
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Container(width: 3, color: spine),
            Expanded(
              child: Padding(
                padding: const EdgeInsets.fromLTRB(6, 6, 6, 8),
                child: Align(
                  alignment: Alignment.bottomLeft,
                  child: Text(
                    label,
                    maxLines: 2,
                    style: TextStyle(
                      fontSize: 12,
                      fontWeight: FontWeight.w600,
                      height: 1.25,
                      color: fg,
                    ),
                  ),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

/// 17/600 标题（可带说明）+ 右侧 mintDeep 动作文字。
class SectionHeader extends StatelessWidget {
  const SectionHeader({
    super.key,
    required this.title,
    this.hint,
    this.actionLabel,
    this.onAction,
    this.padding = EdgeInsets.zero,
  });

  final String title;
  final String? hint;
  final String? actionLabel;
  final VoidCallback? onAction;
  final EdgeInsetsGeometry padding;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: padding,
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.baseline,
        textBaseline: TextBaseline.alphabetic,
        children: [
          Text(title, style: AppText.h3),
          if (hint != null) ...[
            const SizedBox(width: 8),
            Text(
              hint!,
              style: AppText.caption.copyWith(
                fontSize: 12,
                color: AppColors.ink3,
              ),
            ),
          ],
          const Spacer(),
          if (actionLabel != null)
            InkWell(
              onTap: onAction,
              borderRadius: BorderRadius.circular(6),
              child: Padding(
                padding: const EdgeInsets.symmetric(horizontal: 2, vertical: 4),
                child: Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Text(
                      actionLabel!,
                      style: AppText.label.copyWith(
                        color: AppColors.mintDeep,
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                    const Icon(
                      Icons.chevron_right,
                      size: 16,
                      color: AppColors.mintDeep,
                    ),
                  ],
                ),
              ),
            ),
        ],
      ),
    );
  }
}

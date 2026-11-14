import 'package:flutter/material.dart';

import '../theme/tokens.dart';
import '../theme/typography.dart';

enum TimelineState { done, current, future }

/// 圆点 10 + 竖线 1px + 时间/标题/说明。
class TimelineItem extends StatelessWidget {
  const TimelineItem({
    super.key,
    required this.state,
    required this.time,
    required this.title,
    this.subtitle,
    this.isLast = false,
  });

  final TimelineState state;
  final String time;
  final String title;
  final String? subtitle;
  final bool isLast;

  @override
  Widget build(BuildContext context) {
    return IntrinsicHeight(
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          SizedBox(
            width: 16,
            child: Column(
              children: [
                const SizedBox(height: 4),
                _Dot(state: state),
                if (!isLast)
                  Expanded(
                    child: Container(
                      width: 1,
                      margin: const EdgeInsets.symmetric(vertical: 2),
                      color: AppColors.line2,
                    ),
                  ),
              ],
            ),
          ),
          const SizedBox(width: 10),
          Expanded(
            child: Padding(
              padding: EdgeInsets.only(bottom: isLast ? 0 : 18),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    time,
                    style: AppText.caption.copyWith(
                      fontFeatures: AppText.tabular.fontFeatures,
                    ),
                  ),
                  const SizedBox(height: 2),
                  Text(
                    title,
                    style: AppText.bodySm.copyWith(
                      fontWeight: FontWeight.w600,
                      color: switch (state) {
                        TimelineState.future => AppColors.ink3,
                        TimelineState.current => AppColors.amber,
                        TimelineState.done => AppColors.ink,
                      },
                    ),
                  ),
                  if (subtitle != null) ...[
                    const SizedBox(height: 2),
                    Text(subtitle!, style: AppText.caption),
                  ],
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _Dot extends StatelessWidget {
  const _Dot({required this.state});

  final TimelineState state;

  @override
  Widget build(BuildContext context) {
    switch (state) {
      case TimelineState.done:
        return Container(
          width: 10,
          height: 10,
          decoration: const BoxDecoration(
            color: AppColors.mint,
            shape: BoxShape.circle,
          ),
        );
      case TimelineState.current:
        return Container(
          width: 14,
          height: 14,
          decoration: const BoxDecoration(
            color: AppColors.amber,
            shape: BoxShape.circle,
          ),
          child: const Center(
            child: SizedBox(
              width: 4,
              height: 4,
              child: DecoratedBox(
                decoration: BoxDecoration(
                  color: AppColors.card,
                  shape: BoxShape.circle,
                ),
              ),
            ),
          ),
        );
      case TimelineState.future:
        return Container(
          width: 10,
          height: 10,
          margin: const EdgeInsets.only(top: 3),
          decoration: BoxDecoration(
            color: AppColors.card,
            shape: BoxShape.circle,
            border: Border.all(color: AppColors.line),
          ),
        );
    }
  }
}

import 'package:flutter/material.dart';

import '../theme/tokens.dart';
import '../theme/typography.dart';

class SummaryStat {
  const SummaryStat({
    required this.value,
    required this.label,
    this.accent = false,
    this.onTap,
  });

  final String value;
  final String label;
  final bool accent;
  final VoidCallback? onTap;
}

/// 深色概览卡：三列数字 30/700，列间 1px #45536B 分隔。
class SummaryBlock extends StatelessWidget {
  const SummaryBlock({
    super.key,
    required this.stats,
    this.note,
    this.caption,
  });

  final List<SummaryStat> stats;
  final String? note;
  final String? caption;

  @override
  Widget build(BuildContext context) {
    return ClipRRect(
      borderRadius: BorderRadius.circular(AppRadius.card),
      child: Container(
        color: AppColors.ink,
        child: Stack(
          children: [
            Positioned(
              right: -46,
              top: -54,
              child: Container(
                width: 168,
                height: 168,
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  color: AppColors.mint.withOpacity(.14),
                ),
              ),
            ),
            Padding(
              padding: const EdgeInsets.fromLTRB(20, 20, 20, 14),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  if (caption != null) ...[
                    Text(
                      caption!,
                      style: AppText.caption.copyWith(
                        color: Colors.white.withOpacity(.66),
                      ),
                    ),
                    const SizedBox(height: 12),
                  ],
                  IntrinsicHeight(
                    child: Row(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        for (var i = 0; i < stats.length; i++) ...[
                          if (i > 0)
                            Container(
                              width: 1,
                              margin:
                                  const EdgeInsets.symmetric(horizontal: 16),
                              color: AppColors.deepDivider,
                            ),
                          Expanded(child: _StatColumn(stat: stats[i])),
                        ],
                      ],
                    ),
                  ),
                  if (note != null) ...[
                    Container(
                      margin: const EdgeInsets.only(top: 18),
                      padding: const EdgeInsets.only(top: 12),
                      decoration: const BoxDecoration(
                        border: Border(
                            top: BorderSide(color: AppColors.deepDivider)),
                      ),
                      child: Row(
                        children: [
                          const Icon(
                            Icons.check,
                            size: 15,
                            color: AppColors.mint,
                          ),
                          const SizedBox(width: 6),
                          Expanded(
                            child: Text(
                              note!,
                              style: AppText.caption.copyWith(
                                color: Colors.white.withOpacity(.72),
                              ),
                            ),
                          ),
                        ],
                      ),
                    ),
                  ],
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _StatColumn extends StatelessWidget {
  const _StatColumn({required this.stat});

  final SummaryStat stat;

  @override
  Widget build(BuildContext context) {
    final column = Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          stat.value,
          style: AppText.number.copyWith(
            fontSize: 40,
            height: 1.05,
            color: stat.accent ? AppColors.mint : Colors.white,
          ),
        ),
        const SizedBox(height: 4),
        Text(
          stat.label,
          style: AppText.caption.copyWith(
            color: Colors.white.withOpacity(.66),
            height: 1.35,
          ),
        ),
      ],
    );
    if (stat.onTap == null) return column;
    return InkWell(
      onTap: stat.onTap,
      borderRadius: BorderRadius.circular(6),
      child: column,
    );
  }
}

import 'package:flutter/material.dart';

import '../theme/tokens.dart';
import '../theme/typography.dart';

class SegItem {
  const SegItem({required this.label, this.count = 0});

  final String label;
  final int count;
}

/// 分段控件：槽 sunken，选中项白底描边。
class SegmentedTabs extends StatelessWidget {
  const SegmentedTabs({
    super.key,
    required this.items,
    required this.index,
    required this.onChanged,
  });

  final List<SegItem> items;
  final int index;
  final ValueChanged<int> onChanged;

  @override
  Widget build(BuildContext context) {
    return Container(
      height: 42,
      padding: const EdgeInsets.all(4),
      decoration: BoxDecoration(
        color: AppColors.sunken,
        borderRadius: BorderRadius.circular(11),
      ),
      child: Row(
        children: [
          for (var i = 0; i < items.length; i++)
            Expanded(
              child: Semantics(
                selected: i == index,
                button: true,
                child: GestureDetector(
                  behavior: HitTestBehavior.opaque,
                  onTap: () => onChanged(i),
                  child: AnimatedContainer(
                    duration: const Duration(milliseconds: 160),
                    margin: const EdgeInsets.symmetric(horizontal: 2),
                    decoration: BoxDecoration(
                      color: i == index ? AppColors.card : Colors.transparent,
                      borderRadius: BorderRadius.circular(8),
                      border: Border.all(
                        color: i == index ? AppColors.line : Colors.transparent,
                      ),
                    ),
                    child: Center(
                      child: Text(
                        items[i].count > 0
                            ? '${items[i].label} ${items[i].count}'
                            : items[i].label,
                        style: AppText.caption.copyWith(
                          fontSize: 12.5,
                          fontWeight: FontWeight.w600,
                          color: i == index ? AppColors.ink : AppColors.ink3,
                        ),
                      ),
                    ),
                  ),
                ),
              ),
            ),
        ],
      ),
    );
  }
}

/// 选中：mint 底 + 描边 + 勾；未选：白底 line 描边。
class AppChoiceChip extends StatelessWidget {
  const AppChoiceChip({
    super.key,
    required this.label,
    required this.selected,
    required this.onTap,
    this.icon,
  });

  final String label;
  final bool selected;
  final VoidCallback onTap;
  final IconData? icon;

  @override
  Widget build(BuildContext context) {
    final fg = selected ? AppColors.mintDeep : AppColors.ink2;
    return Semantics(
      selected: selected,
      button: true,
      child: Material(
        color: selected ? AppColors.mintSoft : AppColors.card,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(AppRadius.pill),
          side: BorderSide(
            color: selected ? AppColors.mint : AppColors.line,
          ),
        ),
        clipBehavior: Clip.antiAlias,
        child: InkWell(
          onTap: onTap,
          child: Container(
            height: AppSizes.touch,
            padding: const EdgeInsets.symmetric(horizontal: 14),
            child: Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                if (selected)
                  const Padding(
                    padding: EdgeInsets.only(right: 6),
                    child:
                        Icon(Icons.check, size: 14, color: AppColors.mintDeep),
                  )
                else if (icon != null)
                  Padding(
                    padding: const EdgeInsets.only(right: 6),
                    child: Icon(icon, size: 14, color: fg),
                  ),
                Text(
                  label,
                  style: AppText.caption.copyWith(
                    fontSize: 13,
                    fontWeight: FontWeight.w600,
                    color: fg,
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}

/// 轻量筛选文字（C4 全部/可借/可预约）。
class AppFilterTabs extends StatelessWidget {
  const AppFilterTabs({
    super.key,
    required this.labels,
    required this.index,
    required this.onChanged,
  });

  final List<String> labels;
  final int index;
  final ValueChanged<int> onChanged;

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        for (var i = 0; i < labels.length; i++)
          Padding(
            padding: const EdgeInsets.only(right: 6),
            child: TextButton(
              onPressed: () => onChanged(i),
              style: TextButton.styleFrom(
                minimumSize: const Size(44, 40),
                padding: const EdgeInsets.symmetric(horizontal: 14),
                backgroundColor: i == index ? AppColors.ink : AppColors.sunken,
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(AppRadius.pill),
                ),
              ),
              child: Text(
                labels[i],
                style: AppText.caption.copyWith(
                  fontSize: 12.5,
                  fontWeight: FontWeight.w600,
                  color: i == index ? Colors.white : AppColors.ink2,
                ),
              ),
            ),
          ),
      ],
    );
  }
}

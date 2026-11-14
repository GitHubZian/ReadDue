import 'package:flutter/material.dart';

import '../theme/tokens.dart';
import '../theme/typography.dart';
import 'press_scale.dart';

/// 列表行内的小按钮：触控 44，视觉紧凑；filled 用于「借阅」等主操作。
class SmallButton extends StatelessWidget {
  const SmallButton({
    super.key,
    required this.label,
    this.onPressed,
    this.loading = false,
    this.filled = false,
  });

  final String label;
  final VoidCallback? onPressed;
  final bool loading;
  final bool filled;

  @override
  Widget build(BuildContext context) {
    final enabled = onPressed != null && !loading;
    final bg = filled && enabled ? AppColors.ink : AppColors.card;
    final fg =
        !enabled ? AppColors.ink3 : (filled ? Colors.white : AppColors.ink);
    return PressScale(
      enabled: enabled,
      scale: 0.96,
      child: ConstrainedBox(
        constraints:
            const BoxConstraints(minWidth: 64, minHeight: AppSizes.touch),
        child: Material(
          color: bg,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(9),
            side: filled
                ? BorderSide.none
                : BorderSide(color: enabled ? AppColors.line : AppColors.line2),
          ),
          clipBehavior: Clip.antiAlias,
          child: InkWell(
            onTap: enabled ? onPressed : null,
            child: Padding(
              padding: const EdgeInsets.symmetric(horizontal: 14),
              child: Center(
                child: loading
                    ? SizedBox(
                        width: 14,
                        height: 14,
                        child: CircularProgressIndicator(
                          strokeWidth: 2,
                          color: fg,
                        ),
                      )
                    : Text(
                        label,
                        style: AppText.caption.copyWith(
                          fontSize: 12.5,
                          fontWeight: FontWeight.w600,
                          color: fg,
                        ),
                      ),
              ),
            ),
          ),
        ),
      ),
    );
  }
}

import 'package:flutter/material.dart';

import '../theme/tokens.dart';
import '../theme/typography.dart';
import 'press_scale.dart';

/// 高 48、radius 12；主操作 ink 底白字，危险操作 red。
class PrimaryButton extends StatelessWidget {
  const PrimaryButton({
    super.key,
    required this.label,
    this.onPressed,
    this.loading = false,
    this.danger = false,
    this.ghost = false,
    this.icon,
    this.height = AppSizes.button,
  });

  final String label;
  final VoidCallback? onPressed;
  final bool loading;
  final bool danger;
  final bool ghost;
  final IconData? icon;
  final double height;

  @override
  Widget build(BuildContext context) {
    final enabled = onPressed != null && !loading;
    final background = !enabled
        ? AppColors.sunken
        : ghost
            ? Colors.transparent
            : danger
                ? AppColors.red
                : AppColors.ink;
    final foreground = !enabled
        ? AppColors.ink3
        : ghost
            ? (danger ? AppColors.red : AppColors.ink)
            : Colors.white;

    return Semantics(
      button: true,
      enabled: enabled,
      label: label,
      child: SizedBox(
        height: height,
        width: double.infinity,
        child: PressScale(
          enabled: enabled,
          child: Material(
            color: background,
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(AppRadius.button),
              side: ghost && enabled
                  ? BorderSide(color: danger ? AppColors.red : AppColors.line)
                  : BorderSide.none,
            ),
            clipBehavior: Clip.antiAlias,
            child: InkWell(
              onTap: enabled ? onPressed : null,
              child: Center(
                child: loading
                    ? SizedBox(
                        width: 16,
                        height: 16,
                        child: CircularProgressIndicator(
                          strokeWidth: 2,
                          color: foreground,
                        ),
                      )
                    : Row(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          if (icon != null) ...[
                            Icon(icon, size: 18, color: foreground),
                            const SizedBox(width: 6),
                          ],
                          Text(
                            label,
                            style: AppText.bodySm.copyWith(
                              fontSize: 14,
                              fontWeight: FontWeight.w600,
                              color: foreground,
                            ),
                          ),
                        ],
                      ),
              ),
            ),
          ),
        ),
      ),
    );
  }
}

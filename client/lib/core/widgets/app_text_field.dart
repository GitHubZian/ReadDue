import 'package:flutter/material.dart';

import '../theme/tokens.dart';
import '../theme/typography.dart';

/// 全端统一输入框：48dp、单层描边、内联错误、suffix 40×40 触控区。
class AppTextField extends StatelessWidget {
  const AppTextField({
    super.key,
    required this.controller,
    required this.hint,
    this.errorText,
    this.suffix,
    this.focusNode,
    this.obscure = false,
    this.keyboardType,
    this.textInputAction,
    this.onSubmitted,
    this.onChanged,
    this.maxLines = 1,
  });

  final TextEditingController controller;
  final String hint;
  final String? errorText;
  final Widget? suffix;
  final FocusNode? focusNode;
  final bool obscure;
  final TextInputType? keyboardType;
  final TextInputAction? textInputAction;
  final ValueChanged<String>? onSubmitted;
  final ValueChanged<String>? onChanged;
  final int maxLines;

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        TextField(
          controller: controller,
          focusNode: focusNode,
          obscureText: obscure,
          keyboardType: keyboardType,
          textInputAction: textInputAction,
          onSubmitted: onSubmitted,
          onChanged: onChanged,
          maxLines: maxLines,
          style: AppText.body,
          decoration: InputDecoration(
            hintText: hint,
            hintStyle: AppText.body.copyWith(color: AppColors.ink3),
            filled: true,
            fillColor: AppColors.card,
            isDense: true,
            contentPadding:
                const EdgeInsets.symmetric(horizontal: 14, vertical: 14),
            suffixIcon: suffix == null
                ? null
                : SizedBox(
                    width: 40,
                    height: 40,
                    child: Center(child: suffix),
                  ),
            suffixIconConstraints:
                const BoxConstraints(minWidth: 40, minHeight: 40),
            enabledBorder: OutlineInputBorder(
              borderRadius: BorderRadius.circular(AppRadius.field),
              borderSide: const BorderSide(color: AppColors.line),
            ),
            focusedBorder: OutlineInputBorder(
              borderRadius: BorderRadius.circular(AppRadius.field),
              borderSide: const BorderSide(color: AppColors.mintDeep, width: 2),
            ),
          ),
        ),
        if (errorText != null) ...[
          const SizedBox(height: 6),
          Text(
            errorText!,
            style: AppText.caption.copyWith(color: AppColors.red),
          ),
        ],
      ],
    );
  }
}

import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/config/app_config.dart';
import '../../../core/theme/tokens.dart';
import '../../../core/theme/typography.dart';
import '../../../core/widgets/buttons.dart';
import '../../../core/widgets/dialogs.dart';
import 'auth_controller.dart';

/// C9 登录：校园统一身份（学号/工号 + 密码）。
class LoginPage extends ConsumerStatefulWidget {
  const LoginPage({super.key});

  @override
  ConsumerState<LoginPage> createState() => _LoginPageState();
}

class _LoginPageState extends ConsumerState<LoginPage> {
  final _studentNo = TextEditingController(
    text: AppConfig.useMock ? '20230114' : '2021001',
  );
  final _password = TextEditingController();
  final _passwordFocus = FocusNode();
  bool _obscure = true;
  bool _remember = true;
  String? _localError;
  String? _studentError;
  String? _passwordError;
  Timer? _ticker;

  @override
  void initState() {
    super.initState();
    _ticker = Timer.periodic(const Duration(milliseconds: 500), (_) {
      if (!mounted) return;
      if (ref.read(authControllerProvider).lockedUntil != null) {
        setState(() {});
      }
    });
  }

  @override
  void dispose() {
    _ticker?.cancel();
    _studentNo.dispose();
    _password.dispose();
    _passwordFocus.dispose();
    super.dispose();
  }

  Future<void> _submit() async {
    final auth = ref.read(authControllerProvider);
    if (auth.loading || auth.locked) return;
    FocusScope.of(context).unfocus();
    final no = _studentNo.text.trim();
    final pwd = _password.text;
    setState(() {
      _localError = null;
      _studentError = no.isEmpty ? '请输入学号/工号' : null;
      _passwordError = pwd.isEmpty ? '请输入密码' : null;
    });
    if (_studentError != null || _passwordError != null) return;
    await ref.read(authControllerProvider.notifier).login(
          studentNo: no,
          password: pwd,
          remember: _remember,
        );
  }

  @override
  Widget build(BuildContext context) {
    final auth = ref.watch(authControllerProvider);
    final error = _localError ?? auth.error;
    final lockText = _lockText(auth.lockedUntil);

    return Scaffold(
      backgroundColor: AppColors.bg,
      body: Stack(
        children: [
          Positioned(
            right: -70,
            top: -90,
            child: Container(
              width: 290,
              height: 290,
              decoration: const BoxDecoration(
                color: AppColors.mintSoft,
                shape: BoxShape.circle,
              ),
            ),
          ),
          Positioned(
            right: -120,
            top: 300,
            child: Container(
              width: 200,
              height: 200,
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                border: Border.all(color: AppColors.mint.withOpacity(.35)),
              ),
            ),
          ),
          SafeArea(
            child: SingleChildScrollView(
              padding: const EdgeInsets.fromLTRB(20, 28, 20, 20),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Container(
                    width: 46,
                    height: 46,
                    decoration: BoxDecoration(
                      color: AppColors.ink,
                      borderRadius: BorderRadius.circular(13),
                    ),
                    alignment: Alignment.center,
                    child: Text(
                      '阅',
                      style: AppText.h2.copyWith(
                        color: Colors.white,
                        fontWeight: FontWeight.w700,
                      ),
                    ),
                  ),
                  const SizedBox(height: 18),
                  const Text('阅有期', style: AppText.h1),
                  const SizedBox(height: 8),
                  Text(
                    '让每一本书，都按期回到下一位读者手中。',
                    style: AppText.bodySm.copyWith(color: AppColors.ink2),
                  ),
                  const SizedBox(height: 28),
                  _LoginField(
                    label: '学号 / 工号',
                    controller: _studentNo,
                    hint: '请输入学号或工号',
                    icon: Icons.person_outline,
                    errorText: _studentError,
                    textInputAction: TextInputAction.next,
                    onSubmitted: (_) => _passwordFocus.requestFocus(),
                    onChanged: (_) {
                      if (_studentError != null) {
                        setState(() => _studentError = null);
                      }
                    },
                  ),
                  const SizedBox(height: 12),
                  _LoginField(
                    label: '密码',
                    controller: _password,
                    hint: '请输入密码',
                    icon: Icons.lock_outline,
                    showIcon: false,
                    focusNode: _passwordFocus,
                    obscure: _obscure,
                    errorText: _passwordError,
                    textInputAction: TextInputAction.done,
                    onSubmitted: (_) => _submit(),
                    onChanged: (_) {
                      if (_passwordError != null) {
                        setState(() => _passwordError = null);
                      }
                    },
                    suffix: IconButton(
                      onPressed: () => setState(() => _obscure = !_obscure),
                      tooltip: _obscure ? '显示密码' : '隐藏密码',
                      icon: Icon(
                        _obscure
                            ? Icons.visibility_off_outlined
                            : Icons.visibility_outlined,
                        size: 20,
                        color: AppColors.ink3,
                      ),
                    ),
                  ),
                  const SizedBox(height: 4),
                  Row(
                    children: [
                      SizedBox(
                        width: 44,
                        height: 44,
                        child: Checkbox(
                          value: _remember,
                          onChanged: (value) =>
                              setState(() => _remember = value ?? false),
                          activeColor: AppColors.mintDeep,
                          side: const BorderSide(color: AppColors.line),
                          materialTapTargetSize:
                              MaterialTapTargetSize.shrinkWrap,
                        ),
                      ),
                      const SizedBox(width: 2),
                      Text(
                        '记住此设备',
                        style: AppText.bodySm.copyWith(color: AppColors.ink2),
                      ),
                      const Spacer(),
                      TextButton(
                        onPressed: () => showToast(
                          context,
                          '请通过校园统一身份认证重置密码',
                        ),
                        style: TextButton.styleFrom(
                          minimumSize: const Size(64, AppSizes.touch),
                        ),
                        child: Text(
                          '忘记密码?',
                          style: AppText.bodySm.copyWith(
                            color: AppColors.mintDeep,
                            fontWeight: FontWeight.w600,
                          ),
                        ),
                      ),
                    ],
                  ),
                  if (error != null) ...[
                    const SizedBox(height: 4),
                    _ErrorBanner(message: error),
                  ],
                  if (lockText != null) ...[
                    const SizedBox(height: 8),
                    _ErrorBanner(message: '尝试次数过多，请 $lockText 后重试'),
                  ],
                  const SizedBox(height: 12),
                  PrimaryButton(
                    label: '登录',
                    height: 56,
                    loading: auth.loading,
                    onPressed: auth.locked ? null : _submit,
                  ),
                  const SizedBox(height: 18),
                  const Row(
                    children: [
                      Expanded(child: Divider(color: AppColors.line)),
                      Padding(
                        padding: EdgeInsets.symmetric(horizontal: 10),
                        child: Text('或', style: AppText.caption),
                      ),
                      Expanded(child: Divider(color: AppColors.line)),
                    ],
                  ),
                  const SizedBox(height: 18),
                  PrimaryButton(
                    label: '校园统一身份认证',
                    ghost: true,
                    height: 52,
                    icon: Icons.shield_outlined,
                    onPressed: () => showToast(
                      context,
                      '统一身份认证将跳转校园门户，当前为演示账号',
                    ),
                  ),
                  const SizedBox(height: 24),
                  Center(
                    child: Image.asset(
                      'assets/img/rdu-hero-01-crop-alpha.png',
                      height: 196,
                      fit: BoxFit.contain,
                      errorBuilder: (_, __, ___) => const SizedBox(
                        height: 196,
                        child: Icon(
                          Icons.auto_stories_outlined,
                          size: 96,
                          color: AppColors.mint,
                        ),
                      ),
                    ),
                  ),
                  const SizedBox(height: 20),
                  Center(
                    child: Text(
                      '登录即表示同意《服务条款》与《隐私政策》',
                      style: AppText.caption.copyWith(color: AppColors.ink3),
                      textAlign: TextAlign.center,
                    ),
                  ),
                  const SizedBox(height: 6),
                  Center(
                    child: Text(
                      '阅有期 1.0.0 · 校园图书馆',
                      style: AppText.caption.copyWith(color: AppColors.ink3),
                    ),
                  ),
                  const SizedBox(height: 8),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }

  String? _lockText(DateTime? until) {
    if (until == null) return null;
    final diff = until.difference(DateTime.now());
    if (diff.isNegative) return null;
    final minutes = diff.inMinutes.toString().padLeft(2, '0');
    final seconds = (diff.inSeconds % 60).toString().padLeft(2, '0');
    return '$minutes:$seconds';
  }
}

/// 白卡内嵌标签输入框：60 高、单层描边、错误在卡下方。
class _LoginField extends StatelessWidget {
  const _LoginField({
    required this.label,
    required this.controller,
    required this.hint,
    required this.icon,
    this.showIcon = true,
    this.focusNode,
    this.obscure = false,
    this.errorText,
    this.suffix,
    this.textInputAction,
    this.onChanged,
    this.onSubmitted,
  });

  final String label;
  final TextEditingController controller;
  final String hint;
  final IconData icon;
  final bool showIcon;
  final FocusNode? focusNode;
  final bool obscure;
  final String? errorText;
  final Widget? suffix;
  final TextInputAction? textInputAction;
  final ValueChanged<String>? onChanged;
  final ValueChanged<String>? onSubmitted;

  @override
  Widget build(BuildContext context) {
    final hasError = errorText != null;
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Container(
          height: 60,
          padding: const EdgeInsets.fromLTRB(14, 8, 6, 8),
          decoration: BoxDecoration(
            color: AppColors.card,
            borderRadius: BorderRadius.circular(13),
            border: Border.all(
              color: hasError ? AppColors.red : AppColors.line,
            ),
          ),
          child: Row(
            children: [
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    Text(
                      label,
                      style: AppText.micro.copyWith(color: AppColors.ink3),
                    ),
                    const SizedBox(height: 1),
                    TextField(
                      controller: controller,
                      focusNode: focusNode,
                      obscureText: obscure,
                      onChanged: onChanged,
                      onSubmitted: onSubmitted,
                      textInputAction: textInputAction,
                      style: AppText.bodySm.copyWith(
                        fontWeight: FontWeight.w500,
                      ),
                      decoration: InputDecoration(
                        isDense: true,
                        hintText: hint,
                        hintStyle:
                            AppText.bodySm.copyWith(color: AppColors.ink3),
                        border: InputBorder.none,
                        enabledBorder: InputBorder.none,
                        focusedBorder: InputBorder.none,
                        contentPadding: EdgeInsets.zero,
                      ),
                    ),
                  ],
                ),
              ),
              if (showIcon) Icon(icon, size: 19, color: AppColors.ink3),
              if (suffix != null) suffix!,
            ],
          ),
        ),
        if (hasError)
          Padding(
            padding: const EdgeInsets.fromLTRB(4, 5, 0, 0),
            child: Text(
              errorText!,
              style: AppText.caption.copyWith(color: AppColors.red),
            ),
          ),
      ],
    );
  }
}

class _ErrorBanner extends StatelessWidget {
  const _ErrorBanner({required this.message});

  final String message;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
      decoration: BoxDecoration(
        color: AppColors.redSoft,
        borderRadius: BorderRadius.circular(10),
      ),
      child: Row(
        children: [
          const Icon(Icons.error_outline, size: 16, color: AppColors.red),
          const SizedBox(width: 8),
          Expanded(
            child: Text(
              message,
              style: AppText.caption.copyWith(color: AppColors.red),
            ),
          ),
        ],
      ),
    );
  }
}

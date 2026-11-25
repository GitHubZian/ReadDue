import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/network/api_exception.dart';
import '../../../core/theme/tokens.dart';
import '../../../core/theme/typography.dart';
import '../../../core/widgets/app_text_field.dart';
import '../../../core/widgets/async_body.dart';
import '../../../core/widgets/buttons.dart';
import '../../../core/widgets/dialogs.dart';
import '../../../core/widgets/page_title.dart';
import '../../../core/widgets/states.dart';
import '../domain/profile_repository.dart';
import '../domain/profile_validation.dart';
import 'profile_providers.dart';

/// 资料编辑：姓名/手机/邮箱，前端校验 + 服务端 422 message 回显。
class EditProfilePage extends ConsumerWidget {
  const EditProfilePage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return Scaffold(
      backgroundColor: AppColors.bg,
      body: SafeArea(
        bottom: false,
        child: Column(
          children: [
            const PageTitle(title: '资料编辑'),
            Expanded(
              child: AsyncBody<ProfileContact>(
                value: ref.watch(contactProvider),
                onRetry: () => ref.invalidate(contactProvider),
                skeleton: const SkeletonList(count: 3, itemHeight: 72),
                builder: (contact) => _ContactForm(contact: contact),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _ContactForm extends ConsumerStatefulWidget {
  const _ContactForm({required this.contact});

  final ProfileContact contact;

  @override
  ConsumerState<_ContactForm> createState() => _ContactFormState();
}

class _ContactFormState extends ConsumerState<_ContactForm> {
  late final TextEditingController _name =
      TextEditingController(text: widget.contact.name);
  late final TextEditingController _phone =
      TextEditingController(text: widget.contact.phone);
  late final TextEditingController _email =
      TextEditingController(text: widget.contact.email);

  String? _nameError;
  String? _phoneError;
  String? _emailError;
  bool _saving = false;

  @override
  void dispose() {
    _name.dispose();
    _phone.dispose();
    _email.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return ListView(
      padding: const EdgeInsets.fromLTRB(20, 8, 20, 28),
      children: [
        const _FieldLabel('姓名'),
        AppTextField(
          controller: _name,
          hint: '请输入姓名',
          errorText: _nameError,
          textInputAction: TextInputAction.next,
          onChanged: (_) {
            if (_nameError != null) setState(() => _nameError = null);
          },
        ),
        const SizedBox(height: 16),
        const _FieldLabel('手机号'),
        AppTextField(
          controller: _phone,
          hint: '11 位手机号',
          keyboardType: TextInputType.phone,
          errorText: _phoneError,
          textInputAction: TextInputAction.next,
          onChanged: (_) {
            if (_phoneError != null) setState(() => _phoneError = null);
          },
        ),
        const SizedBox(height: 16),
        const _FieldLabel('邮箱'),
        AppTextField(
          controller: _email,
          hint: 'name@example.com',
          keyboardType: TextInputType.emailAddress,
          errorText: _emailError,
          textInputAction: TextInputAction.done,
          onChanged: (_) {
            if (_emailError != null) setState(() => _emailError = null);
          },
        ),
        const SizedBox(height: 12),
        Text(
          '手机号与邮箱仅用于借阅与到书提醒，不会对外展示。',
          style: AppText.caption.copyWith(color: AppColors.ink3),
        ),
        const SizedBox(height: 24),
        PrimaryButton(
          label: '保存',
          loading: _saving,
          onPressed: _saving ? null : _save,
        ),
      ],
    );
  }

  Future<void> _save() async {
    final name = _name.text.trim();
    final phone = _phone.text.trim();
    final email = _email.text.trim();
    final nameError = validateProfileName(name);
    final phoneError = validateProfilePhone(phone);
    final emailError = validateProfileEmail(email);
    setState(() {
      _nameError = nameError;
      _phoneError = phoneError;
      _emailError = emailError;
    });
    if (nameError != null || phoneError != null || emailError != null) return;

    setState(() => _saving = true);
    try {
      await ref.read(profileRepositoryProvider).updateProfile(
            name: name,
            phone: phone,
            email: email,
          );
      ref.invalidate(contactProvider);
      ref.invalidate(profileProvider);
      if (!mounted) return;
      showToast(context, '资料已更新');
      Navigator.of(context).pop();
    } catch (error) {
      if (!mounted) return;
      setState(() => _saving = false);
      final message = error is ApiException && error.isValidation
          ? error.message
          : messageForError(error);
      showToast(context, message);
    }
  }
}

class _FieldLabel extends StatelessWidget {
  const _FieldLabel(this.text);

  final String text;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 8),
      child: Text(
        text,
        style: AppText.bodySm.copyWith(fontWeight: FontWeight.w600),
      ),
    );
  }
}

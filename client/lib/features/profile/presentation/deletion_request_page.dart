import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/network/api_exception.dart';
import '../../../core/theme/tokens.dart';
import '../../../core/theme/typography.dart';
import '../../../core/widgets/buttons.dart';
import '../../../core/widgets/dialogs.dart';
import '../../../core/widgets/page_title.dart';
import '../../../core/widgets/widgets.dart';
import '../domain/deletion_request_repository.dart';
import 'profile_providers.dart';

/// C7 子页：注销申请。提交后由馆员审批，批准后立即打码、借阅事实保留。
class DeletionRequestPage extends ConsumerStatefulWidget {
  const DeletionRequestPage({super.key});

  @override
  ConsumerState<DeletionRequestPage> createState() =>
      _DeletionRequestPageState();
}

class _DeletionRequestPageState extends ConsumerState<DeletionRequestPage> {
  late Future<DeletionRequest?> _latest;
  final _reason = TextEditingController();
  bool _submitting = false;

  @override
  void initState() {
    super.initState();
    _latest = _fetch();
  }

  @override
  void dispose() {
    _reason.dispose();
    super.dispose();
  }

  Future<DeletionRequest?> _fetch() =>
      ref.read(deletionRequestRepositoryProvider).fetchLatest();

  bool _canSubmit(DeletionRequest? request) =>
      request == null || request.status == 'REJECTED';

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.bg,
      body: SafeArea(
        bottom: false,
        child: Column(
          children: [
            const PageTitle(title: '注销账号'),
            Expanded(
              child: FutureBuilder<DeletionRequest?>(
                future: _latest,
                builder: (context, snapshot) {
                  if (snapshot.connectionState != ConnectionState.done) {
                    return const Center(
                      child: CircularProgressIndicator(
                        color: AppColors.mintDeep,
                      ),
                    );
                  }
                  if (snapshot.hasError) {
                    return _loadError(context, snapshot.error);
                  }
                  return _body(context, snapshot.data);
                },
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _loadError(BuildContext context, Object? error) {
    return ListView(
      padding: const EdgeInsets.fromLTRB(20, 8, 20, 24),
      children: [
        AppCard(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                '查询注销状态失败',
                style: AppText.h3.copyWith(fontSize: 16),
              ),
              const SizedBox(height: 6),
              Text(
                messageForError(error),
                style: AppText.bodySm.copyWith(
                  color: AppColors.ink2,
                  height: 1.7,
                ),
              ),
              const SizedBox(height: 10),
              Align(
                alignment: Alignment.centerLeft,
                child: TextButton(
                  onPressed: () => setState(() => _latest = _fetch()),
                  child: const Text('重试'),
                ),
              ),
            ],
          ),
        ),
      ],
    );
  }

  Widget _body(BuildContext context, DeletionRequest? request) {
    final canSubmit = _canSubmit(request);
    return ListView(
      padding: const EdgeInsets.fromLTRB(20, 8, 20, 32),
      children: [
        AppCard(
          padding: const EdgeInsets.all(16),
          child: Text(
            '提交后由馆员审批；批准后姓名、手机号、邮箱立即打码，借阅事实保留。\n\n'
            '注销不会删除借阅、逾期与费用记录。',
            style: AppText.bodySm.copyWith(color: AppColors.ink2, height: 1.8),
          ),
        ),
        if (request != null) ...[
          const SizedBox(height: 12),
          _StatusCard(request: request),
        ],
        const SizedBox(height: 20),
        Text(
          '注销原因（选填）',
          style: AppText.bodySm.copyWith(fontWeight: FontWeight.w600),
        ),
        const SizedBox(height: 8),
        TextField(
          controller: _reason,
          enabled: canSubmit,
          maxLines: 3,
          maxLength: 100,
          style: AppText.bodySm.copyWith(color: AppColors.ink),
          decoration: InputDecoration(
            hintText: '可说明注销原因，便于馆员判断',
            hintStyle: AppText.caption.copyWith(color: AppColors.ink3),
            filled: true,
            fillColor: AppColors.card,
            counterStyle: AppText.caption.copyWith(color: AppColors.ink3),
            border: OutlineInputBorder(
              borderRadius: BorderRadius.circular(AppRadius.card),
              borderSide: const BorderSide(color: AppColors.line),
            ),
            enabledBorder: OutlineInputBorder(
              borderRadius: BorderRadius.circular(AppRadius.card),
              borderSide: const BorderSide(color: AppColors.line),
            ),
          ),
        ),
        if (canSubmit) ...[
          const SizedBox(height: 12),
          PrimaryButton(
            label: request?.status == 'REJECTED' ? '重新提交申请' : '提交注销申请',
            danger: true,
            loading: _submitting,
            onPressed: _submit,
          ),
        ],
      ],
    );
  }

  Future<void> _submit() async {
    final ok = await showConfirmDialog(
      context,
      title: '提交注销申请',
      message: '提交后由馆员审批；批准后姓名、手机号、邮箱立即打码，借阅事实保留。',
      confirmLabel: '提交申请',
      danger: true,
    );
    if (!ok || !mounted) return;

    setState(() => _submitting = true);
    try {
      final result =
          await ref.read(deletionRequestRepositoryProvider).submit(
                reason: _reason.text,
              );
      if (!mounted) return;
      setState(() {
        _submitting = false;
        _reason.clear();
        _latest = Future<DeletionRequest?>.value(result);
      });
      showToast(context, '已提交，等待馆员处理');
    } catch (error) {
      if (!mounted) return;
      setState(() => _submitting = false);
      // 409/422 直接展示服务端 message。
      showToast(context, messageForError(error));
    }
  }
}

class _StatusCard extends StatelessWidget {
  const _StatusCard({required this.request});

  final DeletionRequest request;

  @override
  Widget build(BuildContext context) {
    final view = _view();
    return AppCard(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          StatusPill(label: view.$1, tone: view.$3),
          const SizedBox(height: 8),
          Text(view.$2, style: AppText.h3.copyWith(fontSize: 16)),
          const SizedBox(height: 6),
          Text(
            view.$4,
            style: AppText.bodySm.copyWith(color: AppColors.ink2, height: 1.7),
          ),
        ],
      ),
    );
  }

  /// (状态标签, 标题, 色调, 说明)
  (String, String, PillTone, String) _view() {
    switch (request.status) {
      case 'PENDING':
        return (
          '待审批',
          '已提交，等待馆员处理',
          PillTone.amber,
          '无需重复提交；批准后个人信息立即打码，借阅事实保留。',
        );
      case 'APPROVED':
        return (
          '已批准',
          '注销申请已批准',
          PillTone.mint,
          '姓名、手机号、邮箱已打码，借阅事实保留。',
        );
      case 'REJECTED':
        return (
          '未通过',
          '注销申请未通过',
          PillTone.red,
          request.reason?.trim().isNotEmpty == true
              ? request.reason!
              : '可补充说明后重新提交，或通过申诉说明情况。',
        );
      default:
        return (
          '处理中',
          '当前状态：${request.status}',
          PillTone.slate,
          '如有疑问可通过帮助与反馈联系馆员。',
        );
    }
  }
}

import 'package:file_picker/file_picker.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/models/models.dart';
import '../../../core/network/api_exception.dart';
import '../../../core/network/evidence_upload.dart';
import '../../../core/network/network_providers.dart';
import '../../../core/theme/tokens.dart';
import '../../../core/theme/typography.dart';
import '../../../core/widgets/async_body.dart';
import '../../../core/widgets/buttons.dart';
import '../../../core/widgets/dialogs.dart';
import '../../../core/widgets/page_title.dart';
import '../../../core/widgets/segmented.dart';
import '../../../core/widgets/states.dart';
import 'appeal_providers.dart';

/// C8 新建申诉：关联单据、类型、说明、凭证。
class AppealCreatePage extends ConsumerStatefulWidget {
  const AppealCreatePage({super.key});

  @override
  ConsumerState<AppealCreatePage> createState() => _AppealCreatePageState();
}

class _AppealCreatePageState extends ConsumerState<AppealCreatePage> {
  final _controller = TextEditingController();
  BorrowItem? _order;
  String? _type;
  final List<PickedEvidence> _attachments = [];
  bool _submitting = false;
  String? _error;

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  bool get _canSubmit =>
      _order != null &&
      _type != null &&
      _controller.text.trim().length >= 15 &&
      _attachments.isNotEmpty;

  @override
  Widget build(BuildContext context) {
    final types = ref.watch(appealTypesProvider);
    final orders = ref.watch(appealOrderOptionsProvider);

    return Scaffold(
      backgroundColor: AppColors.bg,
      body: SafeArea(
        bottom: false,
        child: Column(
          children: [
            const PageTitle(title: '新建申诉'),
            Expanded(
              child: ListView(
                padding: const EdgeInsets.fromLTRB(20, 4, 20, 24),
                children: [
                  _label('关联单据', required: true),
                  _OrderPicker(
                    order: _order,
                    onTap: orders.asData?.value == null
                        ? null
                        : () => _pickOrder(orders.asData!.value),
                  ),
                  const SizedBox(height: 16),
                  _label('申诉类型', required: true),
                  AsyncBody<List<AppealType>>(
                    value: types,
                    onRetry: () => ref.invalidate(appealTypesProvider),
                    skeleton: const SkeletonList(
                      count: 1,
                      itemHeight: 40,
                      padding: EdgeInsets.zero,
                    ),
                    builder: (list) => Wrap(
                      spacing: 8,
                      runSpacing: 8,
                      children: [
                        for (final type in list)
                          AppChoiceChip(
                            label: type.label,
                            selected: _type == type.code,
                            onTap: () => setState(() => _type = type.code),
                          ),
                      ],
                    ),
                  ),
                  const SizedBox(height: 16),
                  _label('问题说明', required: true),
                  TextField(
                    controller: _controller,
                    minLines: 5,
                    maxLines: 7,
                    maxLength: 300,
                    onChanged: (_) => setState(() {}),
                    style: AppText.bodySm.copyWith(
                      color: AppColors.ink,
                      height: 1.6,
                    ),
                    decoration: InputDecoration(
                      hintText: '请说明具体情况，至少 15 个字；附上可核对的时间与凭证。',
                      hintStyle: AppText.caption.copyWith(
                        color: AppColors.ink3,
                      ),
                      counterStyle: AppText.caption.copyWith(
                        color: AppColors.ink3,
                      ),
                    ),
                  ),
                  if (_controller.text.trim().isNotEmpty &&
                      _controller.text.trim().length < 15)
                    Text(
                      '还需 ${15 - _controller.text.trim().length} 个字',
                      style: AppText.caption.copyWith(color: AppColors.red),
                    ),
                  const SizedBox(height: 10),
                  _label('凭证', required: true),
                  Wrap(
                    spacing: 8,
                    runSpacing: 8,
                    children: [
                      for (final file in _attachments)
                        _AttachmentChip(
                          name: file.name,
                          onRemove: () =>
                              setState(() => _attachments.remove(file)),
                        ),
                      if (_attachments.length < kMaxEvidenceFiles)
                        InkWell(
                          onTap: _addAttachment,
                          borderRadius: BorderRadius.circular(9),
                          child: Container(
                            height: 34,
                            padding: const EdgeInsets.symmetric(horizontal: 12),
                            decoration: BoxDecoration(
                              color: AppColors.card,
                              borderRadius: BorderRadius.circular(9),
                              border: Border.all(color: AppColors.line),
                            ),
                            child: Row(
                              mainAxisSize: MainAxisSize.min,
                              children: [
                                const Icon(
                                  Icons.add,
                                  size: 15,
                                  color: AppColors.ink2,
                                ),
                                const SizedBox(width: 5),
                                Text(
                                  '添加凭证',
                                  style: AppText.caption.copyWith(
                                    color: AppColors.ink2,
                                  ),
                                ),
                              ],
                            ),
                          ),
                        ),
                    ],
                  ),
                  const SizedBox(height: 6),
                  Text(
                    '支持 PDF / JPG / PNG，单个不超过 5MB，最多 $kMaxEvidenceFiles 个。',
                    style: AppText.caption.copyWith(color: AppColors.ink3),
                  ),
                  const SizedBox(height: 16),
                  Container(
                    padding: const EdgeInsets.all(12),
                    decoration: BoxDecoration(
                      color: AppColors.mintSoft,
                      borderRadius: BorderRadius.circular(10),
                    ),
                    child: Row(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        const Icon(
                          Icons.schedule,
                          size: 16,
                          color: AppColors.mintDeep,
                        ),
                        const SizedBox(width: 8),
                        Expanded(
                          child: Text(
                            '馆员将在 2 小时内答复；超时系统自动补 3 天。'
                            '同一单据最多提交 2 次。',
                            style: AppText.caption.copyWith(
                              color: AppColors.mintDeep,
                              height: 1.6,
                            ),
                          ),
                        ),
                      ],
                    ),
                  ),
                  if (_error != null) ...[
                    const SizedBox(height: 12),
                    Text(
                      _error!,
                      style: AppText.caption.copyWith(color: AppColors.red),
                    ),
                  ],
                ],
              ),
            ),
          ],
        ),
      ),
      bottomNavigationBar: SafeArea(
        top: false,
        child: Container(
          padding: const EdgeInsets.fromLTRB(20, 10, 20, 12),
          decoration: const BoxDecoration(
            color: AppColors.bg,
            border: Border(top: BorderSide(color: AppColors.line)),
          ),
          child: PrimaryButton(
            label: '提交申诉',
            loading: _submitting,
            onPressed: _canSubmit && !_submitting ? _submit : null,
          ),
        ),
      ),
    );
  }

  Widget _label(String text, {bool required = false}) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 8),
      child: Row(
        children: [
          Text(
            text,
            style: AppText.bodySm.copyWith(
              fontSize: 14,
              fontWeight: FontWeight.w600,
            ),
          ),
          if (required)
            Text(
              ' *',
              style: AppText.caption.copyWith(color: AppColors.red),
            ),
        ],
      ),
    );
  }

  Future<void> _pickOrder(List<BorrowItem> orders) async {
    final picked = await showModalBottomSheet<BorrowItem>(
      context: context,
      backgroundColor: AppColors.card,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(16)),
      ),
      builder: (sheetContext) => SafeArea(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const SizedBox(height: 10),
            Text('选择关联单据', style: AppText.h3.copyWith(fontSize: 16)),
            const SizedBox(height: 6),
            Flexible(
              child: ListView.separated(
                shrinkWrap: true,
                padding: const EdgeInsets.fromLTRB(16, 6, 16, 12),
                itemCount: orders.length,
                separatorBuilder: (_, __) =>
                    const Divider(height: 1, color: AppColors.line2),
                itemBuilder: (_, index) {
                  final order = orders[index];
                  return ListTile(
                    contentPadding: EdgeInsets.zero,
                    title: Text(
                      order.title,
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                      style: AppText.bodySm.copyWith(
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                    subtitle: Text(
                      '${order.dueLabel} · ${order.orderNo}',
                      style: AppText.caption.copyWith(color: AppColors.ink3),
                    ),
                    trailing: const Icon(
                      Icons.chevron_right,
                      size: 18,
                      color: AppColors.ink3,
                    ),
                    onTap: () => Navigator.of(sheetContext).pop(order),
                  );
                },
              ),
            ),
          ],
        ),
      ),
    );
    if (picked != null) setState(() => _order = picked);
  }

  Future<void> _addAttachment() async {
    final remaining = kMaxEvidenceFiles - _attachments.length;
    if (remaining <= 0) return;
    final result = await FilePicker.platform.pickFiles(
      type: FileType.custom,
      allowedExtensions: kEvidenceExtensions,
      allowMultiple: true,
    );
    if (result == null || result.files.isEmpty || !mounted) return;
    if (result.files.length > remaining) {
      showToast(context, '最多 $kMaxEvidenceFiles 个凭证，已保留前 $remaining 个');
    }
    for (final file in result.files.take(remaining)) {
      final error = evidenceFileError(file);
      if (error != null) {
        showToast(context, '${file.name}：$error');
        continue;
      }
      _attachments.add((name: file.name, path: file.path!));
    }
    setState(() {});
  }

  Future<void> _submit() async {
    setState(() {
      _submitting = true;
      _error = null;
    });
    try {
      final evidenceIds =
          await uploadEvidence(ref.read(apiClientProvider), _attachments);
      final result = await ref.read(appealRepositoryProvider).submit(
            AppealCreateRequest(
              orderId: _order!.id,
              type: _type!,
              description: _controller.text.trim(),
              attachments: evidenceIds,
            ),
          );
      if (!mounted) return;
      showToast(
        context,
        '已提交 ${result.ticketNo}，馆员将在 2 小时内答复',
      );
      Navigator.of(context).pop(true);
    } catch (error) {
      if (!mounted) return;
      final message = messageForError(error);
      setState(() => _error = message);
      showToast(context, message);
    } finally {
      if (mounted) setState(() => _submitting = false);
    }
  }
}

class _OrderPicker extends StatelessWidget {
  const _OrderPicker({this.order, this.onTap});

  final BorrowItem? order;
  final VoidCallback? onTap;

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(AppRadius.card),
      child: Container(
        constraints: const BoxConstraints(minHeight: 48),
        padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
        decoration: BoxDecoration(
          color: AppColors.card,
          borderRadius: BorderRadius.circular(AppRadius.card),
          border: Border.all(color: AppColors.line),
        ),
        child: Row(
          children: [
            Expanded(
              child: order == null
                  ? Text(
                      '请选择要申诉的借阅单据',
                      style: AppText.bodySm.copyWith(color: AppColors.ink3),
                    )
                  : Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          order!.title,
                          style: AppText.bodySm.copyWith(
                            fontWeight: FontWeight.w600,
                          ),
                        ),
                        const SizedBox(height: 2),
                        Text(
                          '${order!.dueLabel} · ${order!.orderNo}',
                          style: AppText.caption.copyWith(
                            color: AppColors.ink3,
                          ),
                        ),
                      ],
                    ),
            ),
            const Icon(
              Icons.unfold_more,
              size: 18,
              color: AppColors.ink3,
            ),
          ],
        ),
      ),
    );
  }
}

class _AttachmentChip extends StatelessWidget {
  const _AttachmentChip({required this.name, required this.onRemove});

  final String name;
  final VoidCallback onRemove;

  @override
  Widget build(BuildContext context) {
    return Container(
      height: 34,
      padding: const EdgeInsets.only(left: 10, right: 4),
      decoration: BoxDecoration(
        color: AppColors.wash,
        borderRadius: BorderRadius.circular(9),
        border: Border.all(color: AppColors.line),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          const Icon(
            Icons.attach_file,
            size: 14,
            color: AppColors.ink2,
          ),
          const SizedBox(width: 5),
          Text(
            name,
            style: AppText.caption.copyWith(color: AppColors.ink2),
          ),
          IconButton(
            onPressed: onRemove,
            tooltip: '移除',
            padding: EdgeInsets.zero,
            constraints: const BoxConstraints(minWidth: 32, minHeight: 32),
            icon: const Icon(
              Icons.close,
              size: 14,
              color: AppColors.ink3,
            ),
          ),
        ],
      ),
    );
  }
}

import 'package:file_picker/file_picker.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/models/models.dart';
import '../../../core/network/api_exception.dart';
import '../../../core/network/evidence_upload.dart';
import '../../../core/network/network_providers.dart';
import '../../../core/theme/tokens.dart';
import '../../../core/theme/typography.dart';
import '../../../core/utils/app_date.dart';
import '../../../core/widgets/async_body.dart';
import '../../../core/widgets/buttons.dart';
import '../../../core/widgets/dialogs.dart';
import '../../../core/widgets/page_title.dart';
import '../../../core/widgets/segmented.dart';
import '../../../core/widgets/states.dart';
import '../../../core/widgets/widgets.dart';
import '../../borrow/presentation/borrow_providers.dart';
import '../domain/grace_repository.dart';
import 'grace_providers.dart';

/// C3 宽限申请：场景 + 说明 + 材料 + 历史 + 规则提示。
class GracePage extends ConsumerStatefulWidget {
  const GracePage({super.key, required this.orderId});

  final String orderId;

  @override
  ConsumerState<GracePage> createState() => _GracePageState();
}

class _GracePageState extends ConsumerState<GracePage> {
  final _description = TextEditingController();
  final _scroll = ScrollController();
  final _sceneKey = GlobalKey();
  final _descKey = GlobalKey();
  final List<PickedEvidence> _attachments = [];
  String? _scene;
  String? _error;
  String? _sceneError;
  String? _descError;
  bool _submitting = false;

  @override
  void dispose() {
    _description.dispose();
    _scroll.dispose();
    super.dispose();
  }

  bool get _complete => _scene != null && _description.text.trim().length >= 10;

  String get _hint {
    final missing = <String>[
      if (_scene == null) '选择场景',
      if (_description.text.trim().length < 10) '填写至少 10 字说明',
    ];
    if (missing.isEmpty) return '提交后不可修改，请在说明中一次写清。';
    return '还差 ${missing.length} 项：${missing.join('、')}';
  }

  @override
  Widget build(BuildContext context) {
    final page = ref.watch(gracePageProvider(widget.orderId));
    return Scaffold(
      backgroundColor: AppColors.bg,
      body: SafeArea(
        bottom: false,
        child: Column(
          children: [
            const PageTitle(title: '申请宽限'),
            Expanded(
              child: AsyncBody<GracePageData>(
                value: page,
                onRetry: () =>
                    ref.invalidate(gracePageProvider(widget.orderId)),
                skeleton: const SkeletonList(count: 3, itemHeight: 88),
                builder: (data) => _form(data),
              ),
            ),
          ],
        ),
      ),
      bottomNavigationBar: BottomActionBar(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            PrimaryButton(
              label: '提交申请',
              loading: _submitting,
              onPressed: _submitting || !_complete ? null : _submit,
            ),
            const SizedBox(height: 6),
            Text(
              _hint,
              textAlign: TextAlign.center,
              style: AppText.caption.copyWith(color: AppColors.ink3),
            ),
          ],
        ),
      ),
    );
  }

  Widget _form(GracePageData data) {
    final estimate = data.estimate;
    final count = _description.text.length;
    return ListView(
      controller: _scroll,
      padding: const EdgeInsets.fromLTRB(20, 4, 20, 24),
      children: [
        if (data.isOffline) const OfflineBanner(),
        const Text('说明情况，申请延后归还', style: AppText.h2),
        const SizedBox(height: 8),
        Text(
          '毕业、实习、考试与合理事由可申请，单纯遗忘不计入。',
          style: AppText.bodySm.copyWith(color: AppColors.ink2),
        ),
        const SizedBox(height: 18),
        if (data.hasPending) ...[
          Container(
            padding: const EdgeInsets.all(12),
            decoration: BoxDecoration(
              color: AppColors.amberSoft,
              borderRadius: BorderRadius.circular(12),
            ),
            child: Row(
              children: [
                const Icon(Icons.schedule, size: 16, color: AppColors.amber),
                const SizedBox(width: 8),
                Expanded(
                  child: Text(
                    '已有一笔申请正在审核，馆员将在 2 小时内答复。',
                    style: AppText.caption.copyWith(color: AppColors.amber),
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(height: 16),
        ],
        Column(
          key: _sceneKey,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('申请场景',
                style: AppText.label.copyWith(fontWeight: FontWeight.w600)),
            const SizedBox(height: 10),
            Wrap(
              spacing: 8,
              runSpacing: 8,
              children: [
                for (final scene in data.scenes)
                  AppChoiceChip(
                    label: scene.label,
                    selected: _scene == scene.code,
                    onTap: () => setState(() {
                      _scene = scene.code;
                      _sceneError = null;
                      _error = null;
                    }),
                  ),
              ],
            ),
            if (_sceneError != null) ...[
              const SizedBox(height: 8),
              Text(
                _sceneError!,
                style: AppText.caption.copyWith(color: AppColors.red),
              ),
            ],
          ],
        ),
        const SizedBox(height: 20),
        Row(
          children: [
            Text(
              '说明',
              style: AppText.label.copyWith(fontWeight: FontWeight.w600),
            ),
            const Spacer(),
            Text(
              '$count / 200',
              style: AppText.caption.copyWith(
                color: count > 200 ? AppColors.red : AppColors.ink3,
              ),
            ),
          ],
        ),
        const SizedBox(height: 8),
        Container(
          key: _descKey,
          padding: const EdgeInsets.all(14),
          decoration: BoxDecoration(
            color: AppColors.card,
            borderRadius: BorderRadius.circular(AppRadius.field),
            border: Border.all(
              color: _descError == null ? AppColors.line : AppColors.red,
            ),
          ),
          child: TextField(
            controller: _description,
            maxLines: 5,
            maxLength: 200,
            onChanged: (_) => setState(() {
              _descError = null;
              _error = null;
            }),
            decoration: const InputDecoration(
              hintText: '例如：4 月 8 日起在苏州实习，预计 4 月 20 日返校，附实习证明。',
              border: InputBorder.none,
              counterText: '',
              contentPadding: EdgeInsets.zero,
              isDense: true,
            ),
            style: AppText.bodySm.copyWith(height: 1.7),
          ),
        ),
        if (_descError != null) ...[
          const SizedBox(height: 6),
          Text(
            _descError!,
            style: AppText.caption.copyWith(color: AppColors.red),
          ),
        ],
        const SizedBox(height: 18),
        Row(
          children: [
            Text(
              '材料（选填）',
              style: AppText.label.copyWith(fontWeight: FontWeight.w600),
            ),
            const Spacer(),
            Text(
              '最多 $kMaxEvidenceFiles 个 · 单个 ≤ 5MB',
              style: AppText.caption.copyWith(color: AppColors.ink3),
            ),
          ],
        ),
        const SizedBox(height: 8),
        Wrap(
          spacing: 8,
          runSpacing: 8,
          children: [
            for (final file in _attachments)
              _AttachmentChip(
                name: file.name,
                onRemove: () => setState(() => _attachments.remove(file)),
              ),
            if (_attachments.length < kMaxEvidenceFiles)
              _AddAttachment(onTap: _addAttachment),
          ],
        ),
        const SizedBox(height: 20),
        if (estimate != null && _scene != null) ...[
          Container(
            padding: const EdgeInsets.all(14),
            decoration: BoxDecoration(
              color: AppColors.card,
              borderRadius: BorderRadius.circular(AppRadius.card),
              border: Border.all(color: AppColors.line),
            ),
            child: Column(
              children: [
                Row(
                  children: [
                    Text(
                      '批准后预计应还日',
                      style: AppText.caption.copyWith(color: AppColors.ink3),
                    ),
                    const Spacer(),
                    Text(
                      '${AppDate.monthDay(estimate.originalDueAt)} → '
                      '${AppDate.monthDay(estimate.estimatedDueAt)}',
                      style: AppText.bodySm.copyWith(
                        fontWeight: FontWeight.w600,
                        fontFeatures: AppText.tabular.fontFeatures,
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 6),
                Align(
                  alignment: Alignment.centerRight,
                  child: Text(
                    '预计顺延 ${estimate.days} 天，以馆员批准为准',
                    style: AppText.caption.copyWith(color: AppColors.ink3),
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(height: 14),
        ],
        Container(
          padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 11),
          decoration: BoxDecoration(
            color: AppColors.mintSoft,
            borderRadius: BorderRadius.circular(10),
          ),
          child: Row(
            children: [
              const Icon(Icons.schedule, size: 15, color: AppColors.mintDeep),
              const SizedBox(width: 8),
              Expanded(
                child: Text(
                  '馆员将在 2 小时内答复；超时自动补 3 天。',
                  style: AppText.caption.copyWith(color: AppColors.mintDeep),
                ),
              ),
            ],
          ),
        ),
        if (data.history.isNotEmpty) ...[
          const SizedBox(height: 22),
          const SectionHeader(title: '本期宽限记录'),
          const SizedBox(height: 8),
          for (final item in data.history)
            Padding(
              padding: const EdgeInsets.only(bottom: 8),
              child: _HistoryRow(
                item: item,
                onTap: () => _openDetail(item),
              ),
            ),
        ],
        if (_error != null) ...[
          const SizedBox(height: 12),
          Text(
            _error!,
            style: AppText.caption.copyWith(color: AppColors.red),
          ),
        ],
      ],
    );
  }

  Future<void> _openDetail(GraceApplication item) {
    return showModalBottomSheet<void>(
      context: context,
      isScrollControlled: true,
      backgroundColor: AppColors.card,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(18)),
      ),
      builder: (_) => _GraceDetailSheet(application: item),
    );
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
      showToast(context, '最多 $kMaxEvidenceFiles 个材料，已保留前 $remaining 个');
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
    final description = _description.text.trim();
    final sceneMissing = _scene == null;
    final descMissing = description.length < 10;
    if (sceneMissing || descMissing) {
      setState(() {
        _sceneError = sceneMissing ? '请选择申请场景' : null;
        _descError = descMissing ? '说明至少 10 个字，便于馆员判断' : null;
        _error = null;
      });
      final target = sceneMissing ? _sceneKey : _descKey;
      final targetContext = target.currentContext;
      if (targetContext != null) {
        final reduce = MediaQuery.of(context).disableAnimations;
        await Scrollable.ensureVisible(
          targetContext,
          alignment: 0.15,
          duration: reduce ? Duration.zero : const Duration(milliseconds: 250),
        );
      }
      return;
    }
    setState(() {
      _error = null;
      _submitting = true;
    });
    try {
      final evidenceIds =
          await uploadEvidence(ref.read(apiClientProvider), _attachments);
      final result = await ref.read(graceRepositoryProvider).submit(
            GraceSubmitRequest(
              orderId: widget.orderId,
              sceneCode: _scene!,
              description: description,
              attachments: evidenceIds,
            ),
          );
      if (!mounted) return;
      ref.invalidate(gracePageProvider(widget.orderId));
      ref.invalidate(borrowDashboardProvider);
      showToast(context, '已提交，受理编号 ${result.applyNo}');
      context.pop(true);
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

class _AttachmentChip extends StatelessWidget {
  const _AttachmentChip({required this.name, required this.onRemove});

  final String name;
  final VoidCallback onRemove;

  @override
  Widget build(BuildContext context) {
    return Container(
      height: AppSizes.touch,
      padding: const EdgeInsets.only(left: 12),
      decoration: BoxDecoration(
        color: AppColors.card,
        borderRadius: BorderRadius.circular(9),
        border: Border.all(color: AppColors.line),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          const Icon(Icons.description_outlined,
              size: 15, color: AppColors.ink2),
          const SizedBox(width: 6),
          Text(name, style: AppText.caption),
          IconButton(
            onPressed: onRemove,
            tooltip: '移除',
            icon: const Icon(Icons.close, size: 15),
            color: AppColors.ink3,
          ),
        ],
      ),
    );
  }
}

class _AddAttachment extends StatelessWidget {
  const _AddAttachment({required this.onTap});

  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return Material(
      color: AppColors.wash,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(9),
        side: const BorderSide(color: AppColors.line),
      ),
      child: InkWell(
        onTap: onTap,
        child: Container(
          height: AppSizes.touch,
          padding: const EdgeInsets.symmetric(horizontal: 14),
          child: Row(
            mainAxisSize: MainAxisSize.min,
            children: [
              const Icon(Icons.add, size: 16, color: AppColors.ink3),
              const SizedBox(width: 6),
              Text(
                '添加材料',
                style: AppText.caption.copyWith(color: AppColors.ink2),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _HistoryRow extends StatelessWidget {
  const _HistoryRow({required this.item, required this.onTap});

  final GraceApplication item;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    final approved = item.status == 'APPROVED';
    final pending = item.status == 'PENDING';
    return Material(
      color: AppColors.card,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(12),
        side: const BorderSide(color: AppColors.line),
      ),
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(12),
        child: Padding(
          padding: const EdgeInsets.all(12),
          child: Row(
            children: [
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      '${item.sceneLabel} · ${item.applyNo}',
                      style:
                          AppText.bodySm.copyWith(fontWeight: FontWeight.w600),
                    ),
                    const SizedBox(height: 3),
                    Text(
                      '${AppDate.date(item.submittedAt)} 提交'
                      '${item.grantedDays != null ? ' · 批准 ${item.grantedDays} 天' : ''}',
                      style: AppText.caption.copyWith(color: AppColors.ink3),
                    ),
                  ],
                ),
              ),
              const SizedBox(width: 8),
              StatusPill(
                label: graceStatusLabel(item.status),
                tone: pending
                    ? PillTone.amber
                    : (approved ? PillTone.mint : PillTone.slate),
              ),
              const SizedBox(width: 2),
              const Icon(Icons.chevron_right, size: 18, color: AppColors.ink3),
            ],
          ),
        ),
      ),
    );
  }
}

class _GraceDetailSheet extends ConsumerStatefulWidget {
  const _GraceDetailSheet({required this.application});

  final GraceApplication application;

  @override
  ConsumerState<_GraceDetailSheet> createState() => _GraceDetailSheetState();
}

class _GraceDetailSheetState extends ConsumerState<_GraceDetailSheet> {
  GraceDetail? _detail;
  Object? _error;
  String? _downloading;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    try {
      final detail = await ref
          .read(graceRepositoryProvider)
          .fetchDetail(widget.application.id);
      if (mounted) setState(() => _detail = detail);
    } catch (error) {
      if (mounted) setState(() => _error = error);
    }
  }

  Future<void> _download(GraceEvidence evidence) async {
    if (_downloading != null) return;
    setState(() => _downloading = evidence.id);
    try {
      final bytes = await ref
          .read(graceRepositoryProvider)
          .downloadEvidence(evidence.id);
      if (!mounted) return;
      if (evidence.isImage) {
        await showDialog<void>(
          context: context,
          builder: (dialogContext) => Dialog(
            backgroundColor: AppColors.card,
            child: ConstrainedBox(
              constraints: const BoxConstraints(maxHeight: 420, maxWidth: 360),
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Padding(
                    padding: const EdgeInsets.fromLTRB(16, 8, 4, 0),
                    child: Row(
                      children: [
                        Expanded(
                          child: Text(
                            evidence.name,
                            maxLines: 1,
                            overflow: TextOverflow.ellipsis,
                            style: AppText.bodySm
                                .copyWith(fontWeight: FontWeight.w600),
                          ),
                        ),
                        IconButton(
                          tooltip: '关闭',
                          onPressed: () => Navigator.of(dialogContext).pop(),
                          icon: const Icon(Icons.close, size: 18),
                        ),
                      ],
                    ),
                  ),
                  Flexible(
                    child: InteractiveViewer(
                      child: Image.memory(
                        bytes,
                        fit: BoxFit.contain,
                        errorBuilder: (_, __, ___) => Padding(
                          padding: const EdgeInsets.all(24),
                          child: Text(
                            '证据已失效',
                            style: AppText.caption
                                .copyWith(color: AppColors.ink3),
                          ),
                        ),
                      ),
                    ),
                  ),
                  const SizedBox(height: 8),
                ],
              ),
            ),
          ),
        );
      } else if (mounted) {
        showToast(context, '已下载 ${evidence.name}');
      }
    } catch (_) {
      if (mounted) showToast(context, '证据已失效');
    } finally {
      if (mounted) setState(() => _downloading = null);
    }
  }

  @override
  Widget build(BuildContext context) {
    final detail = _detail;
    final status = detail?.status ?? widget.application.status;
    final pending = status == 'PENDING';
    return SafeArea(
      child: SingleChildScrollView(
        padding: const EdgeInsets.fromLTRB(16, 14, 16, 24),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Text('宽限详情', style: AppText.h3.copyWith(fontSize: 18)),
                const Spacer(),
                StatusPill(
                  label: graceStatusLabel(status),
                  tone: pending
                      ? PillTone.amber
                      : (status == 'APPROVED'
                          ? PillTone.mint
                          : PillTone.slate),
                ),
              ],
            ),
            const SizedBox(height: 14),
            if (_error != null) ...[
              Text(
                '详情加载失败',
                style: AppText.bodySm.copyWith(color: AppColors.red),
              ),
              const SizedBox(height: 10),
              OutlinedButton(onPressed: _load, child: const Text('重试')),
            ] else if (detail == null)
              const Padding(
                padding: EdgeInsets.symmetric(vertical: 24),
                child: Center(child: CircularProgressIndicator()),
              )
            else ...[
              _DetailRow(label: '申请编号', value: detail.applyNo),
              _DetailRow(
                label: '关联借阅',
                value: detail.borrowId.isEmpty ? '—' : detail.borrowId,
              ),
              _DetailRow(label: '申请场景', value: detail.sceneLabel),
              _DetailRow(label: '申请天数', value: '${detail.requestedDays} 天'),
              _DetailRow(
                label: '批准天数',
                value:
                    detail.approvedDays == null ? '待审批' : '${detail.approvedDays} 天',
              ),
              _DetailRow(
                label: '创建时间',
                value: AppDate.dateTime(detail.createdAt),
              ),
              _DetailRow(
                label: '快照有效期',
                value: detail.snapshotExpiresAt == null
                    ? '—'
                    : '${AppDate.dateTime(detail.snapshotExpiresAt!)}'
                        '${detail.snapshotExpired ? '（已过期）' : ''}',
              ),
              const SizedBox(height: 10),
              Text(
                '证据材料',
                style: AppText.bodySm.copyWith(fontWeight: FontWeight.w600),
              ),
              const SizedBox(height: 6),
              if (detail.evidence.isEmpty)
                Text(
                  '未上传证据材料',
                  style: AppText.caption.copyWith(color: AppColors.ink3),
                )
              else
                for (final evidence in detail.evidence)
                  _EvidenceRow(
                    evidence: evidence,
                    loading: _downloading == evidence.id,
                    onTap: () => _download(evidence),
                  ),
              const SizedBox(height: 10),
              Text(
                detail.approvedDays == null
                    ? '审批结果与批准天数以馆员最终裁定为准。'
                    : '宽限已生效：应还日期已按批准天数顺延。',
                style: AppText.caption.copyWith(color: AppColors.ink3),
              ),
            ],
          ],
        ),
      ),
    );
  }
}

class _DetailRow extends StatelessWidget {
  const _DetailRow({required this.label, required this.value});

  final String label;
  final String value;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 5),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          SizedBox(
            width: 86,
            child: Text(
              label,
              style: AppText.caption.copyWith(color: AppColors.ink3),
            ),
          ),
          Expanded(
            child: Text(
              value,
              style: AppText.caption.copyWith(color: AppColors.ink),
            ),
          ),
        ],
      ),
    );
  }
}

class _EvidenceRow extends StatelessWidget {
  const _EvidenceRow({
    required this.evidence,
    required this.loading,
    required this.onTap,
  });

  final GraceEvidence evidence;
  final bool loading;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 6),
      child: Material(
        color: AppColors.wash,
        borderRadius: BorderRadius.circular(9),
        child: InkWell(
          onTap: loading ? null : onTap,
          borderRadius: BorderRadius.circular(9),
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 9),
            child: Row(
              children: [
                Icon(
                  evidence.isImage
                      ? Icons.image_outlined
                      : Icons.description_outlined,
                  size: 17,
                  color: AppColors.ink2,
                ),
                const SizedBox(width: 8),
                Expanded(
                  child: Text(
                    evidence.name,
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: AppText.caption.copyWith(color: AppColors.ink2),
                  ),
                ),
                const SizedBox(width: 8),
                Text(
                  evidence.sizeLabel,
                  style: AppText.caption.copyWith(color: AppColors.ink3),
                ),
                const SizedBox(width: 6),
                if (loading)
                  const SizedBox(
                    width: 14,
                    height: 14,
                    child: CircularProgressIndicator(strokeWidth: 2),
                  )
                else
                  const Icon(
                    Icons.download_outlined,
                    size: 16,
                    color: AppColors.ink3,
                  ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}

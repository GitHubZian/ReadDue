import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/models/models.dart';
import '../../../core/network/api_exception.dart';
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
  final List<String> _attachments = [];
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
              '最多 3 个 · 单个 ≤ 5MB',
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
                name: file,
                onRemove: () => setState(() => _attachments.remove(file)),
              ),
            if (_attachments.length < 3) _AddAttachment(onTap: _addAttachment),
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
              child: _HistoryRow(item: item),
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

  Future<void> _addAttachment() async {
    final name = await showModalBottomSheet<String>(
      context: context,
      backgroundColor: AppColors.card,
      builder: (sheetContext) => SafeArea(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            ListTile(
              leading: const Icon(Icons.photo_camera_outlined),
              title: const Text('拍照上传'),
              onTap: () => Navigator.of(sheetContext).pop('实习证明.pdf'),
            ),
            ListTile(
              leading: const Icon(Icons.folder_outlined),
              title: const Text('从文件选择'),
              onTap: () => Navigator.of(sheetContext).pop('实习安排.png'),
            ),
          ],
        ),
      ),
    );
    if (name != null && _attachments.length < 3) {
      setState(() => _attachments.add(name));
    }
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
      final result = await ref.read(graceRepositoryProvider).submit(
            GraceSubmitRequest(
              orderId: widget.orderId,
              sceneCode: _scene!,
              description: description,
              attachments: List.of(_attachments),
            ),
          );
      if (!mounted) return;
      ref.invalidate(gracePageProvider(widget.orderId));
      ref.invalidate(borrowDashboardProvider);
      showToast(context, '已提交，受理编号 ${result.applyNo}');
      context.pop(true);
    } catch (error) {
      if (!mounted) return;
      setState(() => _error = messageForError(error));
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
  const _HistoryRow({required this.item});

  final GraceApplication item;

  @override
  Widget build(BuildContext context) {
    final approved = item.status == 'APPROVED';
    final pending = item.status == 'PENDING';
    return Container(
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: AppColors.card,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: AppColors.line),
      ),
      child: Row(
        children: [
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  '${item.sceneLabel} · ${item.applyNo}',
                  style: AppText.bodySm.copyWith(fontWeight: FontWeight.w600),
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
          StatusPill(
            label: pending ? '审核中' : (approved ? '已通过' : '已驳回'),
            tone: pending
                ? PillTone.amber
                : (approved ? PillTone.mint : PillTone.slate),
          ),
        ],
      ),
    );
  }
}

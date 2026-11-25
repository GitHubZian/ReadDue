import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/network/api_exception.dart';
import '../../../core/theme/tokens.dart';
import '../../../core/theme/typography.dart';
import '../../../core/utils/app_date.dart';
import '../../../core/widgets/async_body.dart';
import '../../../core/widgets/buttons.dart';
import '../../../core/widgets/dialogs.dart';
import '../../../core/widgets/page_title.dart';
import '../../../core/widgets/states.dart';
import '../../../core/widgets/widgets.dart';
import '../domain/timetable.dart';
import 'timetable_providers.dart';

/// 课表免打扰：按周一…周日分组维护课程，保存即整体 PUT。
class TimetablePage extends ConsumerStatefulWidget {
  const TimetablePage({super.key});

  @override
  ConsumerState<TimetablePage> createState() => _TimetablePageState();
}

class _TimetablePageState extends ConsumerState<TimetablePage> {
  bool _saving = false;

  @override
  Widget build(BuildContext context) {
    final value = ref.watch(timetableProvider);

    return Scaffold(
      backgroundColor: AppColors.bg,
      body: SafeArea(
        bottom: false,
        child: Column(
          children: [
            const PageTitle(title: '课表免打扰'),
            Expanded(
              child: AsyncBody<Timetable>(
                value: value,
                onRetry: () {
                  ref.invalidate(timetableProvider);
                  ref.invalidate(quietWindowsProvider);
                },
                skeleton: const SkeletonList(count: 4, itemHeight: 88),
                builder: _body,
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _body(Timetable data) {
    final days = <int>[
      for (var day = 1; day <= 7; day++)
        if (data.courses.any((course) => course.weekday == day)) day,
    ];

    return ListView(
      padding: const EdgeInsets.fromLTRB(20, 8, 20, 28),
      children: [
        const _TodayQuietCard(),
        const SizedBox(height: 20),
        if (days.isEmpty)
          AppCard(
            padding: const EdgeInsets.symmetric(vertical: 28, horizontal: 20),
            child: Column(
              children: [
                const Icon(Icons.event_busy_outlined,
                    size: 28, color: AppColors.ink3),
                const SizedBox(height: 10),
                Text(
                  '还没有课表，添加后上课时段自动静音',
                  textAlign: TextAlign.center,
                  style: AppText.bodySm.copyWith(color: AppColors.ink2),
                ),
              ],
            ),
          )
        else
          for (final day in days) ...[
            SectionHeader(
              title: weekdayLabel(day),
              hint: '${data.courses.where((course) => course.weekday == day).length} 节',
            ),
            const SizedBox(height: 8),
            AppCard(
              padding: EdgeInsets.zero,
              child: Column(
                children: [
                  for (var i = 0; i < data.courses.length; i++)
                    if (data.courses[i].weekday == day) ...[
                      if (data.courses
                              .take(i)
                              .any((course) => course.weekday == day))
                        const Divider(height: 1, color: AppColors.line2),
                      _CourseRow(
                        course: data.courses[i],
                        onDelete: () => _delete(data, data.courses[i]),
                      ),
                    ],
                ],
              ),
            ),
            const SizedBox(height: 18),
          ],
        const SizedBox(height: 6),
        PrimaryButton(
          label: '添加课程',
          icon: Icons.add,
          loading: _saving,
          onPressed: _saving ? null : () => _add(data),
        ),
        const SizedBox(height: 14),
        Text(
          '课表只用于计算上课免打扰，不上传教学系统',
          textAlign: TextAlign.center,
          style: AppText.caption.copyWith(color: AppColors.ink3),
        ),
      ],
    );
  }

  Future<void> _add(Timetable data) async {
    final course = await showModalBottomSheet<Course>(
      context: context,
      isScrollControlled: true,
      backgroundColor: AppColors.card,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(16)),
      ),
      builder: (_) => _AddCourseSheet(existing: data.courses),
    );
    if (course == null) return;
    await _save([...data.courses, course]);
  }

  Future<void> _delete(Timetable data, Course course) async {
    final ok = await showConfirmDialog(
      context,
      title: '删除这节课',
      message: '${weekdayLabel(course.weekday)} ${course.label} 将不再自动静音。',
      confirmLabel: '删除',
      danger: true,
    );
    if (!ok) return;
    await _save(
      data.courses.where((item) => item != course).toList(),
    );
  }

  Future<void> _save(List<Course> courses) async {
    setState(() => _saving = true);
    try {
      await ref
          .read(timetableRepositoryProvider)
          .save(sortCourses(courses));
      ref.invalidate(timetableProvider);
      ref.invalidate(quietWindowsProvider);
      await ref.read(timetableProvider.future);
      if (mounted) showToast(context, '课表已保存');
    } catch (error) {
      if (mounted) showToast(context, messageForError(error));
    } finally {
      if (mounted) setState(() => _saving = false);
    }
  }
}

class _TodayQuietCard extends ConsumerWidget {
  const _TodayQuietCard();

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final value = ref.watch(quietWindowsProvider);
    final windows = value.asData?.value;

    return AppCard(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Text(
                '今日免打扰',
                style: AppText.body.copyWith(fontWeight: FontWeight.w600),
              ),
              const Spacer(),
              Text(
                AppDate.monthDay(DateTime.now()),
                style: AppText.caption.copyWith(color: AppColors.ink3),
              ),
            ],
          ),
          const SizedBox(height: 10),
          if (windows == null)
            Text(
              value.hasError ? '今日时段暂时无法加载' : '正在计算今天的免打扰时段…',
              style: AppText.caption.copyWith(color: AppColors.ink3),
            )
          else if (windows.isEmpty)
            Text(
              '今天没有课，按手动免打扰执行',
              style: AppText.bodySm.copyWith(color: AppColors.ink2),
            )
          else ...[
            Wrap(
              spacing: 8,
              runSpacing: 8,
              children: [
                for (final window in windows)
                  StatusPill(label: window.label, tone: PillTone.mint),
              ],
            ),
            const SizedBox(height: 8),
            Text(
              '每段前后各外扩 15 分钟，课间与自习同样静音。',
              style: AppText.caption.copyWith(color: AppColors.ink3),
            ),
          ],
        ],
      ),
    );
  }
}

class _CourseRow extends StatelessWidget {
  const _CourseRow({required this.course, required this.onDelete});

  final Course course;
  final VoidCallback onDelete;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(16, 6, 6, 6),
      child: Row(
        children: [
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  course.label,
                  style: AppText.bodySm.copyWith(fontWeight: FontWeight.w600),
                ),
                const SizedBox(height: 2),
                Text(
                  '上课时段 · 自动静音',
                  style: AppText.caption.copyWith(color: AppColors.ink3),
                ),
              ],
            ),
          ),
          IconButton(
            onPressed: onDelete,
            tooltip: '删除课程',
            icon: const Icon(Icons.delete_outline, size: 20),
            color: AppColors.ink3,
          ),
        ],
      ),
    );
  }
}

class _AddCourseSheet extends StatefulWidget {
  const _AddCourseSheet({required this.existing});

  final List<Course> existing;

  @override
  State<_AddCourseSheet> createState() => _AddCourseSheetState();
}

class _AddCourseSheetState extends State<_AddCourseSheet> {
  int _weekday = 1;
  TimeOfDay _start = const TimeOfDay(hour: 8, minute: 0);
  TimeOfDay _end = const TimeOfDay(hour: 9, minute: 40);
  String? _error;

  @override
  Widget build(BuildContext context) {
    return SafeArea(
      child: Padding(
        padding: EdgeInsets.fromLTRB(
          20,
          12,
          20,
          20 + MediaQuery.of(context).viewInsets.bottom,
        ),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Center(
              child: Container(
                width: 36,
                height: 4,
                decoration: BoxDecoration(
                  color: AppColors.line,
                  borderRadius: BorderRadius.circular(2),
                ),
              ),
            ),
            const SizedBox(height: 14),
            Text('添加课程', style: AppText.h3.copyWith(fontSize: 17)),
            const SizedBox(height: 14),
            Text('星期', style: AppText.label.copyWith(color: AppColors.ink2)),
            const SizedBox(height: 8),
            Wrap(
              spacing: 8,
              runSpacing: 8,
              children: [
                for (var day = 1; day <= 7; day++)
                  ChoiceChip(
                    label: Text(weekdayLabel(day)),
                    selected: _weekday == day,
                    onSelected: (_) => setState(() => _weekday = day),
                  ),
              ],
            ),
            const SizedBox(height: 16),
            Row(
              children: [
                Expanded(
                  child: _TimeField(
                    label: '开始',
                    value: _formatTime(_start),
                    onTap: () => _pickTime(isStart: true),
                  ),
                ),
                const Padding(
                  padding: EdgeInsets.symmetric(horizontal: 10),
                  child: Icon(Icons.arrow_forward,
                      size: 16, color: AppColors.ink3),
                ),
                Expanded(
                  child: _TimeField(
                    label: '结束',
                    value: _formatTime(_end),
                    onTap: () => _pickTime(isStart: false),
                  ),
                ),
              ],
            ),
            if (_error != null) ...[
              const SizedBox(height: 8),
              Text(
                _error!,
                style: AppText.caption.copyWith(color: AppColors.red),
              ),
            ],
            const SizedBox(height: 18),
            PrimaryButton(label: '保存课程', onPressed: _submit),
          ],
        ),
      ),
    );
  }

  Future<void> _pickTime({required bool isStart}) async {
    final picked = await showTimePicker(
      context: context,
      initialTime: isStart ? _start : _end,
    );
    if (picked == null) return;
    setState(() {
      if (isStart) {
        _start = picked;
      } else {
        _end = picked;
      }
      _error = null;
    });
  }

  void _submit() {
    final course = Course(
      weekday: _weekday,
      startTime: _formatTime(_start),
      endTime: _formatTime(_end),
    );
    final error = validateCourse(course, widget.existing);
    if (error != null) {
      setState(() => _error = error);
      return;
    }
    Navigator.of(context).pop(course);
  }
}

class _TimeField extends StatelessWidget {
  const _TimeField({
    required this.label,
    required this.value,
    required this.onTap,
  });

  final String label;
  final String value;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return AppCard(
      onTap: onTap,
      padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(label, style: AppText.caption.copyWith(color: AppColors.ink3)),
          const SizedBox(height: 2),
          Text(
            value,
            style: AppText.body.copyWith(fontWeight: FontWeight.w600),
          ),
        ],
      ),
    );
  }
}

String _formatTime(TimeOfDay time) =>
    '${time.hour.toString().padLeft(2, '0')}:'
    '${time.minute.toString().padLeft(2, '0')}';

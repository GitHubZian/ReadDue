import '../models/models.dart';

/// 后端 JSON → 客户端模型。字段缺失时给中性值，不猜业务结论。
DateTime? parseTime(dynamic value) {
  final text = value?.toString();
  if (text == null || text.isEmpty || text == 'null') return null;
  return DateTime.tryParse(text)?.toLocal();
}

String coverLabelOf(String title) {
  final text = title.trim();
  if (text.isEmpty) return '书';
  final runes = text.runes.toList();
  return String.fromCharCodes(runes.take(2));
}

int _daysLeft(DateTime dueAt, DateTime now) {
  final due = DateTime(dueAt.year, dueAt.month, dueAt.day);
  final today = DateTime(now.year, now.month, now.day);
  return due.difference(today).inDays;
}

bool isOverdue(String status, int overdueDays) =>
    overdueDays > 0 || status == 'OVERDUE' || status == 'LOST';

BorrowItem borrowItemFromJson(
  Map<String, dynamic> json, {
  DateTime? now,
}) {
  final dueAt = parseTime(json['dueAt']) ?? DateTime.now();
  final status = (json['status'] ?? 'BORROWED').toString();
  final overdue = isOverdue(status, 0);
  final daysLeft = _daysLeft(dueAt, now ?? DateTime.now());
  final overdueDays = int.tryParse((json['overdueDays'] ?? 0).toString()) ?? 0;
  final renewCount = int.tryParse((json['renewCount'] ?? 0).toString()) ?? 0;
  final title = (json['bookTitle'] ?? '').toString();
  return BorrowItem(
    id: (json['id'] ?? '').toString(),
    orderNo: (json['code'] ?? '').toString(),
    title: title,
    author: (json['bookAuthor'] ?? '').toString(),
    coverLabel: coverLabelOf(title),
    coverTone: overdue ? 'amber' : 'mint',
    borrowedAt: parseTime(json['borrowedAt']) ?? dueAt,
    dueAt: dueAt,
    effectiveDueAt: parseTime(json['effectiveDueAt']),
    overdueDays: overdueDays,
    daysLeft: daysLeft,
    renewCount: renewCount,
    dueLabel: overdue
        ? (overdueDays > 0 ? '逾期 $overdueDays 天' : '已逾期')
        : '剩余 $daysLeft 天',
    status: overdue ? 'OVERDUE' : 'BORROWED',
    canRenew: !overdue && renewCount == 0,
  );
}

OrderDetail orderDetailFromJson(
  Map<String, dynamic> json, {
  bool gracePending = false,
}) {
  final item = borrowItemFromJson(json);
  final overdue = item.overdueDays > 0 || item.status == 'OVERDUE';
  final timeline = <TimelineEvent>[
    TimelineEvent(
      id: 'borrowed',
      time: item.borrowedAt,
      title: '借出',
      subtitle: '馆员办理 · 已确认',
      state: 'done',
    ),
    TimelineEvent(
      id: 'due',
      time: item.dueAt,
      title: overdue ? '已过应还日' : '应还日',
      subtitle: overdue ? '可申请宽限或归还结清' : '到期前会收到提醒',
      state: overdue && item.overdueDays > 0 ? 'done' : 'current',
    ),
    if (overdue && item.overdueDays > 0)
      TimelineEvent(
        id: 'overdue',
        time: item.dueAt.add(const Duration(days: 1)),
        title: '进入逾期',
        subtitle: '逾期 ${item.overdueDays} 天',
        state: 'current',
      ),
  ];
  return OrderDetail(
    id: item.id,
    orderNo: item.orderNo,
    title: item.title,
    author: item.author,
    publisher: '—',
    callNo: '—',
    barcode: '—',
    coverLabel: item.coverLabel,
    coverTone: item.coverTone,
    status: item.status,
    overdueDays: item.overdueDays,
    borrowedAt: item.borrowedAt,
    dueAt: item.dueAt,
    effectiveDueAt: item.effectiveDueAt,
    graceDays: 0,
    feePerDay: '按天计费',
    feeEstimate: overdue ? '以归还结算为准' : null,
    gracePending: gracePending,
    canGrace: true,
    timeline: timeline,
    settleNote: '待归还 · 归还后自动核算，减免费用需馆员操作并留痕',
  );
}

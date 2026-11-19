import '../models/models.dart';

/// 后端 JSON → 客户端模型。字段缺失时给中性值，不猜业务结论。
///
/// 带时区的时间统一换算到北京时间（UTC+8）再展示，避免模拟器/海外设备
/// 因本地时区不同显示成另一个时刻；纯日期（yyyy-MM-dd）按字面保留。
DateTime? parseTime(dynamic value) {
  final text = value?.toString();
  if (text == null || text.isEmpty || text == 'null') return null;
  final parsed = DateTime.tryParse(text);
  if (parsed == null) return null;
  final hasZone =
      text.endsWith('Z') || RegExp(r'[+-]\d{2}:\d{2}$').hasMatch(text);
  if (!hasZone) return parsed;
  return parsed.toUtc().add(const Duration(hours: 8));
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

/// 检索结果行。库存取后端视图口径，不在端上重算。
BookItem bookItemFromJson(Map<String, dynamic> json) {
  final title = (json['title'] ?? '—').toString();
  final available = (json['availableCopies'] as num?)?.toInt() ?? 0;
  final queue = (json['queueLen'] as num?)?.toInt() ?? 0;
  final category = (json['categoryCode'] ?? '').toString();
  return BookItem(
    id: (json['id'] ?? '').toString(),
    title: title,
    author: (json['author'] ?? '').toString(),
    publisher: (json['publisher'] ?? '').toString(),
    callNo: category.isEmpty ? '' : category,
    libraryLabel: available > 0 ? '可借 · $available 本' : '已借出 · 可预约',
    status: available > 0 ? 'AVAILABLE' : 'ON_LOAN',
    availableCopies: available,
    queueLen: queue,
    coverLabel: coverLabelOf(title),
    coverTone: available > 0 ? 'mint' : 'slate',
    year: (json['publishDate'] ?? '').toString().split('-').first,
  );
}

BookDetail bookDetailFromJson(Map<String, dynamic> json) {
  final tags = (json['tags'] as List<dynamic>? ?? const [])
      .map((t) => t.toString())
      .toList();
  return BookDetail(
    id: (json['id'] ?? '').toString(),
    title: (json['title'] ?? '—').toString(),
    author: (json['author'] ?? '').toString(),
    publisher: (json['publisher'] ?? '').toString(),
    isbn13: (json['isbn13'] ?? '').toString(),
    publishDate: (json['publishDate'] ?? '').toString(),
    price: (json['price'] ?? '').toString(),
    summary: (json['summary'] ?? '').toString(),
    tags: tags,
    totalCopies: (json['totalCopies'] as num?)?.toInt() ?? 0,
    availableCopies: (json['availableCopies'] as num?)?.toInt() ?? 0,
    onLoanCopies: (json['onLoanCopies'] as num?)?.toInt() ?? 0,
    queueLen: (json['queueLen'] as num?)?.toInt() ?? 0,
  );
}

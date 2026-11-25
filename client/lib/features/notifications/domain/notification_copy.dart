/// 通知文案纠偏：后端可能对已逾期图书仍发「今日到期」，
/// 这里用同一时刻的借阅状态（overdueDays）做自洽改写。
class NotificationCopy {
  const NotificationCopy({required this.title, required this.body});

  final String title;
  final String body;
}

NotificationCopy normalizeNotificationCopy({
  required String title,
  required String body,
  required int overdueDays,
}) {
  final hasDueToday = title.contains('今日到期') || body.contains('今日到期');
  if (overdueDays <= 0 || !hasDueToday) {
    return NotificationCopy(title: title, body: body);
  }
  return NotificationCopy(
    title: title.contains('今日到期')
        ? title.replaceAll('今日到期', '逾期提醒')
        : title,
    body: body
        .replaceAll('今日到期', '已逾期 $overdueDays 天')
        .replaceAll('请按时归还', '请尽快归还'),
  );
}

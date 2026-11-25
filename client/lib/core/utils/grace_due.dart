import '../models/models.dart';
import 'app_date.dart';

/// 宽限回显：全端统一用「宽限后应还日」与派生天数。
///
/// `BorrowItem`/`OrderDetail` 由 freezed 生成，不新增字段；宽限天数由
/// `effectiveDueAt - dueAt` 推导（`OrderDetail.graceDays` 已由 mapper 回填）。
extension BorrowGraceView on BorrowItem {
  DateTime get settledDueAt => effectiveDueAt ?? dueAt;

  int get appliedGraceDays => graceDaysBetweenDates(dueAt, effectiveDueAt);

  bool get hasAppliedGrace => appliedGraceDays > 0;

  /// 「已延至 11-15（宽限 +3 天）」
  String get graceDueLabel =>
      '已延至 ${AppDate.monthDayNumeric(settledDueAt)}（宽限 +$appliedGraceDays 天）';

  /// 列表副标题：有宽限时先回显延后结果，再接当前逾期/剩余天数。
  String get dueSummary {
    if (hasAppliedGrace) {
      return dueLabel.isEmpty ? graceDueLabel : '$graceDueLabel · $dueLabel';
    }
    if (dueLabel.isEmpty) return '应还 ${AppDate.monthDay(dueAt)}';
    return '应还 ${AppDate.monthDay(settledDueAt)} · $dueLabel';
  }
}

extension OrderGraceView on OrderDetail {
  DateTime get settledDueAt => effectiveDueAt ?? dueAt;

  int get appliedGraceDays {
    if (graceDays > 0) return graceDays;
    return graceDaysBetweenDates(dueAt, effectiveDueAt);
  }

  bool get hasAppliedGrace => appliedGraceDays > 0;

  String get graceDueLabel =>
      '已延至 ${AppDate.monthDayNumeric(settledDueAt)}（宽限 +$appliedGraceDays 天）';
}

/// 自然日差，避免时区偏移把 3 天算成 2 天。
int graceDaysBetweenDates(DateTime dueAt, DateTime? effectiveDueAt) {
  if (effectiveDueAt == null) return 0;
  final original = DateTime(dueAt.year, dueAt.month, dueAt.day);
  final extended =
      DateTime(effectiveDueAt.year, effectiveDueAt.month, effectiveDueAt.day);
  final days = extended.difference(original).inDays;
  return days > 0 ? days : 0;
}

import 'package:client/core/network/reader_mappers.dart';
import 'package:client/core/utils/grace_due.dart';
import 'package:client/features/notifications/domain/notification_copy.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  group('parseTime 时区口径', () {
    test('带 +08:00 偏移只换算一次', () {
      final value = parseTime('2026-11-24T21:40:34+08:00');
      expect(value, isNotNull);
      // 北京 21:40 就是 UTC 13:40；旧实现会多 +8 小时。
      expect(value!.toUtc(), DateTime.utc(2026, 11, 24, 13, 40, 34));
      expect(value.toUtc().hour, 13);
    });

    test('Z 结尾按 UTC 解析后转本地', () {
      final value = parseTime('2026-11-24T13:40:34Z');
      expect(value!.toUtc(), DateTime.utc(2026, 11, 24, 13, 40, 34));
    });

    test('纯日期不做时区偏移', () {
      final value = parseTime('2026-11-24');
      expect(value, isNotNull);
      expect(value!.isUtc, isFalse);
      expect(value.year, 2026);
      expect(value.month, 11);
      expect(value.day, 24);
    });
  });

  group('宽限回显口径', () {
    Map<String, dynamic> borrowJson() => {
          'id': '2121884875573895169',
          'code': 'B20261112001',
          'bookTitle': '活着',
          'bookAuthor': '余华',
          'borrowedAt': '2026-10-29T10:00:00+08:00',
          'dueAt': '2026-11-12T23:59:59+08:00',
          'effectiveDueAt': '2026-11-15T23:59:59+08:00',
          'status': 'BORROWED',
          'renewCount': 0,
          'overdueDays': 10,
        };

    test('BorrowItem 用 effectiveDueAt 展示延后结果', () {
      final item = borrowItemFromJson(
        borrowJson(),
        now: DateTime(2026, 11, 25, 12),
      );
      expect(item.dueAt, DateTime(2026, 11, 12, 23, 59, 59));
      expect(item.effectiveDueAt, DateTime(2026, 11, 15, 23, 59, 59));
      expect(item.appliedGraceDays, 3);
      expect(item.hasAppliedGrace, isTrue);
      expect(item.settledDueAt, DateTime(2026, 11, 15, 23, 59, 59));
      expect(item.graceDueLabel, '已延至 11-15（宽限 +3 天）');
      expect(item.daysLeft, -10);
      expect(item.dueSummary, contains('已延至 11-15（宽限 +3 天）'));
      expect(item.dueSummary, contains('逾期 10 天'));
    });

    test('OrderDetail 回填 graceDays 且时间线不再用原到期日算逾期', () {
      final detail = orderDetailFromJson(borrowJson());
      expect(detail.graceDays, 3);
      expect(detail.appliedGraceDays, 3);
      expect(detail.hasAppliedGrace, isTrue);
      expect(detail.graceDueLabel, '已延至 11-15（宽限 +3 天）');

      final graceEvent =
          detail.timeline.firstWhere((event) => event.id == 'grace');
      expect(graceEvent.title, contains('宽限已通过'));
      expect(graceEvent.subtitle, contains('已延至 11-15'));

      final overdueEvent =
          detail.timeline.firstWhere((event) => event.id == 'overdue');
      // 原到期日 11-12，宽限后 11-15，进入逾期应是 11-16。
      expect(overdueEvent.time, DateTime(2026, 11, 16, 23, 59, 59));
      expect(overdueEvent.subtitle, '已逾期 10 天');
    });

    test('无宽限时 graceDays 为 0 且不出现宽限事件', () {
      final json = borrowJson()
        ..['effectiveDueAt'] = '2026-11-12T23:59:59+08:00'
        ..['overdueDays'] = 0;
      final detail = orderDetailFromJson(json);
      expect(detail.graceDays, 0);
      expect(detail.hasAppliedGrace, isFalse);
      expect(detail.timeline.any((event) => event.id == 'grace'), isFalse);
    });
  });

  group('通知文案纠偏', () {
    test('已逾期图书的「今日到期」改为逾期提醒', () {
      final copy = normalizeNotificationCopy(
        title: '图书今日到期',
        body: '《活着》今日到期，请按时归还。借阅单：2121884875573895169。',
        overdueDays: 10,
      );
      expect(copy.title, '图书逾期提醒');
      expect(copy.body, contains('已逾期 10 天'));
      expect(copy.body, isNot(contains('今日到期')));
      expect(copy.body, contains('请尽快归还'));
    });

    test('未逾期时保持原文案', () {
      final copy = normalizeNotificationCopy(
        title: '图书今日到期',
        body: '《活着》今日到期，请按时归还。',
        overdueDays: 0,
      );
      expect(copy.title, '图书今日到期');
      expect(copy.body, '《活着》今日到期，请按时归还。');
    });
  });
}

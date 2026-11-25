import 'package:flutter_test/flutter_test.dart';
import 'package:client/features/profile/domain/deletion_request_repository.dart';

void main() {
  test('驳回备注读 note，不把读者自填的 reason 当驳回原因', () {
    final request = deletionRequestFromJson({
      'status': 'REJECTED',
      'reason': '我不想用了',
      'note': '借阅未结清，暂不能注销',
    });

    expect(request?.status, 'REJECTED');
    expect(request?.reason, '借阅未结清，暂不能注销');
  });

  test('没有 note 时退回 message', () {
    final request = deletionRequestFromJson({
      'status': 'REJECTED',
      'message': '材料不完整',
    });

    expect(request?.reason, '材料不完整');
  });

  test('无记录或缺少 status 时返回 null', () {
    expect(deletionRequestFromJson(null), isNull);
    expect(deletionRequestFromJson({'note': 'x'}), isNull);
    expect(deletionRequestFromJson('PENDING'), isNull);
  });
}

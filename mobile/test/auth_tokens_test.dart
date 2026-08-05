import 'package:flutter_test/flutter_test.dart';
import 'package:workbridge_mobile/models/auth_tokens.dart';

void main() {
  test('로그인 응답을 토큰과 사용자로 파싱한다', () {
    final result = LoginResult.fromJson({
      'accessToken': 'a',
      'refreshToken': 'r',
      'accessExpiresAt': '2026-08-05T12:00:00Z',
      'refreshExpiresAt': '2026-08-20T12:00:00Z',
      'employeeId': 3,
      'employeeNo': 'WB-2001',
      'employeeName': '박승인',
      'roles': ['APPROVER'],
    });

    expect(result.user.roles, contains('APPROVER'));
    expect(result.tokens.accessToken, 'a');
  });
}

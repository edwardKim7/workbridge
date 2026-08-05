import 'dart:convert';

import 'package:flutter_test/flutter_test.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';
import 'package:workbridge_mobile/models/auth_tokens.dart';
import 'package:workbridge_mobile/services/api_client.dart';
import 'package:workbridge_mobile/services/token_store.dart';

void main() {
  test('401이면 한 번 갱신하고 원 요청을 재실행한다', () async {
    final tokenStore = MemoryTokenStore()
      ..value = AuthTokens(
        accessToken: 'old',
        refreshToken: 'r',
        accessExpiresAt: DateTime.now(),
        refreshExpiresAt: DateTime.now().add(const Duration(days: 1)),
      );

    var calls = 0;
    final httpClient = MockClient((request) async {
      calls++;
      if (calls == 1) {
        return http.Response(jsonEncode({'message': 'expired'}), 401);
      }
      return http.Response(
        jsonEncode({
          'data': {'ok': true},
        }),
        200,
      );
    });

    final apiClient = ApiClient(
      baseUrl: 'http://test',
      tokenStore: tokenStore,
      client: httpClient,
      refreshCallback: (_) async => AuthTokens(
        accessToken: 'new',
        refreshToken: 'r2',
        accessExpiresAt: DateTime.now().add(const Duration(minutes: 10)),
        refreshExpiresAt: DateTime.now().add(const Duration(days: 1)),
      ),
    );

    final payload = await apiClient.get('/x');

    expect((payload['data'] as Map)['ok'], true);
    expect(calls, 2);
  });

  test('갱신 실패 시 저장 토큰을 제거한다', () async {
    final tokenStore = MemoryTokenStore()
      ..value = AuthTokens(
        accessToken: 'old',
        refreshToken: 'r',
        accessExpiresAt: DateTime.now(),
        refreshExpiresAt: DateTime.now().add(const Duration(days: 1)),
      );

    final apiClient = ApiClient(
      baseUrl: 'http://test',
      tokenStore: tokenStore,
      client: MockClient((_) async => http.Response('{}', 401)),
      refreshCallback: (_) => throw Exception(),
    );

    await expectLater(apiClient.get('/x'), throwsA(isA<Exception>()));
    await Future<void>.delayed(Duration.zero);

    expect(await tokenStore.read(), isNull);
  });
}

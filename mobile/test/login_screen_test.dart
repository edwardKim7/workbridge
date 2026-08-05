import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:provider/provider.dart';
import 'package:workbridge_mobile/screens/login_screen.dart';
import 'package:workbridge_mobile/services/api_client.dart';
import 'package:workbridge_mobile/services/auth_service.dart';
import 'package:workbridge_mobile/services/token_store.dart';
import 'package:workbridge_mobile/state/session_controller.dart';

void main() {
  testWidgets('로그인 입력과 버튼을 표시한다', (tester) async {
    final tokenStore = MemoryTokenStore();
    final apiClient = ApiClient(
      baseUrl: 'http://test',
      tokenStore: tokenStore,
    );
    final controller = SessionController(
      AuthService(apiClient, tokenStore),
      tokenStore,
      apiClient,
    );

    await tester.pumpWidget(
      ChangeNotifierProvider.value(
        value: controller,
        child: const MaterialApp(home: LoginScreen()),
      ),
    );

    expect(find.text('WorkBridge'), findsOneWidget);
    expect(find.text('로그인'), findsOneWidget);
  });
}

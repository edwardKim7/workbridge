import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:workbridge_mobile/widgets/state_views.dart';

void main() {
  testWidgets('API 오류 메시지를 화면에 표시한다', (tester) async {
    await tester.pumpWidget(
      const MaterialApp(home: ErrorView('서버 오류')),
    );

    expect(find.text('서버 오류'), findsOneWidget);
  });
}

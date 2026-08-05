import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import 'app.dart';
import 'config/app_config.dart';
import 'services/api_client.dart';
import 'services/approval_service.dart';
import 'services/auth_service.dart';
import 'services/opportunity_service.dart';
import 'services/token_store.dart';
import 'state/approval_controller.dart';
import 'state/session_controller.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();

  final tokenStore = SecureTokenStore();
  final apiClient = ApiClient(
    baseUrl: AppConfig.apiBaseUrl,
    tokenStore: tokenStore,
  );
  final authService = AuthService(apiClient, tokenStore);
  final sessionController = SessionController(
    authService,
    tokenStore,
    apiClient,
  );

  sessionController.restore();

  runApp(
    MultiProvider(
      providers: [
        Provider.value(value: apiClient),
        Provider.value(value: authService),
        Provider(create: (_) => ApprovalService(apiClient)),
        Provider(create: (_) => OpportunityService(apiClient)),
        ChangeNotifierProvider.value(value: sessionController),
        ChangeNotifierProvider(
          create: (context) => ApprovalController(
            context.read<ApprovalService>(),
          ),
        ),
      ],
      child: const WorkBridgeApp(),
    ),
  );
}

import '../models/auth_tokens.dart';
import 'api_client.dart';
import 'token_store.dart';

class AuthService {
  AuthService(this.api, this.store) {
    api.refreshCallback = refreshTokens;
  }

  final ApiClient api;
  final TokenStore store;

  Future<LoginResult> login(String loginId, String password) async {
    final payload = await api.post(
      '/api/v1/auth/login',
      authenticated: false,
      body: {
        'loginId': loginId,
        'password': password,
        'deviceInfo': 'WorkBridge Flutter',
      },
    );
    final result = LoginResult.fromJson(payload['data'] as Map<String, dynamic>);
    await store.write(result.tokens);
    return result;
  }

  Future<LoginResult> refreshSession(String refreshToken) async {
    final payload = await api.post(
      '/api/v1/auth/refresh',
      authenticated: false,
      body: {
        'refreshToken': refreshToken,
        'deviceInfo': 'WorkBridge Flutter',
      },
    );
    return LoginResult.fromJson(payload['data'] as Map<String, dynamic>);
  }

  Future<AuthTokens> refreshTokens(String refreshToken) async {
    return (await refreshSession(refreshToken)).tokens;
  }

  Future<void> logout() async {
    final tokens = await store.read();
    try {
      if (tokens != null) {
        await api.post(
          '/api/v1/auth/logout',
          body: {'refreshToken': tokens.refreshToken},
        );
      }
    } finally {
      await store.clear();
    }
  }
}

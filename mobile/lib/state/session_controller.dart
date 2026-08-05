import 'dart:async';

import 'package:flutter/foundation.dart';

import '../models/auth_tokens.dart';
import '../services/api_client.dart';
import '../services/auth_service.dart';
import '../services/token_store.dart';

class SessionController extends ChangeNotifier {
  SessionController(this.auth, this.store, this.api) {
    _subscription = api.sessionExpired.listen((_) {
      user = null;
      error = '로그인 세션이 만료되었습니다. 다시 로그인해 주세요.';
      notifyListeners();
    });
  }

  final AuthService auth;
  final TokenStore store;
  final ApiClient api;

  AuthUser? user;
  bool busy = false;
  String? error;
  late final StreamSubscription<void> _subscription;

  bool get loggedIn => user != null;

  Future<void> restore() async {
    final tokens = await store.read();
    if (tokens == null || tokens.refreshExpiresAt.isBefore(DateTime.now())) {
      await store.clear();
      return;
    }

    try {
      final restored = await auth.refreshSession(tokens.refreshToken);
      await store.write(restored.tokens);
      user = restored.user;
    } catch (_) {
      await store.clear();
      user = null;
    }
    notifyListeners();
  }

  Future<bool> login(String loginId, String password) async {
    busy = true;
    error = null;
    notifyListeners();
    try {
      user = (await auth.login(loginId.trim(), password)).user;
      return true;
    } catch (exception) {
      error = exception.toString();
      return false;
    } finally {
      busy = false;
      notifyListeners();
    }
  }

  Future<void> logout() async {
    busy = true;
    notifyListeners();
    try {
      await auth.logout();
    } catch (_) {
      await store.clear();
    } finally {
      user = null;
      busy = false;
      notifyListeners();
    }
  }

  @override
  void dispose() {
    _subscription.cancel();
    super.dispose();
  }
}

import 'package:flutter_secure_storage/flutter_secure_storage.dart';

import '../models/auth_tokens.dart';

abstract class TokenStore {
  Future<AuthTokens?> read();

  Future<void> write(AuthTokens tokens);

  Future<void> clear();
}

class SecureTokenStore implements TokenStore {
  SecureTokenStore([FlutterSecureStorage? storage])
      : _storage = storage ?? const FlutterSecureStorage();

  final FlutterSecureStorage _storage;

  static const _access = 'workbridge_access_token';
  static const _refresh = 'workbridge_refresh_token';
  static const _accessExp = 'workbridge_access_exp';
  static const _refreshExp = 'workbridge_refresh_exp';

  @override
  Future<AuthTokens?> read() async {
    final values = await Future.wait([
      _storage.read(key: _access),
      _storage.read(key: _refresh),
      _storage.read(key: _accessExp),
      _storage.read(key: _refreshExp),
    ]);
    if (values.any((value) => value == null)) {
      return null;
    }
    try {
      return AuthTokens(
        accessToken: values[0]!,
        refreshToken: values[1]!,
        accessExpiresAt: DateTime.parse(values[2]!),
        refreshExpiresAt: DateTime.parse(values[3]!),
      );
    } on FormatException {
      await clear();
      return null;
    }
  }

  @override
  Future<void> write(AuthTokens tokens) async {
    await Future.wait([
      _storage.write(key: _access, value: tokens.accessToken),
      _storage.write(key: _refresh, value: tokens.refreshToken),
      _storage.write(key: _accessExp, value: tokens.accessExpiresAt.toIso8601String()),
      _storage.write(key: _refreshExp, value: tokens.refreshExpiresAt.toIso8601String()),
    ]);
  }

  @override
  Future<void> clear() async {
    await Future.wait([
      _storage.delete(key: _access),
      _storage.delete(key: _refresh),
      _storage.delete(key: _accessExp),
      _storage.delete(key: _refreshExp),
    ]);
  }
}

class MemoryTokenStore implements TokenStore {
  AuthTokens? value;

  @override
  Future<AuthTokens?> read() async => value;

  @override
  Future<void> write(AuthTokens tokens) async {
    value = tokens;
  }

  @override
  Future<void> clear() async {
    value = null;
  }
}

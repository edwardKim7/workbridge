import 'dart:async';
import 'dart:convert';

import 'package:http/http.dart' as http;

import '../models/api_exception.dart';
import '../models/auth_tokens.dart';
import 'token_store.dart';

typedef RefreshCallback = Future<AuthTokens> Function(String refreshToken);

class ApiClient {
  ApiClient({
    required this.baseUrl,
    required this.tokenStore,
    http.Client? client,
    this.refreshCallback,
  }) : _client = client ?? http.Client();

  final String baseUrl;
  final TokenStore tokenStore;
  final http.Client _client;

  RefreshCallback? refreshCallback;
  Future<bool>? _refreshFuture;
  final _expired = StreamController<void>.broadcast();

  Stream<void> get sessionExpired => _expired.stream;

  Future<Map<String, dynamic>> get(String path) {
    return _send('GET', path);
  }

  Future<Map<String, dynamic>> post(
    String path, {
    Object? body,
    bool authenticated = true,
  }) {
    return _send(
      'POST',
      path,
      body: body,
      authenticated: authenticated,
    );
  }

  Future<Map<String, dynamic>> put(
    String path, {
    Object? body,
  }) {
    return _send('PUT', path, body: body);
  }

  Future<Map<String, dynamic>> _send(
    String method,
    String path, {
    Object? body,
    bool authenticated = true,
    bool retried = false,
  }) async {
    final tokens = authenticated ? await tokenStore.read() : null;
    final headers = <String, String>{
      'Accept': 'application/json',
      'Content-Type': 'application/json',
    };

    if (tokens != null) {
      headers['Authorization'] = 'Bearer ${tokens.accessToken}';
    }

    http.Response response;
    try {
      final uri = Uri.parse('$baseUrl$path');
      final encodedBody = body == null ? null : jsonEncode(body);
      final request = switch (method) {
        'POST' => _client.post(uri, headers: headers, body: encodedBody),
        'PUT' => _client.put(uri, headers: headers, body: encodedBody),
        _ => _client.get(uri, headers: headers),
      };
      response = await request.timeout(const Duration(seconds: 10));
    } on TimeoutException {
      throw const ApiException(
        '서버 응답 시간이 초과되었습니다.',
        code: 'NETWORK_TIMEOUT',
      );
    } on http.ClientException {
      throw const ApiException(
        '서버에 연결할 수 없습니다.',
        code: 'NETWORK_ERROR',
      );
    }

    if (response.statusCode == 401 &&
        authenticated &&
        !retried &&
        await _refresh()) {
      return _send(
        method,
        path,
        body: body,
        authenticated: true,
        retried: true,
      );
    }

    final payload = _decode(response.body);
    if (response.statusCode < 200 || response.statusCode >= 300) {
      throw ApiException(
        payload['message']?.toString() ?? '요청 처리에 실패했습니다.',
        status: response.statusCode,
        code: payload['code']?.toString(),
        traceId: payload['traceId']?.toString(),
      );
    }
    return payload;
  }

  Future<bool> _refresh() {
    final running = _refreshFuture;
    if (running != null) {
      return running;
    }

    final future = _performRefresh();
    _refreshFuture = future;
    return future.whenComplete(() => _refreshFuture = null);
  }

  Future<bool> _performRefresh() async {
    final current = await tokenStore.read();
    if (current == null || refreshCallback == null) {
      await _expire();
      return false;
    }

    try {
      final refreshed = await refreshCallback!(current.refreshToken);
      await tokenStore.write(refreshed);
      return true;
    } catch (_) {
      await _expire();
      return false;
    }
  }

  Future<void> _expire() async {
    await tokenStore.clear();
    _expired.add(null);
  }

  Map<String, dynamic> _decode(String text) {
    if (text.isEmpty) {
      return <String, dynamic>{};
    }

    final value = jsonDecode(text);
    if (value is! Map<String, dynamic>) {
      throw const ApiException(
        '서버 응답 형식을 해석할 수 없습니다.',
        code: 'INVALID_RESPONSE',
      );
    }
    return value;
  }

  void dispose() {
    _client.close();
    _expired.close();
  }
}

class AuthTokens {
  const AuthTokens({
    required this.accessToken,
    required this.refreshToken,
    required this.accessExpiresAt,
    required this.refreshExpiresAt,
  });

  final String accessToken;
  final String refreshToken;
  final DateTime accessExpiresAt;
  final DateTime refreshExpiresAt;

  factory AuthTokens.fromJson(Map<String, dynamic> json) {
    return AuthTokens(
      accessToken: json['accessToken'] as String,
      refreshToken: json['refreshToken'] as String,
      accessExpiresAt: DateTime.parse(json['accessExpiresAt'] as String),
      refreshExpiresAt: DateTime.parse(json['refreshExpiresAt'] as String),
    );
  }
}

class AuthUser {
  const AuthUser({
    required this.employeeId,
    required this.employeeNo,
    required this.employeeName,
    required this.roles,
  });

  final int employeeId;
  final String employeeNo;
  final String employeeName;
  final List<String> roles;

  factory AuthUser.fromJson(Map<String, dynamic> json) {
    return AuthUser(
      employeeId: (json['employeeId'] as num).toInt(),
      employeeNo: json['employeeNo'] as String,
      employeeName: json['employeeName'] as String,
      roles: (json['roles'] as List<dynamic>? ?? const [])
          .map((role) => role.toString())
          .toList(),
    );
  }
}

class LoginResult {
  const LoginResult(this.tokens, this.user);

  final AuthTokens tokens;
  final AuthUser user;

  factory LoginResult.fromJson(Map<String, dynamic> json) {
    return LoginResult(
      AuthTokens.fromJson(json),
      AuthUser.fromJson(json),
    );
  }
}

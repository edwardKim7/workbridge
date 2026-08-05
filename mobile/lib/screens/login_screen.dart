import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../state/session_controller.dart';

class LoginScreen extends StatefulWidget {
  const LoginScreen({super.key});

  @override
  State<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> {
  final loginIdController = TextEditingController();
  final passwordController = TextEditingController();

  @override
  void dispose() {
    loginIdController.dispose();
    passwordController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final session = context.watch<SessionController>();

    return Scaffold(
      body: SafeArea(
        child: Center(
          child: SingleChildScrollView(
            padding: const EdgeInsets.all(28),
            child: ConstrainedBox(
              constraints: const BoxConstraints(maxWidth: 420),
              child: Card(
                child: Padding(
                  padding: const EdgeInsets.all(24),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.stretch,
                    children: [
                      const Icon(
                        Icons.approval,
                        size: 54,
                        color: Color(0xff1f5f9f),
                      ),
                      const SizedBox(height: 12),
                      Text(
                        'WorkBridge',
                        textAlign: TextAlign.center,
                        style: Theme.of(context)
                            .textTheme
                            .headlineMedium
                            ?.copyWith(fontWeight: FontWeight.bold),
                      ),
                      const Text('모바일 결재', textAlign: TextAlign.center),
                      const SizedBox(height: 28),
                      TextField(
                        controller: loginIdController,
                        decoration: const InputDecoration(
                          labelText: '로그인 ID',
                        ),
                      ),
                      const SizedBox(height: 12),
                      TextField(
                        controller: passwordController,
                        obscureText: true,
                        decoration: const InputDecoration(
                          labelText: '비밀번호',
                        ),
                      ),
                      if (session.error != null)
                        Padding(
                          padding: const EdgeInsets.only(top: 12),
                          child: Text(
                            session.error!,
                            style: const TextStyle(color: Colors.red),
                          ),
                        ),
                      const SizedBox(height: 18),
                      FilledButton(
                        onPressed: session.busy
                            ? null
                            : () => session.login(
                                  loginIdController.text,
                                  passwordController.text,
                                ),
                        child: session.busy
                            ? const SizedBox(
                                width: 20,
                                height: 20,
                                child: CircularProgressIndicator(
                                  strokeWidth: 2,
                                ),
                              )
                            : const Text('로그인'),
                      ),
                    ],
                  ),
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }
}

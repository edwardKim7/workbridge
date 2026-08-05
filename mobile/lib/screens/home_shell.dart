import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../state/session_controller.dart';
import 'approval_list_screen.dart';
import 'dashboard_screen.dart';
import 'history_screen.dart';
import 'opportunity_list_screen.dart';

class HomeShell extends StatefulWidget {
  const HomeShell({super.key});

  @override
  State<HomeShell> createState() => _HomeShellState();
}

class _HomeShellState extends State<HomeShell> {
  int index = 0;

  final pages = const [
    MobileDashboardScreen(),
    ApprovalListScreen(),
    OpportunityListScreen(),
    HistoryScreen(),
  ];

  static const titles = ['대시보드', '결재 대기', '영업기회', '내 처리 이력'];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(titles[index]),
        actions: [
          IconButton(
            onPressed: () => context.read<SessionController>().logout(),
            icon: const Icon(Icons.logout),
            tooltip: '로그아웃',
          ),
        ],
      ),
      body: IndexedStack(index: index, children: pages),
      bottomNavigationBar: NavigationBar(
        selectedIndex: index,
        onDestinationSelected: (value) => setState(() => index = value),
        destinations: const [
          NavigationDestination(icon: Icon(Icons.dashboard_outlined), label: '홈'),
          NavigationDestination(icon: Icon(Icons.approval), label: '결재'),
          NavigationDestination(icon: Icon(Icons.trending_up), label: '기회'),
          NavigationDestination(icon: Icon(Icons.history), label: '이력'),
        ],
      ),
    );
  }
}

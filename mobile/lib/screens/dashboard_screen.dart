import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../state/approval_controller.dart';
import '../state/session_controller.dart';
import '../widgets/state_views.dart';
import '../widgets/status_badge.dart';
import 'approval_detail_screen.dart';

class MobileDashboardScreen extends StatefulWidget {
  const MobileDashboardScreen({super.key});

  @override
  State<MobileDashboardScreen> createState() => _MobileDashboardScreenState();
}

class _MobileDashboardScreenState extends State<MobileDashboardScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      final controller = context.read<ApprovalController>();
      if (controller.items.isEmpty && !controller.loading) {
        controller.loadPending();
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    final session = context.watch<SessionController>();
    final approvals = context.watch<ApprovalController>();

    return RefreshIndicator(
      onRefresh: approvals.loadPending,
      child: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          Text(
            '${session.user?.employeeName ?? '승인자'}님, 확인할 업무입니다.',
            style: Theme.of(context).textTheme.titleLarge?.copyWith(
                  fontWeight: FontWeight.bold,
                ),
          ),
          const SizedBox(height: 14),
          Card(
            child: Padding(
              padding: const EdgeInsets.all(18),
              child: Row(
                children: [
                  const CircleAvatar(
                    child: Icon(Icons.pending_actions),
                  ),
                  const SizedBox(width: 14),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        const Text('결재 대기'),
                        Text(
                          '${approvals.items.length}건',
                          style: Theme.of(context).textTheme.headlineSmall,
                        ),
                      ],
                    ),
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 18),
          Text('최근 결재 대기', style: Theme.of(context).textTheme.titleMedium),
          const SizedBox(height: 8),
          if (approvals.loading && approvals.items.isEmpty)
            const SizedBox(height: 180, child: LoadingView())
          else if (approvals.error != null && approvals.items.isEmpty)
            SizedBox(
              height: 220,
              child: ErrorView(approvals.error!, onRetry: approvals.loadPending),
            )
          else if (approvals.items.isEmpty)
            const SizedBox(height: 160, child: EmptyView('결재 대기 항목이 없습니다.'))
          else
            ...approvals.items.take(3).map(
                  (approval) => Card(
                    child: ListTile(
                      title: Text(approval.title),
                      subtitle: Text('${approval.no} · ${approval.requesterName}'),
                      trailing: StatusBadge(approval.status),
                      onTap: () => Navigator.push(
                        context,
                        MaterialPageRoute(
                          builder: (_) => ApprovalDetailScreen(id: approval.id),
                        ),
                      ),
                    ),
                  ),
                ),
        ],
      ),
    );
  }
}

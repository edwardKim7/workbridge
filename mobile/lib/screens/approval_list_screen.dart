import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../state/approval_controller.dart';
import '../widgets/state_views.dart';
import '../widgets/status_badge.dart';
import 'approval_detail_screen.dart';

class ApprovalListScreen extends StatefulWidget {
  const ApprovalListScreen({super.key});

  @override
  State<ApprovalListScreen> createState() => _ApprovalListScreenState();
}

class _ApprovalListScreenState extends State<ApprovalListScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<ApprovalController>().loadPending();
    });
  }

  @override
  Widget build(BuildContext context) {
    final state = context.watch<ApprovalController>();

    if (state.loading && state.items.isEmpty) {
      return const LoadingView();
    }
    if (state.error != null && state.items.isEmpty) {
      return ErrorView(state.error!, onRetry: state.loadPending);
    }

    return RefreshIndicator(
      onRefresh: state.loadPending,
      child: state.items.isEmpty
          ? ListView(
              children: const [
                SizedBox(height: 180),
                EmptyView('결재 대기 항목이 없습니다.'),
              ],
            )
          : ListView.separated(
              padding: const EdgeInsets.all(16),
              itemCount: state.items.length,
              separatorBuilder: (_, __) => const SizedBox(height: 10),
              itemBuilder: (context, index) {
                final approval = state.items[index];
                return Card(
                  child: ListTile(
                    title: Text(approval.title),
                    subtitle: Text(
                      '${approval.no}\n${approval.requesterName} → ${approval.approverName}',
                    ),
                    isThreeLine: true,
                    trailing: StatusBadge(approval.status),
                    onTap: () => Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (_) => ApprovalDetailScreen(id: approval.id),
                      ),
                    ),
                  ),
                );
              },
            ),
    );
  }
}

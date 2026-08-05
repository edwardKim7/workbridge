import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../models/approval.dart';
import '../services/approval_service.dart';
import '../widgets/state_views.dart';
import '../widgets/status_badge.dart';
import 'approval_detail_screen.dart';

class HistoryScreen extends StatelessWidget {
  const HistoryScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return FutureBuilder<List<ApprovalSummary>>(
      future: context.read<ApprovalService>().history(),
      builder: (context, snapshot) {
        if (snapshot.connectionState != ConnectionState.done) {
          return const LoadingView();
        }
        if (snapshot.hasError) {
          return ErrorView(snapshot.error.toString());
        }

        final items = snapshot.data!;
        if (items.isEmpty) {
          return const EmptyView('처리 이력이 없습니다.');
        }

        return ListView.builder(
          padding: const EdgeInsets.all(16),
          itemCount: items.length,
          itemBuilder: (context, index) {
            final approval = items[index];
            return Card(
              child: ListTile(
                title: Text(approval.title),
                subtitle: Text(approval.no),
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
        );
      },
    );
  }
}

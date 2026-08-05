import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../models/approval.dart';
import '../services/approval_service.dart';
import '../state/approval_controller.dart';
import '../widgets/state_views.dart';
import '../widgets/status_badge.dart';

class ApprovalDetailScreen extends StatefulWidget {
  const ApprovalDetailScreen({super.key, required this.id});

  final int id;

  @override
  State<ApprovalDetailScreen> createState() => _ApprovalDetailScreenState();
}

class _ApprovalDetailScreenState extends State<ApprovalDetailScreen> {
  late Future<ApprovalDetail> future;
  final opinionController = TextEditingController();

  @override
  void initState() {
    super.initState();
    future = context.read<ApprovalService>().detail(widget.id);
  }

  @override
  void dispose() {
    opinionController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('결재 상세')),
      body: FutureBuilder<ApprovalDetail>(
        future: future,
        builder: (context, snapshot) {
          if (snapshot.connectionState != ConnectionState.done) {
            return const LoadingView();
          }
          if (snapshot.hasError) {
            return ErrorView(
              snapshot.error.toString(),
              onRetry: () {
                setState(() {
                  future = context.read<ApprovalService>().detail(widget.id);
                });
              },
            );
          }

          final detail = snapshot.data!;
          return ListView(
            padding: const EdgeInsets.all(16),
            children: [
              Card(
                child: Padding(
                  padding: const EdgeInsets.all(18),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        children: [
                          Expanded(
                            child: Text(
                              detail.approval.title,
                              style: Theme.of(context).textTheme.titleLarge,
                            ),
                          ),
                          StatusBadge(detail.approval.status),
                        ],
                      ),
                      const SizedBox(height: 12),
                      Text(detail.approval.no),
                      Text(
                        '요청자 ${detail.approval.requesterName} · '
                        '승인자 ${detail.approval.approverName}',
                      ),
                      const SizedBox(height: 18),
                      TextField(
                        controller: opinionController,
                        maxLines: 3,
                        decoration: const InputDecoration(
                          labelText: '승인·반려 의견',
                        ),
                      ),
                      const SizedBox(height: 12),
                      Consumer<ApprovalController>(
                        builder: (context, controller, _) {
                          if (!detail.canApprove && !detail.canReject) {
                            return const SizedBox.shrink();
                          }
                          return Row(
                            children: [
                              Expanded(
                                child: FilledButton(
                                  onPressed: controller.processing
                                      ? null
                                      : () => _process(
                                            context,
                                            detail,
                                            'approve',
                                          ),
                                  child: const Text('승인'),
                                ),
                              ),
                              const SizedBox(width: 10),
                              Expanded(
                                child: OutlinedButton(
                                  onPressed: controller.processing
                                      ? null
                                      : () => _process(
                                            context,
                                            detail,
                                            'reject',
                                          ),
                                  child: const Text('반려'),
                                ),
                              ),
                            ],
                          );
                        },
                      ),
                    ],
                  ),
                ),
              ),
              const SizedBox(height: 12),
              Text('처리 이력', style: Theme.of(context).textTheme.titleMedium),
              ...detail.histories.map(
                (history) => ListTile(
                  contentPadding: EdgeInsets.zero,
                  leading: const Icon(Icons.history),
                  title: Text('${history.action} · ${history.actorName}'),
                  subtitle: Text(history.opinion ?? '의견 없음'),
                  trailing: Text(
                    '${history.actedAt.month}/${history.actedAt.day}',
                  ),
                ),
              ),
            ],
          );
        },
      ),
    );
  }

  Future<void> _process(
    BuildContext context,
    ApprovalDetail detail,
    String action,
  ) async {
    final opinion = opinionController.text.trim();
    final result = await context.read<ApprovalController>().process(
          detail,
          action,
          opinion.isEmpty ? null : opinion,
        );

    if (!mounted) {
      return;
    }
    if (result == null) {
      final message = context.read<ApprovalController>().error ?? '처리 실패';
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text(message)),
      );
      return;
    }
    Navigator.pop(context);
  }
}

import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../models/opportunity.dart';
import '../services/opportunity_service.dart';
import '../widgets/state_views.dart';

class OpportunityListScreen extends StatefulWidget {
  const OpportunityListScreen({super.key});

  @override
  State<OpportunityListScreen> createState() => _OpportunityListScreenState();
}

class _OpportunityListScreenState extends State<OpportunityListScreen> {
  late Future<List<Opportunity>> future;

  @override
  void initState() {
    super.initState();
    future = context.read<OpportunityService>().list();
  }

  Future<void> reload() async {
    setState(() {
      future = context.read<OpportunityService>().list();
    });
    await future;
  }

  @override
  Widget build(BuildContext context) {
    return FutureBuilder<List<Opportunity>>(
      future: future,
      builder: (context, snapshot) {
        if (snapshot.connectionState != ConnectionState.done) {
          return const LoadingView();
        }
        if (snapshot.hasError) {
          return ErrorView(snapshot.error.toString(), onRetry: reload);
        }

        final items = snapshot.data!;
        return RefreshIndicator(
          onRefresh: reload,
          child: items.isEmpty
              ? ListView(
                  children: const [
                    SizedBox(height: 180),
                    EmptyView('영업기회가 없습니다.'),
                  ],
                )
              : ListView.builder(
                  padding: const EdgeInsets.all(16),
                  itemCount: items.length,
                  itemBuilder: (context, index) {
                    final opportunity = items[index];
                    return Card(
                      child: ListTile(
                        title: Text(opportunity.name),
                        subtitle: Text(
                          '${opportunity.no} · ${opportunity.customerName}',
                        ),
                        trailing: Column(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            Text(opportunity.stage),
                            Text(
                              '${opportunity.expectedRevenue}원',
                              style: const TextStyle(fontSize: 11),
                            ),
                          ],
                        ),
                      ),
                    );
                  },
                ),
        );
      },
    );
  }
}

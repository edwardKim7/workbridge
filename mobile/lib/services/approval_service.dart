import '../models/approval.dart';
import 'api_client.dart';

class ApprovalService {
  ApprovalService(this.api);

  final ApiClient api;

  Future<List<ApprovalSummary>> pending() async {
    final payload = await api.get(
      '/api/v1/approvals?view=MY_APPROVALS&status=REQUESTED&size=100',
    );
    final data = payload['data'] as Map<String, dynamic>;
    return (data['items'] as List<dynamic>)
        .map((item) => ApprovalSummary.fromJson(item as Map<String, dynamic>))
        .toList();
  }

  Future<List<ApprovalSummary>> history() async {
    final payload = await api.get(
      '/api/v1/approvals?view=MY_APPROVALS&size=100',
    );
    final data = payload['data'] as Map<String, dynamic>;
    return (data['items'] as List<dynamic>)
        .map((item) => ApprovalSummary.fromJson(item as Map<String, dynamic>))
        .where((approval) => approval.status != 'REQUESTED')
        .toList();
  }

  Future<ApprovalDetail> detail(int id) async {
    final payload = await api.get('/api/v1/approvals/$id');
    return ApprovalDetail.fromJson(
      payload['data'] as Map<String, dynamic>,
    );
  }

  Future<ApprovalDetail> process(
    int id,
    int version,
    String action,
    String? opinion,
  ) async {
    final payload = await api.post(
      '/api/v1/approvals/$id/$action',
      body: {
        'version': version,
        'opinion': opinion,
      },
    );
    return ApprovalDetail.fromJson(
      payload['data'] as Map<String, dynamic>,
    );
  }
}

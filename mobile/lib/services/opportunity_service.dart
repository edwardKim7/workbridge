import '../models/opportunity.dart';
import 'api_client.dart';

class OpportunityService {
  OpportunityService(this.api);

  final ApiClient api;

  Future<List<Opportunity>> list() async {
    final payload = await api.get('/api/v1/opportunities?size=50');
    final data = payload['data'] as Map<String, dynamic>;
    return (data['items'] as List<dynamic>)
        .map((item) => Opportunity.fromJson(item as Map<String, dynamic>))
        .toList();
  }
}

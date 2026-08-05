class Opportunity {
  const Opportunity({
    required this.id,
    required this.no,
    required this.name,
    required this.customerName,
    required this.stage,
    required this.expectedRevenue,
  });

  final int id;
  final String no;
  final String name;
  final String customerName;
  final String stage;
  final num expectedRevenue;

  factory Opportunity.fromJson(Map<String, dynamic> json) {
    return Opportunity(
      id: (json['opportunityId'] as num).toInt(),
      no: json['opportunityNo'] as String,
      name: json['opportunityName'] as String,
      customerName: json['customerName'] as String,
      stage: json['stage'] as String,
      expectedRevenue: json['expectedRevenue'] as num,
    );
  }
}

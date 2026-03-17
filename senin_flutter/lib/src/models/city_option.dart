class CityOption {
  const CityOption({
    required this.id,
    required this.name,
    required this.region,
    required this.country,
    required this.countryCode,
    required this.latitude,
    required this.longitude,
    this.isDefault = false,
  });

  final String id;
  final String name;
  final String region;
  final String country;
  final String countryCode;
  final double latitude;
  final double longitude;
  final bool isDefault;

  String get subtitle {
    final normalizedRegion = region
        .replaceFirst(RegExp(r'^Județul\s+', caseSensitive: false), '')
        .replaceFirst(RegExp(r'^Judetul\s+', caseSensitive: false), '')
        .trim();

    if (countryCode.toUpperCase() == 'RO') {
      return normalizedRegion.isNotEmpty ? normalizedRegion : country;
    }

    if (normalizedRegion.isEmpty ||
        normalizedRegion.toLowerCase() == country.toLowerCase()) {
      return country;
    }

    return '$normalizedRegion, $country';
  }

  Map<String, dynamic> toJson() => <String, dynamic>{
        'id': id,
        'name': name,
        'region': region,
        'country': country,
        'countryCode': countryCode,
        'latitude': latitude,
        'longitude': longitude,
        'isDefault': isDefault,
      };

  factory CityOption.fromJson(Map<String, dynamic> json) => CityOption(
        id: json['id'] as String,
        name: json['name'] as String,
        region: json['region'] as String? ?? '',
        country: json['country'] as String? ?? '',
        countryCode: json['countryCode'] as String? ?? '',
        latitude: (json['latitude'] as num).toDouble(),
        longitude: (json['longitude'] as num).toDouble(),
        isDefault: json['isDefault'] as bool? ?? false,
      );
}

import '../models/city_option.dart';
import '../models/weather_models.dart';

abstract class WeatherRepository {
  CityOption defaultCity();
  List<CityOption> favoriteCities();
  Future<List<CityOption>> searchCities(String query);
  Future<WeatherOverview> weatherFor(CityOption city);
}

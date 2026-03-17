import 'dart:async';

import 'package:flutter/material.dart';

import 'data/favorite_cities_store.dart';
import 'data/open_meteo_weather_repository.dart';
import 'data/selected_city_store.dart';
import 'data/weather_repository.dart';
import 'models/city_option.dart';
import 'models/weather_models.dart';
import 'home_widgets.dart';

class HomePage extends StatefulWidget {
  const HomePage({super.key});

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  final WeatherRepository _repository = OpenMeteoWeatherRepository();
  final SelectedCityStore _selectedCityStore = const SelectedCityStore();
  final FavoriteCitiesStore _favoriteCitiesStore = const FavoriteCitiesStore();
  final TextEditingController _searchController = TextEditingController();

  late CityOption _selectedCity;
  List<CityOption> _favoriteCities = <CityOption>[];
  List<CityOption> _suggestions = <CityOption>[];
  WeatherOverview? _weather;
  bool _isLoading = true;
  bool _isSearching = false;
  String? _errorMessage;
  String? _searchStatusMessage;

  @override
  void initState() {
    super.initState();
    _selectedCity = _repository.defaultCity();
    unawaited(_initialize());
  }

  @override
  void dispose() {
    _searchController.dispose();
    super.dispose();
  }

  Future<void> _initialize() async {
    final storedFavorites = await _favoriteCitiesStore.load();
    final hasStoredFavorites = await _favoriteCitiesStore.hasStoredFavorites();
    final initialFavorites = _normalizeFavorites(
      hasStoredFavorites ? storedFavorites : _repository.favoriteCities(),
    );
    final storedCity = await _selectedCityStore.load();

    if (!hasStoredFavorites) {
      await _favoriteCitiesStore.save(initialFavorites);
    }

    setState(() {
      _favoriteCities = initialFavorites;
      _suggestions = initialFavorites;
      _selectedCity = storedCity ?? _repository.defaultCity();
    });

    await _refreshWeather(_selectedCity, preserveCurrentWeather: false);
  }

  List<CityOption> _normalizeFavorites(List<CityOption> cities) {
    final seen = <String>{};
    final distinct = cities.where((city) => seen.add(city.id)).toList();
    final defaults = distinct.where((city) => city.isDefault).toList();
    final rest = distinct.where((city) => !city.isDefault).toList();
    return <CityOption>[...defaults, ...rest];
  }

  Future<void> _refreshWeather(
    CityOption city, {
    required bool preserveCurrentWeather,
  }) async {
    setState(() {
      _isLoading = true;
      _errorMessage = null;
      if (!preserveCurrentWeather) {
        _weather = null;
      }
    });

    try {
      final weather = await _repository.weatherFor(city);
      await _selectedCityStore.save(city);
      if (!mounted) return;
      setState(() {
        _selectedCity = city;
        _weather = weather;
        _isLoading = false;
      });
    } catch (error) {
      if (!mounted) return;
      setState(() {
        _isLoading = false;
        _errorMessage = error.toString().replaceFirst('Exception: ', '');
      });
    }
  }

  Future<void> _toggleFavorite(CityOption city) async {
    final isFavorite = _favoriteCities.any((item) => item.id == city.id);
    final updated = isFavorite
        ? _favoriteCities.where((item) => item.id != city.id).toList()
        : _normalizeFavorites(<CityOption>[city, ..._favoriteCities]);

    await _favoriteCitiesStore.save(updated);
    if (!mounted) return;
    setState(() {
      _favoriteCities = updated;
      _suggestions = _searchController.text.trim().isEmpty ? updated : _suggestions;
      _searchStatusMessage = isFavorite
          ? '${city.name} a fost scos din favorite.'
          : '${city.name} a fost adăugat la favorite.';
    });
  }

  Future<void> _searchCities(String value) async {
    final query = value.trim();
    if (query.isEmpty) {
      setState(() {
        _suggestions = _favoriteCities;
        _searchStatusMessage = _favoriteCities.isEmpty
            ? 'Adaugă orașe la favorite pentru acces rapid.'
            : null;
        _isSearching = false;
      });
      return;
    }

    if (query.length < 2) {
      final local = _favoriteCities.where((city) {
        return city.name.toLowerCase().contains(query.toLowerCase()) ||
            city.region.toLowerCase().contains(query.toLowerCase()) ||
            city.country.toLowerCase().contains(query.toLowerCase());
      }).toList();
      setState(() {
        _suggestions = local;
        _searchStatusMessage =
            local.isEmpty ? 'Scrie măcar 2 litere pentru căutare live.' : null;
        _isSearching = false;
      });
      return;
    }

    setState(() {
      _isSearching = true;
      _searchStatusMessage = null;
    });

    try {
      final results = await _repository.searchCities(query);
      if (!mounted || _searchController.text.trim() != query) return;
      setState(() {
        _suggestions = results;
        _isSearching = false;
        _searchStatusMessage = results.isEmpty
            ? 'Nu am găsit niciun oraș pentru "$query".'
            : null;
      });
    } catch (_) {
      if (!mounted) return;
      setState(() {
        _suggestions = _favoriteCities;
        _isSearching = false;
        _searchStatusMessage = 'Momentan îți arăt favoritele salvate local.';
      });
    }
  }

  Future<void> _openCityPicker() async {
    _searchController.clear();
    setState(() {
      _suggestions = _favoriteCities;
      _searchStatusMessage = _favoriteCities.isEmpty
          ? 'Adaugă orașe la favorite pentru acces rapid.'
          : null;
    });

    await showModalBottomSheet<void>(
      context: context,
      isScrollControlled: true,
      showDragHandle: true,
      builder: (context) {
        return StatefulBuilder(
          builder: (context, setModalState) {
            return FractionallySizedBox(
              heightFactor: 0.90,
              child: SafeArea(
                child: Padding(
                  padding: EdgeInsets.only(
                    left: 20,
                    right: 20,
                    top: 8,
                    bottom: MediaQuery.of(context).viewInsets.bottom + 20,
                  ),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const Text(
                        'Schimbă orașul',
                        style: TextStyle(fontSize: 24, fontWeight: FontWeight.w700),
                      ),
                      const SizedBox(height: 6),
                      const Text(
                        'România mai întâi, apoi orice oraș important din lume.',
                      ),
                      const SizedBox(height: 16),
                      TextField(
                        controller: _searchController,
                        decoration: const InputDecoration(
                          labelText: 'Caută oraș',
                          hintText: 'Oradea, Cluj-Napoca, London, Tokyo...',
                          prefixIcon: Icon(Icons.search),
                        ),
                        onChanged: (value) async {
                          await _searchCities(value);
                          setModalState(() {});
                        },
                      ),
                      const SizedBox(height: 16),
                      if (_searchStatusMessage != null) ...[
                        Text(_searchStatusMessage!),
                        const SizedBox(height: 12),
                      ],
                      if (_favoriteCities.isNotEmpty) ...[
                        const Text(
                          'Favorite rapide',
                          style: TextStyle(fontSize: 18, fontWeight: FontWeight.w600),
                        ),
                        const SizedBox(height: 12),
                        Wrap(
                          spacing: 8,
                          runSpacing: 8,
                          children: _favoriteCities.map((city) {
                            return ActionChip(
                              label: Text(city.name),
                              onPressed: () async {
                                Navigator.of(context).pop();
                                await _refreshWeather(city, preserveCurrentWeather: true);
                              },
                            );
                          }).toList(),
                        ),
                        const SizedBox(height: 16),
                      ],
                      const Text(
                        'Rezultate',
                        style: TextStyle(fontSize: 18, fontWeight: FontWeight.w600),
                      ),
                      const SizedBox(height: 12),
                      Expanded(
                        child: _isSearching
                            ? const Center(child: CircularProgressIndicator())
                            : ListView.separated(
                                padding: const EdgeInsets.only(bottom: 4),
                                itemCount: _suggestions.length,
                                separatorBuilder: (_, _) => const SizedBox(height: 10),
                                itemBuilder: (context, index) {
                                  final city = _suggestions[index];
                                  final isFavorite =
                                      _favoriteCities.any((item) => item.id == city.id);
                                  return Card(
                                    child: ListTile(
                                      title: Text(city.name),
                                      subtitle: Text(city.subtitle),
                                      trailing: IconButton(
                                        onPressed: () async {
                                          await _toggleFavorite(city);
                                          setModalState(() {});
                                        },
                                        icon: Icon(
                                          isFavorite ? Icons.favorite : Icons.favorite_border,
                                          color: isFavorite ? const Color(0xFF2C93CB) : null,
                                        ),
                                      ),
                                      onTap: () async {
                                        Navigator.of(context).pop();
                                        await _refreshWeather(
                                          city,
                                          preserveCurrentWeather: true,
                                        );
                                      },
                                    ),
                                  );
                                },
                              ),
                      ),
                    ],
                  ),
                ),
              ),
            );
          },
        );
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: DecoratedBox(
        decoration: const BoxDecoration(
          gradient: LinearGradient(
            begin: Alignment.topCenter,
            end: Alignment.bottomCenter,
            colors: [
              Color(0xFFFFC57B),
              Color(0xFFFF8E63),
              Color(0xFF1E5877),
              Color(0xFF0D1B2A),
            ],
          ),
        ),
        child: SafeArea(
          child: RefreshIndicator(
            onRefresh: () => _refreshWeather(_selectedCity, preserveCurrentWeather: true),
            child: ListView(
              padding: const EdgeInsets.fromLTRB(20, 12, 20, 28),
              children: [
                HeaderSection(
                  selectedCity: _selectedCity,
                  onPickCity: _openCityPicker,
                ),
                const SizedBox(height: 18),
                HeroCardSection(
                  city: _selectedCity,
                  weather: _weather,
                  isLoading: _isLoading,
                  errorMessage: _errorMessage,
                ),
                if (_favoriteCities.isNotEmpty) ...[
                  const SizedBox(height: 18),
                  const SectionTitle(text: 'Orașe favorite'),
                  const SizedBox(height: 12),
                  FavoriteCitiesRow(
                    favoriteCities: _favoriteCities,
                    selectedCity: _selectedCity,
                    onSelectCity: (city) => _refreshWeather(city, preserveCurrentWeather: true),
                    onToggleFavorite: _toggleFavorite,
                  ),
                ],
                if (_weather != null) ...[
                  const SizedBox(height: 18),
                  MetricsRow(weather: _weather!),
                  const SizedBox(height: 18),
                  const SectionTitle(text: 'Ritmul de azi'),
                  const SizedBox(height: 12),
                  HourlySection(hourly: _weather!.hourly),
                  const SizedBox(height: 18),
                  const SectionTitle(text: 'Următoarele zile'),
                  const SizedBox(height: 12),
                  ..._weather!.daily.map((day) => DailyCard(day: day)),
                  const SizedBox(height: 8),
                  Text(
                    _weather!.sourceLabel,
                    style: const TextStyle(color: Colors.white70),
                  ),
                ],
              ],
            ),
          ),
        ),
      ),
    );
  }
}

class HeaderSection extends StatelessWidget {
  const HeaderSection({
    super.key,
    required this.selectedCity,
    required this.onPickCity,
  });

  final CityOption selectedCity;
  final Future<void> Function() onPickCity;

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Text(
          'Senin',
          style: TextStyle(
            color: Color(0xFF173046),
            fontSize: 24,
            fontWeight: FontWeight.w700,
          ),
        ),
        const SizedBox(height: 6),
        Text(
          'Acum în ${selectedCity.name}',
          style: const TextStyle(
            color: Color(0xFF173046),
            fontSize: 16,
          ),
        ),
        const SizedBox(height: 14),
        FilledButton.icon(
          onPressed: onPickCity,
          icon: const Icon(Icons.search),
          label: const Text('Schimbă orașul'),
          style: FilledButton.styleFrom(
            backgroundColor: const Color(0xFF14344D).withValues(alpha: 0.28),
            foregroundColor: Colors.white,
            minimumSize: const Size.fromHeight(54),
          ),
        ),
      ],
    );
  }
}


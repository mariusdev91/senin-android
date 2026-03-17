import 'package:flutter/material.dart';

import 'dart:ui' show PointerDeviceKind;

import 'models/city_option.dart';
import 'models/weather_models.dart';
import 'models/weather_ui.dart';

class HeroCardSection extends StatelessWidget {
  const HeroCardSection({
    super.key,
    required this.city,
    required this.weather,
    required this.isLoading,
    required this.errorMessage,
  });

  final CityOption city;
  final WeatherOverview? weather;
  final bool isLoading;
  final String? errorMessage;

  @override
  Widget build(BuildContext context) {
    return Card(
      color: const Color(0xFFF5EBDD),
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(28)),
      child: Padding(
        padding: const EdgeInsets.all(22),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        city.isDefault ? 'Oradea implicit' : city.name,
                        style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                              color: const Color(0xFF173046),
                              fontWeight: FontWeight.w700,
                            ),
                      ),
                      const SizedBox(height: 6),
                      Text(
                        city.subtitle,
                        style: const TextStyle(color: Color(0xFF5C6F81), fontSize: 18),
                      ),
                      const SizedBox(height: 6),
                      Text(
                        weather?.updatedAtLabel ?? 'Se pregătesc datele live...',
                        style: const TextStyle(
                          color: Color(0xFF2C93CB),
                          fontWeight: FontWeight.w600,
                        ),
                      ),
                    ],
                  ),
                ),
                Container(
                  width: 92,
                  height: 92,
                  decoration: BoxDecoration(
                    color: const Color(0xFFDCEFF9),
                    borderRadius: BorderRadius.circular(24),
                  ),
                  child: Icon(
                    conditionIcon(weather?.current.condition ?? WeatherCondition.clear),
                    color: const Color(0xFF2C93CB),
                    size: 34,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 18),
            if (isLoading && weather == null)
              const Center(child: CircularProgressIndicator())
            else if (weather != null) ...[
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                crossAxisAlignment: CrossAxisAlignment.end,
                children: [
                  Text(
                    '${weather!.current.temperatureC}°',
                    style: Theme.of(context).textTheme.displayMedium?.copyWith(
                          color: const Color(0xFF173046),
                          fontWeight: FontWeight.w700,
                        ),
                  ),
                  Column(
                    crossAxisAlignment: CrossAxisAlignment.end,
                    children: [
                      Text(
                        conditionLabel(weather!.current.condition),
                        style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                              color: const Color(0xFF173046),
                              fontWeight: FontWeight.w700,
                            ),
                      ),
                      const SizedBox(height: 4),
                      Text(
                        'Se simte ca ${weather!.current.feelsLikeC}°',
                        style: const TextStyle(color: Color(0xFF5C6F81)),
                      ),
                    ],
                  ),
                ],
              ),
              const SizedBox(height: 12),
              Text(
                weather!.current.summary,
                style: const TextStyle(
                  color: Color(0xFF173046),
                  fontSize: 16,
                ),
              ),
            ],
            if (errorMessage != null) ...[
              const SizedBox(height: 16),
              Text(
                errorMessage!,
                style: const TextStyle(
                  color: Colors.redAccent,
                  fontWeight: FontWeight.w600,
                ),
              ),
            ],
          ],
        ),
      ),
    );
  }
}

class FavoriteCitiesRow extends StatelessWidget {
  const FavoriteCitiesRow({
    super.key,
    required this.favoriteCities,
    required this.selectedCity,
    required this.onSelectCity,
    required this.onToggleFavorite,
  });

  final List<CityOption> favoriteCities;
  final CityOption selectedCity;
  final Future<void> Function(CityOption city) onSelectCity;
  final Future<void> Function(CityOption city) onToggleFavorite;

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      height: 132,
      child: ListView.separated(
        scrollDirection: Axis.horizontal,
        itemCount: favoriteCities.length,
        separatorBuilder: (_, _) => const SizedBox(width: 12),
        itemBuilder: (context, index) {
          final city = favoriteCities[index];
          final isSelected = city.id == selectedCity.id;
          return SizedBox(
            width: 180,
            child: Card(
              color: isSelected
                  ? const Color(0xFFF5EBDD)
                  : Colors.white.withValues(alpha: 0.16),
              child: InkWell(
                borderRadius: BorderRadius.circular(24),
                onTap: () => onSelectCity(city),
                child: Padding(
                  padding: const EdgeInsets.all(16),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        city.name,
                        style: TextStyle(
                          fontWeight: FontWeight.w700,
                          color: isSelected
                              ? const Color(0xFF173046)
                              : Colors.white,
                        ),
                      ),
                      const SizedBox(height: 4),
                      Text(
                        city.subtitle,
                        style: TextStyle(
                          color: isSelected
                              ? const Color(0xFF5C6F81)
                              : Colors.white70,
                        ),
                      ),
                      const Spacer(),
                      Align(
                        alignment: Alignment.bottomRight,
                        child: IconButton(
                          onPressed: () => onToggleFavorite(city),
                          icon: const Icon(Icons.favorite, size: 20),
                          visualDensity: VisualDensity.compact,
                          constraints: const BoxConstraints.tightFor(width: 36, height: 36),
                          padding: EdgeInsets.zero,
                          color: isSelected
                              ? const Color(0xFF2C93CB)
                              : const Color(0xFFF8D38E),
                        ),
                      ),
                    ],
                  ),
                ),
              ),
            ),
          );
        },
      ),
    );
  }
}

class MetricsRow extends StatelessWidget {
  const MetricsRow({
    super.key,
    required this.weather,
  });

  final WeatherOverview weather;

  @override
  Widget build(BuildContext context) {
    return Wrap(
      spacing: 12,
      runSpacing: 12,
      children: [
        MetricCard(label: 'Ploaie', value: '${weather.current.precipitationChance}%'),
        MetricCard(label: 'Vânt', value: '${weather.current.windKph} km/h'),
        MetricCard(label: 'Umiditate', value: '${weather.current.humidity}%'),
        MetricCard(label: 'UV', value: '${weather.current.uvIndex}'),
      ],
    );
  }
}

class MetricCard extends StatelessWidget {
  const MetricCard({
    super.key,
    required this.label,
    required this.value,
  });

  final String label;
  final String value;

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      width: 160,
      child: Card(
        color: Colors.white.withValues(alpha: 0.14),
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(label, style: const TextStyle(color: Colors.white70)),
              const SizedBox(height: 8),
              Text(
                value,
                style: const TextStyle(
                  color: Colors.white,
                  fontSize: 24,
                  fontWeight: FontWeight.w700,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class HourlySection extends StatefulWidget {
  const HourlySection({
    super.key,
    required this.hourly,
  });

  final List<HourlyForecast> hourly;

  @override
  State<HourlySection> createState() => _HourlySectionState();
}

class _HourlySectionState extends State<HourlySection> {
  late final ScrollController _scrollController;

  @override
  void initState() {
    super.initState();
    _scrollController = ScrollController();
  }

  @override
  void dispose() {
    _scrollController.dispose();
    super.dispose();
  }

  Future<void> _scrollBy(double delta) async {
    if (!_scrollController.hasClients) return;
    final position = _scrollController.position;
    final target = (_scrollController.offset + delta).clamp(
      position.minScrollExtent,
      position.maxScrollExtent,
    );

    await _scrollController.animateTo(
      target,
      duration: const Duration(milliseconds: 280),
      curve: Curves.easeOutCubic,
    );
  }

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      height: 252,
      child: Column(
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.end,
            children: [
              _HourlyScrollButton(
                icon: Icons.chevron_left_rounded,
                onPressed: () => _scrollBy(-220),
              ),
              const SizedBox(width: 8),
              _HourlyScrollButton(
                icon: Icons.chevron_right_rounded,
                onPressed: () => _scrollBy(220),
              ),
            ],
          ),
          const SizedBox(height: 10),
          Expanded(
            child: ScrollConfiguration(
              behavior: const MaterialScrollBehavior().copyWith(
                dragDevices: {
                  PointerDeviceKind.touch,
                  PointerDeviceKind.mouse,
                  PointerDeviceKind.trackpad,
                  PointerDeviceKind.stylus,
                },
              ),
              child: Scrollbar(
                controller: _scrollController,
                thumbVisibility: true,
                trackVisibility: true,
                scrollbarOrientation: ScrollbarOrientation.bottom,
                child: ListView.separated(
                  controller: _scrollController,
                  scrollDirection: Axis.horizontal,
                  padding: const EdgeInsets.only(bottom: 14),
                  itemCount: widget.hourly.length,
                  separatorBuilder: (_, _) => const SizedBox(width: 12),
                  itemBuilder: (context, index) {
                    final hour = widget.hourly[index];
                    return HourlyCard(
                      hour: hour,
                      isCurrent: hour.timeLabel == 'Acum',
                    );
                  },
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _HourlyScrollButton extends StatelessWidget {
  const _HourlyScrollButton({
    required this.icon,
    required this.onPressed,
  });

  final IconData icon;
  final VoidCallback onPressed;

  @override
  Widget build(BuildContext context) {
    return Material(
      color: Colors.white.withValues(alpha: 0.12),
      borderRadius: BorderRadius.circular(14),
      child: InkWell(
        borderRadius: BorderRadius.circular(14),
        onTap: onPressed,
        child: SizedBox(
          width: 42,
          height: 42,
          child: Icon(icon, color: Colors.white),
        ),
      ),
    );
  }
}

class HourlyCard extends StatelessWidget {
  const HourlyCard({
    super.key,
    required this.hour,
    required this.isCurrent,
  });

  final HourlyForecast hour;
  final bool isCurrent;

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      width: isCurrent ? 156 : 132,
      child: Card(
        color: isCurrent
            ? const Color(0xFFF5EBDD)
            : Colors.white.withValues(alpha: 0.14),
        child: Padding(
          padding: const EdgeInsets.all(14),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                hour.timeLabel,
                style: TextStyle(
                  color: isCurrent ? const Color(0xFF2C93CB) : const Color(0xFFF9D58B),
                  fontWeight: FontWeight.w600,
                ),
              ),
              const SizedBox(height: 8),
              Icon(
                conditionIcon(hour.condition),
                color: isCurrent ? const Color(0xFF173046) : Colors.white,
                size: isCurrent ? 30 : 26,
              ),
              const SizedBox(height: 8),
              Text(
                '${hour.temperatureC}°',
                style: TextStyle(
                  color: isCurrent ? const Color(0xFF173046) : Colors.white,
                  fontSize: isCurrent ? 28 : 24,
                  fontWeight: FontWeight.w700,
                ),
              ),
              const Spacer(),
              Text(
                'Ploaie ${hour.precipitationChance}%',
                maxLines: 1,
                overflow: TextOverflow.ellipsis,
                style: TextStyle(
                  color: isCurrent ? const Color(0xFF5C6F81) : Colors.white70,
                  fontSize: 13,
                ),
              ),
              const SizedBox(height: 4),
              Text(
                'Vânt ${hour.windKph} km/h',
                maxLines: 1,
                overflow: TextOverflow.ellipsis,
                style: TextStyle(
                  color: isCurrent ? const Color(0xFF5C6F81) : Colors.white60,
                  fontSize: 13,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class DailyCard extends StatelessWidget {
  const DailyCard({
    super.key,
    required this.day,
  });

  final DailyForecast day;

  @override
  Widget build(BuildContext context) {
    final isToday = day.dayLabel == 'Azi';
    return Padding(
      padding: const EdgeInsets.only(bottom: 12),
      child: Card(
        color: isToday
            ? const Color(0xFFF5EBDD)
            : Colors.white.withValues(alpha: 0.14),
        child: Padding(
          padding: const EdgeInsets.all(18),
          child: Row(
            children: [
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      day.dayLabel,
                      style: TextStyle(
                        color: isToday ? const Color(0xFF173046) : Colors.white,
                        fontSize: isToday ? 24 : 20,
                        fontWeight: FontWeight.w700,
                      ),
                    ),
                    const SizedBox(height: 6),
                    Text(
                      conditionLabel(day.condition),
                      style: TextStyle(
                        color: isToday ? const Color(0xFF5C6F81) : Colors.white70,
                      ),
                    ),
                  ],
                ),
              ),
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
                decoration: BoxDecoration(
                  color: isToday
                      ? const Color(0xFF173046).withValues(alpha: 0.10)
                      : Colors.white.withValues(alpha: 0.10),
                  borderRadius: BorderRadius.circular(20),
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.end,
                  children: [
                    Text(
                      '${day.highC}° / ${day.lowC}°',
                      style: TextStyle(
                        color: isToday ? const Color(0xFF173046) : Colors.white,
                        fontWeight: FontWeight.w700,
                      ),
                    ),
                    const SizedBox(height: 4),
                    Text(
                      'Max / Min',
                      style: TextStyle(
                        color: isToday ? const Color(0xFF5C6F81) : Colors.white70,
                      ),
                    ),
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class SectionTitle extends StatelessWidget {
  const SectionTitle({
    super.key,
    required this.text,
  });

  final String text;

  @override
  Widget build(BuildContext context) {
    return Text(
      text,
      style: Theme.of(context).textTheme.headlineSmall?.copyWith(
            color: Colors.white,
            fontWeight: FontWeight.w700,
          ),
    );
  }
}


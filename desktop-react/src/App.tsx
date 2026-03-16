import { startTransition, useDeferredValue, useEffect, useState } from 'react'
import './App.css'
import {
  DEFAULT_CITY,
  type CityOption,
  type DailyForecast,
  type HourlyForecast,
  type WeatherCondition,
  type WeatherOverview,
  favoriteSeedCities,
  fetchWeatherOverview,
  searchCities,
} from './weather'

const FAVORITES_KEY = 'senin.desktop-react.favorites'
const SELECTED_CITY_KEY = 'senin.desktop-react.selected-city'

function loadStoredCity(key: string): CityOption | null {
  const value = localStorage.getItem(key)
  if (!value) return null

  try {
    return JSON.parse(value) as CityOption
  } catch {
    return null
  }
}

function loadStoredCities(key: string): CityOption[] {
  const value = localStorage.getItem(key)
  if (!value) return []

  try {
    const parsed = JSON.parse(value) as CityOption[]
    return Array.isArray(parsed) ? parsed : []
  } catch {
    return []
  }
}

function uniqueCities(cities: CityOption[]) {
  return cities.filter(
    (city, index) => cities.findIndex((candidate) => candidate.id === city.id) === index,
  )
}

function App() {
  const [selectedCity, setSelectedCity] = useState<CityOption>(() => {
    return loadStoredCity(SELECTED_CITY_KEY) ?? DEFAULT_CITY
  })
  const [favoriteCities, setFavoriteCities] = useState<CityOption[]>(() => {
    const stored = loadStoredCities(FAVORITES_KEY)
    return stored.length > 0 ? uniqueCities(stored) : favoriteSeedCities
  })
  const [query, setQuery] = useState('')
  const [suggestions, setSuggestions] = useState<CityOption[]>(favoriteSeedCities)
  const [weather, setWeather] = useState<WeatherOverview | null>(null)
  const [isLoadingWeather, setIsLoadingWeather] = useState(true)
  const [isSearching, setIsSearching] = useState(false)
  const [isFavoritesOpen, setIsFavoritesOpen] = useState(false)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const [searchMessage, setSearchMessage] = useState<string | null>(null)

  const deferredQuery = useDeferredValue(query.trim())

  useEffect(() => {
    localStorage.setItem(FAVORITES_KEY, JSON.stringify(favoriteCities))
  }, [favoriteCities])

  useEffect(() => {
    localStorage.setItem(SELECTED_CITY_KEY, JSON.stringify(selectedCity))
  }, [selectedCity])

  useEffect(() => {
    let cancelled = false

    async function loadWeather() {
      setIsLoadingWeather(true)
      setErrorMessage(null)

      try {
        const result = await fetchWeatherOverview(selectedCity)
        if (cancelled) return
        setWeather(result)
      } catch (error) {
        if (cancelled) return
        setErrorMessage(
          error instanceof Error
            ? error.message
            : 'A apărut o eroare neașteptată la încărcarea vremii.',
        )
      } finally {
        if (!cancelled) {
          setIsLoadingWeather(false)
        }
      }
    }

    void loadWeather()

    return () => {
      cancelled = true
    }
  }, [selectedCity])

  useEffect(() => {
    let cancelled = false

    async function loadSuggestions() {
      if (!deferredQuery) {
        setSuggestions(favoriteCities)
        setIsSearching(false)
        setSearchMessage(favoriteCities.length === 0 ? 'Adaugă orașe la favorite.' : null)
        return
      }

      if (deferredQuery.length < 2) {
        const localMatches = favoriteCities.filter((city) =>
          `${city.name} ${city.region} ${city.country}`
            .toLowerCase()
            .includes(deferredQuery.toLowerCase()),
        )
        setSuggestions(localMatches)
        setIsSearching(false)
        setSearchMessage(
          localMatches.length === 0 ? 'Scrie cel puțin 2 litere pentru căutare live.' : null,
        )
        return
      }

      setIsSearching(true)
      setSearchMessage(null)

      try {
        const results = await searchCities(deferredQuery)
        if (cancelled) return

        startTransition(() => {
          setSuggestions(results)
          setSearchMessage(
            results.length === 0 ? `Nu am găsit rezultate pentru "${deferredQuery}".` : null,
          )
        })
      } catch {
        if (cancelled) return

        const fallback = favoriteCities.filter((city) =>
          `${city.name} ${city.region} ${city.country}`
            .toLowerCase()
            .includes(deferredQuery.toLowerCase()),
        )
        setSuggestions(fallback)
        setSearchMessage(
          fallback.length === 0
            ? 'Căutarea live nu a răspuns. Îți arăt doar favoritele locale.'
            : 'Momentan îți arăt rezultatele locale.',
        )
      } finally {
        if (!cancelled) {
          setIsSearching(false)
        }
      }
    }

    void loadSuggestions()

    return () => {
      cancelled = true
    }
  }, [deferredQuery, favoriteCities])

  function toggleFavorite(city: CityOption) {
    setFavoriteCities((current) => {
      const exists = current.some((item) => item.id === city.id)
      return exists ? current.filter((item) => item.id !== city.id) : uniqueCities([city, ...current])
    })
  }

  const condition = weather?.current.condition ?? 'clear'
  const upcomingDays = weather?.daily.slice(1, 8) ?? []

  return (
    <div className={`app-shell condition-${condition}`}>
      <div className="sky-layer sky-layer-a" aria-hidden="true" />
      <div className="sky-layer sky-layer-b" aria-hidden="true" />
      <div className="sky-layer sky-layer-c" aria-hidden="true" />
      {(condition === 'rain' || condition === 'thunderstorm') && (
        <div className="rain-layer" aria-hidden="true" />
      )}
      {condition === 'mist' && <div className="mist-layer" aria-hidden="true" />}
      {condition === 'snow' && <div className="snow-layer" aria-hidden="true" />}

      <div className="app-frame">
        <aside className="sidebar">
          <div className="brand-block">
            <span className="brand-mark">Senin</span>
            <span className="brand-kicker">Forecast desktop pentru România și lume</span>
            <h1>Vreme live, gândită pentru focus.</h1>
            <p>
              Cauți rapid un oraș, păstrezi favoritele aproape și vezi imediat ritmul zilei în{' '}
              {selectedCity.name}.
            </p>
          </div>

          <div className="panel search-panel">
            <label className="search-label" htmlFor="city-search">
              Schimbă orașul
            </label>
            <input
              id="city-search"
              className="search-input"
              type="text"
              value={query}
              onChange={(event) => setQuery(event.target.value)}
              placeholder="Oradea, Cluj-Napoca, London, Tokyo..."
            />
            {isSearching && <p className="helper-text">Caut orașe...</p>}
            {!isSearching && searchMessage && <p className="helper-text">{searchMessage}</p>}

            <div className="search-results">
              {suggestions.map((city) => {
                const isFavorite = favoriteCities.some((item) => item.id === city.id)
                const isSelected = city.id === selectedCity.id

                return (
                  <button
                    key={city.id}
                    type="button"
                    className={`search-result ${isSelected ? 'selected' : ''}`}
                    onClick={() => {
                      setSelectedCity(city)
                      setQuery('')
                      setSuggestions(favoriteCities)
                    }}
                  >
                    <span className="search-result-copy">
                      <strong>{city.name}</strong>
                      <small>{city.subtitle}</small>
                    </span>
                    <span
                      className={`favorite-toggle ${isFavorite ? 'active' : ''}`}
                      onClick={(event) => {
                        event.stopPropagation()
                        toggleFavorite(city)
                      }}
                    >
                      {isFavorite ? '\u2605' : '\u2606'}
                    </span>
                  </button>
                )
              })}
            </div>
          </div>

          <div className="panel favorites-panel">
            <button
              type="button"
              className={`favorites-toggle ${isFavoritesOpen ? 'open' : ''}`}
              onClick={() => setIsFavoritesOpen((current) => !current)}
            >
              <span className="panel-heading">
                <span className="panel-heading-copy">
                  <h2>Orașe favorite</h2>
                  <small>Alege rapid un oraș salvat</small>
                </span>
                <span className="favorites-toggle-meta">
                  <span>{favoriteCities.length}</span>
                  <span className={`favorites-chevron ${isFavoritesOpen ? 'open' : ''}`} aria-hidden="true" />
                </span>
              </span>
            </button>

            {isFavoritesOpen && (
              <div className="favorites-list">
                {favoriteCities.map((city) => (
                  <button
                    key={city.id}
                    type="button"
                    className={`favorite-city ${city.id === selectedCity.id ? 'selected' : ''}`}
                    onClick={() => setSelectedCity(city)}
                  >
                    <span>
                      <strong>{city.name}</strong>
                      <small>{city.subtitle}</small>
                    </span>
                    <span
                      className="favorite-remove"
                      onClick={(event) => {
                        event.stopPropagation()
                        toggleFavorite(city)
                      }}
                    >
                      {'\u00D7'}
                    </span>
                  </button>
                ))}
              </div>
            )}
          </div>
        </aside>

        <main className="main-content">
          <section className="hero-card">
            {weather ? (
              <div className="hero-layout">
                <div className="hero-primary">
                  <div className="hero-copy">
                    <span className="eyebrow">{weather.updatedAtLabel}</span>
                    <h2>
                      {selectedCity.name}
                      <small>{selectedCity.subtitle}</small>
                    </h2>
                    <p>{weather.current.summary}</p>
                  </div>

                  <div className="hero-current-row">
                    <div className="temperature-block">
                      <div className="temperature-hero">
                        <div className="weather-illustration" aria-hidden="true">
                          <WeatherGlyph condition={weather.current.condition} />
                        </div>
                        <div className="temperature-copy">
                          <span className="temperature-value">{formatDegrees(weather.current.temperatureC)}</span>
                          <span className="temperature-condition">
                            {labelForCondition(weather.current.condition)}
                          </span>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>

                <div className="hero-secondary">
                  <SunPathCard weather={weather} />
                  <div className="hero-side">
                    <div className="metric-chip-grid">
                      <MetricChip label="Se simte" value={formatDegrees(weather.current.feelsLikeC)} />
                      <MetricChip label="Umiditate" value={`${weather.current.humidity}%`} />
                      <MetricChip label="Vânt" value={`${weather.current.windKph} km/h`} />
                      <MetricChip label="Ploaie" value={`${weather.current.precipitationChance}%`} />
                    </div>
                  </div>
                </div>
              </div>
            ) : (
              <div className="hero-copy">
                <span className="eyebrow">Pregătesc datele live</span>
                <h2>
                  {selectedCity.name}
                  <small>{selectedCity.subtitle}</small>
                </h2>
                <p>Colectez forecast-ul actual.</p>
              </div>
            )}

            {isLoadingWeather && <div className="hero-loading">Actualizez prognoza...</div>}
            {errorMessage && <div className="error-banner">{errorMessage}</div>}
          </section>

          <section className="main-grid">
            <section className="panel hourly-panel">
              <div className="panel-heading">
                <h2>Următoarele 24 de ore</h2>
                <span>Acum + 24h</span>
              </div>

              <div className="hourly-strip">
                {(weather?.hourly ?? []).map((hour) => (
                  <HourlyCard key={`${hour.timeLabel}-${hour.temperatureC}`} hour={hour} />
                ))}
              </div>
            </section>

            <section className="panel days-panel">
              <div className="panel-heading">
                <h2>Următoarele 7 zile</h2>
                <span>{upcomingDays.length} zile</span>
              </div>

              <div className="daily-list">
                {upcomingDays.map((day) => (
                  <DailyCard key={day.dayLabel} day={day} />
                ))}
              </div>
            </section>

            <section className="panel insights-panel">
              <div className="panel-heading">
                <h2>Detalii locale</h2>
                <span>{selectedCity.name}</span>
              </div>

              {weather ? (
                <>
                  <p className="insight-summary">{buildInsightSummary(selectedCity, weather)}</p>
                  <div className="insight-grid">
                    <MetricTile label="Ora locală" value={weather.localTimeLabel} />
                    <MetricTile label="Lumina zilei" value={weather.daylightLabel} />
                    <MetricTile
                      label="Răsărit / apus"
                      value={`${weather.sunriseLabel} / ${weather.sunsetLabel}`}
                    />
                    <MetricTile
                      label="Max / min"
                      value={`${formatDegrees(weather.daily[0]?.highC ?? '--')} / ${formatDegrees(weather.daily[0]?.lowC ?? '--')}`}
                    />
                  </div>
                </>
              ) : (
                <p className="insight-summary">Pregătesc insight-urile pentru orașul selectat.</p>
              )}
            </section>
          </section>
        </main>
      </div>
    </div>
  )
}

function MetricChip(props: { label: string; value: string }) {
  return (
    <div className="metric-chip">
      <span>{props.label}</span>
      <strong>{props.value}</strong>
    </div>
  )
}

function MetricTile(props: { label: string; value: string }) {
  return (
    <article className="metric-tile">
      <span>{props.label}</span>
      <strong>{props.value}</strong>
    </article>
  )
}

function SunPathCard(props: { weather: WeatherOverview }) {
  const progress = sunProgress(
    props.weather.localTimeLabel,
    props.weather.sunriseLabel,
    props.weather.sunsetLabel,
  )
  const clampedProgress = Math.min(Math.max(progress, 0), 1)
  const { x: sunX, y: sunY } = sunPathPoint(clampedProgress)
  let sunLabel =
    progress <= 0
    ? 'Înainte de răsărit'
    : progress >= 1
      ? 'După apus'
      : 'Soarele este pe traseu'
  let timeHint = timeUntilSunsetLabel(
    props.weather.localTimeLabel,
    props.weather.sunsetLabel,
    progress,
  )

  if (progress <= 0) {
    sunLabel = 'Înainte de răsărit'
    timeHint = `Răsare la ${props.weather.sunriseLabel}`
  } else if (progress >= 1) {
    sunLabel = 'După apus'
    timeHint = `A apus la ${props.weather.sunsetLabel}`
  } else {
    sunLabel = 'Soarele traversează cerul'
  }

  return (
    <div className="sun-card">
      <div className="sun-card-header">
        <span>Lumina de azi</span>
        <strong>{props.weather.daylightLabel}</strong>
      </div>
      <div className="sun-card-body">
        <div className="sun-side sun-side-left">
          <span>Răsărit</span>
          <strong>{props.weather.sunriseLabel}</strong>
        </div>
        <div className="sun-arc-wrap">
          <div className="sun-status-pill">{sunLabel}</div>
          <div className="sun-arc" aria-hidden="true">
            <svg viewBox="0 0 260 132" className="sun-arc-graphic">
              <defs>
                <filter id="sunGlow">
                  <feGaussianBlur stdDeviation="5" result="blur" />
                  <feMerge>
                    <feMergeNode in="blur" />
                    <feMergeNode in="SourceGraphic" />
                  </feMerge>
                </filter>
              </defs>
              <rect x="12" y="18" width="236" height="86" rx="43" className="sun-backdrop" />
              <path
                d="M22 92 C68 92 90 24 130 24 C170 24 192 92 238 92"
                className="sun-main-curve"
              />
              <path
                d="M22 92 C68 92 90 124 130 124 C170 124 192 92 238 92"
                className="sun-shadow-curve"
              />
              <line x1="16" y1="92" x2="244" y2="92" className="sun-baseline" />
              <path
                d="M22 92 C68 92 90 24 130 24 C170 24 192 92 238 92"
                className="sun-highlight-curve"
              />
              <circle cx={sunX} cy={sunY} r="13" className="sun-halo" filter="url(#sunGlow)" />
              <circle cx={sunX} cy={sunY} r="7.5" className="sun-core" />
              <circle cx={sunX} cy={sunY} r="3" className="sun-core-dot" />
            </svg>
          </div>
          <div className="sun-card-center">
            <strong>{timeHint}</strong>
            <span>{props.weather.localTimeLabel} ora locală</span>
          </div>
        </div>
        <div className="sun-side sun-side-right">
          <span>Apus</span>
          <strong>{props.weather.sunsetLabel}</strong>
        </div>
      </div>
    </div>
  )
}

function HourlyCard(props: { hour: HourlyForecast }) {
  return (
    <article className="hourly-card">
      <span className="hourly-time">{props.hour.timeLabel}</span>
      <div className="hourly-icon" aria-hidden="true">
        <WeatherGlyph condition={props.hour.condition} />
      </div>
      <strong className="hourly-temp">{formatDegrees(props.hour.temperatureC)}</strong>
      <span className="hourly-condition">{labelForCondition(props.hour.condition)}</span>
      <small>Ploaie {props.hour.precipitationChance}%</small>
      <small>Vânt {props.hour.windKph} km/h</small>
    </article>
  )
}

function DailyCard(props: { day: DailyForecast }) {
  return (
    <article className="daily-card">
      <div className="daily-card-left">
        <div className="daily-icon" aria-hidden="true">
          <WeatherGlyph condition={props.day.condition} />
        </div>
        <div className="daily-copy">
          <strong>{props.day.dayLabel}</strong>
          <small>{labelForCondition(props.day.condition)}</small>
        </div>
      </div>
      <div className="daily-card-right">
        <strong>
          {formatDegrees(props.day.highC)} / {formatDegrees(props.day.lowC)}
        </strong>
        <small>Ploaie {props.day.precipitationChance}%</small>
      </div>
    </article>
  )
}

function WeatherGlyph(props: { condition: WeatherCondition }) {
  switch (props.condition) {
    case 'clear':
      return (
        <svg viewBox="0 0 64 64" className="weather-glyph weather-glyph-clear">
          <circle cx="32" cy="32" r="12" fill="currentColor" />
          {Array.from({ length: 8 }).map((_, index) => {
            const angle = (index * Math.PI) / 4
            const x1 = 32 + Math.cos(angle) * 18
            const y1 = 32 + Math.sin(angle) * 18
            const x2 = 32 + Math.cos(angle) * 26
            const y2 = 32 + Math.sin(angle) * 26
            return (
              <line
                key={index}
                x1={x1}
                y1={y1}
                x2={x2}
                y2={y2}
                stroke="currentColor"
                strokeWidth="4"
                strokeLinecap="round"
              />
            )
          })}
        </svg>
      )
    case 'partly-cloudy':
      return (
        <svg viewBox="0 0 64 64" className="weather-glyph weather-glyph-partly-cloudy">
          <circle cx="24" cy="24" r="10" className="weather-glyph-sun" />
          <path
            d="M23 43c-5.5 0-10-4-10-9s4.5-9 10-9c1.3 0 2.5.2 3.7.6A11 11 0 0 1 47 32a8 8 0 1 1 0 16H23Z"
            className="weather-glyph-cloud"
          />
        </svg>
      )
    case 'cloudy':
    case 'mist':
      return (
        <svg viewBox="0 0 64 64" className="weather-glyph weather-glyph-cloudy">
          <path
            d="M21 45c-6 0-11-4.4-11-10s5-10 11-10c1.3 0 2.7.2 3.9.7A13 13 0 0 1 49 32a9 9 0 1 1 0 18H21Z"
            fill="currentColor"
          />
        </svg>
      )
    case 'rain':
      return (
        <svg viewBox="0 0 64 64" className="weather-glyph weather-glyph-rain">
          <path
            d="M21 39c-6 0-11-4.4-11-10s5-10 11-10c1.3 0 2.7.2 3.9.7A13 13 0 0 1 49 26a9 9 0 1 1 0 18H21Z"
            fill="currentColor"
          />
          <path d="M24 46l-4 10" stroke="currentColor" strokeWidth="4" strokeLinecap="round" />
          <path d="M34 46l-4 10" stroke="currentColor" strokeWidth="4" strokeLinecap="round" />
          <path d="M44 46l-4 10" stroke="currentColor" strokeWidth="4" strokeLinecap="round" />
        </svg>
      )
    case 'thunderstorm':
      return (
        <svg viewBox="0 0 64 64" className="weather-glyph weather-glyph-thunderstorm">
          <path
            d="M21 39c-6 0-11-4.4-11-10s5-10 11-10c1.3 0 2.7.2 3.9.7A13 13 0 0 1 49 26a9 9 0 1 1 0 18H21Z"
            fill="currentColor"
          />
          <path
            d="M33 42h9l-7 10h7l-14 12 5-12h-7l7-10Z"
            fill="#fff5d6"
          />
        </svg>
      )
    case 'snow':
      return (
        <svg viewBox="0 0 64 64" className="weather-glyph weather-glyph-snow">
          <path
            d="M21 37c-6 0-11-4.4-11-10s5-10 11-10c1.3 0 2.7.2 3.9.7A13 13 0 0 1 49 24a9 9 0 1 1 0 18H21Z"
            fill="currentColor"
          />
          <path d="M24 47v12M18 53h12M20 49l8 8M28 49l-8 8" stroke="#ffffff" strokeWidth="3" strokeLinecap="round" />
          <path d="M42 47v12M36 53h12M38 49l8 8M46 49l-8 8" stroke="#ffffff" strokeWidth="3" strokeLinecap="round" />
        </svg>
      )
    default:
      return null
  }
}

function labelForCondition(condition: WeatherCondition) {
  switch (condition) {
    case 'clear':
      return 'Senin'
    case 'partly-cloudy':
      return 'Cer variabil'
    case 'cloudy':
      return 'Înnorat'
    case 'rain':
      return 'Ploaie'
    case 'thunderstorm':
      return 'Furtună'
    case 'snow':
      return 'Ninsoare'
    case 'mist':
      return 'Ceață'
    default:
      return 'Vreme'
  }
}

function buildInsightSummary(city: CityOption, weather: WeatherOverview) {
  const condition = labelForCondition(weather.current.condition).toLowerCase()
  return `Acum în ${city.name} este ${condition}, cu vânt de ${weather.current.windKph} km/h și umiditate ${weather.current.humidity}%.`
}

function formatDegrees(value: number | string) {
  return `${value}\u00B0`
}

function sunProgress(currentLabel: string, sunriseLabel: string, sunsetLabel: string) {
  const current = timeToMinutes(currentLabel)
  const sunrise = timeToMinutes(sunriseLabel)
  const sunset = timeToMinutes(sunsetLabel)

  if (current == null || sunrise == null || sunset == null || sunset <= sunrise) {
    return 0.5
  }

  if (current <= sunrise) return 0
  if (current >= sunset) return 1

  return (current - sunrise) / (sunset - sunrise)
}

function sunPathPoint(progress: number) {
  const t = Math.min(Math.max(progress, 0), 1)

  if (t <= 0.5) {
    return cubicBezierPoint(
      t * 2,
      { x: 22, y: 92 },
      { x: 68, y: 92 },
      { x: 90, y: 24 },
      { x: 130, y: 24 },
    )
  }

  return cubicBezierPoint(
    (t - 0.5) * 2,
    { x: 130, y: 24 },
    { x: 170, y: 24 },
    { x: 192, y: 92 },
    { x: 238, y: 92 },
  )
}

function cubicBezierPoint(
  t: number,
  p0: { x: number; y: number },
  p1: { x: number; y: number },
  p2: { x: number; y: number },
  p3: { x: number; y: number },
) {
  const u = 1 - t
  const tt = t * t
  const uu = u * u
  const uuu = uu * u
  const ttt = tt * t

  return {
    x: uuu * p0.x + 3 * uu * t * p1.x + 3 * u * tt * p2.x + ttt * p3.x,
    y: uuu * p0.y + 3 * uu * t * p1.y + 3 * u * tt * p2.y + ttt * p3.y,
  }
}

function timeToMinutes(label: string) {
  const match = /^(\d{2}):(\d{2})$/.exec(label)
  if (!match) return null
  return Number(match[1]) * 60 + Number(match[2])
}

function timeUntilSunsetLabel(currentLabel: string, sunsetLabel: string, progress: number) {
  const current = timeToMinutes(currentLabel)
  const sunset = timeToMinutes(sunsetLabel)

  if (current == null || sunset == null) {
    return currentLabel
  }

  if (progress >= 1) {
    return 'Soarele a apus'
  }

  if (progress <= 0) {
    return `${currentLabel} acum`
  }

  const remaining = Math.max(sunset - current, 0)
  const hours = Math.floor(remaining / 60)
  const minutes = remaining % 60
  return `${hours}h ${minutes}m până la apus`
}

export default App

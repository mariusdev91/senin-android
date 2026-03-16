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
            <div className="hero-copy">
              <span className="eyebrow">
                {weather?.updatedAtLabel ?? 'Pregătesc datele live'}
              </span>
              <h2>
                {selectedCity.name}
                <small>{selectedCity.subtitle}</small>
              </h2>
              <p>{weather?.current.summary ?? 'Colectez forecast-ul actual.'}</p>
            </div>

            {weather && (
              <div className="hero-metrics">
                <div className="temperature-block">
                  <span className="temperature-value">{formatDegrees(weather.current.temperatureC)}</span>
                  <span className="temperature-condition">
                    {labelForCondition(weather.current.condition)}
                  </span>
                </div>
                <div className="metric-chip-grid">
                  <MetricChip label="Se simte" value={formatDegrees(weather.current.feelsLikeC)} />
                  <MetricChip label="Umiditate" value={`${weather.current.humidity}%`} />
                  <MetricChip label="Vânt" value={`${weather.current.windKph} km/h`} />
                  <MetricChip label="Ploaie" value={`${weather.current.precipitationChance}%`} />
                </div>
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

function HourlyCard(props: { hour: HourlyForecast }) {
  return (
    <article className="hourly-card">
      <span className="hourly-time">{props.hour.timeLabel}</span>
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
      <div>
        <strong>{props.day.dayLabel}</strong>
        <small>{labelForCondition(props.day.condition)}</small>
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

export default App

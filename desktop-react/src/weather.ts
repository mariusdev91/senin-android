export type WeatherCondition =
  | 'clear'
  | 'partly-cloudy'
  | 'cloudy'
  | 'rain'
  | 'thunderstorm'
  | 'snow'
  | 'mist'

export type CityOption = {
  id: string
  name: string
  region: string
  country: string
  countryCode: string
  latitude: number
  longitude: number
  subtitle: string
  isDefault?: boolean
}

export type CurrentWeather = {
  temperatureC: number
  feelsLikeC: number
  humidity: number
  windKph: number
  precipitationChance: number
  condition: WeatherCondition
  summary: string
}

export type HourlyForecast = {
  timeLabel: string
  temperatureC: number
  precipitationChance: number
  windKph: number
  condition: WeatherCondition
}

export type DailyForecast = {
  dayLabel: string
  highC: number
  lowC: number
  precipitationChance: number
  condition: WeatherCondition
}

export type WeatherOverview = {
  current: CurrentWeather
  hourly: HourlyForecast[]
  daily: DailyForecast[]
  updatedAtLabel: string
  sourceLabel: string
  localTimeLabel: string
  sunriseLabel: string
  sunsetLabel: string
  daylightLabel: string
}

export const DEFAULT_CITY: CityOption = city(
  'oradea',
  'Oradea',
  'Bihor',
  'România',
  'RO',
  47.0465,
  21.9189,
  true,
)

export const favoriteSeedCities: CityOption[] = [
  DEFAULT_CITY,
  city('bucharest', 'București', 'București', 'România', 'RO', 44.4268, 26.1025),
  city('cluj', 'Cluj-Napoca', 'Cluj', 'România', 'RO', 46.7712, 23.6236),
  city('timisoara', 'Timișoara', 'Timiș', 'România', 'RO', 45.7489, 21.2087),
  city('london', 'London', 'England', 'United Kingdom', 'GB', 51.5072, -0.1276),
  city('tokyo', 'Tokyo', 'Tokyo', 'Japan', 'JP', 35.6764, 139.65),
]

function city(
  id: string,
  name: string,
  region: string,
  country: string,
  countryCode: string,
  latitude: number,
  longitude: number,
  isDefault = false,
): CityOption {
  return {
    id,
    name,
    region,
    country,
    countryCode,
    latitude,
    longitude,
    isDefault,
    subtitle: region && region !== country ? `${region}, ${country}` : country,
  }
}

export async function searchCities(query: string): Promise<CityOption[]> {
  const normalized = query.trim()
  if (normalized.length < 2) return favoriteSeedCities

  const url = new URL('https://geocoding-api.open-meteo.com/v1/search')
  url.searchParams.set('name', normalized)
  url.searchParams.set('count', '12')
  url.searchParams.set('language', 'ro')
  url.searchParams.set('format', 'json')

  const response = await fetch(url, {
    headers: {
      Accept: 'application/json',
    },
  })

  if (!response.ok) {
    throw new Error('Serviciul de căutare orașe nu a răspuns.')
  }

  const body = (await response.json()) as {
    results?: Array<{
      id?: number
      name?: string
      admin1?: string
      country?: string
      country_code?: string
      latitude?: number
      longitude?: number
    }>
  }

  return (body.results ?? [])
    .filter((item) => Number.isFinite(item.latitude) && Number.isFinite(item.longitude))
    .map((item) =>
      city(
        String(item.id ?? `${item.name}-${item.latitude}-${item.longitude}`),
        item.name ?? 'Unknown city',
        item.admin1 ?? item.country ?? 'Unknown',
        item.country ?? 'Unknown',
        item.country_code ?? '',
        item.latitude ?? 0,
        item.longitude ?? 0,
      ),
    )
}

export async function fetchWeatherOverview(cityOption: CityOption): Promise<WeatherOverview> {
  const url = new URL('https://api.open-meteo.com/v1/forecast')
  url.searchParams.set('latitude', String(cityOption.latitude))
  url.searchParams.set('longitude', String(cityOption.longitude))
  url.searchParams.set('timezone', 'auto')
  url.searchParams.set('forecast_days', '8')
  url.searchParams.set(
    'current',
    'temperature_2m,apparent_temperature,relative_humidity_2m,weather_code,wind_speed_10m',
  )
  url.searchParams.set(
    'hourly',
    'temperature_2m,precipitation_probability,weather_code,wind_speed_10m',
  )
  url.searchParams.set(
    'daily',
    'weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max,sunrise,sunset',
  )
  url.searchParams.set('wind_speed_unit', 'kmh')

  const response = await fetch(url, {
    headers: {
      Accept: 'application/json',
    },
  })

  if (!response.ok) {
    throw new Error('Serviciul meteo a răspuns cu o eroare temporară.')
  }

  const body = await response.json()
  const current = body.current
  const hourly = body.hourly
  const daily = body.daily
  const timezone = body.timezone as string | undefined

  if (!current || !hourly || !daily) {
    throw new Error('Răspunsul meteo este incomplet.')
  }

  const dailyForecast = createDailyForecast(daily.time, daily)
  const hourlyForecast = createHourlyForecast(hourly.time, hourly, current.time)
  const currentCondition = mapWeatherCode(current.weather_code)

  return {
    current: {
      temperatureC: Math.round(current.temperature_2m),
      feelsLikeC: Math.round(current.apparent_temperature),
      humidity: Math.round(current.relative_humidity_2m),
      windKph: Math.round(current.wind_speed_10m),
      precipitationChance:
        hourlyForecast[0]?.precipitationChance ?? dailyForecast[0]?.precipitationChance ?? 0,
      condition: currentCondition,
      summary: buildSummary(currentCondition, cityOption, dailyForecast[0]),
    },
    hourly: hourlyForecast,
    daily: dailyForecast,
    updatedAtLabel: `Actualizat ${toHourLabel(current.time)} ${body.timezone_abbreviation ?? ''}`.trim(),
    sourceLabel: 'Date live via Open-Meteo',
    localTimeLabel: formatLocalTime(timezone),
    sunriseLabel: toHourLabel(daily.sunrise?.[0]),
    sunsetLabel: toHourLabel(daily.sunset?.[0]),
    daylightLabel: daylightDuration(
      toHourLabel(daily.sunrise?.[0]),
      toHourLabel(daily.sunset?.[0]),
    ),
  }
}

function createHourlyForecast(
  times: string[],
  hourly: Record<string, number[]>,
  currentTime: string,
): HourlyForecast[] {
  const resolvedStartIndex = resolveHourlyStartIndex(times, currentTime)
  const endIndex = Math.min(resolvedStartIndex + 25, times.length)

  return times
    .map((time, index) => ({ time, index }))
    .filter(({ index }) => index >= resolvedStartIndex && index < endIndex)
    .map(({ time, index }) => ({
      timeLabel: index === resolvedStartIndex ? 'Acum' : toHourLabel(time),
      temperatureC: Math.round(hourly.temperature_2m?.[index] ?? 0),
      precipitationChance: Math.round(hourly.precipitation_probability?.[index] ?? 0),
      windKph: Math.round(hourly.wind_speed_10m?.[index] ?? 0),
      condition: mapWeatherCode(hourly.weather_code?.[index] ?? 3),
    }))
}

function resolveHourlyStartIndex(times: string[], currentTime: string) {
  let candidate = 0

  for (let index = 0; index < times.length; index += 1) {
    if (times[index] > currentTime) break
    candidate = index
  }

  return candidate
}

function createDailyForecast(
  times: string[],
  daily: Record<string, Array<number | string>>,
): DailyForecast[] {
  return times.slice(0, 8).map((time, index) => ({
    dayLabel: toDayLabel(time, index),
    highC: Math.round(Number(daily.temperature_2m_max?.[index] ?? 0)),
    lowC: Math.round(Number(daily.temperature_2m_min?.[index] ?? 0)),
    precipitationChance: Math.round(Number(daily.precipitation_probability_max?.[index] ?? 0)),
    condition: mapWeatherCode(Number(daily.weather_code?.[index] ?? 3)),
  }))
}

function mapWeatherCode(code: number): WeatherCondition {
  if (code === 0) return 'clear'
  if (code === 1 || code === 2) return 'partly-cloudy'
  if (code === 3) return 'cloudy'
  if (code === 45 || code === 48) return 'mist'
  if ([51, 53, 55, 56, 57, 61, 63, 65, 66, 67, 80, 81, 82].includes(code)) return 'rain'
  if ([71, 73, 75, 77, 85, 86].includes(code)) return 'snow'
  if ([95, 96, 99].includes(code)) return 'thunderstorm'
  return 'cloudy'
}

function buildSummary(condition: WeatherCondition, cityOption: CityOption, today?: DailyForecast) {
  const prefix = {
    clear: 'Cer senin',
    'partly-cloudy': 'Cer variabil',
    cloudy: 'Înnorat',
    rain: 'Ploaie sau averse',
    thunderstorm: 'Instabilitate și furtuni',
    snow: 'Ninsori ușoare',
    mist: 'Ceață și vizibilitate redusă',
  }[condition]

  if (!today) {
    return `${prefix} acum în ${cityOption.name}.`
  }

  return `${prefix} azi în ${cityOption.name}. Max ${formatDegrees(today.highC)}, min ${formatDegrees(today.lowC)}, ploaie ${today.precipitationChance}%.`
}

function toHourLabel(value?: string) {
  if (!value) return '--:--'
  return new Intl.DateTimeFormat('ro-RO', {
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  }).format(new Date(value))
}

function toDayLabel(value: string, index: number) {
  if (index === 0) return 'Azi'
  if (index === 1) return 'Mâine'
  const day = new Date(value).getDay()
  return ['Duminică', 'Luni', 'Marți', 'Miercuri', 'Joi', 'Vineri', 'Sâmbătă'][day] ?? 'Zi'
}

function formatLocalTime(timezone?: string) {
  try {
    return new Intl.DateTimeFormat('ro-RO', {
      hour: '2-digit',
      minute: '2-digit',
      hour12: false,
      timeZone: timezone || 'Europe/Bucharest',
    }).format(new Date())
  } catch {
    return '--:--'
  }
}

function daylightDuration(sunriseLabel: string, sunsetLabel: string) {
  if (sunriseLabel === '--:--' || sunsetLabel === '--:--') return '--'

  const [sunriseHours, sunriseMinutes] = sunriseLabel.split(':').map(Number)
  const [sunsetHours, sunsetMinutes] = sunsetLabel.split(':').map(Number)
  const sunriseTotal = sunriseHours * 60 + sunriseMinutes
  const sunsetTotal = sunsetHours * 60 + sunsetMinutes
  const duration = Math.max(sunsetTotal - sunriseTotal, 0)
  const hours = Math.floor(duration / 60)
  const minutes = duration % 60
  return `${hours}h ${minutes}m`
}

function formatDegrees(value: number | string) {
  return `${value}\u00B0`
}

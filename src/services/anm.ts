import { haversineKm, toFloat } from "../utils/geo";
import type { LocationPayload } from "../storage/kv";

const ANM_STAREA_VREMII =
  "https://www.meteoromania.ro/wp-json/meteoapi/v2/starea-vremii";

const PREFERRED_STATIONS = [
  "ORADEA",
  "BIHOR"
];

function pickByName(stations: any[]) {
  for (const s of stations) {
    const name = String(s.nume ?? s.name ?? "").toUpperCase();
    if (PREFERRED_STATIONS.some((p) => name.includes(p))) {
      return s;
    }
  }
  return null;
}

function pickNearestStation(stations: any[], lat: number, lon: number) {
  let best: any = null;
  let bestD = Number.POSITIVE_INFINITY;

  for (const s of stations) {
    const slat = toFloat(s.lat ?? s.latitudine ?? s.latitude);
    const slon = toFloat(s.lon ?? s.longitudine ?? s.longitude);
    if (!Number.isFinite(slat) || !Number.isFinite(slon)) continue;

    const d = haversineKm(lat, lon, slat, slon);
    if (d < bestD) {
      bestD = d;
      best = s;
    }
  }
  return { station: best, distKm: bestD };
}

export type AnmWeather = {
  stationName: string;
  distKm?: number;
  temperature?: unknown;
  wind?: unknown;
  humidity?: unknown;
  pressure?: unknown;
  observedAt?: unknown;
  text: string;
  raw?: Record<string, unknown>;
};

export async function fetchAnmWeather(loc: LocationPayload): Promise<AnmWeather> {
  const r = await fetch(ANM_STAREA_VREMII, {
    cf: { cacheTtl: 60, cacheEverything: true },
  });

  if (!r.ok) {
    return { stationName: "ANM", text: `ANM indisponibil (${r.status}).` };
  }

  const data: any = await r.json();

  const features: any[] = Array.isArray(data?.features) ? data.features : [];
  if (features.length === 0) {
    return { stationName: "ANM", text: "Nu am putut obține date ANM (starea vremii)." };
  }

  const stations = features.map((f) => {
    const props = f?.properties ?? {};
    const coords = f?.geometry?.coordinates; // de obicei [lon, lat]

    const lon = Array.isArray(coords) ? coords[0] : undefined;
    const lat = Array.isArray(coords) ? coords[1] : undefined;

    return { ...props, lon, lat };
  });

  // Extra-safety
  if (stations.length === 0) {
    return { stationName: "ANM", text: "Nu am putut obține date ANM (starea vremii)." };
  }

  let station: any | null = null;
  let distKm: number | undefined;

  // 1) Preferăm Oradea / Bihor
  station = pickByName(stations);

  // 2) Dacă nu găsim, alegem după distanță
  if (!station && Number.isFinite(loc.lat) && Number.isFinite(loc.lon)) {
    const picked = pickNearestStation(stations, loc.lat, loc.lon);
    if (picked.station && picked.distKm < 120) { // prag realist
      station = picked.station;
      distKm = picked.distKm;
    }
  }

  // 3) Fallback final (controlat)
  if (!station) {
    station = stations[0];
  }


  const stationName = station.nume ?? station.name ?? "Stație ANM";

  const temperature = station.temperatura ?? station.temp ?? station.t;
  const wind = station.vant ?? station.wind;
  const humidity = station.umiditate ?? station.rh ?? station.humidity;
  const pressure = station.presiune ?? station.pressure;
  const observedAt = station.data ?? station.time ?? station.ora;

  const parts: string[] = [stationName];
  if (temperature != null) parts.push(`Temp: ${temperature}°C`);
  if (wind != null) parts.push(`Vânt: ${wind}`);
  if (humidity != null) parts.push(`Umiditate: ${humidity}`);
  if (pressure != null) parts.push(`Presiune: ${pressure}`);
  if (observedAt != null) parts.push(`Update: ${observedAt}`);
  if (distKm != null) parts.push(`(~${distKm.toFixed(1)} km)`);

  return {
  stationName,
  distKm,
  temperature,
  wind,
  humidity,
  pressure,
  observedAt,
  text: parts.join(" | "),
  raw: station,
};
}

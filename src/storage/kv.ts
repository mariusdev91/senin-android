import type { Env } from "../env";

export type LocationPayload = {
  lat: number;
  lon: number;
  acc?: number;
  ts?: string;
};

const KEY = "last_location";

export async function saveLastLocation(env: Env, payload: LocationPayload) {
  await env.WEATHER_KV.put(KEY, JSON.stringify(payload));
}

export async function getLastLocation(env: Env): Promise<LocationPayload | null> {
  const raw = await env.WEATHER_KV.get(KEY);
  if (!raw) return null;
  return JSON.parse(raw) as LocationPayload;
}

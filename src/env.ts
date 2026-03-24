export interface Env {
  WEATHER_KV: KVNamespace;

  LOCATION_TOKEN: string;

  // ntfy
  NTFY_TOPIC?: string;
  NTFY_BASE_URL?: string;

  // pushover (fallback)
  PUSHOVER_USER_KEY?: string;
  PUSHOVER_APP_TOKEN?: string;
}

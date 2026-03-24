import type { Env } from "../env";
import { htmlResponse, jsonResponse } from "../utils/http";
import { indexHtml } from "../ui/page";
import { saveLastLocation, getLastLocation, type LocationPayload } from "../storage/kv";
import { fetchAnmWeather } from "../services/anm";
import { sendPush } from "../services/push";

function authOrNull(request: Request, env: Env): Response | null {
  const url = new URL(request.url);
  const token =
    request.headers.get("x-location-token") || url.searchParams.get("token") || "";

  if (!token || token !== env.LOCATION_TOKEN) {
    return new Response("Unauthorized", { status: 401 });
  }
  return null;
}

export async function handleRoot(request: Request) {
  const url = new URL(request.url);
  return htmlResponse(indexHtml(url.origin));
}

export async function handleHealth() {
  return jsonResponse({ ok: true });
}

export async function handleLocation(request: Request, env: Env) {
  const auth = authOrNull(request, env);
  if (auth) return auth;

  if (request.method !== "POST") return new Response("Method Not Allowed", { status: 405 });

  const body = (await request.json().catch(() => null)) as any;
  const lat = Number(body?.lat);
  const lon = Number(body?.lon);
  const acc = body?.acc != null ? Number(body.acc) : undefined;

  if (!Number.isFinite(lat) || !Number.isFinite(lon)) {
    return new Response("Invalid lat/lon", { status: 400 });
  }

  const payload = { lat, lon, acc, ts: new Date().toISOString() };
  await saveLastLocation(env, payload);
  return jsonResponse({ ok: true, stored: payload });
}

export async function handleNotifyNow(request: Request, env: Env) {
  const auth = authOrNull(request, env);
  if (auth) return auth;

  const loc = await getLastLocation(env);
  if (!loc) return new Response("No location stored yet", { status: 400 });

  const weather = await fetchAnmWeather(loc);
  await sendPush(env, "Vremea (ANM)", weather.text);

  return jsonResponse({
    ok: true,
    sent: true,
    location: loc,
    weather,
  });
}

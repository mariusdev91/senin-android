import type { Env } from "../env";

async function sendViaNtfy(env: Env, title: string, message: string) {
  const base = (env.NTFY_BASE_URL || "https://ntfy.sh").replace(/\/+$/, "");
  const topic = env.NTFY_TOPIC;
  if (!topic) throw new Error("NTFY_TOPIC missing");

  const url = `${base}/${encodeURIComponent(topic)}`;
  const r = await fetch(url, {
    method: "POST",
    headers: {
      Title: title,
      "Content-Type": "text/plain; charset=utf-8",
    },
    body: message,
  });
  if (!r.ok) throw new Error(`ntfy failed: ${r.status}`);
}

async function sendViaPushover(env: Env, title: string, message: string) {
  if (!env.PUSHOVER_USER_KEY || !env.PUSHOVER_APP_TOKEN) {
    throw new Error("Pushover keys missing");
  }
  const form = new URLSearchParams({
    user: env.PUSHOVER_USER_KEY,
    token: env.PUSHOVER_APP_TOKEN,
    title,
    message,
  });

  const r = await fetch("https://api.pushover.net/1/messages.json", {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: form.toString(),
  });
  if (!r.ok) throw new Error(`pushover failed: ${r.status}`);
}

export async function sendPush(env: Env, title: string, message: string) {
  if (env.NTFY_TOPIC) return sendViaNtfy(env, title, message);
  return sendViaPushover(env, title, message);
}

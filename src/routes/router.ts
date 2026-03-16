import type { Env } from "../env";
import { handleHealth, handleLocation, handleNotifyNow, handleRoot } from "./handlers";

export async function route(request: Request, env: Env): Promise<Response> {
  const url = new URL(request.url);

  if (url.pathname === "/") return handleRoot(request);
  if (url.pathname === "/health") return handleHealth();
  if (url.pathname === "/location") return handleLocation(request, env);
  if (url.pathname === "/notify-now") return handleNotifyNow(request, env);

  return new Response("Not Found", { status: 404 });
}

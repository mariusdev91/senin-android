import type { Env } from "./env";
import { route } from "./routes/router";
import { getLastLocation } from "./storage/kv";
import { fetchAnmWeather  } from "./services/anm";
import { sendPush } from "./services/push";

export default {
  async fetch(request: Request, env: Env): Promise<Response> {
    return route(request, env);
  },

  async scheduled(_event: ScheduledEvent, env: Env, ctx: ExecutionContext) {
    ctx.waitUntil(
      (async () => {
        const loc = await getLastLocation(env);
        if (!loc) return;

        const weather = await fetchAnmWeather(loc);
        await sendPush(env, "Vremea (ANM)", weather.text);
      })()
    );
  },
};

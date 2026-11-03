// Cloudflare Worker: proxy between the Emova app and the Gemini API.
// The API key never ships inside the APK.
//
// Deploy:
//   1. Cloudflare dashboard → Workers → your worker → Edit code → paste this file.
//   2. Settings → Variables and Secrets:
//        GEMINI_API_KEY  (Secret, required)  – your key from Google AI Studio
//        APP_TOKEN       (Secret, optional)  – same value as APP_TOKEN in local.properties
//        GEMINI_MODEL    (Text, optional)    – defaults to gemini-3.1-flash-lite.
//                                              Confirm the current Flash-Lite id in AI Studio.
//   3. Optional but recommended: Cloudflare → Security → WAF → Rate limiting rule on this
//      Worker's route (e.g. 20 requests / minute / IP) so nobody can drain your quota.

const MAX_SYSTEM_CHARS = 4000;
const MAX_PROMPT_CHARS = 1500;

const json = (obj, status = 200) =>
  new Response(JSON.stringify(obj), {
    status,
    headers: { "Content-Type": "application/json; charset=utf-8" },
  });

export default {
  async fetch(request, env) {
    if (request.method !== "POST") return json({ error: "method_not_allowed" }, 405);

    if (env.APP_TOKEN && request.headers.get("X-App-Token") !== env.APP_TOKEN) {
      return json({ error: "unauthorized" }, 401);
    }

    let data;
    try {
      data = await request.json();
    } catch {
      return json({ error: "bad_json" }, 400);
    }

    const system = String(data.system || "").slice(0, MAX_SYSTEM_CHARS);
    const prompt = String(data.prompt || "").slice(0, MAX_PROMPT_CHARS);
    if (!system || !prompt) return json({ error: "missing_fields" }, 400);

    const model = env.GEMINI_MODEL || "gemini-3.1-flash-lite";
    const url = `https://generativelanguage.googleapis.com/v1beta/models/${model}:generateContent`;

    const geminiBody = {
      systemInstruction: { parts: [{ text: system }] },
      contents: [{ role: "user", parts: [{ text: prompt }] }],
      generationConfig: { temperature: 0.8, maxOutputTokens: 300 },
    };

    let res;
    try {
      res = await fetch(url, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "x-goog-api-key": env.GEMINI_API_KEY,
        },
        body: JSON.stringify(geminiBody),
      });
    } catch {
      return json({ error: "upstream_unreachable" }, 502);
    }

    if (!res.ok) return json({ error: "upstream_error", status: res.status }, 502);

    const out = await res.json();
    const text = (out?.candidates?.[0]?.content?.parts || [])
      .map((p) => p.text || "")
      .join("")
      .trim();

    if (!text) return json({ error: "empty_response" }, 502);
    return json({ text });
  },
};

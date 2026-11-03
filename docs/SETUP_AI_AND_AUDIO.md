# Setup: Gemini Worker and mindfulness audio

## 1. Gemini through your Cloudflare Worker
1. Get a key at Google AI Studio and confirm the current **Flash-Lite** model id there
   (the Worker defaults to `gemini-3.1-flash-lite`).
2. Cloudflare → Workers → your worker (`icy-sun-bd17`) → Edit code → paste `worker/gemini-proxy.js` → Deploy.
3. Worker → Settings → Variables and Secrets:
   - `GEMINI_API_KEY` (Secret) – your key
   - `APP_TOKEN` (Secret, optional) – any long random string
   - `GEMINI_MODEL` (Text, optional) – override the model id without touching code
4. If you set `APP_TOKEN`, add the same value to `local.properties` (never committed):
   `APP_TOKEN=the-same-string`
5. Add a Cloudflare rate-limiting rule for the Worker (e.g. 20 requests/min/IP).
6. Remove the old `GROQ_API_KEY` secret from the Worker and from `local.properties`.

Quick test (replace URL and token):
    curl -X POST https://YOUR-WORKER.workers.dev -H "Content-Type: application/json" \
      -H "X-App-Token: YOUR_TOKEN" \
      -d '{"system":"Reply in one short sentence.","prompt":"Say hello.","lang":"en"}'

## 2. Mindfulness audio
Put MP3s in `app/src/main/res/raw/` with exactly these names (lowercase, no spaces):

    mindful_5.mp3    mindful_10.mp3    mindful_20.mp3

A session whose file is missing shows "Coming soon" instead of crashing.
Tip: keep each file under ~25 MB (128 kbps mono is plenty for guided voice) to keep the APK small.

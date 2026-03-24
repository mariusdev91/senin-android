export function indexHtml(origin: string) {
  return `<!doctype html>
<html lang="ro">
<head>
  <meta charset="utf-8" />
  <meta name="viewport" content="width=device-width,initial-scale=1" />
  <title>Weather Push</title>
</head>
<body>
  <h3>Weather Push</h3>
  <p>Deschide pe telefon cu <code>?token=...</code> și apasă Start.</p>
  <p id="status">Status: idle</p>
  <button id="start">Start</button>
  <button id="stop" disabled>Stop</button>

<script>
  let watchId = null;
  const token = new URLSearchParams(location.search).get("token");
  function setStatus(msg){ document.getElementById("status").textContent = "Status: " + msg; }

  async function postLocation(lat, lon, acc){
    const res = await fetch("${origin}/location?token=" + encodeURIComponent(token || ""), {
      method: "POST",
      headers: {"Content-Type":"application/json"},
      body: JSON.stringify({lat, lon, acc})
    });
    if (!res.ok) throw new Error(await res.text());
  }

  document.getElementById("start").onclick = () => {
    if (!token) { setStatus("lipsește token"); return; }
    if (!navigator.geolocation) { setStatus("geolocation indisponibil"); return; }

    watchId = navigator.geolocation.watchPosition(async (pos) => {
      try {
        const {latitude, longitude, accuracy} = pos.coords;
        await postLocation(latitude, longitude, accuracy);
        setStatus("trimis: " + latitude.toFixed(5) + ", " + longitude.toFixed(5));
      } catch(e) {
        setStatus("eroare: " + e.message);
      }
    }, (err) => setStatus("eroare geolocation: " + err.message),
    { enableHighAccuracy: true, maximumAge: 30000, timeout: 10000 });

    document.getElementById("start").disabled = true;
    document.getElementById("stop").disabled = false;
  };

  document.getElementById("stop").onclick = () => {
    if (watchId !== null) navigator.geolocation.clearWatch(watchId);
    watchId = null;
    setStatus("oprit");
    document.getElementById("start").disabled = false;
    document.getElementById("stop").disabled = true;
  };
</script>
</body></html>`;
}

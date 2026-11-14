const SONAR_URL = process.env.SONAR_HOST_URL || "https://sonarcloud.io";
const PROJECT_KEY = process.env.SONAR_PROJECT_KEY || "ematevar_SIGOC-web";
const SONAR_TOKEN = process.env.SONAR_TOKEN || "";

async function main() {
  const headers = {};
  if (SONAR_TOKEN) {
    const auth = Buffer.from(`${SONAR_TOKEN}:`).toString("base64");
    headers.Authorization = `Basic ${auth}`;
  }

  const res = await fetch(
    `${SONAR_URL}/api/issues/search?componentKeys=${encodeURIComponent(PROJECT_KEY)}&resolved=false&ps=100`,
    { headers }
  );

  if (!res.ok) {
    throw new Error(`Error ${res.status}: ${await res.text()}`);
  }

  const data = await res.json();
  console.log(`\n=== SonarCloud Issues para ${PROJECT_KEY} (Total: ${data.total}) ===\n`);

  if (!data.issues || data.issues.length === 0) {
    console.log("No se encontraron issues abiertos.");
    return;
  }

  data.issues.forEach((it, idx) => {
    const file = it.component.replace(`${PROJECT_KEY}:`, "");
    console.log(`${idx + 1}. [${it.severity}] [${it.type}] Regla: ${it.rule}`);
    console.log(`   Archivo: ${file}:${it.line ?? "N/A"}`);
    console.log(`   Mensaje: ${it.message}`);
    console.log(`   Clave:   ${it.key}\n`);
  });
}

main().catch(console.error);

import { execSync } from "node:child_process";

const SONAR_URL = process.env.SONAR_HOST_URL || "https://sonarcloud.io";
const SONAR_TOKEN = process.env.SONAR_TOKEN || "";
const PROJECT_KEY = process.env.SONAR_PROJECT_KEY || "ematevar_SIGOC-web";

const repoEnv = process.env.GITHUB_REPOSITORY || "ematevar/SIGOC-web";
const [owner, repo] = repoEnv.split("/");

function resolveGitHubToken() {
  if (process.env.GITHUB_TOKEN) return process.env.GITHUB_TOKEN;
  if (process.env.GH_TOKEN) return process.env.GH_TOKEN;
  try {
    const token = execSync("gh auth token", { encoding: "utf-8", stdio: ["ignore", "pipe", "ignore"] }).trim();
    if (token) return token;
  } catch {
    // ignore
  }
  return "";
}

const GITHUB_TOKEN = resolveGitHubToken();

const API = "https://api.github.com";
const LABEL = "sonarqube";

async function github(url, options = {}) {
  const headers = {
    Accept: "application/vnd.github+json",
    "X-GitHub-Api-Version": "2022-11-28",
    ...options.headers
  };
  if (GITHUB_TOKEN) {
    headers.Authorization = `Bearer ${GITHUB_TOKEN}`;
  }

  const response = await fetch(url, {
    ...options,
    headers
  });

  if (!response.ok) {
    throw new Error(
      `GitHub error ${response.status}: ${await response.text()}`
    );
  }

  return response.json();
}

async function getSonarIssues() {
  const issues = [];
  const headers = {};
  if (SONAR_TOKEN) {
    const auth = Buffer.from(`${SONAR_TOKEN}:`).toString("base64");
    headers.Authorization = `Basic ${auth}`;
  }

  for (let page = 1; ; page++) {
    const params = new URLSearchParams({
      componentKeys: PROJECT_KEY,
      resolved: "false",
      ps: "100",
      p: String(page)
    });

    const response = await fetch(
      `${SONAR_URL}/api/issues/search?${params}`,
      { headers }
    );

    if (!response.ok) {
      throw new Error(`SonarCloud error: ${response.status} ${await response.text()}`);
    }

    const data = await response.json();

    issues.push(...data.issues);

    if (issues.length >= data.total || data.issues.length === 0) {
      break;
    }
  }

  return issues;
}

async function getExistingTickets() {
  const tickets = [];

  for (let page = 1; ; page++) {
    const params = new URLSearchParams({
      state: "all",
      labels: LABEL,
      per_page: "100",
      page: String(page)
    });

    const data = await github(
      `${API}/repos/${owner}/${repo}/issues?${params}`
    );

    tickets.push(...data.filter(i => !i.pull_request));

    if (data.length < 100) break;
  }

  return tickets;
}

async function createIssue(issue) {
  const file = issue.component.replace(`${PROJECT_KEY}:`, "");

  const sonarLink =
    `${SONAR_URL}/project/issues?id=${encodeURIComponent(PROJECT_KEY)}` +
    `&open=${encodeURIComponent(issue.key)}`;

  const title = `[SonarQube] [${issue.severity}] ${issue.message}`;

  const body = `
## Hallazgo detectado por SonarCloud

- **Severidad:** ${issue.severity}
- **Tipo:** ${issue.type}
- **Regla:** ${issue.rule}
- **Archivo:** ${file}
- **Línea:** ${issue.line ?? "No especificada"}

### Descripción

${issue.message}

### Referencia

[Ver issue en SonarCloud](${sonarLink})

<!-- sonarcloud-issue-key: ${issue.key} -->
`;

  await github(`${API}/repos/${owner}/${repo}/issues`, {
    method: "POST",
    body: JSON.stringify({
      title,
      body,
      labels: [LABEL]
    })
  });

  console.log(`Ticket creado: ${issue.key} - ${file}:${issue.line ?? "?"}`);
}

async function main() {
  console.log("Iniciando sincronización...");
  console.log(`Proyecto SonarCloud: ${PROJECT_KEY}`);
  console.log(`Repositorio GitHub: ${owner}/${repo}`);

  if (!GITHUB_TOKEN) {
    console.warn("ADVERTENCIA: No se detectó GITHUB_TOKEN ni sesión de 'gh auth'. Las llamadas de escritura podrían fallar.");
  }

  const sonarIssues = await getSonarIssues();
  const githubTickets = await getExistingTickets();

  const existingKeys = new Set();

  for (const ticket of githubTickets) {
    const match = ticket.body?.match(
      /<!-- sonarcloud-issue-key: ([^ ]+) -->/
    );

    if (match) existingKeys.add(match[1]);
  }

  let created = 0;

  for (const issue of sonarIssues) {
    if (existingKeys.has(issue.key)) {
      console.log(`Ya existe: ${issue.key}`);
      continue;
    }

    await createIssue(issue);
    existingKeys.add(issue.key);
    created++;
  }

  console.log("--------------------------------");
  console.log(`Issues encontrados en SonarCloud: ${sonarIssues.length}`);
  console.log(`Tickets nuevos creados en GitHub: ${created}`);
  console.log("--------------------------------");
}

main().catch(error => {
  console.error(error);
  process.exit(1);
});

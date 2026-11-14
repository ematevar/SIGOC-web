# Scripts de Integración SonarCloud ↔ GitHub Issues (SIGOC-Web)

Este directorio contiene herramientas automatizadas para auditar y sincronizar los hallazgos de calidad de código y vulnerabilidades reportados por SonarCloud directamente en los **GitHub Issues** del repositorio.

---

## 🛠️ Contenido del Directorio

1. **`sync-sonar.js`**:
   - Lee todos los hallazgos no resueltos (`resolved: false`) desde la API de SonarCloud para el proyecto `ematevar_SIGOC-web`.
   - Consulta los issues existentes en GitHub con la etiqueta `sonarqube` para evitar duplicidad.
   - Crea un nuevo issue en GitHub por cada hallazgo detectado, incluyendo severidad, tipo, archivo, línea afectada, descripción y enlace directo a SonarCloud.
   - Detecta automáticamente el token de GitHub desde la CLI `gh` si no se pasa explícitamente en variables de entorno.

2. **`list-sonar.js`**:
   - Muestra en consola de forma rápida un resumen limpio de todos los issues abiertos en SonarCloud con su severidad, regla, archivo y línea.

3. **`package.json`**:
   - Configuración de scripts NPM para ejecutar con `npm run sync` o `npm run list`.

4. **`.env.example`**:
   - Plantilla de variables de entorno recomendadas.

---

## 🚀 Uso Rápido

### Listar issues de SonarCloud en consola:
```bash
cd scripts
npm run list
# o directamente:
node list-sonar.js
```

### Sincronizar issues hacia GitHub:
```bash
cd scripts
npm run sync
# o directamente:
node sync-sonar.js
```

*(Si ya ejecutaste `gh auth login`, el script utilizará automáticamente tus credenciales locales de GitHub sin necesidad de configurar variables de entorno).*

---

## ⚙️ Automatización en GitHub Actions

Puedes programar esta sincronización en `.github/workflows/sonar-sync.yml` tras cada análisis de SonarCloud o bajo demanda (`workflow_dispatch`).

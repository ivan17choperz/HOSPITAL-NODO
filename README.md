# Hospital Nodo — Sistema Hospitalario Distribuido

Sistema de gestión hospitalaria distribuido en 3 nodos independientes (Bogotá, Medellín, Cali), cada uno con su propio stack completo de microservicios Spring Boot, PostgreSQL, RabbitMQ y Redis. Incluye FHIR R5, OAuth2/Keycloak, circuit breaker, Prometheus y Grafana.

---

## Índice

1. [Requisitos](#requisitos)
2. [Arquitectura](#arquitectura)
3. [Levantar los servicios](#levantar-los-servicios)
4. [Puertos por nodo](#puertos-por-nodo)
5. [Seguridad OAuth2 — Keycloak](#seguridad-oauth2--keycloak)
6. [Endpoints — ms-admission](#ms-admission)
7. [Endpoints — ms-triage](#ms-triage)
8. [Endpoints — ms-emr](#ms-emr)
9. [Endpoints — ms-fhir-adapter](#ms-fhir-adapter)
10. [Endpoints — ms-doctor](#ms-doctor)
11. [Endpoints — ms-sync-agent](#ms-sync-agent)
12. [Monitoreo — Prometheus y Grafana](#monitoreo--prometheus-y-grafana)
13. [Resiliencia — Circuit Breaker](#resiliencia--circuit-breaker)
14. [Flujo de prueba end-to-end](#flujo-de-prueba-end-to-end)
15. [Consolas de administración](#consolas-de-administración)

---

## Requisitos

- Docker Desktop (con Docker Compose v2)
- 8 GB RAM mínimo para nodo 1 solo; 16 GB recomendados para los 3 nodos simultáneos
- Puertos libres: `8080–8288`, `8180`, `5432–5437`, `5672–5674`, `6379–6381`, `9090–9092`, `3000–3002`, `15672–15674`

---

## Arquitectura

```
┌──────────────────────────────────────────────────────────────────────────┐
│  NODO 1 (Bogotá)             NODO 2 (Medellín)      NODO 3 (Cali)       │
│  gateway      :8080           gateway      :8090      gateway      :8100  │
│  ├─ admission :8081           ├─ admission :8181      ├─ admission :8281  │
│  ├─ discharge :8082           ├─ discharge :8182      ├─ discharge :8282  │
│  ├─ emr       :8083           ├─ emr       :8183      ├─ emr       :8283  │
│  ├─ laboratory:8084           ├─ laboratory:8184      ├─ laboratory:8284  │
│  ├─ fhir      :8085           ├─ fhir      :8185      ├─ fhir      :8285  │
│  ├─ sync-agent:8086           ├─ sync-agent:8186      ├─ sync-agent:8286  │
│  ├─ triage    :8087           ├─ triage    :8187      ├─ triage    :8287  │
│  ├─ doctor    :8088           ├─ doctor    :8188      ├─ doctor    :8288  │
│  ├─ keycloak  :8180           ├─ keycloak  :8280      ├─ keycloak  :8380  │
│  ├─ prometheus:9090           ├─ prometheus:9091      ├─ prometheus:9092  │
│  ├─ grafana   :3000           ├─ grafana   :3001      ├─ grafana   :3002  │
│  ├─ rabbitmq  :5672           ├─ rabbitmq  :5673      ├─ rabbitmq  :5674  │
│  ├─ redis     :6379           ├─ redis     :6380      ├─ redis     :6381  │
│  ├─ postgres  :5432           ├─ postgres  :5433      ├─ postgres  :5434  │
│  └─ pg-replica:5435           └─ pg-replica:5436      └─ pg-replica:5437  │
│                                                                            │
│       sync-agent ←──── inter-node-network ────→ sync-agent               │
└──────────────────────────────────────────────────────────────────────────┘
```

Cuando se admite un paciente en nodo 1, el `ms-sync-agent` reenvía el evento a nodo 2 y 3 via HTTP. Cada nodo republica el evento en su RabbitMQ local. Los endpoints `/api/fhir/**` están protegidos por JWT emitido por Keycloak. Prometheus raspa métricas de todos los servicios cada 15 s; Grafana los visualiza.

---

## Levantar los servicios

Ejecutar desde la raíz del proyecto (donde está `docker-compose.yml`):

### Solo nodo 1 (recomendado para demo)

```bash
docker-compose --profile node1 up --build
```

### Los 3 nodos completos

```bash
docker-compose --profile node1 --profile node2 --profile node3 up --build
```

### En segundo plano

```bash
docker-compose --profile node1 up --build -d
```

### Ver logs de un servicio

```bash
docker logs hospital-node1-gateway -f
docker logs hospital-node1-fhir -f
docker logs hospital-keycloak -f
docker logs hospital-node1-prometheus -f
```

### Detener todo

```bash
docker-compose --profile node1 --profile node2 --profile node3 down
```

### Reset total (borra volúmenes de base de datos)

```bash
docker-compose --profile node1 --profile node2 --profile node3 down -v
```

### Reconstruir un solo servicio

```bash
docker-compose --profile node1 up --build ms-fhir-adapter-node1 -d
```

> La primera vez el build tarda 3–5 minutos descargando dependencias Maven. Las siguientes usan caché de Docker.

---

## Puertos por nodo

| Servicio | Nodo 1 | Nodo 2 | Nodo 3 |
|---|---|---|---|
| **Frontend Angular** | **4200** | **4201** | **4202** |
| **Gateway (Spring Cloud)** | 8080 | 8090 | 8100 |
| ms-admission | 8081 | 8181 | 8281 |
| ms-discharge | 8082 | 8182 | 8282 |
| ms-emr | 8083 | 8183 | 8283 |
| ms-laboratory | 8084 | 8184 | 8284 |
| ms-fhir-adapter | 8085 | 8185 | 8285 |
| ms-sync-agent | 8086 | 8186 | 8286 |
| ms-triage | 8087 | 8187 | 8287 |
| ms-doctor | 8088 | 8188 | 8288 |
| **Keycloak** | **8180** | **8280** | **8380** |
| **Prometheus** | **9090** | 9091 | 9092 |
| **Grafana** | **3000** | 3001 | 3002 |
| PostgreSQL primaria | 5432 | 5433 | 5434 |
| PostgreSQL réplica | 5435 | 5436 | 5437 |
| RabbitMQ AMQP | 5672 | 5673 | 5674 |
| RabbitMQ UI | 15672 | 15673 | 15674 |
| Redis | 6379 | 6380 | 6381 |

---

## Seguridad OAuth2 — Keycloak

Las rutas `/api/fhir/**` requieren un JWT Bearer token. El resto de rutas son públicas.

Cada nodo tiene su propio Keycloak independiente con el mismo realm `hospital`. El realm se importa automáticamente al iniciar desde `keycloak/hospital-realm.json`.

### Credenciales pre-configuradas

| Usuario | Contraseña | Rol |
|---|---|---|
| `doctor1` | `doctor123` | `fhir-user` |
| `admin` | `admin123` | `fhir-user`, `fhir-admin` |

Client: `fhir-client` / Secret: `fhir-secret`

### Paso 1 — Obtener token de acceso

Cada nodo tiene su propio Keycloak. Usar el que corresponda al nodo que se quiere llamar:

| Nodo | Token endpoint |
|---|---|
| Nodo 1 | `http://localhost:8180/realms/hospital/protocol/openid-connect/token` |
| Nodo 2 | `http://localhost:8280/realms/hospital/protocol/openid-connect/token` |
| Nodo 3 | `http://localhost:8380/realms/hospital/protocol/openid-connect/token` |

```bash
# Nodo 1
curl -s -X POST http://localhost:8180/realms/hospital/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=fhir-client" \
  -d "client_secret=fhir-secret" \
  -d "username=doctor1" \
  -d "password=doctor123" | jq .access_token
```

En Postman: `POST <token-endpoint>` con body `x-www-form-urlencoded` y los mismos campos.

### Paso 2 — Usar el token en llamadas FHIR

```bash
curl -H "Authorization: Bearer <TOKEN>" \
  http://localhost:8080/api/fhir/resources/Patient
```

### Verificar que sin token devuelve 401

```bash
curl -i http://localhost:8080/api/fhir/resources/Patient
# HTTP/1.1 401 Unauthorized
```

---

## ms-admission

Puerto directo: `8081` (nodo 1) · `8181` (nodo 2) · `8281` (nodo 3)
Via gateway: `/api/admission/`

Al crear una admisión se publica un evento `patient.admitted` en RabbitMQ → recibido por EMR, FHIR, Lab y el sync-agent.

---

### `POST /admissions` — Admitir paciente

**URL nodo 1**: `http://localhost:8080/api/admission/admissions`

```json
{
  "patientId": "550e8400-e29b-41d4-a716-446655440000",
  "firstName": "Carlos",
  "lastName": "Ramirez",
  "gender": "male",
  "birthDate": "1985-03-15",
  "identifier": "CC-12345678",
  "ward": "URGENCIAS",
  "episodeNumber": "EP-2024-001"
}
```

| Campo | Tipo | Requerido | Descripción |
|---|---|---|---|
| `patientId` | UUID | Sí | ID único del paciente |
| `firstName` | String | Sí | Nombre |
| `lastName` | String | Sí | Apellido |
| `ward` | String | Sí | `URGENCIAS`, `UCI`, `CIRUGIA`, `PEDIATRIA`, `GINECOLOGIA` |
| `gender` | String | No | `male`, `female`, `other`, `unknown` |
| `birthDate` | `YYYY-MM-DD` | No | Fecha de nacimiento |
| `identifier` | String | No | Número de documento |
| `episodeNumber` | String | No | Se genera automáticamente si se omite |

**Response `201 Created`**: objeto admisión con `id`, `nodeId`, `admittedAt`, `status: "ADMITTED"`.

---

### `GET /admissions` — Listar admisiones

```
GET http://localhost:8080/api/admission/admissions
```

> Si la base de datos está caída, el circuit breaker devuelve `200 []` en lugar de error 500.

---

### `GET /reports/summary` — Reporte estadístico

```
GET http://localhost:8080/api/admission/reports/summary
```

---

## ms-triage

Puerto directo: `8087` · `8187` · `8287` | Via gateway: `/api/triage/`

---

### `POST /triages` — Registrar triage

**URL nodo 1**: `http://localhost:8080/api/triage/triages`

```json
{
  "admissionId": "<uuid admisión>",
  "patientId": "550e8400-e29b-41d4-a716-446655440000",
  "vitalSigns": {
    "temperature": 38.5,
    "heartRate": 95,
    "systolicBp": 130,
    "diastolicBp": 85,
    "oxygenSaturation": 97.0,
    "respiratoryRate": 18,
    "painScore": 4
  },
  "category": "YELLOW",
  "notes": "Paciente consciente, dolor abdominal moderado"
}
```

**Categorías Manchester**: `RED` (inmediato) · `ORANGE` (muy urgente) · `YELLOW` (urgente) · `GREEN` (menor) · `BLUE` (no urgente)

---

### `GET /triages` — Listar todos los triages

```
GET http://localhost:8080/api/triage/triages
```

---

### `GET /triages/admission/{admissionId}` — Triages de una admisión

```
GET http://localhost:8080/api/triage/triages/admission/<uuid>
```

---

## ms-emr

Puerto directo: `8083` · `8183` · `8283` | Via gateway: `/api/emr/`

Historia Clínica Electrónica. Almacena diagnósticos, medicaciones y notas en formato FHIR-compatible.

---

### `POST /records` — Crear registro EMR

**URL nodo 1**: `http://localhost:8080/api/emr/records`

```json
{
  "admissionId": "<uuid admisión>",
  "patientId": "550e8400-e29b-41d4-a716-446655440000",
  "anamnesis": "Dolor abdominal de 3 días, náuseas y vómito",
  "diagnoses": [
    { "code": "K35.2", "display": "Apendicitis aguda", "status": "active" }
  ],
  "medications": [
    { "code": "J01CA04", "display": "Amoxicilina 500mg", "dosage": "1 cápsula cada 8 horas" }
  ],
  "clinicianNotes": "Se solicita valoración por cirugía"
}
```

---

### `GET /records/admission/{admissionId}` — Registros de una admisión

```
GET http://localhost:8080/api/emr/records/admission/<uuid>
```

> Si la base de datos está caída, el circuit breaker devuelve `200 []`.

---

### `GET /episodes/{episodeNumber}` — Historia clínica por episodio

```
GET http://localhost:8080/api/emr/episodes/EP-2024-001
```

---

## ms-fhir-adapter

Puerto directo: `8085` · `8185` · `8285` | Via gateway: `/api/fhir/`

> **Autenticación requerida** para todas las rutas `/api/fhir/**`. Ver sección [Keycloak](#seguridad-oauth2--keycloak).

Todos los ejemplos usan el header: `Authorization: Bearer <TOKEN>`

---

### `GET /metadata` — CapabilityStatement

```
GET http://localhost:8080/api/fhir/metadata
```

Devuelve el FHIR R5 CapabilityStatement declarando recursos: `Patient`, `Observation`, `Condition`, `MedicationRequest`, `Encounter`.

---

### `GET /resources/{type}` — Buscar recursos por tipo

```
GET http://localhost:8080/api/fhir/resources/Patient
GET http://localhost:8080/api/fhir/resources/Condition
GET http://localhost:8080/api/fhir/resources/MedicationRequest
```

**Tipos válidos**: `Patient`, `Observation`, `Condition`, `MedicationRequest`, `Encounter`, `Coverage`, `Organization`

**Response `200 OK`**: FHIR Bundle (searchset).

---

### `GET /resources/{type}/{id}` — Obtener recurso por ID

```
GET http://localhost:8080/api/fhir/resources/Patient/550e8400-e29b-41d4-a716-446655440000
```

**Response `200`**: FHIR resource JSON · **`404`**: si no existe.

---

### `POST /resources/{type}` — Crear recurso FHIR genérico

```
POST http://localhost:8080/api/fhir/resources/Encounter
Content-Type: application/json

{
  "resourceType": "Encounter",
  "status": "in-progress",
  "class": [{ "coding": [{ "code": "AMB" }] }],
  "subject": { "reference": "Patient/550e8400-e29b-41d4-a716-446655440000" }
}
```

**Response `201 Created`**: recurso FHIR con ID asignado.

---

### `PUT /resources/{type}/{id}` — Actualizar recurso FHIR

```
PUT http://localhost:8080/api/fhir/resources/Condition/cond-uuid-1234
Content-Type: application/json

{
  "resourceType": "Condition",
  "id": "cond-uuid-1234",
  "clinicalStatus": {
    "coding": [{ "system": "http://terminology.hl7.org/CodeSystem/condition-clinical", "code": "resolved" }]
  },
  "code": { "text": "Apendicitis aguda" },
  "subject": { "reference": "Patient/550e8400-e29b-41d4-a716-446655440000" }
}
```

**Response `200 OK`**: recurso actualizado.

---

### `DELETE /resources/{type}/{id}` — Eliminar recurso FHIR

```
DELETE http://localhost:8080/api/fhir/resources/Patient/550e8400-e29b-41d4-a716-446655440000
```

**Response `204 No Content`**: eliminación lógica (el registro queda marcado como `deleted=true`).

---

### `POST /export/patient` — Exportar paciente desde admisión

```
POST http://localhost:8080/api/fhir/export/patient
Content-Type: application/json

{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "firstName": "Carlos",
  "lastName": "Ramirez",
  "gender": "male",
  "birthDate": "1985-03-15",
  "identifier": "CC-12345678"
}
```

---

### `POST /condition` — Crear Condition FHIR directamente

```
POST http://localhost:8080/api/fhir/condition
Content-Type: application/json

{
  "resourceType": "Condition",
  "clinicalStatus": {
    "coding": [{ "system": "http://terminology.hl7.org/CodeSystem/condition-clinical", "code": "active" }]
  },
  "code": {
    "coding": [{ "system": "http://hl7.org/fhir/sid/icd-10", "code": "K35.2" }],
    "text": "Apendicitis aguda con peritonitis generalizada"
  },
  "subject": { "reference": "Patient/550e8400-e29b-41d4-a716-446655440000" }
}
```

---

## ms-doctor

Puerto directo: `8088` · `8188` · `8288` | Via gateway: `/api/doctor/`

Gestión del catálogo de médicos del hospital.

---

### `POST /doctors` — Registrar médico

**URL nodo 1**: `http://localhost:8080/api/doctor/doctors`

```json
{
  "firstName": "María",
  "lastName": "González",
  "specialty": "Cardiología",
  "licenseNumber": "MED-2024-001",
  "email": "mgonzalez@hospital.com"
}
```

**Response `201 Created`**: objeto médico con `id` UUID.

---

### `GET /doctors` — Listar médicos

```
GET http://localhost:8080/api/doctor/doctors
```

---

### `GET /doctors/{id}` — Obtener médico por ID

```
GET http://localhost:8080/api/doctor/doctors/<uuid>
```

---

## ms-sync-agent

Puerto directo: `8086` · `8186` · `8286` | Via gateway: `/api/sync/`

Agente de sincronización entre nodos. Escucha eventos RabbitMQ y los reenvía a los peers via HTTP. Usa Redis para idempotencia.

---

### `GET /status` — Estado del agente

```
GET http://localhost:8080/api/sync/status
```

```json
{
  "nodeId": "node1",
  "peers": ["http://ms-sync-agent-node2:8086", "http://ms-sync-agent-node3:8086"],
  "eventsForwarded": 12,
  "status": "UP"
}
```

---

### `GET /status/peers` — Estado de conectividad entre nodos (tiempo real)

Hace ping activo al `/actuator/health` de cada peer y devuelve su estado.

```
GET http://localhost:8080/api/sync/status/peers
```

```json
{
  "http://ms-sync-agent-node2:8086": "UP",
  "http://ms-sync-agent-node3:8086": "DOWN"
}
```

Este endpoint alimenta el panel "Conectividad entre Nodos" del dashboard Angular.

---

## Monitoreo — Prometheus y Grafana

Todos los microservicios exponen métricas en `/actuator/prometheus`. Prometheus las raspa cada 15 segundos.

### Acceder a Prometheus (nodo 1)

```
http://localhost:9090
```

Ver todos los targets activos:
```
http://localhost:9090/targets
```

Consultas de ejemplo en Prometheus:
```
# Tasa de requests HTTP
rate(http_server_requests_seconds_count[1m])

# Memoria JVM heap usada
jvm_memory_used_bytes{area="heap"}

# Conexiones activas de base de datos (HikariCP)
hikaricp_connections_active
```

### Acceder a Grafana (nodo 1)

```
http://localhost:3000
Usuario: admin
Contraseña: admin
```

El dashboard **"Hospital Distributed System"** se provisiona automáticamente con 6 paneles:
- JVM Heap Memory
- HTTP Request Rate
- HTTP 5xx Error Rate
- Active DB Connections (HikariCP)
- CPU Usage
- HTTP Avg Response Time

### Prometheus por nodo

| Nodo | Prometheus | Grafana |
|---|---|---|
| Nodo 1 | http://localhost:9090 | http://localhost:3000 |
| Nodo 2 | http://localhost:9091 | http://localhost:3001 |
| Nodo 3 | http://localhost:9092 | http://localhost:3002 |

---

## Resiliencia — Circuit Breaker

ms-admission y ms-emr tienen circuit breaker con Resilience4j. Ante 3+ fallos consecutivos en BD, el circuito se abre y los endpoints devuelven lista vacía en lugar de error 500.

### Demostración de caída de base de datos

```bash
# 1. Verificar que hay datos
curl http://localhost:8080/api/admission/admissions

# 2. Detener la base de datos de nodo 1
docker stop hospital-node1-db

# 3. Hacer varias peticiones — devuelven [] con HTTP 200 (fallback activo)
curl http://localhost:8080/api/admission/admissions

# 4. Ver el estado del circuit breaker
curl http://localhost:8080/api/admission/actuator/health
# "circuitBreakers": { "admission-db": { "status": "CIRCUIT_OPEN" } }

# 5. Reiniciar la base de datos
docker start hospital-node1-db

# El circuito pasa a HALF_OPEN → CLOSED automáticamente en ~10 segundos
```

### Demostración de desconexión de nodo

```bash
# 1. Ver conectividad actual
curl http://localhost:8080/api/sync/status/peers

# 2. Detener el gateway de nodo 2
docker stop hospital-node2-gateway

# 3. Consultar estado de peers — nodo 2 aparece como DOWN
curl http://localhost:8080/api/sync/status/peers
# { "http://ms-sync-agent-node2:8086": "DOWN", "http://ms-sync-agent-node3:8086": "UP" }

# El dashboard Angular en la sección "Conectividad entre Nodos" también lo muestra en rojo

# 4. Restaurar nodo 2
docker start hospital-node2-gateway
```

---

## Flujo de prueba end-to-end

### Paso 1 — Obtener token OAuth2 (nodo 1)

```bash
TOKEN=$(curl -s -X POST http://localhost:8180/realms/hospital/protocol/openid-connect/token \
  -d "grant_type=password&client_id=fhir-client&client_secret=fhir-secret&username=doctor1&password=doctor123" \
  | jq -r .access_token)

# Nodo 2: cambiar 8180 → 8280 y el gateway 8080 → 8090
# Nodo 3: cambiar 8180 → 8380 y el gateway 8080 → 8100
```

### Paso 2 — Admitir paciente en nodo 1

```
POST http://localhost:8080/api/admission/admissions
Content-Type: application/json

{
  "patientId": "550e8400-e29b-41d4-a716-446655440001",
  "firstName": "Ana",
  "lastName": "Gomez",
  "gender": "female",
  "birthDate": "1990-06-20",
  "identifier": "CC-98765432",
  "ward": "URGENCIAS"
}
```

Guarda el `id` y `episodeNumber` de la respuesta.

### Paso 3 — Verificar sincronización en nodo 2

```
GET http://localhost:8090/api/admission/admissions
```

La admisión de Ana debe aparecer aunque fue creada en nodo 1.

### Paso 4 — Registrar triage

```
POST http://localhost:8080/api/triage/triages
Content-Type: application/json

{
  "admissionId": "<id del paso 2>",
  "patientId": "550e8400-e29b-41d4-a716-446655440001",
  "vitalSigns": { "temperature": 38.2, "heartRate": 88, "systolicBp": 125, "diastolicBp": 80, "oxygenSaturation": 98.0, "respiratoryRate": 16, "painScore": 3 },
  "category": "GREEN",
  "notes": "Paciente estable"
}
```

### Paso 5 — Crear registro EMR

```
POST http://localhost:8080/api/emr/records
Content-Type: application/json

{
  "admissionId": "<id del paso 2>",
  "patientId": "550e8400-e29b-41d4-a716-446655440001",
  "anamnesis": "Cefalea y fiebre de 2 días",
  "diagnoses": [{ "code": "J06.9", "display": "Infección vías respiratorias altas", "status": "active" }],
  "medications": [{ "code": "N02BE01", "display": "Paracetamol 500mg", "dosage": "1 tableta cada 6 horas" }],
  "clinicianNotes": "Manejo ambulatorio, control en 48 horas"
}
```

### Paso 6 — Consultar FHIR con token

```bash
# Listar pacientes FHIR (requiere token)
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/fhir/resources/Patient

# Crear un Encounter FHIR
curl -X POST -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  http://localhost:8080/api/fhir/resources/Encounter \
  -d '{"resourceType":"Encounter","status":"in-progress","class":[{"coding":[{"code":"AMB"}]}],"subject":{"reference":"Patient/550e8400-e29b-41d4-a716-446655440001"}}'
```

### Paso 7 — Verificar estado de los nodos

```
GET http://localhost:8080/api/sync/status/peers
GET http://localhost:9090/targets     ← todos los servicios en Prometheus
```

---

## Consolas de administración

| Recurso | URL | Usuario / Contraseña |
|---|---|---|
| **Frontend nodo 1** | http://localhost:4200 | — |
| **Frontend nodo 2** | http://localhost:4201 | — |
| **Frontend nodo 3** | http://localhost:4202 | — |
| **Swagger UI nodo 1** | http://localhost:8080/swagger-ui.html | — |
| **Swagger UI nodo 2** | http://localhost:8090/swagger-ui.html | — |
| **Swagger UI nodo 3** | http://localhost:8100/swagger-ui.html | — |
| **Keycloak Admin nodo 1** | http://localhost:8180/admin | admin / admin |
| **Keycloak Admin nodo 2** | http://localhost:8280/admin | admin / admin |
| **Keycloak Admin nodo 3** | http://localhost:8380/admin | admin / admin |
| **Grafana nodo 1** | http://localhost:3000 | admin / admin |
| **Grafana nodo 2** | http://localhost:3001 | admin / admin |
| **Grafana nodo 3** | http://localhost:3002 | admin / admin |
| **Prometheus nodo 1** | http://localhost:9090 | — |
| **Prometheus nodo 2** | http://localhost:9091 | — |
| **Prometheus nodo 3** | http://localhost:9092 | — |
| RabbitMQ nodo 1 | http://localhost:15672 | guest / guest |
| RabbitMQ nodo 2 | http://localhost:15673 | guest / guest |
| RabbitMQ nodo 3 | http://localhost:15674 | guest / guest |
| Actuator gateway nodo 1 | http://localhost:8080/actuator/health | — |
| Métricas Prometheus nodo 1 | http://localhost:8081/actuator/prometheus | — |

### Consultar réplica de PostgreSQL

```bash
# Conectar a la réplica de nodo 1 (solo lectura, puerto 5435)
psql -h localhost -p 5435 -U user -d hospital_db
```

```sql
-- Verificar que es réplica (debe retornar 't')
SELECT pg_is_in_recovery();

-- Ver admisiones replicadas
SELECT id, first_name, last_name, ward, admitted_at
FROM admissions ORDER BY admitted_at DESC LIMIT 10;
```

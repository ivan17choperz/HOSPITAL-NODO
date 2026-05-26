# Hospital Nodo — Sistema Hospitalario Distribuido

Sistema de gestión hospitalaria distribuido en 3 nodos independientes (Bogotá, Medellín, Cali), cada uno con su propia base de datos PostgreSQL, RabbitMQ, Redis y stack completo de microservicios Spring Boot.

---

## Índice

1. [Requisitos](#requisitos)
2. [Arquitectura](#arquitectura)
3. [Levantar los servicios](#levantar-los-servicios)
4. [Puertos por nodo](#puertos-por-nodo)
5. [Cómo hacer las peticiones](#cómo-hacer-las-peticiones)
6. [Endpoints — ms-admission](#ms-admission)
7. [Endpoints — ms-triage](#ms-triage)
8. [Endpoints — ms-emr](#ms-emr)
9. [Endpoints — ms-fhir-adapter](#ms-fhir-adapter)
10. [Endpoints — ms-sync-agent](#ms-sync-agent)
11. [Flujo de prueba end-to-end](#flujo-de-prueba-end-to-end)
12. [Consolas de administración](#consolas-de-administración)

---

## Requisitos

- Docker Desktop (con Docker Compose v2)
- 8 GB RAM mínimo recomendados para los 3 nodos simultáneos
- Puertos `8080–8287`, `5432–5437`, `5672–5674`, `6379–6381`, `15672–15674` libres

---

## Arquitectura

```
┌───────────────────────────────────────────────────────────────────────┐
│  NODO 1 (Bogotá)           NODO 2 (Medellín)       NODO 3 (Cali)     │
│  nginx        :8080         nginx        :8090       nginx        :8100│
│  ├─ admission :8081         ├─ admission :8181       ├─ admission :8281│
│  ├─ discharge :8082         ├─ discharge :8182       ├─ discharge :8282│
│  ├─ emr       :8083         ├─ emr       :8183       ├─ emr       :8283│
│  ├─ laboratory:8084         ├─ laboratory:8184       ├─ laboratory:8284│
│  ├─ fhir      :8085         ├─ fhir      :8185       ├─ fhir      :8285│
│  ├─ sync-agent:8086         ├─ sync-agent:8186       ├─ sync-agent:8286│
│  ├─ triage    :8087         ├─ triage    :8187       ├─ triage    :8287│
│  ├─ rabbitmq  :5672         ├─ rabbitmq  :5673       ├─ rabbitmq  :5674│
│  ├─ redis     :6379         ├─ redis     :6380       ├─ redis     :6381│
│  ├─ postgres  :5432         ├─ postgres  :5433       ├─ postgres  :5434│
│  └─ pg-replica:5435         └─ pg-replica:5436       └─ pg-replica:5437│
│                                                                        │
│          sync-agent ←──── inter-node-network ────→ sync-agent         │
└───────────────────────────────────────────────────────────────────────┘
```

Cuando se admite un paciente en nodo 1, el `ms-sync-agent` reenvía el evento a nodo 2 y nodo 3 automáticamente via HTTP. Cada nodo republica el evento en su RabbitMQ local para que EMR, FHIR y Lab lo procesen.

---

## Levantar los servicios

Ejecutar desde la raíz del proyecto (donde está `docker-compose.yml`):

### Solo nodo 1

```bash
docker-compose --profile node1 up --build
```

### Solo nodo 2

```bash
docker-compose --profile node2 up --build
```

### Los 3 nodos completos

```bash
docker-compose --profile node1 --profile node2 --profile node3 up --build
```

### En segundo plano (background)

```bash
docker-compose --profile node1 --profile node2 --profile node3 up --build -d
```

### Ver logs de un servicio específico

```bash
docker logs hospital-node1-admission -f
docker logs hospital-node1-sync -f
docker logs hospital-node1-fhir -f
```

### Detener todo

```bash
docker-compose --profile node1 --profile node2 --profile node3 down
```

### Detener y borrar volúmenes (reset total de base de datos)

```bash
docker-compose --profile node1 --profile node2 --profile node3 down -v
```

### Reconstruir un solo servicio (después de cambios en el código)

```bash
docker-compose --profile node1 up --build ms-fhir-adapter-node1 -d
```

> **Nota**: La primera vez el build tarda 3–5 minutos porque descarga dependencias Maven. Las siguientes veces usa caché de Docker y es más rápido.

---

## Puertos por nodo

| Servicio | Nodo 1 | Nodo 2 | Nodo 3 |
|---|---|---|---|
| **Nginx (gateway)** | 8080 | 8090 | 8100 |
| ms-admission | 8081 | 8181 | 8281 |
| ms-discharge | 8082 | 8182 | 8282 |
| ms-emr | 8083 | 8183 | 8283 |
| ms-laboratory | 8084 | 8184 | 8284 |
| ms-fhir-adapter | 8085 | 8185 | 8285 |
| ms-sync-agent | 8086 | 8186 | 8286 |
| ms-triage | 8087 | 8187 | 8287 |
| PostgreSQL primary | 5432 | 5433 | 5434 |
| PostgreSQL replica | 5435 | 5436 | 5437 |
| RabbitMQ AMQP | 5672 | 5673 | 5674 |
| RabbitMQ UI | 15672 | 15673 | 15674 |
| Redis | 6379 | 6380 | 6381 |

---

## Cómo hacer las peticiones

Hay dos formas de llegar a cada microservicio:

### Opción A — Via nginx (recomendado)

El nginx de cada nodo escucha en el puerto principal y enruta por prefijo de path. El prefijo `/api/{servicio}/` se **elimina** antes de reenviar al microservicio.

| Servicio | Nodo 1 via nginx | Nodo 2 via nginx | Nodo 3 via nginx |
|---|---|---|---|
| admission | `http://localhost:8080/api/admission/` | `http://localhost:8090/api/admission/` | `http://localhost:8100/api/admission/` |
| triage | `http://localhost:8080/api/triage/` | `http://localhost:8090/api/triage/` | `http://localhost:8100/api/triage/` |
| emr | `http://localhost:8080/api/emr/` | `http://localhost:8090/api/emr/` | `http://localhost:8100/api/emr/` |
| fhir | `http://localhost:8080/api/fhir/` | `http://localhost:8090/api/fhir/` | `http://localhost:8100/api/fhir/` |
| sync | `http://localhost:8080/api/sync/` | `http://localhost:8090/api/sync/` | `http://localhost:8100/api/sync/` |

**Ejemplo**: `POST http://localhost:8080/api/admission/admissions`

### Opción B — Directo al microservicio (útil para Postman/debugging)

Llamar directamente al puerto del servicio **sin** el prefijo `/api/`.

**Ejemplo**: `POST http://localhost:8081/admissions`

> **Importante**: En acceso directo NO incluyas `/api/` en la URL. Ese prefijo solo existe en nginx y no lo entiende el microservicio.

---

## ms-admission

Puerto directo: `8081` (nodo 1) · `8181` (nodo 2) · `8281` (nodo 3)  
Via nginx: `/api/admission/`

Al crear una admisión, el servicio publica un evento `patient.admitted` en RabbitMQ que es recibido automáticamente por EMR, FHIR, Lab y el sync-agent de cada nodo.

---

### `POST /admissions` — Admitir paciente

**URL directa nodo 1**: `http://localhost:8081/admissions`

**Headers**: `Content-Type: application/json`

**Request body**:
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
| `patientId` | UUID | **Sí** | ID único del paciente |
| `firstName` | String | **Sí** | Nombre |
| `lastName` | String | **Sí** | Apellido |
| `gender` | String | No | `male`, `female`, `other`, `unknown` |
| `birthDate` | `YYYY-MM-DD` | No | Fecha de nacimiento |
| `identifier` | String | No | Número de documento (ej. `CC-12345678`) |
| `ward` | String | **Sí** | Sala: `URGENCIAS`, `UCI`, `CIRUGIA`, `PEDIATRIA`, `GINECOLOGIA`, etc. |
| `episodeNumber` | String | No | Número de episodio. Si se omite, se genera automáticamente |
| `targetNodeId` | String | No | Nodo destino para sincronización |

**Response `201 Created`**:
```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "patientId": "550e8400-e29b-41d4-a716-446655440000",
  "firstName": "Carlos",
  "lastName": "Ramirez",
  "gender": "male",
  "birthDate": "1985-03-15",
  "identifier": "CC-12345678",
  "ward": "URGENCIAS",
  "episodeNumber": "EP-2024-001",
  "nodeId": "node1",
  "admittedAt": "2024-11-15T10:30:00",
  "status": "ADMITTED"
}
```

---

### `GET /admissions` — Listar todas las admisiones

**URL directa nodo 1**: `http://localhost:8081/admissions`

**Response `200 OK`**: Array de objetos con la misma estructura que el response del POST.

---

### `GET /admissions/node` — Identificador del nodo actual

**URL directa nodo 1**: `http://localhost:8081/admissions/node`

**Response `200 OK`**:
```json
{
  "nodeId": "node1"
}
```

---

### `GET /reports/summary` — Reporte estadístico de admisiones

**URL directa nodo 1**: `http://localhost:8081/reports/summary`

**Response `200 OK`**:
```json
{
  "nodeId": "node1",
  "generatedAt": "2024-11-15T10:30:00",
  "totalAdmissions": 42,
  "admissionsToday": 5,
  "activeAdmissions": 18,
  "admissionsByWard": {
    "URGENCIAS": 12,
    "UCI": 4,
    "CIRUGIA": 2
  },
  "admissionsByStatus": {
    "ADMITTED": 18,
    "DISCHARGED": 24
  },
  "admissionsByGender": {
    "male": 22,
    "female": 20
  }
}
```

---

## ms-triage

Puerto directo: `8087` (nodo 1) · `8187` (nodo 2) · `8287` (nodo 3)  
Via nginx: `/api/triage/`

---

### `POST /triages` — Registrar triage

**URL directa nodo 1**: `http://localhost:8087/triages`

**Headers**: `Content-Type: application/json`

**Request body**:
```json
{
  "admissionId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
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
  "notes": "Paciente consciente, orientado, dolor abdominal moderado"
}
```

| Campo | Tipo | Requerido | Descripción |
|---|---|---|---|
| `admissionId` | String (UUID) | No | ID de la admisión asociada |
| `patientId` | String (UUID) | No | ID del paciente |
| `vitalSigns.temperature` | Double | No | Temperatura en °C |
| `vitalSigns.heartRate` | Integer | No | Frecuencia cardiaca (lpm) |
| `vitalSigns.systolicBp` | Integer | No | Tensión sistólica (mmHg) |
| `vitalSigns.diastolicBp` | Integer | No | Tensión diastólica (mmHg) |
| `vitalSigns.oxygenSaturation` | Double | No | Saturación de oxígeno (%) |
| `vitalSigns.respiratoryRate` | Integer | No | Frecuencia respiratoria |
| `vitalSigns.painScore` | Integer | No | Escala de dolor 0–10 |
| `category` | Enum | No | `RED`, `ORANGE`, `YELLOW`, `GREEN`, `BLUE` |
| `notes` | String | No | Notas clínicas |

**Categorías Manchester**:
- `RED` — Inmediato, amenaza vital
- `ORANGE` — Muy urgente, situación grave
- `YELLOW` — Urgente, estado estable con riesgo
- `GREEN` — Normal, urgencia menor
- `BLUE` — No urgente, consulta diferible

**Response `200 OK`**:
```json
{
  "id": "tri-uuid-1234",
  "admissionId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "patientId": "550e8400-e29b-41d4-a716-446655440000",
  "temperature": 38.5,
  "heartRate": 95,
  "systolicBp": 130,
  "diastolicBp": 85,
  "oxygenSaturation": 97.0,
  "respiratoryRate": 18,
  "painScore": 4,
  "category": "YELLOW",
  "notes": "Paciente consciente, orientado, dolor abdominal moderado",
  "createdAt": "2024-11-15T10:35:00"
}
```

---

### `GET /triages` — Listar todos los triages

**URL directa nodo 1**: `http://localhost:8087/triages`

**Response `200 OK`**: Array de objetos triage.

---

### `GET /triages/admission/{admissionId}` — Triages de una admisión

**URL directa nodo 1**: `http://localhost:8087/triages/admission/a1b2c3d4-e5f6-7890-abcd-ef1234567890`

**Response `200 OK`**: Array de objetos triage filtrados por la admisión.

---

## ms-emr

Puerto directo: `8083` (nodo 1) · `8183` (nodo 2) · `8283` (nodo 3)  
Via nginx: `/api/emr/`

Historia Clínica Electrónica. Almacena diagnósticos, medicaciones y notas clínicas en formato FHIR-compatible.

---

### `POST /records` — Crear registro EMR completo

**URL directa nodo 1**: `http://localhost:8083/records`

**Headers**: `Content-Type: application/json`

**Request body**:
```json
{
  "admissionId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "patientId": "550e8400-e29b-41d4-a716-446655440000",
  "anamnesis": "Paciente refiere dolor abdominal de 3 días de evolución, náuseas y vómito",
  "diagnoses": [
    {
      "code": "K35.2",
      "display": "Apendicitis aguda con peritonitis generalizada",
      "status": "active"
    }
  ],
  "medications": [
    {
      "code": "J01CA04",
      "display": "Amoxicilina 500mg",
      "dosage": "1 cápsula cada 8 horas por 7 días"
    }
  ],
  "clinicianNotes": "Se solicita valoración por cirugía"
}
```

**Response `200 OK`**:
```json
{
  "id": "rec-uuid-5678",
  "admissionId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "patientId": "550e8400-e29b-41d4-a716-446655440000",
  "anamnesis": "Paciente refiere dolor abdominal de 3 días...",
  "diagnoses": [],
  "medications": [],
  "clinicianNotes": "Se solicita valoración por cirugía",
  "createdAt": "2024-11-15T11:00:00"
}
```

---

### `GET /records` — Listar todos los registros EMR

**URL directa nodo 1**: `http://localhost:8083/records`

**Response `200 OK`**: Array de objetos EMR.

---

### `GET /records/admission/{admissionId}` — Registros de una admisión

**URL directa nodo 1**: `http://localhost:8083/records/admission/a1b2c3d4-e5f6-7890-abcd-ef1234567890`

**Response `200 OK`**: Array de registros EMR de esa admisión.

---

### `GET /episodes/{episodeNumber}` — Historia clínica por episodio

**URL directa nodo 1**: `http://localhost:8083/episodes/EP-2024-001`

**Response `200 OK`**: JSON FHIR Bundle con el historial completo del episodio.

---

### `POST /episodes/{episodeNumber}/condition` — Agregar diagnóstico a un episodio

**URL directa nodo 1**: `http://localhost:8083/episodes/EP-2024-001/condition`

**Headers**: `Content-Type: application/json`

**Request body**:
```json
{
  "patientId": "550e8400-e29b-41d4-a716-446655440000",
  "clinicalStatus": "active",
  "diagnosisCode": "K35.2",
  "diagnosisDisplay": "Apendicitis aguda con peritonitis generalizada"
}
```

**Response `200 OK`**: JSON FHIR Condition resource.

---

### `POST /episodes/{episodeNumber}/medication-request` — Agregar medicación a un episodio

**URL directa nodo 1**: `http://localhost:8083/episodes/EP-2024-001/medication-request`

**Headers**: `Content-Type: application/json`

**Request body**:
```json
{
  "patientId": "550e8400-e29b-41d4-a716-446655440000",
  "intent": "order",
  "medicationCode": "J01CA04",
  "medicationDisplay": "Amoxicilina 500mg",
  "dosageInstruction": "1 cápsula cada 8 horas por 7 días"
}
```

**Response `200 OK`**: JSON FHIR MedicationRequest resource.

---

## ms-fhir-adapter

Puerto directo: `8085` (nodo 1) · `8185` (nodo 2) · `8285` (nodo 3)  
Via nginx: `/api/fhir/`

Adaptador FHIR R5. Persiste y expone recursos clínicos en formato estándar HL7 FHIR R5. Es alimentado automáticamente por eventos RabbitMQ (admisiones, altas).

---

### `GET /patients` — Listar todos los pacientes FHIR

**URL directa nodo 1**: `http://localhost:8085/patients`

**Response `200 OK`**: FHIR Bundle (SearchSet) con todos los Patient resources.

---

### `GET /patients/{id}` — Obtener paciente por ID

**URL directa nodo 1**: `http://localhost:8085/patients/550e8400-e29b-41d4-a716-446655440000`

**Response `200 OK`**: FHIR Patient resource en JSON.  
**Response `404`**: Si no existe.

---

### `GET /observations` — Listar todas las observaciones

**URL directa nodo 1**: `http://localhost:8085/observations`

**Response `200 OK`**: FHIR Bundle con todos los Observation resources (triages, signos vitales).

---

### `GET /observations/{id}` — Obtener observación por ID

**URL directa nodo 1**: `http://localhost:8085/observations/triage-uuid-abcd`

**Response `200 OK`**: FHIR Observation resource en JSON.  
**Response `404`**: Si no existe.

---

### `GET /resources/{type}` — Listar recursos por tipo FHIR

**URL directa nodo 1**: `http://localhost:8085/resources/Condition`

**Tipos válidos**: `Patient`, `Observation`, `Condition`, `MedicationRequest`, `Coverage`, `Organization`

**Response `200 OK`**: FHIR Bundle con todos los recursos del tipo solicitado.

---

### `POST /export/patient` — Exportar paciente al repositorio FHIR

**URL directa nodo 1**: `http://localhost:8085/export/patient`

**Headers**: `Content-Type: application/json`

**Request body**:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "firstName": "Carlos",
  "lastName": "Ramirez",
  "gender": "male",
  "birthDate": "1985-03-15",
  "identifier": "CC-12345678"
}
```

**Response `200 OK`**: FHIR Patient resource en JSON.

---

### `POST /register/core` — Registro completo de paciente

**URL directa nodo 1**: `http://localhost:8085/register/core`

**Headers**: `Content-Type: application/json`

**Request body**:
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "documentType": "CC",
  "documentNumber": "12345678",
  "firstName": "Carlos",
  "middleName": "Alberto",
  "lastName": "Ramirez",
  "secondLastName": "Lopez",
  "birthDate": "1985-03-15",
  "age": 39,
  "biologicalSex": "male",
  "genderIdentity": "male",
  "maritalStatus": "married",
  "birthCountry": "CO",
  "birthState": "Cundinamarca",
  "birthCity": "Bogotá",
  "addressLine": "Cra 7 # 32-16",
  "addressNumberOrApartment": "Apto 301",
  "neighborhood": "La Soledad",
  "postalCode": "110311",
  "residenceCountry": "CO",
  "residenceState": "Cundinamarca",
  "residenceCity": "Bogotá",
  "landlinePhone": "6015551234",
  "mobilePhone": "3001234567",
  "email": "carlos.ramirez@email.com",
  "emergencyContactFullName": "Maria Lopez",
  "emergencyRelationship": "spouse",
  "emergencyPhone": "3009876543"
}
```

**Response `200 OK`**: FHIR Patient resource completo en JSON.

---

### `POST /register/coverage` — Registrar cobertura médica

**URL directa nodo 1**: `http://localhost:8085/register/coverage`

**Headers**: `Content-Type: application/json`

**Request body**:
```json
{
  "patientId": "550e8400-e29b-41d4-a716-446655440000",
  "payerName": "Compensar EPS",
  "payerIdentifier": "COMPENSAR-NIT-800251955",
  "affiliationType": "CONTRIBUTIVO",
  "affiliationNumber": "AFF-2024-98765",
  "affiliationStartDate": "2020-01-01",
  "affiliationEndDate": "2025-12-31",
  "coveragePlan": "PLAN_BASICO",
  "coverageLevel": "ALTA_COMPLEJIDAD"
}
```

**Response `200 OK`**: FHIR Bundle con Organization y Coverage resources.

---

### `POST /triage` — Registrar triage como Observation FHIR

**URL directa nodo 1**: `http://localhost:8085/triage`

**Headers**: `Content-Type: application/json`

**Request body**:
```json
{
  "patientId": "550e8400-e29b-41d4-a716-446655440000",
  "episodeNumber": "EP-2024-001",
  "heartRate": 95,
  "respiratoryRate": 18,
  "oxygenSaturation": 97.0,
  "temperatureCelsius": 38.5,
  "systolicBloodPressure": 130,
  "diastolicBloodPressure": 85,
  "painScale0To10": 4,
  "triageScale": "MANCHESTER"
}
```

| Campo | Descripción |
|---|---|
| `triageScale` | `MANCHESTER` (MTS) o `ESI` |

**Clasificación automática por signos vitales**:

| Nivel | Criterio |
|---|---|
| `MTS-RED` / `ESI-1` | SpO₂ < 90% **ó** TAS < 80 mmHg **ó** FC > 150 lpm |
| `MTS-ORANGE` / `ESI-2` | SpO₂ < 94% **ó** TAS < 95 mmHg **ó** FC > 120 **ó** dolor ≥ 7 **ó** temp ≥ 39°C |
| `MTS-YELLOW` / `ESI-3` | Resto de casos |

**Response `200 OK`**:
```json
{
  "episodeNumber": "EP-2024-001",
  "triageScale": "MANCHESTER",
  "triageLevel": "MTS-YELLOW",
  "observationId": "triage-uuid-abcd-1234",
  "observationFhirJson": "{ ... FHIR Observation JSON ... }"
}
```

---

### `POST /condition` — Registrar diagnóstico/condición en FHIR

**URL directa nodo 1**: `http://localhost:8085/condition`

Recibe un recurso FHIR Condition R5 directamente.

**Headers**: `Content-Type: application/json`

**Request body**:
```json
{
  "resourceType": "Condition",
  "clinicalStatus": {
    "coding": [{
      "system": "http://terminology.hl7.org/CodeSystem/condition-clinical",
      "code": "active"
    }]
  },
  "code": {
    "coding": [{
      "system": "http://hl7.org/fhir/sid/icd-10",
      "code": "K35.2"
    }],
    "text": "Apendicitis aguda con peritonitis generalizada"
  },
  "subject": { "reference": "Patient/550e8400-e29b-41d4-a716-446655440000" },
  "encounter": { "reference": "Encounter/EP-2024-001" }
}
```

**Response `200 OK`**: FHIR Condition resource en JSON con ID asignado.

---

### `POST /medication-request` — Registrar medicación en FHIR

**URL directa nodo 1**: `http://localhost:8085/medication-request`

**Headers**: `Content-Type: application/json`

**Request body**:
```json
{
  "resourceType": "MedicationRequest",
  "status": "active",
  "intent": "order",
  "medication": {
    "concept": {
      "coding": [{
        "system": "http://www.whocc.no/atc",
        "code": "J01CA04"
      }],
      "text": "Amoxicilina 500mg"
    }
  },
  "subject": { "reference": "Patient/550e8400-e29b-41d4-a716-446655440000" },
  "encounter": { "reference": "Encounter/EP-2024-001" },
  "dosageInstruction": [{ "text": "1 cápsula cada 8 horas por 7 días" }]
}
```

**Response `200 OK`**: FHIR MedicationRequest resource en JSON con ID asignado.

---

### `DELETE /resources/{type}/{id}` — Eliminar recurso FHIR

**URL directa nodo 1**: `http://localhost:8085/resources/Patient/550e8400-e29b-41d4-a716-446655440000`

**Tipos válidos**: `Patient`, `Observation`, `Condition`, `MedicationRequest`, `Coverage`, `Organization`

**Response `204 No Content`**: Recurso eliminado (también si no existía — es idempotente).

---

## ms-sync-agent

Puerto directo: `8086` (nodo 1) · `8186` (nodo 2) · `8286` (nodo 3)  
Via nginx: `/api/sync/`

Agente de sincronización entre nodos. Escucha eventos RabbitMQ locales (`patient.admitted`, `patient.discharged`) y los reenvía a los otros nodos via HTTP. Usa Redis para garantizar idempotencia y evitar loops.

---

### `GET /status` — Estado del agente de sincronización

**URL directa nodo 1**: `http://localhost:8086/status`

**Response `200 OK`**:
```json
{
  "nodeId": "node1",
  "peers": [
    "http://ms-sync-agent-node2:8086",
    "http://ms-sync-agent-node3:8086"
  ],
  "eventsForwarded": 12,
  "status": "UP"
}
```

---

### `POST /events` — Recibir evento de un nodo peer

> Este endpoint es llamado **automáticamente** por el sync-agent del nodo origen. No es necesario llamarlo manualmente en condiciones normales.

**URL directa nodo 1**: `http://localhost:8086/events`

**Headers**: `Content-Type: application/json`

**Request body**:
```json
{
  "eventId": "uuid-unico-del-evento",
  "eventType": "patient.admitted",
  "sourceNode": "node2",
  "payloadJson": "{\"patientId\":\"550e8400...\",\"firstName\":\"Carlos\",...}",
  "typeId": "com.parcial_final.ms_admission.event.PatientAdmittedEvent",
  "timestamp": "2024-11-15T10:30:00"
}
```

**Response `200 OK`**: Evento aceptado y republicado en el RabbitMQ local.

---

## Flujo de prueba end-to-end

Secuencia completa para demostrar sincronización entre nodos (todo en Postman o curl):

### Paso 1 — Admitir paciente en nodo 1

```
POST http://localhost:8081/admissions
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

Guarda el `id` y `episodeNumber` de la respuesta para los pasos siguientes.

### Paso 2 — Verificar sincronización en nodo 2

```
GET http://localhost:8181/admissions
```

La admisión de Ana Gomez debe aparecer aquí también, aunque fue creada en nodo 1.

### Paso 3 — Registrar triage en nodo 1

```
POST http://localhost:8087/triages
Content-Type: application/json

{
  "admissionId": "<id del paso 1>",
  "patientId": "550e8400-e29b-41d4-a716-446655440001",
  "vitalSigns": {
    "temperature": 38.2,
    "heartRate": 88,
    "systolicBp": 125,
    "diastolicBp": 80,
    "oxygenSaturation": 98.0,
    "respiratoryRate": 16,
    "painScore": 3
  },
  "category": "GREEN",
  "notes": "Paciente estable, consciente y orientada"
}
```

### Paso 4 — Crear registro EMR en nodo 1

```
POST http://localhost:8083/records
Content-Type: application/json

{
  "admissionId": "<id del paso 1>",
  "patientId": "550e8400-e29b-41d4-a716-446655440001",
  "anamnesis": "Consulta por cefalea y fiebre de 2 días de evolución",
  "diagnoses": [{
    "code": "J06.9",
    "display": "Infección aguda vías respiratorias altas",
    "status": "active"
  }],
  "medications": [{
    "code": "N02BE01",
    "display": "Paracetamol 500mg",
    "dosage": "1 tableta cada 6 horas"
  }],
  "clinicianNotes": "Se indica manejo ambulatorio con control en 48 horas"
}
```

### Paso 5 — Verificar paciente en FHIR nodo 1

```
GET http://localhost:8085/patients
GET http://localhost:8085/observations
```

### Paso 6 — Verificar estado de sincronización de todos los nodos

```
GET http://localhost:8086/status    ← nodo 1 (Bogotá)
GET http://localhost:8186/status    ← nodo 2 (Medellín)
GET http://localhost:8286/status    ← nodo 3 (Cali)
```

El campo `eventsForwarded` debe ser mayor que 0 después del paso 1.

---

## Consolas de administración

| Recurso | URL | Usuario / Contraseña |
|---|---|---|
| RabbitMQ nodo 1 | http://localhost:15672 | guest / guest |
| RabbitMQ nodo 2 | http://localhost:15673 | guest / guest |
| RabbitMQ nodo 3 | http://localhost:15674 | guest / guest |
| Health nginx nodo 1 | http://localhost:8080/health | — |
| Health nginx nodo 2 | http://localhost:8090/health | — |
| Health nginx nodo 3 | http://localhost:8100/health | — |
| Actuator admission nodo 1 | http://localhost:8081/actuator/health | — |

### Consultar réplica de PostgreSQL

```bash
# Conectar a la réplica de nodo 1 (solo lectura, puerto 5435)
psql -h localhost -p 5435 -U user -d hospital_db
```

```sql
-- Verificar que es una réplica (debe retornar 't')
SELECT pg_is_in_recovery();

-- Ver admisiones replicadas
SELECT id, first_name, last_name, ward, admitted_at
FROM admissions
ORDER BY admitted_at DESC
LIMIT 10;
```

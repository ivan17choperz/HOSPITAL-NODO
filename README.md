# Hospital Distributed System

## Resumen

Este repositorio contiene una plataforma distribuida basada en microservicios para un hospital. Incluye servicios para admisión, alta, historia clínica, laboratorio, adaptador FHIR, agente de sincronización y triage.

## Ejecuciones principales

### 1. Docker Compose por nodo

Usa perfiles para levantar un nodo completo con todos los servicios del nodo.

- Nodo 1:
  ```bash
  docker compose --profile node1 up --build
  ```
- Todos los nodos:
  ```bash
  docker compose --profile node1 --profile node2 --profile node3 up --build
  ```
- Detener todo:
  ```bash
  docker compose down
  ```

> Si tu versión de Docker no soporta `docker compose`, usa `docker-compose` en su lugar.

### 2. Comandos por servicio (desde cada carpeta de servicio)

- Con Maven Wrapper:
  ```bash
  cd ms-admission && ./mvnw clean spring-boot:run
  cd ms-discharge && ./mvnw clean spring-boot:run
  cd ms-emr && ./mvnw clean spring-boot:run
  cd ms-laboratory && ./mvnw clean spring-boot:run
  cd ms-fhir-adapter && ./mvnw clean spring-boot:run
  cd ms-sync-agent && ./mvnw clean spring-boot:run
  ```
- Servicio `ms-triage` no incluye wrapper en esta copia, por lo que se debe usar Maven instalado globalmente:
  ```bash
  cd ms-triage && mvn clean spring-boot:run
  ```
- En Windows, usa `mvnw.cmd` si trabajas con los wrappers:
  ```powershell
  cd ms-admission; .\mvnw.cmd clean spring-boot:run
  ```

### 3. Build general de un servicio

- Con wrapper:
  ```bash
  cd ms-emr && ./mvnw clean package
  ```
- Sin wrapper:
  ```bash
  cd ms-triage && mvn clean package
  ```

## Descripción de los microservicios

| Servicio          | Carpeta                         | Puerto local Node 1                        | Descripción                                                                               | Notas                                           |
| ----------------- | ------------------------------- | ------------------------------------------ | ----------------------------------------------------------------------------------------- | ----------------------------------------------- |
| `ms-admission`    | `ms-admission`                  | 8081                                       | Administra los procesos de admisión de pacientes y publica eventos de llegada al sistema. | Usa PostgreSQL y RabbitMQ.                      |
| `ms-discharge`    | `ms-discharge`                  | 8082                                       | Gestiona el proceso de alta médica y publica eventos de salida.                           | Usa PostgreSQL y RabbitMQ.                      |
| `ms-emr`          | `ms-emr`                        | 8083                                       | Servicio de historia clínica electrónica (EMR).                                           | Usa PostgreSQL y RabbitMQ.                      |
| `ms-laboratory`   | `ms-laboratory`                 | 8084                                       | Gestiona órdenes y resultados de laboratorio.                                             | Usa PostgreSQL y RabbitMQ.                      |
| `ms-fhir-adapter` | `ms-fhir-adapter`               | 8085                                       | Adaptador FHIR para interoperabilidad entre servicios y formatos.                         | Usa RabbitMQ.                                   |
| `ms-sync-agent`   | `ms-sync-agent`                 | 8086                                       | Agente de sincronización entre nodos, con Redis y peers para replicación.                 | Usa RabbitMQ, Redis y llamadas HTTP a otros MS. |
| `ms-triage`       | `ms-triage`                     | 8087                                       | Servicio de triage para priorizar pacientes y enrutar datos clínicos.                     | Usa PostgreSQL y RabbitMQ.                      |
| `ms-gateway`      | `nginx/` + `docker-compose.yml` | 8080 (node1) / 8090 (node2) / 8100 (node3) | Gateway API basado en NGINX que enruta peticiones externas al nodo correspondiente.       | Configurado en `nginx/node?.conf`.              |

## Puertos y endpoints útiles

- Nodo 1:
  - API Gateway: `http://localhost:8080`
  - RabbitMQ Management: `http://localhost:15672` (`guest` / `guest`)
  - Postgres: `localhost:5432`
  - Redis: `localhost:6379`
  - Servicios:
    - `ms-admission`: `http://localhost:8081`
    - `ms-discharge`: `http://localhost:8082`
    - `ms-emr`: `http://localhost:8083`
    - `ms-laboratory`: `http://localhost:8084`
    - `ms-fhir-adapter`: `http://localhost:8085`
    - `ms-sync-agent`: `http://localhost:8086`
    - `ms-triage`: `http://localhost:8087`

- Nodo 2:
  - API Gateway: `http://localhost:8090`
  - RabbitMQ Management: `http://localhost:15673`
  - Postgres: `localhost:5433`
  - Redis: `localhost:6380`

- Nodo 3:
  - API Gateway: `http://localhost:8100`
  - RabbitMQ Management: `http://localhost:15674`
  - Postgres: `localhost:5434`
  - Redis: `localhost:6381`

## Notas importantes

- El servicio `ms-triage` no contiene `mvnw` en el directorio. Si necesitas ejecutar el servicio localmente sin Docker, debes tener Maven instalado globalmente o agregar un Maven Wrapper manualmente.
- Todos los servicios de aplicación Java usan PostgreSQL y RabbitMQ en `application.yaml` con valores de entorno definidos en `docker-compose.yml`.
- El gateway no es un servicio Spring Boot; es un `nginx` container configurado en `nginx/node1.conf`, `nginx/node2.conf` y `nginx/node3.conf`.

## Pasos recomendados para iniciar todo

1. Levantar el nodo 1 con Docker Compose:
   ```bash
   docker compose --profile node1 up --build
   ```
2. Verificar que RabbitMQ está activo en `http://localhost:15672`.
3. Verificar que el gateway responde en `http://localhost:8080`.
4. Probar los servicios directamente usando sus puertos locales.

## Componentes adicionales

- `hospital-app/` - interfaz Angular del sistema.

---

Si necesitas, puedo añadir también un resumen de las APIs disponibles por servicio o un ejemplo de endpoint para cada microservicio.

# api-gateway

Puerta de entrada única de SP Parqueaderos (web y app móvil), con Spring Cloud Gateway. Corre en el puerto `8090`.

| Prefijo | Destino por defecto | Variable para cambiarlo |
| --- | --- | --- |
| `/tenant/**` | `http://localhost:8081` | `TENANT_SERVICE_URL` |
| `/operation/**` | `http://localhost:8082` | `PARKING_OPERATION_SERVICE_URL` |
| `/pricing/**` | `http://localhost:8083` | `PRICING_SERVICE_URL` |
| `/payment/**` | `http://localhost:8084` | `PAYMENT_CASH_SERVICE_URL` |
| `/reporting/**` | `http://localhost:8085` | `REPORTING_AUDIT_SERVICE_URL` |

El primer segmento se quita antes de reenviar: `/operation/api/tickets` llega a `http://localhost:8082/api/tickets`.

- **CORS:** lo responde solo el gateway, con los orígenes de `GATEWAY_ALLOWED_ORIGINS`, separados por coma. El encabezado `Origin` no se reenvía a los servicios.
- **Servicio caído:** devuelve `503` con `{ "message": "El servicio de ... no responde..." }`.
- **Autenticación:** el gateway no la valida. Los encabezados `Authorization` y `X-Tenant-Id` pasan tal cual y cada servicio valida la sesión.
- **Salud:** `GET /actuator/health`.

```bash
mvn clean spring-boot:run
```

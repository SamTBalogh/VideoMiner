# Guia de Autenticacion por Tokens (VideoMiner)

## 1. Objetivo
Este documento explica el sistema de autenticacion interna por tokens en VideoMiner:
- Que es el `Management Key` y para que sirve.
- Como se emiten, usan, revocan y rotan tokens.
- Que secretos existen y cual es su rol.
- Como operar esto de forma segura en desarrollo y en entorno real.

## 2. Resumen rapido (mental model)
Piensalo como dos planos distintos:

1. Plano de uso de API (dia a dia):
- Usas `Authorization: Bearer <accessToken>` para llamar endpoints protegidos.

2. Plano de administracion de credenciales:
- Usas `X-Token-Management-Key` solo para crear o revocar tokens.
- Este secreto no debe viajar en llamadas normales de negocio.

Si mezclas ambos planos, aumentas riesgo. Separarlos mejora seguridad operativa.

## 3. Conceptos clave
## 3.1 Access Token (Bearer)
- Es el secreto que el cliente usa en llamadas normales a `/videoMiner/v1/*`.
- Se recibe solo una vez al emitir el token.
- El servidor no guarda el token en claro, guarda solo su hash.

## 3.2 Token ID
- Es un identificador tecnico (UUID) que acompana al token emitido.
- Sirve para revocacion (`DELETE /token/{id}`).
- No autentica por si mismo.

## 3.3 Management Key
- Es una clave administrativa global.
- Autoriza operaciones sensibles de ciclo de vida del token:
  - Emitir token (`POST /token`)
  - Revocar token (`DELETE /token/{id}`)
- No reemplaza al Bearer token en endpoints protegidos.
- Debe estar restringida a muy pocos procesos/personas (idealmente backend-only o CI seguro).

## 3.4 Pepper
- Secreto del servidor usado para hashear tokens.
- Formula actual: `SHA-256(pepper + ":" + accessToken)`.
- Nunca sale del servidor.
- Si hay fuga de base de datos, el pepper dificulta ataques offline.

## 4. Como valida VideoMiner un token
Cuando llega `Authorization: Bearer <token>`:
1. Extrae y valida formato Bearer.
2. Calcula hash con pepper.
3. Busca ese hash en base de datos.
4. Comprueba que el token este activo:
- no revocado
- no expirado (`expiresAt` debe ser mayor que `now`)
5. Si algo falla: 403.

## 5. Endpoints del ciclo de vida del token
Base URL: `http://localhost:8080/videoMiner/v1`

## 5.1 Emitir token
`POST /token`

Headers:
- `Content-Type: application/json`
- `X-Token-Management-Key: <management-key>`

Body opcional:
```json
{
  "ttlHours": 24
}
```

Respuesta `201`:
```json
{
  "tokenId": "uuid",
  "accessToken": "opaque_token",
  "tokenType": "Bearer",
  "createdAt": "2026-04-24T15:00:00Z",
  "expiresAt": "2026-04-25T15:00:00Z"
}
```

## 5.2 Usar token en endpoints protegidos
Ejemplo:
`GET /channels`

Header:
- `Authorization: Bearer <accessToken>`

## 5.3 Revocar token
`DELETE /token/{tokenId}`

Header:
- `X-Token-Management-Key: <management-key>`

Respuesta esperada: `204 No Content`

## 6. Flujo completo recomendado (entorno real)
## 6.1 Bootstrap inicial
1. Un proceso administrativo con `Management Key` emite token.
2. Entrega `accessToken` al consumidor autorizado por canal seguro.
3. El consumidor usa solo Bearer.

## 6.2 Operacion normal
- El cliente llama endpoints con Bearer.
- Nunca usa Management Key en llamadas de negocio.

## 6.3 Rotacion segura de token
Patron recomendado (sin ventana ciega):
1. Emitir token nuevo.
2. Verificar token nuevo en endpoint protegido.
3. Actualizar consumidor para usar token nuevo.
4. Revocar token viejo por `tokenId`.

Este flujo es exactamente el que automatiza el script:
- `scripts/token-bootstrap.sh`

## 7. Script de bootstrap incluido
Ruta:
- `scripts/token-bootstrap.sh`

Variables que usa:
- `VIDEOMINER_BASE_URL` (default: `http://localhost:8080/videoMiner/v1`)
- `VIDEOMINER_MANAGEMENT_KEY` (requerida)
- `TOKEN_TTL_HOURS` (default: `24`)
- `PROTECTED_PATH` (default: `/channels`)

Ejecucion:
```bash
export VIDEOMINER_BASE_URL="http://localhost:8080/videoMiner/v1"
export VIDEOMINER_MANAGEMENT_KEY="TU_MANAGEMENT_KEY"
export TOKEN_TTL_HOURS=24
export PROTECTED_PATH="/channels"

./scripts/token-bootstrap.sh
```

Que hace:
1. Emite token inicial.
2. Prueba el token en endpoint protegido.
3. Emite token nuevo.
4. Prueba token nuevo.
5. Revoca token anterior.
6. Imprime exports finales (`VIDEOMINER_TOKEN_ID`, `VIDEOMINER_TOKEN`).

## 8. Configuracion de secretos
Propiedades en backend:
- `videominer.auth.token.pepper`
- `videominer.auth.token.management-key`
- `videominer.auth.token.default-ttl-hours`
- `videominer.auth.token.min-ttl-hours`
- `videominer.auth.token.max-ttl-hours`

Variables de entorno esperadas:
- `VIDEOMINER_TOKEN_PEPPER`
- `VIDEOMINER_TOKEN_MANAGEMENT_KEY`
- `VIDEOMINER_TOKEN_DEFAULT_TTL_HOURS`
- `VIDEOMINER_TOKEN_MIN_TTL_HOURS`
- `VIDEOMINER_TOKEN_MAX_TTL_HOURS`

Referencias:
- `.env.dev.example`
- `.env.prod.example`
- `docker-compose.yml`

## 9. Errores frecuentes y significado
## 9.1 En endpoints de token (`/token`)
- `403`: falta o es invalido `X-Token-Management-Key`.
- `400`: `ttlHours` invalido o fuera de rango.
- `404` (en revoke): `tokenId` inexistente.

## 9.2 En endpoints protegidos de negocio
- `403` con mensaje de token requerido:
  falta `Authorization`.
- `403` con mensaje de token no valido:
  token mal formado, desconocido, revocado o expirado.

## 10. Buenas practicas de seguridad (realistas)
1. No guardes `Management Key` ni `pepper` en repositorio.
2. Inyecta secretos por vault o variables protegidas de despliegue.
3. Limita acceso a Management Key a procesos administrativos.
4. Usa TTL corto para tokens de integracion cuando sea viable.
5. Rota tokens periodicamente (ej. semanal/mensual segun riesgo).
6. Revoca inmediatamente tokens comprometidos.
7. Loguea eventos de emision/revocacion (quien, cuando, tokenId), nunca el token completo.
8. No expongas endpoints de token a internet publica sin controles adicionales.

## 11. Diferencia critica: Management Key vs Access Token
- `Access Token`: abre puertas de negocio (leer/escribir recursos API).
- `Management Key`: abre la fabrica de llaves (crear/revocar tokens).

Si se filtra un Access Token:
- revocas ese token y limitas impacto.

Si se filtra Management Key:
- un atacante puede emitir muchos tokens validos y revocar existentes.
- es incidente mayor; requiere rotacion urgente de Management Key y auditoria.

## 12. Procedimiento rapido ante incidente
## 12.1 Fuga de Access Token
1. Revocar token comprometido por `tokenId`.
2. Emitir token nuevo.
3. Actualizar consumidor.
4. Revisar logs de uso del token comprometido.

## 12.2 Fuga de Management Key
1. Cambiar `VIDEOMINER_TOKEN_MANAGEMENT_KEY` en entorno seguro.
2. Reiniciar/redistribuir servicio para aplicar nuevo valor.
3. Emitir nuevos tokens para integraciones criticas.
4. Revocar tokens sospechosos.
5. Auditar trazas de emision/revocacion durante la ventana de riesgo.

## 13. Referencia de implementacion (codigo)
- Controller de token:
  - `VideoMiner/src/main/java/aiss/videominer/controller/TokenController.java`
- Logica de validacion, hash, emision y revocacion:
  - `VideoMiner/src/main/java/aiss/videominer/service/TokenService.java`
- Modelo persistido de token:
  - `VideoMiner/src/main/java/aiss/videominer/model/Token.java`
- Script de flujo automatizado:
  - `scripts/token-bootstrap.sh`

---
Si vas a operar esto en produccion, la regla principal es:
`Management Key` muy restringido + rotacion disciplinada de tokens + secretos fuera del repo.

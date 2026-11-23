# Backend_RinconSurProbadores

## Cómo ejecutar

Requisitos: Java 21 y MySQL en `localhost:3306` con usuario `root` sin clave (por ejemplo, Laragon). Las bases de datos se crean solas.

Desde VS Code: abre esta carpeta, ve a **Ejecución y depuración**, elige **Backend completo** y presiona ▶. Inicia Eureka, los 6 servicios y el gateway (en el puerto 8090, porque el 8080 suele estar ocupado por Oracle). Si agregas un servicio nuevo, ejecuta antes `Java: Clean Java Language Server Workspace` para que VS Code lo reconozca como proyecto Maven.

| Servicio | Puerto | Base de datos |
|---|---|---|
| Eureka | 8761 | - |
| roles-service | 8081 | `rinconsur_roles` |
| usuarios-service | 8082 | `rinconsur_usuarios` |
| probadores-service | 8083 | `rinconsur_probadores` |
| turnos-service | 8084 | `rinconsur_turnos` |
| cuentas-inhabilitadas-service | 8085 | `rinconsur_cuentas_inhabilitadas` |
| probadores-inhabilitados-service | 8086 | `rinconsur_probadores_inhabilitados` |
| gateway | 8090 (8080 si lo tienes libre) | - |

No pongas el proyecto en una ruta muy larga (por ejemplo, la carpeta de descargas de WhatsApp): Java no puede abrir los `.jar` si la ruta pasa los 260 caracteres.

## Swagger

Por el gateway, con un selector para cada servicio: `http://localhost:8090/swagger-ui.html`.
Cada servicio también tiene el suyo directo, por ejemplo `http://localhost:8085/swagger-ui.html`.

## Cuentas y probadores eliminados

Cuando el administrador presiona el basurero, el elemento desaparece de la app y no se puede reactivar, pero **no se pierde**: se guarda una copia en un servicio aparte.

| Se elimina | Servicio que guarda la copia | Qué guarda | Endpoint |
|---|---|---|---|
| Cuenta de usuario | `cuentas-inhabilitadas-service` | Usuario, nombre, correo, rol, contraseña cifrada, si estaba bloqueada, quién la eliminó y cuándo | `/api/cuentas-inhabilitadas` |
| Probador | `probadores-inhabilitados-service` | Datos del probador y todas las personas que lo usaron (nombre del invitado, turno, hora de inicio y fin) | `/api/probadores-inhabilitados` |

- Primero se archiva y solo después se borra el original. Si el servicio de archivo no responde, no se elimina nada y la app muestra el error.
- Los archivos solo permiten agregar y consultar: no hay endpoints para borrar ni reactivar. La API de cuentas nunca devuelve la contraseña.
- Un probador ocupado no se puede eliminar.
- El candado (bloquear/desbloquear) es distinto: la cuenta sigue existiendo y se puede desbloquear.
- Si se crea una cuenta o probador con el mismo usuario, correo o código de uno eliminado, el archivo conserva ambos por separado.

## Historial de uso de los probadores

`probadores-service` guarda en la tabla `usos_probador` una fila por cada persona que ocupa un probador: código del probador, `nombre_cliente` (el invitado), `numero_turno`, `inicio` y `fin`. La hora de fin se completa cuando el probador vuelve a quedar libre; vacía significa que la persona sigue dentro.

```sql
SELECT nombre_cliente, numero_turno, inicio, fin
FROM rinconsur_probadores.usos_probador
WHERE codigo_probador = 'P-01'
ORDER BY inicio;
```

También se puede consultar con `GET /api/probadores/{codigo}/historial`. Solo se registran los usos posteriores a esta funcionalidad. Al eliminar un probador, su historial se copia al archivo y se borra de esta tabla.

## Pruebas y cobertura

Desde la carpeta de cada servicio (con Java 21):

```powershell
.\mvnw.cmd verify
```

El reporte de JaCoCo queda en `target/site/jacoco/index.html`.

## Configurar el envío de correos (usuarios-service)

La clave de aplicación de Gmail **no está en el repositorio**. Cada integrante la guarda en un archivo local `.env` que git ignora.

1. Pide la clave de aplicación a quien administra la cuenta `soporte.rinconsur@gmail.com`, por un canal privado (gestor de contraseñas o mensaje directo). Nunca la pegues en un issue, commit, README ni chat del grupo.
2. En la carpeta `usuarios-service`, copia `.env.example` y renombra la copia a `.env`:

   ```powershell
   Copy-Item usuarios-service\.env.example usuarios-service\.env
   ```

3. Abre `usuarios-service\.env` y completa la clave, sin espacios ni comillas:

   ```
   MAIL_USERNAME=soporte.rinconsur@gmail.com
   MAIL_PASSWORD=la-clave-que-te-pasaron
   ```

4. Inicia `usuarios-service` como siempre. Funciona tanto si lo ejecutas desde `usuarios-service` como desde la raíz del backend.

Si no existe `.env`, el servicio arranca igual, pero no envía correos.

En un servidor también puedes definir `MAIL_PASSWORD` como variable de entorno en lugar de usar `.env`.

### Antes de hacer commit

Revisa con `git status` que `usuarios-service/.env` **no** aparezca en la lista. Si aparece, no hagas commit y revisa el `.gitignore`.

### Si cambian la clave

Quien la cambie en Google debe avisar al equipo por el canal privado. Cada integrante actualiza la línea `MAIL_PASSWORD` de su propio `.env`.

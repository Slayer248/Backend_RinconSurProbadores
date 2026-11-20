# Backend_RinconSurProbadores

## Cómo ejecutar

Requisitos: Java 21 y MySQL en `localhost:3306` con usuario `root` sin clave (por ejemplo, Laragon). Las bases de datos se crean solas.

Desde esta carpeta:

```powershell
powershell -ExecutionPolicy Bypass -File .\iniciar-backend.ps1
```

El script inicia Eureka, los 4 servicios y el gateway, cada uno en su propia ventana, y los compila si falta el `.jar`. Para recompilar después de cambiar código, agrega `-Compilar`. Para detenerlos, cierra sus ventanas.

| Servicio | Puerto |
|---|---|
| Eureka | 8761 |
| roles-service | 8081 |
| usuarios-service | 8082 |
| probadores-service | 8083 |
| turnos-service | 8084 |
| gateway | 8080 (o 8090 si 8080 está ocupado, por ejemplo por Oracle) |

No pongas el proyecto en una ruta muy larga (por ejemplo, la carpeta de descargas de WhatsApp): Java no puede abrir los `.jar` si la ruta pasa los 260 caracteres.

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

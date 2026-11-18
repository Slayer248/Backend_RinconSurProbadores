# Backend_RinconSurProbadores

## Inyección de datos

Cada microservicio carga sus datos iniciales al arrancar mediante su clase `DatosIniciales`:

| Servicio | Datos |
| --- | --- |
| roles-service | Roles ADMIN y ASISTENTE |
| usuarios-service | Cuenta `admin` y asistentes `mgonzalez` y `pmunoz` (clave temporal `Rincon2026!`) |
| probadores-service | Probadores P-01 a P-05, con último uso de ejemplo en P-04 y P-05 |
| turnos-service | Cuatro clientes en la fila, solo si la fila está vacía al arrancar |

Para no cargar los datos de demostración, inicia los servicios con `--app.datos-iniciales.demo=false`.

## Uso con MariaDB

MariaDB no es compatible con la detección automática de Hibernate usando `mysql-connector-j`. Inicia cada servicio con estos argumentos:

```
--spring.jpa.database-platform=org.hibernate.dialect.MariaDBDialect
--spring.jpa.properties.hibernate.boot.allow_jdbc_metadata_access=false
```
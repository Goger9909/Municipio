# Municipio Activo

Portal municipal con inicio de sesión, registro y envío de reclamos.

## Para quienes visitan el sitio

Abre la **URL HTTPS del portal** que comparta la municipalidad. Regístrate o inicia sesión desde el navegador; no necesitas instalar nada ni conocer los datos de MySQL.

La dirección `localhost`, el archivo `frontend/index.html` y `file://` no son direcciones públicas: solo sirven para pruebas en el equipo donde se ejecuta el backend.

## Publicar gratis en Render

Para no pagar los **€16 mensuales** del servidor de Clever Cloud, publica la aplicación web en el plan **Free de Render** y conserva la base MySQL actual. El Dockerfile de este proyecto compila el backend y sirve el sitio y la API juntos. La conexión de MySQL se configura una sola vez en el servidor; los visitantes solo abren la URL.

> El alojamiento web Free cuesta €0, pero tiene límites: Render apaga la aplicación tras 15 minutos sin visitas y puede tardar cerca de un minuto en reactivarla. Las imágenes subidas se borran al apagarse o reiniciarse la aplicación. Este plan sirve para presentar un trabajo práctico, no para producción. La base MySQL de Clever Cloud puede tener un costo independiente.

### Configuración (una sola vez)

1. Confirma que GitHub contiene los cambios de este proyecto, incluido el archivo [`Dockerfile`](Dockerfile).
2. En [Render](https://render.com/), crea un **Web Service** y conecta el repositorio `municipio-activo`.
3. Configura el servicio así:

   | Opción | Valor |
   | --- | --- |
   | Runtime | `Docker` |
   | Branch | La rama publicada en GitHub, normalmente `main` |
   | Root Directory | Vacío |
   | Dockerfile Path | `./Dockerfile` |
   | Instance Type | `Free` |

   No agregues un comando de compilación ni de inicio: Render usa el [`Dockerfile`](Dockerfile).
4. En **Environment → Environment Variables**, agrega las siguientes variables del servidor. Obtén los valores de MySQL en Clever Cloud; no pongas la contraseña en GitHub ni en el frontend.

   | Variable | Valor |
   | --- | --- |
   | `SPRING_PROFILES_ACTIVE` | `cloud` |
   | `SESSION_COOKIE_SECURE` | `true` |
   | `MYSQL_ADDON_HOST` | Host MySQL de Clever Cloud |
   | `MYSQL_ADDON_PORT` | Puerto MySQL de Clever Cloud, normalmente `3306` |
   | `MYSQL_ADDON_DB` | Nombre de la base |
   | `MYSQL_ADDON_USER` | Usuario MySQL |
   | `MYSQL_ADDON_PASSWORD` | Contraseña MySQL |

5. Pulsa **Create Web Service** y espera a que el primer despliegue termine correctamente. Si el servicio no puede conectar con la base, verifica las credenciales y que la conexión externa al servidor MySQL esté permitida.
6. Copia la dirección `https://...onrender.com` que muestra Render y compártela. **Esa es la única dirección que necesita quien visita la página.**

La aplicación atiende la página, el login y la base desde el mismo backend, usando HTTPS y una cookie segura para la sesión. No hace falta Apache, CORS ni que los visitantes instalen o configuren nada. Render Free recibe hasta 750 horas de servicio por mes en cada espacio de trabajo; revisa el uso y el plan seleccionado para evitar activar servicios de pago.

Guías: [Render Free](https://render.com/docs/free) y [desplegar con Docker](https://render.com/docs/docker).

## Ejecutar localmente (solo desarrollo)

En Windows, Java 21 y conexión a MySQL son necesarios. Ejecuta [`Iniciar Municipio.bat`](<Iniciar Municipio.bat>), introduce las credenciales privadas de MySQL cuando las solicite y deja abierta la ventana. Después entra en [http://localhost:8080/](http://localhost:8080/). Cierra el backend con `Ctrl+C`.

Este iniciador es para desarrollo; **los visitantes del sitio publicado no lo necesitan**. Apache/XAMPP puede mostrar `404` y `localhost:8080` rechaza la conexión si el backend local no está ejecutándose.

## Cuentas y base de datos

El registro público crea usuarios ciudadanos. Las contraseñas de usuario deben tener de 6 a 8 letras o números.

Si falta el esquema, ejecuta una vez [`crear-esquema-clever-cloud.sql`](frontend/Sql/crear-esquema-clever-cloud.sql) en la consola SQL de la base. Para asignar roles administrativos, primero registra las cuentas y luego sigue [`asignar-roles-municipales.sql`](frontend/Sql/asignar-roles-municipales.sql).

## Reclamos

El backend guarda los reclamos y las imágenes en el servidor. En plataformas con almacenamiento temporal, los archivos de `uploads/` pueden desaparecer cuando se reinicia la aplicación.

## Pruebas

Desde PowerShell, en la carpeta del proyecto:

```powershell
.\backend\mvnw.cmd -f .\backend\pom.xml test
```

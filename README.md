# 📌Proyecto: Qro Territory API
Este proyecto es una API RESTful diseñada para gestionar la distribución territorial oficial de Querétaro. Permite la consulta rápida y paginada del catálogo de delegaciones y colonias, e incluye un panel de administración protegido con llaves de acceso (API Keys) para la gestión segura de los datos.

### 🛠️ Stack Tecnológico y Versiones
El proyecto está empaquetado para funcionar sin necesidad de instalar herramientas locales en tu máquina, gracias a Docker. Las versiones clave son:
* **Lenguaje:** Java 21 (Eclipse Temurin)
* **Framework:** Quarkus (optimizado para la nube)
* **Gestor de dependencias:** Maven 3.9.6
* **Base de Datos:** MySQL 8.0
* **Orquestación:** Docker Compose

---

### 🐳 Anatomía del `compose.yaml`
Tu archivo de orquestación define dos "servicios" (contenedores) que trabajan en equipo.

**1. Servicio `db` (Base de Datos)**
* **Función:** Aloja el motor de MySQL 8.0.
* **Credenciales:** Crea automáticamente la base de datos `qro_territory_db` con el usuario `user_qro` y la contraseña `pass_qro`.
* **Seguridad y Sincronía:** Tiene configurado un `healthcheck`. Esto significa que Docker hace "pings" constantes a MySQL para saber cuándo está realmente listo para recibir conexiones.

**2. Servicio `dev-app` (El Motor de Desarrollo)**
* **Función:** Ejecuta tu código de Java/Quarkus en modo de desarrollo (`mvn quarkus:dev`).
* **Sincronización de Código (`volumes`):** Monta tu carpeta local en `/project` dentro del contenedor. Esto es lo que permite el "Live Reloading": cuando guardas un archivo `.java`, Quarkus se reinicia en milisegundos sin que tengas que reiniciar Docker.
* **Dependencia Inteligente:** Gracias a `depends_on`, este contenedor espera pacientemente a que el contenedor `db` pase su `healthcheck` antes de intentar arrancar, evitando los clásicos errores de "Connection Refused".
* **Caché de Maven:** Usa el volumen `maven-repo` para guardar las librerías de Java descargadas. Así, la próxima vez que levantes el entorno, no tendrás que volver a descargar medio internet.

---

### 💻 Comandos de Arranque y Gestión

Para darle vida a este entorno, abre tu terminal en la carpeta donde está el `compose.yaml` y utiliza estos comandos:

* **Levantar el proyecto:** `docker compose up`
  *(Verás todos los logs en tiempo real. Para salir, presiona `Ctrl + C`).*
* **Levantar en segundo plano (Modo Detached):** `docker compose up -d`
* **Apagar el proyecto:** `docker compose down`

---

### 🧼 Consejos de Supervivencia y "Limpieza" en Docker

Una de las dudas más comunes es si **es bueno borrar todo en Docker e iniciar de cero.** La respuesta corta es: **Sí, es una excelente práctica en etapa de desarrollo.**

A veces, Docker guarda configuraciones, redes temporales o estados en la base de datos que pueden generar comportamientos extraños ("fantasmas" en tu código). Para resolver dudas y limpiar, usa estas herramientas:

**1. El reseteo nuclear (Limpieza Profunda)**
Si sientes que la base de datos tiene basura o el contenedor está fallando por razones misteriosas, el mejor comando que puedes usar es:
```bash
docker compose down -v
```
* **¿Qué hace la `-v`?** Destruye los *volúmenes* no nombrados (como los datos físicos de MySQL). Cuando vuelvas a hacer `docker compose up`, la base de datos nacerá completamente en blanco, lista para que tu script inicial inyecte las 1000 colonias frescas y sin conflictos.

**2. Revisar qué está vivo**
Si no sabes si tu API está corriendo en el fondo, usa:
```bash
docker ps
```
Te mostrará una lista de los contenedores activos y en qué puertos están escuchando.

**3. Revisar los errores (Logs)**
Si levantaste el proyecto con `-d` y algo falla (por ejemplo, Quarkus no arranca), no te quedes a ciegas. Mira el registro de tu aplicación:
```bash
docker logs -f qro-territory-dev
```
*(La `-f` te deja conectado viendo los errores aparecer en tiempo real).*
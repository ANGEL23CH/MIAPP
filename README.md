# Press Planning Scanner — compilación APK con GitHub

Este repositorio compila automáticamente la aplicación Android sin necesitar Android Studio.

## Qué genera

GitHub Actions produce:

- `PressPlanningScanner.apk`: APK de prueba firmado automáticamente e instalable en Android.
- `PressPlanningScanner.apk.sha256`: checksum para verificar la integridad del APK.

El APK abre el módulo móvil de Press Planning y utiliza la cámara del teléfono para leer etiquetas.

## Cómo generar el APK desde el navegador

1. En GitHub, crea un repositorio nuevo.
2. Descomprime `PressPlanningScanner-GitHub-APK.zip` en tu computadora.
3. Sube **el contenido interno** de la carpeta al nivel principal del repositorio. Deben quedar visibles en la raíz:
   - `.github`
   - `app`
   - `build.gradle`
   - `settings.gradle`
   - `gradle.properties`
4. Confirma la carga con **Commit changes**.
5. Abre la pestaña **Actions**.
6. Selecciona **Compilar APK Android**.
7. Presiona **Run workflow** y nuevamente **Run workflow**.
8. Cuando la ejecución muestre una marca verde, ábrela.
9. En **Artifacts**, descarga `PressPlanningScanner-APK`.
10. Descomprime el archivo descargado. Dentro estará `PressPlanningScanner.apk`.

También se inicia una compilación automáticamente cuando se realizan cambios en las ramas `main` o `master`.

## Instalación en el teléfono

1. Copia `PressPlanningScanner.apk` al Android.
2. Ábrelo desde Archivos o Descargas.
3. Android solicitará autorizar temporalmente **Instalar aplicaciones desconocidas** para el navegador o administrador de archivos utilizado.
4. Instala la aplicación.
5. Abre **Press Planning Scanner**.
6. Introduce la dirección del servidor, por ejemplo:

   `http://192.168.1.50:8787/mobile`

El teléfono y la computadora servidor deben estar conectados a la misma red.

## Tipo de APK

El flujo genera un APK `debug`, firmado automáticamente por las herramientas de Android. Es adecuado para instalación interna y pruebas. No debe publicarse en Google Play como versión definitiva.

Para una distribución formal se necesita una llave de firma privada permanente y un flujo `release`. Esa llave nunca debe subirse directamente al repositorio.

## Versiones fijadas

- Android Gradle Plugin: `8.9.2`
- Gradle: `8.11.1`
- Java: `17`
- Compile SDK / Target SDK: `35`
- Minimum SDK: `24`

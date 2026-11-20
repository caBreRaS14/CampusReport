# CampusReport

MVP Android (Kotlin, Jetpack Compose, Room, MVVM) para registrar novedades del campus (daños, luminarias, equipos, conectividad) y consultarlas en un listado. Taller final integrador — Computación móvil.

## Alcance
- Formulario: categoría (Infraestructura / Equipos / Conectividad / Otro), descripción y ubicación escrita.
- Validación: los tres campos son obligatorios; una descripción o ubicación de solo espacios no es válida. Mensaje por campo.
- Guardado en Room (SQLite local) con confirmación (Snackbar) y paso al listado.
- Listado con categoría, ubicación, descripción, fecha y estado.
- Estado inicial: **«Pendiente de sincronización»**. Se eligió sobre «Pendiente de atención» porque el reporte solo está en el dispositivo y no se envía a nadie; así el estado no sugiere un envío que no existe.
- Borrador del formulario en `SavedStateHandle`: se conserva al rotar y al volver de otra app (incluso si el sistema cierra el proceso).
- Filtro por categoría, detalle de cada reporte, edición (conserva fecha y estado) y eliminación con confirmación (v1.2).
- Anti-duplicados: el botón se deshabilita y el ViewModel ignora pulsaciones mientras hay un guardado en curso.

## Limitaciones declaradas
Sin backend ni sincronización real (el estado nunca cambia desde la app), sin búsqueda por texto, sin fotos ni geolocalización (fuera del alcance del taller). No incluye datos de ejemplo.

## Compilar
```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat testDebugUnitTest assembleDebug
.\gradlew.bat connectedDebugAndroidTest   # celular conectado y desbloqueado; desinstala la app al terminar
```
APK: `releases/CampusReport-v1.2-debug.apk`.

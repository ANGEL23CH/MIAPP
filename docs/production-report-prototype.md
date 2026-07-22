# Prototipo — reporte diario de producción y tiempo muerto

Este prototipo convierte el formato de Excel/PPTX en una pantalla táctil para operadores. Conserva una proporción vertical equivalente a **19.05 cm × 30.40 cm** y se adapta al ancho disponible en teléfonos, tabletas y monitores.

## Cómo abrirlo en la APK

La barra superior de la aplicación incluye cuatro accesos:

- **Web**: vuelve al módulo `/mobile` del servidor configurado.
- **Reporte**: abre el prototipo local incluido en la APK.
- **Servidor**: cambia la dirección del servidor.
- **Scan**: abre el lector nativo de códigos.

Si todavía no existe una dirección de servidor guardada, la APK abre el prototipo local al iniciar.

## Datos incluidos

### Encabezado del turno

- Línea de prensa
- Fecha y turno
- Líder
- Supervisor
- Operador
- Gruísta
- Operarios

### Producción por parte

El reporte permite hasta cinco partes. Cada parte registra:

- Número de parte
- Coil No.
- Presión del amortiguador
- Hora de inicio y término de producción
- SPM
- Minutos de cambio de molde
- Etiquetas y piezas por etiqueta
- Cantidad total calculada
- Corrección
- Scrap
- Pieza OK calculada
- Tipos de defecto: superior/inferior, crack, neck, marca, mal formado, materia prima y arruga

### Tiempo muerto

Cada evento registra hora de inicio, hora de fin, duración calculada, departamento, tipo de falla, descripción y responsable/firma. Se incluyeron las 17 causas del formato original:

1. Cambio de molde
2. Falta de pallet
3. Otros de operación
4. Falta de material
5. Descarrilamiento de stacker
6. Material equivocado o dañado
7. Sensor
8. Rebaba
9. Pieza fuera de medida
10. Pieza con defecto
11. Otros de moldes
12. Falla de prensa
13. Falla de robot o grippers
14. Otros de mantenimiento
15. Aprobación de calidad
16. No plan
17. Try Out

## Escaneo

Los botones de cámara llaman a `PressPlanningAndroid.scanBarcode()` cuando la pantalla corre dentro de la APK. La actividad Android devuelve el resultado mediante:

```javascript
window.PressPlanningSetBarcode(codigo)
```

En un navegador normal se usa un cuadro de captura manual como respaldo.

## Persistencia e integración con el servidor

El botón **Guardar borrador** conserva el reporte en `localStorage` del WebView. El botón **Guardar reporte** valida los campos y emite el evento:

```javascript
window.addEventListener('pressPlanningReportSubmit', (event) => {
  const payload = event.detail;
  // Enviar payload al endpoint definitivo.
});
```

También puede obtenerse el contenido actual con:

```javascript
const payload = window.getPressPlanningReportPayload();
```

El prototipo no inventa una ruta de API. La conexión definitiva debe mapear ese `payload` al endpoint y al modelo de datos del servidor Press Planning.

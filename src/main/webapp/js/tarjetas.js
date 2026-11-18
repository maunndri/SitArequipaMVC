// Filtro de la tabla de tarjetas en el navegador.
// Archivo externo (no inline) para cumplir la CSP "script-src 'self'" (mitigación XSS).
// Solo se lee texto (textContent); nunca se inserta HTML, por lo que no abre vectores DOM-XSS.
(function () {
    'use strict';
    document.addEventListener('DOMContentLoaded', function () {
        var input = document.getElementById('filtroTarjetas');
        var tabla = document.getElementById('tablaTarjetas');
        if (!input || !tabla) return;
        input.addEventListener('input', function () {
            var filtro = input.value.toLowerCase().trim();
            var filas = tabla.tBodies.length ? tabla.tBodies[0].rows : [];
            for (var i = 0; i < filas.length; i++) {
                var texto = (filas[i].textContent || '').toLowerCase();
                filas[i].style.display = texto.indexOf(filtro) > -1 ? '' : 'none';
            }
        });
    });
})();

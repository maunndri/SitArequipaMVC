// Emisión de tarjeta: generación de UID sugerido.
// Archivo externo (no inline) para cumplir la CSP "script-src 'self'" (mitigación XSS).
(function () {
    'use strict';
    document.addEventListener('DOMContentLoaded', function () {
        var boton = document.getElementById('btnGenerarUid');
        var campo = document.getElementById('numeroTarjeta');
        if (!boton || !campo) return;
        boton.addEventListener('click', function () {
            var aleatorio = new Uint32Array(1);
            window.crypto.getRandomValues(aleatorio);
            campo.value = 'SIT-' + (100000 + (aleatorio[0] % 900000));
        });
    });
})();

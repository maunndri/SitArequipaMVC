package util;

import org.apache.commons.lang3.StringUtils;

/**
 * 2.8.2. Registro Seguro de Eventos (Zero-Leak Logging):
 * - Enmascaramiento parcial de identificadores de tarjetas (ej. 'SIT-****-0001').
 * - Neutralización de saltos de línea (CRLF Injection) en bitácoras.
 * - Prevención de fuga de contraseñas, tokens y datos sensibles en logs.
 */
public final class LogSeguroUtil {

    private LogSeguroUtil() {}

    /**
     * Enmascara el número de tarjeta protegiendo los datos intermedios.
     * Ejemplo: "SIT-100001" -> "SIT-****-0001", "12345678" -> "****-5678"
     */
    public static String enmascararTarjeta(String numeroTarjeta) {
        if (StringUtils.isBlank(numeroTarjeta)) return "[VACIO]";
        String limpio = sanitizarCRLF(numeroTarjeta.trim());
        if (limpio.length() <= 4) return "****";
        if (limpio.length() <= 8) {
            return "****-" + limpio.substring(limpio.length() - 4);
        }
        int prefijo = Math.min(3, limpio.length() - 4);
        return limpio.substring(0, prefijo) + "-****-" + limpio.substring(limpio.length() - 4);
    }

    /**
     * Sanitiza entradas de usuario para prevenir CRLF Injection (\r, \n) en logs de auditoría.
     */
    public static String sanitizarCRLF(String entrada) {
        if (entrada == null) return "";
        return entrada.replace('\r', ' ').replace('\n', ' ').trim();
    }
}

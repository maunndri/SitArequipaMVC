package seguridad;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Tokens anti-CSRF (OWASP: "Signed Double-Submit Cookie").
 * <p>
 * Formato: {@code nonce.firma}, donde firma = HMAC-SHA256(secreto, nonce).
 * El token viaja en una cookie HttpOnly y, además, en un campo oculto {@code _csrf} de cada formulario.
 * Un sitio atacante no puede leer la cookie ni falsificar la firma, por lo que no puede construir
 * una petición válida (Semana 14 - Prevención CSRF: tokens únicos ocultos en formularios).
 */
public final class CsrfTokens {
    public static final String COOKIE = "SIT_CSRF";
    public static final String PARAMETRO = "_csrf";
    public static final String CABECERA = "X-CSRF-Token";
    public static final String ATRIBUTO = "csrfToken";

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Base64.Encoder B64 = Base64.getUrlEncoder().withoutPadding();
    private final byte[] secreto;

    public CsrfTokens(String secreto) {
        if (secreto == null || secreto.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("El secreto CSRF debe tener al menos 32 bytes.");
        }
        this.secreto = secreto.getBytes(StandardCharsets.UTF_8);
    }

    /** Genera un token nuevo, impredecible y firmado. */
    public String generar() {
        byte[] nonce = new byte[32];
        RANDOM.nextBytes(nonce);
        String n = B64.encodeToString(nonce);
        return n + "." + firmar(n);
    }

    /** true si el token tiene formato correcto y su firma es auténtica. */
    public boolean esAutentico(String token) {
        if (token == null || token.length() > 200) return false;
        int punto = token.indexOf('.');
        if (punto <= 0 || punto != token.lastIndexOf('.')) return false;
        String nonce = token.substring(0, punto);
        return iguales(firmar(nonce), token.substring(punto + 1));
    }

    /**
     * Valida el doble envío: el token del formulario/cabecera debe ser idéntico al de la cookie
     * y además estar firmado por el servidor. Comparación en tiempo constante.
     */
    public boolean validar(String tokenCookie, String tokenEnviado) {
        return esAutentico(tokenCookie) && tokenEnviado != null && iguales(tokenCookie, tokenEnviado);
    }

    private String firmar(String nonce) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secreto, "HmacSHA256"));
            return B64.encodeToString(mac.doFinal(("csrf:" + nonce).getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo firmar el token CSRF", e);
        }
    }

    private static boolean iguales(String a, String b) {
        return MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
    }
}

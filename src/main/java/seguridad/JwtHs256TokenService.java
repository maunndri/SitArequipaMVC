package seguridad;

import excepcion.AutenticacionException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import modelo.Rol;
import modelo.SesionUsuario;

/**
 * JWT HS256 mínimo y estricto. El encabezado debe ser EXACTAMENTE el emitido por nosotros,
 * lo que rechaza ataques con alg=none o cambio de algoritmo. Para producción con más claims
 * (refresh tokens, rotación de claves) se puede migrar a jjwt / nimbus-jose-jwt.
 */
public class JwtHs256TokenService implements TokenService {
    private static final String HEADER_JSON = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
    private static final Pattern PAYLOAD = Pattern.compile(
        "\\{\"uid\":(\\d{1,9}),\"pid\":(\\d{1,9}),\"sub\":\"([A-Za-z0-9_.-]{3,50})\","
        + "\"rol\":\"([A-Z_]{3,30})\",\"iat\":(\\d{1,12}),\"exp\":(\\d{1,12})\\}");
    private static final Base64.Encoder B64 = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder B64D = Base64.getUrlDecoder();

    private final byte[] secreto;
    private final long ttl;
    private final Clock reloj;

    public JwtHs256TokenService(String secreto, long ttlSegundos, Clock reloj) {
        if (secreto == null || secreto.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("El secreto JWT debe tener al menos 32 bytes.");
        }
        this.secreto = secreto.getBytes(StandardCharsets.UTF_8);
        this.ttl = ttlSegundos;
        this.reloj = reloj;
    }

    @Override
    public String emitir(SesionUsuario s) {
        long ahora = reloj.instant().getEpochSecond();
        String payload = "{\"uid\":" + s.idUsuario() + ",\"pid\":" + s.idPersona()
            + ",\"sub\":\"" + s.username() + "\",\"rol\":\"" + s.rol().name()
            + "\",\"iat\":" + ahora + ",\"exp\":" + (ahora + ttl) + "}";
        String cuerpo = b64(HEADER_JSON) + "." + b64(payload);
        return cuerpo + "." + B64.encodeToString(firmar(cuerpo));
    }

    @Override
    public SesionUsuario validar(String token) {
        try {
            if (token == null || token.length() > 2048) throw new AutenticacionException("Token inválido.");
            String[] partes = token.split("\\.", -1);
            if (partes.length != 3) throw new AutenticacionException("Token inválido.");

            byte[] firmaEsperada = firmar(partes[0] + "." + partes[1]);
            byte[] firmaRecibida = B64D.decode(partes[2]);
            if (!MessageDigest.isEqual(firmaEsperada, firmaRecibida)) throw new AutenticacionException("Token inválido.");

            String header = new String(B64D.decode(partes[0]), StandardCharsets.UTF_8);
            if (!HEADER_JSON.equals(header)) throw new AutenticacionException("Token inválido.");

            Matcher m = PAYLOAD.matcher(new String(B64D.decode(partes[1]), StandardCharsets.UTF_8));
            if (!m.matches()) throw new AutenticacionException("Token inválido.");

            long exp = Long.parseLong(m.group(6));
            if (reloj.instant().getEpochSecond() >= exp) throw new AutenticacionException("Sesión expirada.");

            return new SesionUsuario(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)),
                m.group(3), Rol.valueOf(m.group(4)), exp);
        } catch (IllegalArgumentException e) {      // Base64 o Rol.valueOf inválidos
            throw new AutenticacionException("Token inválido.");
        }
    }

    @Override
    public long ttlSegundos() { return ttl; }

    private static String b64(String s) { return B64.encodeToString(s.getBytes(StandardCharsets.UTF_8)); }

    private byte[] firmar(String datos) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secreto, "HmacSHA256"));
            return mac.doFinal(datos.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("HMAC no disponible", e);
        }
    }
}

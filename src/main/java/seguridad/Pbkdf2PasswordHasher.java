package seguridad;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Hash con sal usando PBKDF2-HMAC-SHA256 (incluido en el JDK, sin dependencias).
 * Formato almacenado: pbkdf2$iteraciones$salBase64$hashBase64
 */
public class Pbkdf2PasswordHasher implements PasswordHasher {
    private static final int SAL_BYTES = 16;
    private static final int HASH_BITS = 256;
    private final int iteraciones;
    private final SecureRandom random = new SecureRandom();

    public Pbkdf2PasswordHasher() { this(210_000); }          // recomendación OWASP para PBKDF2-SHA256
    public Pbkdf2PasswordHasher(int iteraciones) { this.iteraciones = iteraciones; }

    @Override
    public String hash(String passwordPlano) {
        byte[] sal = new byte[SAL_BYTES];
        random.nextBytes(sal);
        byte[] h = derivar(passwordPlano.toCharArray(), sal, iteraciones);
        Base64.Encoder b64 = Base64.getEncoder();
        return "pbkdf2$" + iteraciones + "$" + b64.encodeToString(sal) + "$" + b64.encodeToString(h);
    }

    @Override
    public boolean verificar(String passwordPlano, String hashAlmacenado) {
        try {
            String[] p = hashAlmacenado.split("\\$");
            if (p.length != 4 || !"pbkdf2".equals(p[0])) return false;
            int iter = Integer.parseInt(p[1]);
            byte[] sal = Base64.getDecoder().decode(p[2]);
            byte[] esperado = Base64.getDecoder().decode(p[3]);
            byte[] calculado = derivar(passwordPlano.toCharArray(), sal, iter);
            return MessageDigest.isEqual(esperado, calculado);   // comparación en tiempo constante
        } catch (RuntimeException e) {
            return false;
        }
    }

    private static byte[] derivar(char[] password, byte[] sal, int iter) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, sal, iter, HASH_BITS);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("PBKDF2 no disponible", e);
        }
    }
}

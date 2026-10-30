package util;

import excepcion.ValidacionException;
import java.math.BigDecimal;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

/**
 * Validación centralizada de TODA entrada del usuario (lista blanca, longitud y formato).
 * 2.7.3. Emplea Apache Commons Lang (StringUtils) para validación defensiva contra NullPointerException.
 */
public final class Validador {
    private Validador() {}

    private static final Pattern DNI       = Pattern.compile("\\d{8}");
    private static final Pattern CE        = Pattern.compile("[A-Za-z0-9]{6,15}");
    private static final Pattern NOMBRE    = Pattern.compile("[\\p{L}][\\p{L} '\\-]{0,99}");
    private static final Pattern EMAIL     = Pattern.compile("[A-Za-z0-9._%+\\-]{1,64}@[A-Za-z0-9.\\-]{1,190}\\.[A-Za-z]{2,}");
    private static final Pattern TELEFONO  = Pattern.compile("\\+?[0-9 ]{7,15}");
    private static final Pattern TARJETA   = Pattern.compile("[A-Za-z0-9_:\\-\\s]{3,30}");
    private static final Pattern LECTOR    = Pattern.compile("[A-Za-z0-9_\\-]{3,20}");
    private static final Pattern USUARIO   = Pattern.compile("[A-Za-z0-9_.\\-]{3,50}");

    public static final BigDecimal MONTO_MAX_RECARGA = new BigDecimal("500.00");

    public static String documento(String tipo, String valor) {
        String v = requerido(valor, "El número de documento").replaceAll("\\s+", "");
        if ("DNI".equals(tipo)) {
            if (!StringUtils.isNumeric(v) || !DNI.matcher(v).matches()) {
                throw new ValidacionException("El DNI debe tener exactamente 8 dígitos.");
            }
        } else if ("CE".equals(tipo)) {
            if (!CE.matcher(v).matches()) throw new ValidacionException("El carné de extranjería debe tener entre 6 y 15 caracteres.");
        } else {
            throw new ValidacionException("Tipo de documento no soportado.");
        }
        return v;
    }

    public static String nombre(String valor, String campo) {
        String v = requerido(valor, campo);
        if (!NOMBRE.matcher(v).matches()) throw new ValidacionException(campo + " contiene caracteres no permitidos.");
        return v;
    }

    public static String nombreOpcional(String valor, String campo) {
        return StringUtils.isBlank(valor) ? null : nombre(valor, campo);
    }

    public static String email(String valor) {
        if (StringUtils.isBlank(valor)) return null;           // opcional
        String v = StringUtils.trimToEmpty(valor);
        if (v.length() > 150 || !EMAIL.matcher(v).matches()) throw new ValidacionException("El correo no es válido.");
        return v;
    }

    public static String telefono(String valor) {
        if (StringUtils.isBlank(valor)) return null;           // opcional
        String v = StringUtils.trimToEmpty(valor);
        if (!TELEFONO.matcher(v).matches()) throw new ValidacionException("El teléfono no es válido.");
        return v;
    }

    public static String numeroTarjeta(String valor) {
        String v = requerido(valor, "El código de tarjeta").toUpperCase().trim();
        if (!TARJETA.matcher(v).matches()) throw new ValidacionException("El código de tarjeta debe tener entre 3 y 30 caracteres alfanuméricos.");
        return v.replaceAll("[:\\s]", "");
    }

    public static String idLector(String valor) {
        String v = requerido(valor, "El lector").toUpperCase();
        if (!LECTOR.matcher(v).matches()) throw new ValidacionException("El identificador del lector no es válido.");
        return v;
    }

    public static String username(String valor) {
        String v = StringUtils.trimToEmpty(valor);
        if (!USUARIO.matcher(v).matches()) throw new ValidacionException("Usuario no válido.");
        return v;
    }

    /** Monto con máximo 2 decimales, > 0 (o >= 0 si permiteCero) y <= tope. */
    public static BigDecimal monto(String valor, boolean permiteCero, BigDecimal tope) {
        String v = requerido(valor, "El monto").replace(',', '.');
        BigDecimal m;
        try {
            m = new BigDecimal(v);
        } catch (NumberFormatException e) {
            throw new ValidacionException("El monto no es un número válido.");
        }
        if (m.scale() > 2) throw new ValidacionException("El monto admite como máximo 2 decimales.");
        if (m.signum() < 0 || (!permiteCero && m.signum() == 0)) throw new ValidacionException("El monto debe ser mayor a cero.");
        if (m.compareTo(tope) > 0) throw new ValidacionException("El monto excede el máximo permitido.");
        return m.setScale(2);
    }

    public static BigDecimal monto(BigDecimal m, boolean permiteCero, BigDecimal tope) {
        if (m == null) throw new ValidacionException("El monto es obligatorio.");
        return monto(m.toPlainString(), permiteCero, tope);
    }

    /** Política de contraseña: mínimo 10 caracteres con mayúscula, minúscula, dígito y símbolo. */
    public static void passwordFuerte(String p) {
        if (p == null || p.length() < 10 || p.length() > 100
            || !p.matches(".*[a-z].*") || !p.matches(".*[A-Z].*")
            || !p.matches(".*\\d.*") || !p.matches(".*[^A-Za-z0-9].*")) {
            throw new ValidacionException("La contraseña debe tener al menos 10 caracteres con mayúscula, minúscula, número y símbolo.");
        }
    }

    private static String requerido(String valor, String campo) {
        if (StringUtils.isBlank(valor)) throw new ValidacionException(campo + " es obligatorio.");
        return StringUtils.trimToEmpty(valor);
    }
}

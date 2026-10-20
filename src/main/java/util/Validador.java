package util;

import excepcion.ValidacionException;
import java.math.BigDecimal;
import java.util.regex.Pattern;

/** Validación centralizada de TODA entrada del usuario (lista blanca, longitud y formato). */
public final class Validador {
    private Validador() {}

    private static final Pattern DNI       = Pattern.compile("\\d{8}");
    private static final Pattern CE        = Pattern.compile("[A-Za-z0-9]{9,12}");
    private static final Pattern NOMBRE    = Pattern.compile("[\\p{L}][\\p{L} '\\-]{1,99}");
    private static final Pattern EMAIL     = Pattern.compile("[A-Za-z0-9._%+\\-]{1,64}@[A-Za-z0-9.\\-]{1,190}\\.[A-Za-z]{2,}");
    private static final Pattern TELEFONO  = Pattern.compile("\\+?[0-9 ]{7,15}");
    private static final Pattern TARJETA   = Pattern.compile("[A-Z0-9\\-]{6,30}");
    private static final Pattern LECTOR    = Pattern.compile("[A-Z0-9\\-]{3,20}");
    private static final Pattern USUARIO   = Pattern.compile("[A-Za-z0-9_.\\-]{3,50}");

    public static final BigDecimal MONTO_MAX_RECARGA = new BigDecimal("500.00");

    public static String documento(String tipo, String valor) {
        String v = requerido(valor, "El número de documento");
        if ("DNI".equals(tipo)) {
            if (!DNI.matcher(v).matches()) throw new ValidacionException("El DNI debe tener 8 dígitos.");
        } else if ("CE".equals(tipo)) {
            if (!CE.matcher(v).matches()) throw new ValidacionException("El carné de extranjería no es válido.");
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
        return (valor == null || valor.isBlank()) ? null : nombre(valor, campo);
    }

    public static String email(String valor) {
        if (valor == null || valor.isBlank()) return null;           // opcional
        String v = valor.trim();
        if (v.length() > 150 || !EMAIL.matcher(v).matches()) throw new ValidacionException("El correo no es válido.");
        return v;
    }

    public static String telefono(String valor) {
        if (valor == null || valor.isBlank()) return null;           // opcional
        String v = valor.trim();
        if (!TELEFONO.matcher(v).matches()) throw new ValidacionException("El teléfono no es válido.");
        return v;
    }

    public static String numeroTarjeta(String valor) {
        String v = requerido(valor, "El código de tarjeta").toUpperCase();
        if (!TARJETA.matcher(v).matches()) throw new ValidacionException("El código de tarjeta no es válido.");
        return v;
    }

    public static String idLector(String valor) {
        String v = requerido(valor, "El lector").toUpperCase();
        if (!LECTOR.matcher(v).matches()) throw new ValidacionException("El identificador del lector no es válido.");
        return v;
    }

    public static String username(String valor) {
        String v = valor == null ? "" : valor.trim();
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
        if (valor == null || valor.isBlank()) throw new ValidacionException(campo + " es obligatorio.");
        return valor.trim();
    }
}

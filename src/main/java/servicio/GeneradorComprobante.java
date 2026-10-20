package servicio;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/** Genera números de comprobante: PREFIJO-yyyyMMddHHmmss-XXXXXX (aleatorio criptográfico). */
public class GeneradorComprobante {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private final Clock reloj;
    private final SecureRandom random = new SecureRandom();

    public GeneradorComprobante(Clock reloj) { this.reloj = reloj; }

    public String siguiente(String prefijo) {
        String fecha = FMT.format(reloj.instant().atZone(ZoneId.of("America/Lima")));
        return prefijo + "-" + fecha + "-" + String.format("%06d", random.nextInt(1_000_000));
    }
}

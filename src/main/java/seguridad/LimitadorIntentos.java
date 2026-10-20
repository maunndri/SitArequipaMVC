package seguridad;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Bloqueo temporal tras N fallos de login (mitiga fuerza bruta). */
public class LimitadorIntentos {
    private record Estado(int fallos, Instant primerFallo) {}

    private final Map<String, Estado> estados = new ConcurrentHashMap<>();
    private final int maxIntentos;
    private final Duration ventana;
    private final Clock reloj;

    public LimitadorIntentos(int maxIntentos, Duration ventana, Clock reloj) {
        this.maxIntentos = maxIntentos; this.ventana = ventana; this.reloj = reloj;
    }

    public boolean bloqueado(String clave) {
        Estado e = estados.get(clave);
        if (e == null) return false;
        if (reloj.instant().isAfter(e.primerFallo().plus(ventana))) { estados.remove(clave); return false; }
        return e.fallos() >= maxIntentos;
    }

    public void registrarFallo(String clave) {
        Instant ahora = reloj.instant();
        estados.merge(clave, new Estado(1, ahora), (viejo, nuevo) ->
            ahora.isAfter(viejo.primerFallo().plus(ventana)) ? nuevo
                : new Estado(viejo.fallos() + 1, viejo.primerFallo()));
    }

    public void limpiar(String clave) { estados.remove(clave); }
}

package fakes;

import dao.*;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.*;
import java.util.function.Supplier;
import modelo.*;

/** DAOs en memoria para probar los servicios sin base de datos. */
public final class Fakes {
    private Fakes() {}

    public static Clock reloj(String instanteIso) { return Clock.fixed(Instant.parse(instanteIso), ZoneOffset.UTC); }

    public static class TransaccionDirecta implements Transaccion {
        public int ejecuciones = 0;
        @Override public <T> T ejecutar(Supplier<T> trabajo) { ejecuciones++; return trabajo.get(); }
    }

    public static class UsuarioFake implements UsuarioDAO {
        public final Map<String, Usuario> datos = new HashMap<>();
        @Override public Optional<Usuario> buscarPorUsername(String u) { return Optional.ofNullable(datos.get(u)); }
    }

    public static class TarjetaFake implements TarjetaDAO {
        public final Map<String, Tarjeta> datos = new HashMap<>();
        @Override public Optional<Tarjeta> buscarPorNumero(String n) { return Optional.ofNullable(datos.get(n)); }
        @Override public Optional<Tarjeta> buscarParaActualizar(String n) { return buscarPorNumero(n); }
        @Override public boolean existeActivaDePasajero(int id) {
            return datos.values().stream().anyMatch(t -> t.getIdPasajero() == id && t.estaActiva());
        }
        @Override public void insertar(Tarjeta t) { datos.put(t.getNumero(), t); }
        @Override public void actualizarSaldo(String n, BigDecimal s) { /* el objeto en memoria ya fue mutado */ }
    }

    public static class PasajeroFake implements PasajeroDAO {
        public final Map<String, Pasajero> datos = new HashMap<>();
        private int seq = 100;
        @Override public Optional<Pasajero> buscarPorDocumento(String d) { return Optional.ofNullable(datos.get(d)); }
        @Override public Pasajero insertar(Pasajero p) { p.setIdPersona(++seq); datos.put(p.getDocumento(), p); return p; }
    }

    public static class ComprobanteFake implements ComprobanteDAO {
        public final List<String> numeros = new ArrayList<>();
        @Override public void insertar(String n, String tipo, BigDecimal m) { numeros.add(n); }
    }

    public static class VentaFake implements VentaTarjetaDAO {
        public int ventas = 0;
        @Override public void insertar(String t, int op, String c, BigDecimal m) { ventas++; }
    }

    public static class RecargaFake implements RecargaDAO {
        public final List<Recarga> datos = new ArrayList<>();
        @Override public void insertar(Recarga r) { datos.add(r); }
    }

    public static class RutaFake implements RutaDAO {
        public final Map<Integer, Ruta> datos = new HashMap<>();
        @Override public Optional<Ruta> buscarPorId(int id) { return Optional.ofNullable(datos.get(id)); }
    }

    public static class LectorFake implements LectorBusDAO {
        public final Set<String> ids = new HashSet<>();
        @Override public boolean existe(String id) { return ids.contains(id); }
    }

    public static class ViajeFake implements ViajeDAO {
        public final List<Viaje> viajes = new ArrayList<>();
        public final List<MotivoRechazo> rechazos = new ArrayList<>();
        @Override public int insertar(Viaje v) { viajes.add(v); return viajes.size(); }
        @Override public void insertarRechazo(int id, MotivoRechazo m) { rechazos.add(m); }
    }

    public static class BitacoraFake implements BitacoraDAO {
        public final List<String> acciones = new ArrayList<>();
        @Override public void registrar(Integer u, String accion, String e, String ip) { acciones.add(accion); }
    }
}

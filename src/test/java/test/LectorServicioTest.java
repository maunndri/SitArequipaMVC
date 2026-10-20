package test;

import static org.junit.jupiter.api.Assertions.*;

import excepcion.NegocioException;
import fakes.Fakes;
import java.math.BigDecimal;
import java.time.LocalDate;
import modelo.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import servicio.LectorServicio;
import servicio.PoliticaTarifaBase;

class LectorServicioTest {
    private Fakes.TarjetaFake tarjetas;
    private Fakes.RutaFake rutas;
    private Fakes.LectorFake lectores;
    private Fakes.ViajeFake viajes;
    private Fakes.BitacoraFake bitacora;
    private LectorServicio servicio;
    private final SesionUsuario operador = new SesionUsuario(7, 21, "operador1", Rol.OPERADOR_PV, 0);

    private Tarjeta tarjeta(String n, EstadoTarjeta e, String saldo) {
        return new Tarjeta(n, 101, PerfilTarifa.GENERAL, e, new BigDecimal(saldo), LocalDate.now());
    }

    @BeforeEach
    void preparar() {
        tarjetas = new Fakes.TarjetaFake(); rutas = new Fakes.RutaFake(); lectores = new Fakes.LectorFake();
        viajes = new Fakes.ViajeFake(); bitacora = new Fakes.BitacoraFake();
        rutas.datos.put(1, new Ruta(1, "TRONCAL-C1", "Río Seco - Socabaya", new BigDecimal("1.00"), true));
        rutas.datos.put(2, new Ruta(2, "INACTIVA", "Ruta cerrada", new BigDecimal("1.00"), false));
        lectores.ids.add("LECTOR-001");
        servicio = new LectorServicio(tarjetas, rutas, lectores, viajes, bitacora,
            new PoliticaTarifaBase(), new Fakes.TransaccionDirecta());
        tarjetas.datos.put("04-A3-1F", tarjeta("04-A3-1F", EstadoTarjeta.ACTIVA, "3.50"));
    }

    @Test void saldoSuficienteAprueba_DescuentaTarifaYRegistraViaje() {
        var r = servicio.cobrar("04-A3-1F", 1, "LECTOR-001", operador, "ip");
        assertTrue(r.aprobado());
        assertEquals(new BigDecimal("2.50"), r.saldoRestante());
        assertEquals(new BigDecimal("2.50"), tarjetas.datos.get("04-A3-1F").getSaldo());
        assertEquals(EstadoViaje.APROBADO, viajes.viajes.get(0).getEstado());
        assertTrue(viajes.rechazos.isEmpty());
    }

    @Test void saldoExactoALaTarifaAprueba_YQuedaEnCero() {
        tarjetas.datos.put("04-C5-3H", tarjeta("04-C5-3H", EstadoTarjeta.ACTIVA, "1.00"));
        var r = servicio.cobrar("04-C5-3H", 1, "LECTOR-001", operador, "ip");
        assertTrue(r.aprobado());
        assertEquals(0, r.saldoRestante().compareTo(BigDecimal.ZERO));
    }

    @Test void saldoInsuficienteRechaza_NoCambiaSaldoYRegistraMotivo() {
        tarjetas.datos.put("04-D6-4I", tarjeta("04-D6-4I", EstadoTarjeta.ACTIVA, "0.50"));
        var r = servicio.cobrar("04-D6-4I", 1, "LECTOR-001", operador, "ip");
        assertFalse(r.aprobado());
        assertEquals(MotivoRechazo.SALDO_INSUFICIENTE, r.motivo());
        assertEquals(new BigDecimal("0.50"), tarjetas.datos.get("04-D6-4I").getSaldo(), "nunca saldo negativo");
        assertEquals(EstadoViaje.RECHAZADO, viajes.viajes.get(0).getEstado());
        assertEquals(MotivoRechazo.SALDO_INSUFICIENTE, viajes.rechazos.get(0));
    }

    @Test void tarjetaBloqueadaRechaza() {
        tarjetas.datos.put("04-E7-5J", tarjeta("04-E7-5J", EstadoTarjeta.BLOQUEADA, "50.00"));
        var r = servicio.cobrar("04-E7-5J", 1, "LECTOR-001", operador, "ip");
        assertEquals(MotivoRechazo.TARJETA_BLOQUEADA, r.motivo());
        assertEquals(new BigDecimal("50.00"), tarjetas.datos.get("04-E7-5J").getSaldo());
    }

    @Test void tarjetaAnuladaRechazaConSuPropioMotivo() {
        tarjetas.datos.put("04-F8-6K", tarjeta("04-F8-6K", EstadoTarjeta.ANULADA, "50.00"));
        assertEquals(MotivoRechazo.TARJETA_ANULADA, servicio.cobrar("04-F8-6K", 1, "LECTOR-001", operador, "ip").motivo());
    }

    @Test void tarjetaInexistenteRechazaYSoloSeAudita() {
        var r = servicio.cobrar("ZZ-99-99", 1, "LECTOR-001", operador, "ip");
        assertEquals(MotivoRechazo.TARJETA_INEXISTENTE, r.motivo());
        assertTrue(viajes.viajes.isEmpty());
        assertTrue(bitacora.acciones.contains("COBRO_RECHAZADO_INEXISTENTE"));
    }

    @Test void rutaInactivaOInexistenteNoPermiteCobrar() {
        assertThrows(NegocioException.class, () -> servicio.cobrar("04-A3-1F", 2, "LECTOR-001", operador, "ip"));
        assertThrows(NegocioException.class, () -> servicio.cobrar("04-A3-1F", 99, "LECTOR-001", operador, "ip"));
    }

    @Test void lectorDesconocidoNoPermiteCobrar() {
        assertThrows(NegocioException.class, () -> servicio.cobrar("04-A3-1F", 1, "LECTOR-999", operador, "ip"));
    }

    @Test void laTarifaVieneDeLaPoliticaInyectada() {
        LectorServicio caro = new LectorServicio(tarjetas, rutas, lectores, viajes, bitacora,
            (ruta, perfil) -> new BigDecimal("2.00"), new Fakes.TransaccionDirecta());
        assertEquals(new BigDecimal("1.50"), caro.cobrar("04-A3-1F", 1, "LECTOR-001", operador, "ip").saldoRestante());
    }
}

package test;

import static org.junit.jupiter.api.Assertions.*;

import excepcion.NegocioException;
import excepcion.ValidacionException;
import fakes.Fakes;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import modelo.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import servicio.GeneradorComprobante;
import servicio.RecargaServicio;

class RecargaServicioTest {
    private Fakes.TarjetaFake tarjetas;
    private Fakes.RecargaFake recargas;
    private Fakes.ComprobanteFake comprobantes;
    private Fakes.BitacoraFake bitacora;
    private List<String> mensajes;
    private RecargaServicio servicio;
    private final SesionUsuario operador = new SesionUsuario(7, 21, "operador1", Rol.OPERADOR_PV, 0);

    private Tarjeta tarjeta(String numero, EstadoTarjeta estado, String saldo) {
        return new Tarjeta(numero, 101, PerfilTarifa.GENERAL, estado, new BigDecimal(saldo), LocalDate.now());
    }

    @BeforeEach
    void preparar() {
        tarjetas = new Fakes.TarjetaFake(); recargas = new Fakes.RecargaFake();
        comprobantes = new Fakes.ComprobanteFake(); bitacora = new Fakes.BitacoraFake();
        mensajes = new ArrayList<>();
        servicio = new RecargaServicio(tarjetas, recargas, comprobantes, bitacora, new Fakes.TransaccionDirecta(),
            new GeneradorComprobante(Fakes.reloj("2026-10-10T12:00:00Z")), mensajes::add);
        tarjetas.datos.put("04-A3-1F", tarjeta("04-A3-1F", EstadoTarjeta.ACTIVA, "3.50"));
        tarjetas.datos.put("04-B4-2G", tarjeta("04-B4-2G", EstadoTarjeta.BLOQUEADA, "3.50"));
    }

    @Test void recargaSumaSaldoYGuardaSaldoAnteriorYPosterior() {
        var r = servicio.recargar("04-A3-1F", "20", operador, "ip");
        assertEquals(new BigDecimal("3.50"), r.saldoAnterior());
        assertEquals(new BigDecimal("23.50"), r.saldoPosterior());
        assertEquals(new BigDecimal("23.50"), tarjetas.datos.get("04-A3-1F").getSaldo());
        assertEquals(1, recargas.datos.size());
        assertEquals(new BigDecimal("3.50"), recargas.datos.get(0).getSaldoAnterior());
        assertEquals(1, comprobantes.numeros.size());
        assertTrue(bitacora.acciones.contains("RECARGA"));
    }

    @Test void rechazaRecargaEnTarjetaBloqueadaSinTocarSaldo() {
        assertThrows(NegocioException.class, () -> servicio.recargar("04-B4-2G", "10", operador, "ip"));
        assertEquals(new BigDecimal("3.50"), tarjetas.datos.get("04-B4-2G").getSaldo());
        assertTrue(recargas.datos.isEmpty());
    }

    @Test void rechazaTarjetaInexistente() {
        assertThrows(NegocioException.class, () -> servicio.recargar("ZZ-99-99", "10", operador, "ip"));
    }

    @Test void rechazaMontosInvalidos() {
        assertThrows(ValidacionException.class, () -> servicio.recargar("04-A3-1F", "-10", operador, "ip"));
        assertThrows(ValidacionException.class, () -> servicio.recargar("04-A3-1F", "0", operador, "ip"));
        assertThrows(ValidacionException.class, () -> servicio.recargar("04-A3-1F", "9999", operador, "ip"));
        assertThrows(ValidacionException.class, () -> servicio.recargar("04-A3-1F", "1; DROP TABLE recarga", operador, "ip"));
    }

    @Test void notificacionNoExponeElNumeroCompletoDeTarjeta() {
        servicio.recargar("04-A3-1F", "10", operador, "ip");
        assertEquals(1, mensajes.size());
        assertFalse(mensajes.get(0).contains("04-A3-1F"));
        assertTrue(mensajes.get(0).contains("****"));
    }
}

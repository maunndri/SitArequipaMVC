package test;

import static org.junit.jupiter.api.Assertions.*;

import excepcion.NegocioException;
import excepcion.ValidacionException;
import fakes.Fakes;
import java.math.BigDecimal;
import java.time.LocalDate;
import modelo.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import servicio.EmisionServicio;
import servicio.EmisionServicio.SolicitudEmision;
import servicio.GeneradorComprobante;

class EmisionServicioTest {
    private Fakes.PasajeroFake pasajeros;
    private Fakes.TarjetaFake tarjetas;
    private Fakes.ComprobanteFake comprobantes;
    private Fakes.VentaFake ventas;
    private Fakes.BitacoraFake bitacora;
    private Fakes.TransaccionDirecta tx;
    private EmisionServicio servicio;
    private final SesionUsuario operador = new SesionUsuario(7, 21, "operador1", Rol.OPERADOR_PV, 0);

    @BeforeEach
    void preparar() {
        pasajeros = new Fakes.PasajeroFake(); tarjetas = new Fakes.TarjetaFake();
        comprobantes = new Fakes.ComprobanteFake(); ventas = new Fakes.VentaFake();
        bitacora = new Fakes.BitacoraFake(); tx = new Fakes.TransaccionDirecta();
        servicio = new EmisionServicio(pasajeros, tarjetas, comprobantes, ventas, bitacora, tx,
            new GeneradorComprobante(Fakes.reloj("2026-10-10T12:00:00Z")));
    }

    private SolicitudEmision solicitud(String dni, String tarjeta) {
        return new SolicitudEmision("DNI", dni, "Juan Carlos", "Pérez", "Quispe", "juan@correo.com",
            "987654321", tarjeta, PerfilTarifa.UNIVERSITARIO, "10.00");
    }

    @Test void emiteTarjetaActivaConSaldoInicialYComprobante() {
        var r = servicio.emitir(solicitud("12345678", "04-A3-1F"), operador, "ip");
        assertEquals(new BigDecimal("10.00"), r.saldo());
        assertTrue(tarjetas.datos.get("04-A3-1F").estaActiva());
        assertEquals(1, comprobantes.numeros.size());
        assertEquals(1, ventas.ventas);
        assertTrue(bitacora.acciones.contains("EMITIR_TARJETA"));
        assertEquals(1, tx.ejecuciones, "todo debe ir en una sola transacción");
    }

    @Test void noDuplicaAlPasajeroExistente() {
        servicio.emitir(solicitud("12345678", "04-A3-1F"), operador, "ip");
        tarjetas.datos.clear();                                   // la tarjeta anterior ya no existe
        servicio.emitir(solicitud("12345678", "04-A3-2G"), operador, "ip");
        assertEquals(1, pasajeros.datos.size());
    }

    @Test void rechazaSegundaTarjetaActivaDelMismoPasajero() {
        servicio.emitir(solicitud("12345678", "04-A3-1F"), operador, "ip");
        assertThrows(NegocioException.class, () -> servicio.emitir(solicitud("12345678", "04-A3-2G"), operador, "ip"));
    }

    @Test void rechazaCodigoDeTarjetaYaAsignado() {
        servicio.emitir(solicitud("12345678", "04-A3-1F"), operador, "ip");
        assertThrows(NegocioException.class, () -> servicio.emitir(solicitud("87654321", "04-A3-1F"), operador, "ip"));
    }

    @Test void entradasInvalidasNoLlegaranALaBaseDeDatos() {
        assertThrows(ValidacionException.class, () -> servicio.emitir(solicitud("1234", "04-A3-1F"), operador, "ip"));
        assertEquals(0, tx.ejecuciones);
        assertTrue(pasajeros.datos.isEmpty());
    }
}

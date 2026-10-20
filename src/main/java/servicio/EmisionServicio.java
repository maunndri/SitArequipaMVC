package servicio;

import dao.BitacoraDAO;
import dao.ComprobanteDAO;
import dao.PasajeroDAO;
import dao.TarjetaDAO;
import dao.Transaccion;
import dao.VentaTarjetaDAO;
import excepcion.NegocioException;
import java.math.BigDecimal;
import java.time.LocalDate;
import modelo.EstadoTarjeta;
import modelo.Pasajero;
import modelo.PerfilTarifa;
import modelo.SesionUsuario;
import modelo.Tarjeta;
import util.Validador;

/** RF-001 + RF-002: registra al pasajero (si no existe) y emite su tarjeta. */
public class EmisionServicio {

    public record SolicitudEmision(String tipoDocumento, String documento, String nombres,
                                   String apellidoPaterno, String apellidoMaterno, String email,
                                   String telefono, String numeroTarjeta, PerfilTarifa perfil,
                                   String montoInicial) {}

    public record ResultadoEmision(String numeroTarjeta, String numeroComprobante, BigDecimal saldo) {}

    private final PasajeroDAO pasajeroDAO;
    private final TarjetaDAO tarjetaDAO;
    private final ComprobanteDAO comprobanteDAO;
    private final VentaTarjetaDAO ventaDAO;
    private final BitacoraDAO bitacora;
    private final Transaccion tx;
    private final GeneradorComprobante generador;

    public EmisionServicio(PasajeroDAO pasajeroDAO, TarjetaDAO tarjetaDAO, ComprobanteDAO comprobanteDAO,
                           VentaTarjetaDAO ventaDAO, BitacoraDAO bitacora, Transaccion tx,
                           GeneradorComprobante generador) {
        this.pasajeroDAO = pasajeroDAO; this.tarjetaDAO = tarjetaDAO; this.comprobanteDAO = comprobanteDAO;
        this.ventaDAO = ventaDAO; this.bitacora = bitacora; this.tx = tx; this.generador = generador;
    }

    public ResultadoEmision emitir(SolicitudEmision s, SesionUsuario operador, String ip) {
        // 1) Validar TODAS las entradas antes de tocar la base de datos
        String doc = Validador.documento(s.tipoDocumento(), s.documento());
        String nombres = Validador.nombre(s.nombres(), "Los nombres");
        String apPat = Validador.nombre(s.apellidoPaterno(), "El apellido paterno");
        String apMat = Validador.nombreOpcional(s.apellidoMaterno(), "El apellido materno");
        String email = Validador.email(s.email());
        String telefono = Validador.telefono(s.telefono());
        String numeroTarjeta = Validador.numeroTarjeta(s.numeroTarjeta());
        if (s.perfil() == null) throw new NegocioException("Seleccione un perfil de tarifa.");
        BigDecimal montoInicial = Validador.monto(s.montoInicial(), true, Validador.MONTO_MAX_RECARGA);

        // 2) Una sola transacción: pasajero + tarjeta + comprobante + venta + bitácora
        return tx.ejecutar(() -> {
            Pasajero pasajero = pasajeroDAO.buscarPorDocumento(doc).orElseGet(() -> {
                Pasajero nuevo = new Pasajero();
                nuevo.setTipoDocumento(s.tipoDocumento());
                nuevo.setDocumento(doc);
                nuevo.setNombres(nombres);
                nuevo.setApellidoPaterno(apPat);
                nuevo.setApellidoMaterno(apMat);
                nuevo.setEmail(email);
                nuevo.setTelefono(telefono);
                return pasajeroDAO.insertar(nuevo);
            });

            if (tarjetaDAO.existeActivaDePasajero(pasajero.getIdPersona())) {
                throw new NegocioException("El pasajero ya tiene una tarjeta activa.");
            }
            if (tarjetaDAO.buscarPorNumero(numeroTarjeta).isPresent()) {
                throw new NegocioException("La tarjeta no está disponible.");
            }

            tarjetaDAO.insertar(new Tarjeta(numeroTarjeta, pasajero.getIdPersona(), s.perfil(),
                EstadoTarjeta.ACTIVA, montoInicial, LocalDate.now()));

            String numeroComprobante = generador.siguiente("VTA");
            comprobanteDAO.insertar(numeroComprobante, "RECIBO", montoInicial);
            ventaDAO.insertar(numeroTarjeta, operador.idPersona(), numeroComprobante, montoInicial);
            bitacora.registrar(operador.idUsuario(), "EMITIR_TARJETA", "tarjeta:" + numeroTarjeta, ip);

            return new ResultadoEmision(numeroTarjeta, numeroComprobante, montoInicial);
        });
    }
}

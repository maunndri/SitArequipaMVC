package servicio;

import dao.BitacoraDAO;
import dao.ComprobanteDAO;
import dao.RecargaDAO;
import dao.TarjetaDAO;
import dao.Transaccion;
import excepcion.NegocioException;
import interfaces.Notificador;
import java.math.BigDecimal;
import modelo.Recarga;
import modelo.SesionUsuario;
import modelo.Tarjeta;
import util.Validador;

/** RF-003: recarga atómica con saldo anterior y posterior (trazabilidad). */
public class RecargaServicio {

    public record ResultadoRecarga(String numeroComprobante, BigDecimal saldoAnterior, BigDecimal saldoPosterior) {}

    private final TarjetaDAO tarjetaDAO;
    private final RecargaDAO recargaDAO;
    private final ComprobanteDAO comprobanteDAO;
    private final BitacoraDAO bitacora;
    private final Transaccion tx;
    private final GeneradorComprobante generador;
    private final Notificador notificador;

    public RecargaServicio(TarjetaDAO tarjetaDAO, RecargaDAO recargaDAO, ComprobanteDAO comprobanteDAO,
                           BitacoraDAO bitacora, Transaccion tx, GeneradorComprobante generador,
                           Notificador notificador) {
        this.tarjetaDAO = tarjetaDAO; this.recargaDAO = recargaDAO; this.comprobanteDAO = comprobanteDAO;
        this.bitacora = bitacora; this.tx = tx; this.generador = generador; this.notificador = notificador;
    }

    public ResultadoRecarga recargar(String numeroTarjeta, String montoTexto, SesionUsuario operador, String ip) {
        String numero = Validador.numeroTarjeta(numeroTarjeta);
        BigDecimal monto = Validador.monto(montoTexto, false, Validador.MONTO_MAX_RECARGA);

        ResultadoRecarga resultado = tx.ejecutar(() -> {
            Tarjeta tarjeta = tarjetaDAO.buscarParaActualizar(numero)
                .orElseThrow(() -> new NegocioException("Tarjeta no encontrada."));
            if (!tarjeta.estaActiva()) {
                throw new NegocioException("La tarjeta está " + tarjeta.getEstado().name().toLowerCase() + " y no admite recargas.");
            }

            BigDecimal saldoAnterior = tarjeta.getSaldo();
            tarjeta.acreditar(monto);
            tarjetaDAO.actualizarSaldo(numero, tarjeta.getSaldo());

            String comprobante = generador.siguiente("REC");
            comprobanteDAO.insertar(comprobante, "RECIBO", monto);
            recargaDAO.insertar(new Recarga(numero, operador.idPersona(), comprobante, monto, saldoAnterior, tarjeta.getSaldo()));
            bitacora.registrar(operador.idUsuario(), "RECARGA", "tarjeta:" + numero, ip);
            return new ResultadoRecarga(comprobante, saldoAnterior, tarjeta.getSaldo());
        });

        // Fuera de la transacción: si falla la notificación, la recarga ya está confirmada.
        notificador.enviar("Recarga de S/ " + monto + " en tarjeta ****" + numero.substring(numero.length() - 4));
        return resultado;
    }
}

package servicio;

import dao.BitacoraDAO;
import dao.LectorBusDAO;
import dao.RutaDAO;
import dao.TarjetaDAO;
import dao.Transaccion;
import dao.ViajeDAO;
import excepcion.NegocioException;
import interfaces.PoliticaTarifa;
import java.math.BigDecimal;
import java.util.Optional;
import modelo.EstadoViaje;
import modelo.MotivoRechazo;
import modelo.Ruta;
import modelo.SesionUsuario;
import modelo.Tarjeta;
import modelo.Viaje;
import util.Validador;

/** RF-004: lector simulado. Valida estado y saldo, descuenta la tarifa y registra viaje o rechazo. */
public class LectorServicio {

    public record ResultadoCobro(boolean aprobado, MotivoRechazo motivo, BigDecimal tarifa, BigDecimal saldoRestante) {
        public String mensaje() {
            return aprobado ? "APROBADO" : "RECHAZADO: " + motivo.getDescripcion();
        }
    }

    private final TarjetaDAO tarjetaDAO;
    private final RutaDAO rutaDAO;
    private final LectorBusDAO lectorDAO;
    private final ViajeDAO viajeDAO;
    private final BitacoraDAO bitacora;
    private final PoliticaTarifa politica;
    private final Transaccion tx;

    public LectorServicio(TarjetaDAO tarjetaDAO, RutaDAO rutaDAO, LectorBusDAO lectorDAO, ViajeDAO viajeDAO,
                          BitacoraDAO bitacora, PoliticaTarifa politica, Transaccion tx) {
        this.tarjetaDAO = tarjetaDAO; this.rutaDAO = rutaDAO; this.lectorDAO = lectorDAO;
        this.viajeDAO = viajeDAO; this.bitacora = bitacora; this.politica = politica; this.tx = tx;
    }

    public ResultadoCobro cobrar(String uid, int idRuta, String idLectorTexto, SesionUsuario operador, String ip) {
        String numero = Validador.numeroTarjeta(uid);
        String idLector = Validador.idLector(idLectorTexto);

        Ruta ruta = rutaDAO.buscarPorId(idRuta).filter(Ruta::isActiva)
            .orElseThrow(() -> new NegocioException("La ruta seleccionada no está disponible."));
        if (!lectorDAO.existe(idLector)) throw new NegocioException("El lector seleccionado no existe.");

        return tx.ejecutar(() -> {
            Optional<Tarjeta> encontrada = tarjetaDAO.buscarParaActualizar(numero);
            if (encontrada.isEmpty()) {
                // Sin tarjeta no existe FK para viaje: solo se audita.
                bitacora.registrar(operador.idUsuario(), "COBRO_RECHAZADO_INEXISTENTE", "tarjeta:" + numero, ip);
                return new ResultadoCobro(false, MotivoRechazo.TARJETA_INEXISTENTE, BigDecimal.ZERO, BigDecimal.ZERO);
            }

            Tarjeta tarjeta = encontrada.get();
            BigDecimal tarifa = politica.calcular(ruta, tarjeta.getPerfil());

            MotivoRechazo motivo = null;
            if (!tarjeta.estaActiva()) {
                motivo = tarjeta.getEstado().name().equals("ANULADA")
                    ? MotivoRechazo.TARJETA_ANULADA : MotivoRechazo.TARJETA_BLOQUEADA;
            } else if (!tarjeta.tieneSaldoPara(tarifa)) {
                motivo = MotivoRechazo.SALDO_INSUFICIENTE;
            }

            if (motivo != null) {
                int idViaje = viajeDAO.insertar(new Viaje(numero, idLector, ruta.getId(), EstadoViaje.RECHAZADO, tarifa));
                viajeDAO.insertarRechazo(idViaje, motivo);
                bitacora.registrar(operador.idUsuario(), "COBRO_RECHAZADO", "tarjeta:" + numero, ip);
                return new ResultadoCobro(false, motivo, tarifa, tarjeta.getSaldo());
            }

            tarjeta.debitar(tarifa);
            tarjetaDAO.actualizarSaldo(numero, tarjeta.getSaldo());
            viajeDAO.insertar(new Viaje(numero, idLector, ruta.getId(), EstadoViaje.APROBADO, tarifa));
            bitacora.registrar(operador.idUsuario(), "COBRO_APROBADO", "tarjeta:" + numero, ip);
            return new ResultadoCobro(true, null, tarifa, tarjeta.getSaldo());
        });
    }
}

package notificaciones;

import interfaces.Notificador;
import java.util.logging.Logger;

public class NotificadorEmail implements Notificador {
    private static final Logger LOG = Logger.getLogger(NotificadorEmail.class.getName());

    @Override
    public void enviar(String mensaje) {
        // Simulado: en producción se integraría un servicio de correo.
        LOG.info("[SIMULADO-EMAIL] " + mensaje);
    }
}

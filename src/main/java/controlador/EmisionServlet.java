package controlador;

import excepcion.SitException;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import modelo.PerfilTarifa;
import servicio.EmisionServicio.ResultadoEmision;
import servicio.EmisionServicio.SolicitudEmision;

@WebServlet(name = "EmisionServlet", urlPatterns = {"/app/emision"})
public class EmisionServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        mostrar(req, resp, "emision.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            PerfilTarifa perfil = parsearPerfil(req.getParameter("perfil"));
            SolicitudEmision s = new SolicitudEmision(
                req.getParameter("tipoDocumento"), req.getParameter("documento"), req.getParameter("nombres"),
                req.getParameter("apellidoPaterno"), req.getParameter("apellidoMaterno"),
                req.getParameter("email"), req.getParameter("telefono"),
                req.getParameter("numeroTarjeta"), perfil, req.getParameter("montoInicial"));
            ResultadoEmision r = contenedor().emision.emitir(s, sesion(req), ip(req));
            req.setAttribute("exito", "Tarjeta " + r.numeroTarjeta() + " emitida. Comprobante " + r.numeroComprobante()
                + ". Saldo inicial: S/ " + r.saldo());
        } catch (SitException e) {
            req.setAttribute("error", util.ManejadorErrores.mensajeSeguro(e));
        }
        mostrar(req, resp, "emision.jsp");
    }

    private PerfilTarifa parsearPerfil(String valor) {
        try {
            return PerfilTarifa.valueOf(valor == null ? "" : valor);
        } catch (IllegalArgumentException e) {
            throw new excepcion.ValidacionException("Perfil de tarifa no válido.");
        }
    }
}

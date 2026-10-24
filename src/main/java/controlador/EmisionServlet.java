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
        req.setAttribute("uidSugerido", generarUidAleatorio());
        mostrar(req, resp, "emision.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        try {
            PerfilTarifa perfil = parsearPerfil(req.getParameter("perfil"));
            String numTarjeta = req.getParameter("numeroTarjeta");
            if (numTarjeta == null || numTarjeta.trim().isEmpty()) {
                numTarjeta = generarUidAleatorio();
            }
            SolicitudEmision s = new SolicitudEmision(
                req.getParameter("tipoDocumento"), req.getParameter("documento"), req.getParameter("nombres"),
                req.getParameter("apellidoPaterno"), req.getParameter("apellidoMaterno"),
                req.getParameter("email"), req.getParameter("telefono"),
                numTarjeta, perfil, req.getParameter("montoInicial"));
            ResultadoEmision r = contenedor().emision.emitir(s, sesion(req), ip(req));
            req.setAttribute("exito", "Tarjeta " + r.numeroTarjeta() + " emitida con éxito. Comprobante " + r.numeroComprobante()
                + ". Saldo inicial: S/ " + r.saldo());
            req.setAttribute("uidSugerido", generarUidAleatorio());
        } catch (Throwable e) {
            req.setAttribute("error", util.ManejadorErrores.mensajeSeguro(e));
            String anterior = req.getParameter("numeroTarjeta");
            req.setAttribute("uidSugerido", (anterior != null && !anterior.isBlank()) ? anterior : generarUidAleatorio());
        }
        mostrar(req, resp, "emision.jsp");
    }

    public static String generarUidAleatorio() {
        int aleatorio = 100000 + (int)(Math.random() * 900000);
        return "SIT-" + aleatorio;
    }

    private PerfilTarifa parsearPerfil(String valor) {
        try {
            return PerfilTarifa.valueOf(valor == null ? "" : valor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new excepcion.ValidacionException("Perfil de tarifa no válido.");
        }
    }
}

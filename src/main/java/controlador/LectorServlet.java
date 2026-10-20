package controlador;

import excepcion.SitException;
import excepcion.ValidacionException;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import servicio.LectorServicio.ResultadoCobro;

@WebServlet(name = "LectorServlet", urlPatterns = {"/app/lector"})
public class LectorServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        mostrar(req, resp, "lector.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            int idRuta = parsearEntero(req.getParameter("idRuta"));
            ResultadoCobro r = contenedor().lector.cobrar(
                req.getParameter("uid"), idRuta, req.getParameter("idLector"), sesion(req), ip(req));
            req.setAttribute("resultadoAprobado", r.aprobado());
            req.setAttribute("resultadoMensaje", r.mensaje());
            req.setAttribute("resultadoTarifa", r.tarifa());
            req.setAttribute("resultadoSaldo", r.saldoRestante());
        } catch (SitException e) {
            req.setAttribute("error", util.ManejadorErrores.mensajeSeguro(e));
        }
        mostrar(req, resp, "lector.jsp");
    }

    private int parsearEntero(String valor) {
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            throw new ValidacionException("La ruta seleccionada no es válida.");
        }
    }
}

package controlador;

import excepcion.SitException;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import servicio.RecargaServicio.ResultadoRecarga;

@WebServlet(name = "RecargaServlet", urlPatterns = {"/app/recargas"})
public class RecargaServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        mostrar(req, resp, "recarga.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            ResultadoRecarga r = contenedor().recarga.recargar(
                req.getParameter("numeroTarjeta"), req.getParameter("monto"), sesion(req), ip(req));
            req.setAttribute("exito", "Recarga registrada. Comprobante " + r.numeroComprobante()
                + ". Saldo anterior S/ " + r.saldoAnterior() + " → saldo actual S/ " + r.saldoPosterior());
        } catch (SitException e) {
            req.setAttribute("error", util.ManejadorErrores.mensajeSeguro(e));
        }
        mostrar(req, resp, "recarga.jsp");
    }
}

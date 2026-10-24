package controlador;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "TarjetasServlet", urlPatterns = {"/app/tarjetas"})
public class TarjetasServlet extends BaseServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("tarjetas", contenedor().listarTarjetasConPasajeros());
        mostrar(req, resp, "tarjetas.jsp");
    }
}

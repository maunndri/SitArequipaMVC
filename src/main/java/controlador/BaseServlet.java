package controlador;

import filtro.AutenticacionFilter;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import modelo.SesionUsuario;
import util.ManejadorErrores;

abstract class BaseServlet extends HttpServlet {
    static final String VISTAS = "/WEB-INF/vistas/";

    protected Contenedor contenedor() {
        return (Contenedor) getServletContext().getAttribute("contenedor");
    }

    protected SesionUsuario sesion(HttpServletRequest req) {
        return (SesionUsuario) req.getAttribute(AutenticacionFilter.ATRIBUTO_SESION);
    }

    protected String ip(HttpServletRequest req) {
        return req.getRemoteAddr();   // si hay proxy inverso confiable, configurar RemoteIpValve en Tomcat
    }

    protected void mostrar(HttpServletRequest req, HttpServletResponse resp, String vista) throws ServletException, IOException {
        req.getRequestDispatcher(VISTAS + vista).forward(req, resp);
    }

    protected void mostrarError(HttpServletRequest req, HttpServletResponse resp, String vista, Exception e)
            throws ServletException, IOException {
        req.setAttribute("error", ManejadorErrores.mensajeSeguro(e));
        mostrar(req, resp, vista);
    }
}

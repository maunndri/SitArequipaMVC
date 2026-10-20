package controlador;

import filtro.AutenticacionFilter;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import excepcion.SitException;

@WebServlet(name = "LoginServlet", urlPatterns = {"/login", "/logout"})
public class LoginServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("/logout".equals(req.getServletPath())) { resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED); return; }
        mostrar(req, resp, "login.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("/logout".equals(req.getServletPath())) {
            cookie(resp, "", 0);
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        try {
            String token = contenedor().autenticacion.iniciarSesion(
                req.getParameter("username"), req.getParameter("password"), ip(req));
            cookie(resp, token, contenedor().tokens.ttlSegundos());
            resp.sendRedirect(req.getContextPath() + "/app/dashboard");
        } catch (SitException e) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            mostrarError(req, resp, "login.jsp", e);
        }
    }

    /** Cookie HttpOnly (inaccesible a JS), Secure (solo HTTPS) y SameSite=Strict (anti-CSRF). */
    private void cookie(HttpServletResponse resp, String valor, long maxAge) {
        resp.addHeader("Set-Cookie", AutenticacionFilter.COOKIE + "=" + valor + "; Path=/; Max-Age=" + maxAge
            + "; HttpOnly; SameSite=Strict" + (contenedor().cookieSegura ? "; Secure" : ""));
    }
}

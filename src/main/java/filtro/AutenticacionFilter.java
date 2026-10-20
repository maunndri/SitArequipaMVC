package filtro;

import controlador.Contenedor;
import excepcion.AutenticacionException;
import java.io.IOException;
import java.net.URI;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import modelo.SesionUsuario;

/** Autenticación (JWT) + autorización por rol para todo lo que cuelga de /app/*. */
@WebFilter(urlPatterns = "/app/*")
public class AutenticacionFilter implements Filter {
    public static final String COOKIE = "SIT_TOKEN";
    public static final String ATRIBUTO_SESION = "sesion";
    private Contenedor contenedor;

    @Override
    public void init(FilterConfig config) {
        contenedor = (Contenedor) config.getServletContext().getAttribute("contenedor");
    }

    @Override
    public void doFilter(ServletRequest rq, ServletResponse rs, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) rq;
        HttpServletResponse resp = (HttpServletResponse) rs;

        if (!mismoOrigen(req)) { resp.sendError(HttpServletResponse.SC_FORBIDDEN); return; }   // defensa CSRF adicional

        SesionUsuario sesion;
        try {
            sesion = contenedor.tokens.validar(extraerToken(req));
        } catch (AutenticacionException e) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String ruta = req.getRequestURI().substring(req.getContextPath().length());
        if (!contenedor.permisos.permitido(ruta, sesion.rol())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        req.setAttribute(ATRIBUTO_SESION, sesion);
        chain.doFilter(rq, rs);
    }

    private static String extraerToken(HttpServletRequest req) {
        String auth = req.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) return auth.substring(7);
        if (req.getCookies() != null) {
            for (Cookie c : req.getCookies()) if (COOKIE.equals(c.getName())) return c.getValue();
        }
        return null;
    }

    /** En peticiones que modifican datos, el Origin (si viene) debe coincidir con el host del servidor. */
    private static boolean mismoOrigen(HttpServletRequest req) {
        if ("GET".equals(req.getMethod()) || "HEAD".equals(req.getMethod())) return true;
        String origin = req.getHeader("Origin");
        if (origin == null) return true;
        try {
            return req.getServerName().equalsIgnoreCase(URI.create(origin).getHost());
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override public void destroy() {}
}

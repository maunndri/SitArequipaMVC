package filtro;

import controlador.Contenedor;
import excepcion.AutenticacionException;
import java.io.IOException;
import java.net.URI;
import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import modelo.SesionUsuario;

/** Autenticación (JWT) + autorización por rol para todo lo que cuelga de /app/*. */
// Registrado en web.xml (después de CabecerasSeguridadFilter y CsrfFilter) para garantizar el orden.
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

    /**
     * En peticiones que modifican datos, el Origin (o en su defecto el Referer) debe coincidir con el host
     * del servidor ("Validar el origen de las solicitudes" - Semana 14). Complementa al token CSRF.
     */
    private static boolean mismoOrigen(HttpServletRequest req) {
        if ("GET".equals(req.getMethod()) || "HEAD".equals(req.getMethod())) return true;
        String origen = req.getHeader("Origin");
        if (origen == null || origen.isBlank() || "null".equalsIgnoreCase(origen)) origen = req.getHeader("Referer");
        if (origen == null || origen.isBlank()) return true;   // sin cabeceras: decide el token CSRF
        try {
            String originHost = URI.create(origen).getHost();
            if (originHost == null || originHost.isBlank()) return false;
            String serverName = req.getServerName();
            if (serverName != null && serverName.equalsIgnoreCase(originHost)) return true;
            return isLocal(serverName) && isLocal(originHost);
        } catch (Exception e) {
            return false;                                       // cabecera malformada: se rechaza
        }
    }

    private static boolean isLocal(String host) {
        if (host == null) return false;
        return host.equalsIgnoreCase("localhost") || host.equals("127.0.0.1") || host.equals("::1") || host.equals("0:0:0:0:0:0:0:1");
    }

    @Override public void destroy() {}
}

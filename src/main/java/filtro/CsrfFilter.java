package filtro;

import controlador.Contenedor;
import java.io.IOException;
import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import seguridad.CsrfTokens;

/**
 * Defensa CSRF (OWASP Top 10 - Semana 14).
 * <ul>
 *   <li>GET/HEAD/OPTIONS: garantiza que exista un token firmado en la cookie {@value CsrfTokens#COOKIE}
 *       y lo expone a las vistas como atributo {@code csrfToken}.</li>
 *   <li>POST/PUT/PATCH/DELETE: exige que el campo {@code _csrf} (o la cabecera {@code X-CSRF-Token})
 *       coincida con la cookie. Si no, responde 403 y lo registra en el log.</li>
 * </ul>
 * Se registra en web.xml (después de CabecerasSeguridadFilter y antes de AutenticacionFilter).
 */
public class CsrfFilter implements Filter {
    private static final Logger LOG = LoggerFactory.getLogger(CsrfFilter.class);
    private CsrfTokens csrf;
    private boolean cookieSegura;

    public CsrfFilter() {}

    /** Constructor para pruebas unitarias. */
    public CsrfFilter(CsrfTokens csrf, boolean cookieSegura) {
        this.csrf = csrf;
        this.cookieSegura = cookieSegura;
    }

    @Override
    public void init(FilterConfig config) {
        if (csrf == null) {
            Contenedor c = (Contenedor) config.getServletContext().getAttribute("contenedor");
            this.csrf = c.csrf;
            this.cookieSegura = c.cookieSegura;
        }
    }

    @Override
    public void doFilter(ServletRequest rq, ServletResponse rs, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) rq;
        HttpServletResponse resp = (HttpServletResponse) rs;
        if (req.getCharacterEncoding() == null) req.setCharacterEncoding("UTF-8");

        String tokenCookie = leerCookie(req, CsrfTokens.COOKIE);

        if (esMetodoSeguro(req.getMethod())) {
            String token = csrf.esAutentico(tokenCookie) ? tokenCookie : null;
            if (token == null) {
                token = csrf.generar();
                resp.addHeader("Set-Cookie", CsrfTokens.COOKIE + "=" + token
                    + "; Path=/; HttpOnly; SameSite=Strict" + (cookieSegura ? "; Secure" : ""));
            }
            req.setAttribute(CsrfTokens.ATRIBUTO, token);
            chain.doFilter(rq, rs);
            return;
        }

        // Clientes API que se autentican SOLO con cabecera Authorization (sin cookie de sesión)
        // no son vulnerables a CSRF: el navegador no adjunta esa cabecera automáticamente.
        if (soloBearer(req)) { chain.doFilter(rq, rs); return; }

        String enviado = req.getHeader(CsrfTokens.CABECERA);
        if (enviado == null) enviado = req.getParameter(CsrfTokens.PARAMETRO);

        if (!csrf.validar(tokenCookie, enviado)) {
            LOG.warn("Petición bloqueada por CSRF: {} {} desde {}", req.getMethod(), req.getRequestURI(), req.getRemoteAddr());
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Solicitud no válida (token CSRF ausente o incorrecto).");
            return;
        }
        req.setAttribute(CsrfTokens.ATRIBUTO, tokenCookie);
        chain.doFilter(rq, rs);
    }

    static boolean esMetodoSeguro(String m) {
        return "GET".equals(m) || "HEAD".equals(m) || "OPTIONS".equals(m);
    }

    private static boolean soloBearer(HttpServletRequest req) {
        String auth = req.getHeader("Authorization");
        return auth != null && auth.startsWith("Bearer ") && leerCookie(req, AutenticacionFilter.COOKIE) == null;
    }

    private static String leerCookie(HttpServletRequest req, String nombre) {
        Cookie[] cookies = req.getCookies();
        if (cookies == null) return null;
        for (Cookie c : cookies) if (nombre.equals(c.getName())) return c.getValue();
        return null;
    }

    @Override public void destroy() {}
}

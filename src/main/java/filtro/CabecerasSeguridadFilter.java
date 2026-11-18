package filtro;

import java.io.IOException;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Cabeceras defensivas (XSS, clickjacking, sniffing) + HSTS para forzar HTTPS en el navegador.
 * CSP estricta para scripts: solo archivos propios (/js/*.js), sin JavaScript inline ni eval,
 * de modo que un &lt;script&gt; inyectado (XSS) no se ejecuta aunque llegara a la página.
 * Registrado en web.xml (primer filtro de la cadena).
 */
public class CabecerasSeguridadFilter implements Filter {
    static final String CSP = "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; img-src 'self' data:; "
        + "object-src 'none'; connect-src 'self'; form-action 'self'; frame-ancestors 'none'; base-uri 'self'";

    @Override public void init(FilterConfig c) {}
    @Override public void destroy() {}

    @Override
    public void doFilter(ServletRequest rq, ServletResponse rs, FilterChain chain) throws IOException, ServletException {
        rq.setCharacterEncoding("UTF-8");
        rs.setCharacterEncoding("UTF-8");
        HttpServletRequest req = (HttpServletRequest) rq;
        HttpServletResponse resp = (HttpServletResponse) rs;
        resp.setHeader("X-Content-Type-Options", "nosniff");
        resp.setHeader("X-Frame-Options", "DENY");
        resp.setHeader("Referrer-Policy", "no-referrer");
        resp.setHeader("Permissions-Policy", "camera=(), microphone=(), geolocation=(), payment=()");
        resp.setHeader("Cross-Origin-Opener-Policy", "same-origin");
        resp.setHeader("Content-Security-Policy", CSP);
        if (req.isSecure()) {
            resp.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        }
        if (req.getRequestURI().contains("/app/") || req.getRequestURI().endsWith("/login")) {
            resp.setHeader("Cache-Control", "no-store");
        }
        chain.doFilter(rq, rs);
    }
}

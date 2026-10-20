package filtro;

import java.io.IOException;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Cabeceras defensivas (XSS, clickjacking, sniffing) + HSTS para forzar HTTPS en el navegador. */
@WebFilter(urlPatterns = "/*")
public class CabecerasSeguridadFilter implements Filter {
    @Override public void init(FilterConfig c) {}
    @Override public void destroy() {}

    @Override
    public void doFilter(ServletRequest rq, ServletResponse rs, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) rq;
        HttpServletResponse resp = (HttpServletResponse) rs;
        resp.setHeader("X-Content-Type-Options", "nosniff");
        resp.setHeader("X-Frame-Options", "DENY");
        resp.setHeader("Referrer-Policy", "no-referrer");
        resp.setHeader("Content-Security-Policy",
            "default-src 'self'; script-src 'self'; style-src 'self'; img-src 'self' data:; "
            + "form-action 'self'; frame-ancestors 'none'; base-uri 'self'");
        if (req.isSecure()) {
            resp.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        }
        if (req.getRequestURI().contains("/app/") || req.getRequestURI().endsWith("/login")) {
            resp.setHeader("Cache-Control", "no-store");
        }
        chain.doFilter(rq, rs);
    }
}

package test;

import static org.junit.jupiter.api.Assertions.*;

import excepcion.AutenticacionException;
import excepcion.PersistenciaException;
import excepcion.ValidacionException;
import fakes.Fakes;
import filtro.AutenticacionFilter;
import filtro.CabecerasSeguridadFilter;
import filtro.CsrfFilter;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import javax.servlet.FilterChain;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import modelo.Rol;
import modelo.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import seguridad.CsrfTokens;
import seguridad.JwtHs256TokenService;
import seguridad.LimitadorIntentos;
import seguridad.Pbkdf2PasswordHasher;
import servicio.AutenticacionServicio;
import util.ManejadorErrores;
import util.SanitizadorHtml;
import util.Validador;

/**
 * Suite de Pruebas de Seguridad (Curso Integrador I - Semana 14).
 * Cubre pruebas de Caja Negra (simulacion de ataques / PenTest) y Caja Blanca (inspeccion de codigo y configuracion):
 * 1. Inyeccion SQL (SQL Injection)
 * 2. Cross-Site Scripting (XSS)
 * 3. Cross-Site Request Forgery (CSRF)
 * 4. Autenticacion, Autorizacion y Configuracion Segura (Security Misconfiguration)
 */
class PruebasSeguridadSemana14Test {

    private static final String SECRETO = "secreto-de-pruebas-con-32-bytes-minimo!!";
    private final Clock reloj = Fakes.reloj("2026-11-17T10:00:00Z");
    private AutenticacionServicio authServicio;

    @BeforeEach
    void configurar() {
        Pbkdf2PasswordHasher hasher = new Pbkdf2PasswordHasher(1000);
        JwtHs256TokenService tokens = new JwtHs256TokenService(SECRETO, 3600, reloj);
        Fakes.UsuarioFake usuarios = new Fakes.UsuarioFake();
        usuarios.datos.put("admin", new Usuario(1, 1, "admin", hasher.hash("Admin#2026!"), Rol.ADMINISTRADOR, true));
        authServicio = new AutenticacionServicio(
            usuarios, hasher, tokens, new Fakes.BitacoraFake(),
            new LimitadorIntentos(5, Duration.ofMinutes(15), reloj)
        );
    }

    // =========================================================================
    // 1. PRUEBAS CONTRA INYECCION SQL (SQL INJECTION)
    // =========================================================================

    @Test
    @DisplayName("SQLi (PenTest Login): Payloads clasicos OR '1'='1' son bloqueados sin acceso")
    void sqlInjectionEnLoginEsRechazado() {
        String[] payloads = {
            "admin' OR '1'='1",
            "' OR '1'='1' --",
            "admin'--",
            "' OR 1=1#",
            "'; DROP TABLE usuario; --",
            "' UNION SELECT 1,2,3,4,5,6 FROM usuario --"
        };
        for (String payload : payloads) {
            AutenticacionException ex = assertThrows(
                AutenticacionException.class,
                () -> authServicio.iniciarSesion(payload, "cualquiera", "127.0.0.1"),
                "El payload SQLi debio ser rechazado: " + payload
            );
            assertEquals("Usuario o contraseña incorrectos.", ex.getMessage());
        }
    }

    @Test
    @DisplayName("SQLi (Validacion de entrada): Campos de negocio rechazan comillas y sintaxis SQL")
    void sqlInjectionEnCamposDeEntradaEsRechazado() {
        assertThrows(ValidacionException.class, () -> Validador.documento("DNI", "12345678' OR '1'='1"));
        assertThrows(ValidacionException.class, () -> Validador.numeroTarjeta("SIT-100001' OR '1'='1"));
        assertThrows(ValidacionException.class, () -> Validador.idLector("LECTOR-001; DROP TABLE viaje;--"));
        assertThrows(ValidacionException.class, () -> Validador.email("test@correo.com' OR '1'='1"));
        assertThrows(ValidacionException.class, () -> Validador.monto("10.00 OR 1=1", false, Validador.MONTO_MAX_RECARGA));
    }

    @Test
    @DisplayName("SQLi (Caja Blanca): Todos los DAO JDBC usan PreparedStatement y nunca concatenan SQL")
    void todosLosDaoJdbcUsanPreparedStatement() throws Exception {
        Path dirJdbc = Path.of("src/main/java/dao/jdbc");
        assertTrue(Files.isDirectory(dirJdbc), "Debe existir el paquete dao.jdbc");

        int daosVerificados = 0;
        try (Stream<Path> archivos = Files.list(dirJdbc)) {
            for (Path archivo : archivos.filter(p -> p.toString().endsWith("DAOJdbc.java")).toList()) {
                String codigo = Files.readString(archivo, StandardCharsets.UTF_8);
                String nombre = archivo.getFileName().toString();
                assertTrue(codigo.contains("PreparedStatement"),
                    nombre + " debe utilizar PreparedStatement");
                assertFalse(codigo.contains("createStatement("),
                    nombre + " NO debe usar createStatement()");
                daosVerificados++;
            }
        }
        assertTrue(daosVerificados >= 8, "Se debieron auditar al menos 8 clases DAOJdbc");
    }

    @Test
    @DisplayName("SQLi (Prevencion de fuga): Errores de base de datos nunca muestran detalles SQL al usuario")
    void erroresSqlNoSeMuestranAlUsuarioFinal() {
        SQLException sqlEx = new SQLException(
            "ERROR: syntax error at or near \"SELECT\" in table usuario_secreto", "42601"
        );
        PersistenciaException pEx = new PersistenciaException("Fallo en SELECT * FROM usuario", sqlEx);

        String mensajeUsuario = ManejadorErrores.mensajeSeguro(pEx);
        assertFalse(mensajeUsuario.toLowerCase().contains("select"), "No debe filtrar consultas SQL");
        assertFalse(mensajeUsuario.toLowerCase().contains("usuario"), "No debe filtrar nombres de tablas");
        assertFalse(mensajeUsuario.contains("42601"), "No debe filtrar codigos SQLSTATE");
        assertTrue(mensajeUsuario.startsWith("Ocurrió un error inesperado."));
    }

    // =========================================================================
    // 2. PRUEBAS CONTRA XSS (CROSS-SITE SCRIPTING)
    // =========================================================================

    @Test
    @DisplayName("XSS (Escapado HTML): Caracteres especiales (<, >, ', \", &) son escapados")
    void escapaCaracteresEspecialesHtml() {
        String payload = "<script>alert('Hackeado!');</script>";
        String escapado = SanitizadorHtml.escapar(payload);

        assertEquals("&lt;script&gt;alert(&#39;Hackeado!&#39;);&lt;/script&gt;", escapado);
        assertFalse(escapado.contains("<script>"));
        assertFalse(escapado.contains("</script>"));
    }

    @Test
    @DisplayName("XSS (Validacion de entrada): Entradas con <script> o eventos JS son rechazadas")
    void entradasConScriptsMaliciososSonRechazadas() {
        String[] payloadsXss = {
            "<script>alert('Hackeado!');</script>",
            "<img src=x onerror=alert(1)>",
            "javascript:alert(document.cookie)",
            "<svg onload=alert(1)>",
            "Juan <script>evil()</script>"
        };
        for (String payload : payloadsXss) {
            assertTrue(SanitizadorHtml.contieneScript(payload), "Debe detectar script en: " + payload);
            assertThrows(ValidacionException.class, () -> Validador.nombre(payload, "Nombres"));
            assertThrows(ValidacionException.class, () -> Validador.sinScripts(payload, "Comentario"));
        }
    }

    @Test
    @DisplayName("XSS (Cabeceras HTTP): CabecerasSeguridadFilter aplica CSP sin unsafe-inline en scripts")
    void filtroInyectaContentSecurityPolicyEstricta() throws Exception {
        CabecerasSeguridadFilter filtro = new CabecerasSeguridadFilter();
        Map<String, String> cabeceras = new HashMap<>();

        HttpServletRequest req = mockRequest("GET", "/SitArequipaMVC/app/dashboard", "/SitArequipaMVC",
            Map.of(), Map.of(), null, true);
        HttpServletResponse resp = mockResponse(cabeceras, new int[]{200});

        filtro.doFilter(req, resp, (rq, rs) -> {});

        String csp = cabeceras.get("Content-Security-Policy");
        assertNotNull(csp, "Debe incluir cabecera Content-Security-Policy");
        assertTrue(csp.contains("script-src 'self'"), "CSP debe restringir scripts a 'self'");
        assertFalse(csp.contains("script-src 'self' 'unsafe-inline'"), "CSP no debe permitir unsafe-inline en script-src");
        assertEquals("nosniff", cabeceras.get("X-Content-Type-Options"));
        assertEquals("DENY", cabeceras.get("X-Frame-Options"));
        assertNotNull(cabeceras.get("Strict-Transport-Security"), "En HTTPS debe enviar HSTS");
    }

    // =========================================================================
    // 3. PRUEBAS CONTRA CSRF (CROSS-SITE REQUEST FORGERY)
    // =========================================================================

    @Test
    @DisplayName("CSRF (Tokens): Genera tokens unicos por sesion, firmados con HMAC y resistentes a falsificacion")
    void csrfTokensGeneraYValidaCorrectamente() {
        CsrfTokens csrf = new CsrfTokens(SECRETO);
        String t1 = csrf.generar();
        String t2 = csrf.generar();

        assertNotEquals(t1, t2, "Cada token CSRF debe ser unico e impredecible");
        assertTrue(csrf.esAutentico(t1));
        assertTrue(csrf.validar(t1, t1));
        assertFalse(csrf.validar(t1, t2), "No debe aceptar un token distinto al de la cookie");
        assertFalse(csrf.validar(t1, t1 + "x"), "No debe aceptar un token con firma alterada");
        assertFalse(csrf.validar(null, t1));
        assertFalse(csrf.validar(t1, null));
    }

    @Test
    @DisplayName("CSRF (Filtro HTTP): Bloquea POST sin token o con token falso (403) y permite POST con token valido")
    void csrfFilterProtegeFormulariosPost() throws Exception {
        CsrfTokens csrf = new CsrfTokens(SECRETO);
        CsrfFilter filtro = new CsrfFilter(csrf, false);

        // 1) Peticion GET genera cookie SIT_CSRF y atributo csrfToken
        Map<String, String> headersGet = new HashMap<>();
        Map<String, Object> attrsGet = new HashMap<>();
        HttpServletRequest getReq = mockRequest("GET", "/login", "", Map.of(), Map.of(), null, false, attrsGet);
        HttpServletResponse getResp = mockResponse(headersGet, new int[]{200});
        filtro.doFilter(getReq, getResp, (rq, rs) -> {});

        String tokenEmitido = (String) attrsGet.get(CsrfTokens.ATRIBUTO);
        assertNotNull(tokenEmitido, "GET debe exponer csrfToken a la vista JSP");
        assertTrue(headersGet.getOrDefault("Set-Cookie", "").contains("SIT_CSRF=" + tokenEmitido));
        assertTrue(headersGet.get("Set-Cookie").contains("HttpOnly"));
        assertTrue(headersGet.get("Set-Cookie").contains("SameSite=Strict"));

        // 2) Ataque CSRF: POST sin campo _csrf (ej. <img src=".../recargas"> o form malicioso) -> 403 Forbidden
        int[] statusSinToken = {200};
        Cookie[] cookies = {new Cookie(CsrfTokens.COOKIE, tokenEmitido)};
        HttpServletRequest postSinToken = mockRequest("POST", "/app/recargas", "", Map.of(), Map.of(), cookies, false);
        HttpServletResponse respSinToken = mockResponse(new HashMap<>(), statusSinToken);
        boolean[] cadenaEjecutada = {false};
        FilterChain chain = (rq, rs) -> cadenaEjecutada[0] = true;

        filtro.doFilter(postSinToken, respSinToken, chain);
        assertEquals(HttpServletResponse.SC_FORBIDDEN, statusSinToken[0], "POST sin token CSRF debe responder 403");
        assertFalse(cadenaEjecutada[0], "No debe ejecutar el controlador cuando falla CSRF");

        // 3) Peticion legitima: POST con cookie y parametro _csrf coincidentes -> pasa al servlet
        int[] statusValido = {200};
        HttpServletRequest postValido = mockRequest("POST", "/app/recargas", "",
            Map.of(), Map.of(CsrfTokens.PARAMETRO, tokenEmitido), cookies, false);
        HttpServletResponse respValido = mockResponse(new HashMap<>(), statusValido);

        filtro.doFilter(postValido, respValido, chain);
        assertEquals(200, statusValido[0]);
        assertTrue(cadenaEjecutada[0], "POST con token CSRF valido debe continuar la cadena");
    }

    // =========================================================================
    // 4. AUDITORIA DE CAJA BLANCA EN VISTAS JSP Y CONFIGURACION
    // =========================================================================

    @Test
    @DisplayName("Caja Blanca JSP: Todos los formularios POST incluyen token _csrf y no hay scripts inline")
    void vistasJspCumplenPoliticasXssYCsrf() throws Exception {
        Path dirVistas = Path.of("src/main/webapp/WEB-INF/vistas");
        assertTrue(Files.isDirectory(dirVistas));

        List<String> formulariosVerificados = new ArrayList<>();
        try (Stream<Path> archivos = Files.list(dirVistas)) {
            for (Path jsp : archivos.filter(p -> p.toString().endsWith(".jsp")).toList()) {
                String nombre = jsp.getFileName().toString();
                String contenido = Files.readString(jsp, StandardCharsets.UTF_8);

                assertTrue(contenido.contains("taglib uri=\"http://java.sun.com/jsp/jstl/core\" prefix=\"c\""),
                    nombre + " debe declarar JSTL core para escapar con <c:out>");

                if (contenido.contains("method=\"post\"")) {
                    assertTrue(contenido.contains("name=\"_csrf\""),
                        nombre + " tiene un formulario POST y debe incluir el campo oculto _csrf");
                    formulariosVerificados.add(nombre);
                }

                assertFalse(contenido.contains("<script>"),
                    nombre + " no debe tener bloques <script> inline (viola CSP)");
            }
        }
        assertEquals(5, formulariosVerificados.size(), "Los 5 formularios POST deben estar protegidos con _csrf");

        // error.jsp nunca debe imprimir exception.message
        String errorJsp = Files.readString(dirVistas.resolve("error.jsp"), StandardCharsets.UTF_8);
        assertFalse(errorJsp.contains("javax.servlet.error.exception"),
            "error.jsp no debe revelar mensajes internos de excepciones al usuario final");
    }

    // =========================================================================
    // Dobles de prueba ligeros para Servlet API (sin dependencias externas)
    // =========================================================================

    private static HttpServletRequest mockRequest(
            String method, String uri, String contextPath,
            Map<String, String> headers, Map<String, String> params,
            Cookie[] cookies, boolean secure) {
        return mockRequest(method, uri, contextPath, headers, params, cookies, secure, new HashMap<>());
    }

    private static HttpServletRequest mockRequest(
            String method, String uri, String contextPath,
            Map<String, String> headers, Map<String, String> params,
            Cookie[] cookies, boolean secure, Map<String, Object> attributes) {
        return (HttpServletRequest) Proxy.newProxyInstance(
            HttpServletRequest.class.getClassLoader(),
            new Class<?>[]{HttpServletRequest.class},
            (proxy, m, args) -> switch (m.getName()) {
                case "getMethod" -> method;
                case "getRequestURI" -> uri;
                case "getContextPath" -> contextPath;
                case "getHeader" -> headers.get((String) args[0]);
                case "getParameter" -> params.get((String) args[0]);
                case "getCookies" -> cookies;
                case "isSecure" -> secure;
                case "getRemoteAddr" -> "127.0.0.1";
                case "getServerName" -> "localhost";
                case "getCharacterEncoding" -> "UTF-8";
                case "setCharacterEncoding" -> null;
                case "setAttribute" -> { attributes.put((String) args[0], args[1]); yield null; }
                case "getAttribute" -> attributes.get((String) args[0]);
                default -> null;
            }
        );
    }

    private static HttpServletResponse mockResponse(Map<String, String> headers, int[] statusOut) {
        return (HttpServletResponse) Proxy.newProxyInstance(
            HttpServletResponse.class.getClassLoader(),
            new Class<?>[]{HttpServletResponse.class},
            (proxy, m, args) -> switch (m.getName()) {
                case "setHeader", "addHeader" -> { headers.put((String) args[0], (String) args[1]); yield null; }
                case "getHeader" -> headers.get((String) args[0]);
                case "sendError" -> { statusOut[0] = (Integer) args[0]; yield null; }
                case "setStatus" -> { statusOut[0] = (Integer) args[0]; yield null; }
                case "getStatus" -> statusOut[0];
                case "setCharacterEncoding" -> null;
                default -> null;
            }
        );
    }
}

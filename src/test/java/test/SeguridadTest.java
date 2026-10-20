package test;

import static org.junit.jupiter.api.Assertions.*;

import excepcion.AutenticacionException;
import fakes.Fakes;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import modelo.Rol;
import modelo.SesionUsuario;
import org.junit.jupiter.api.Test;
import seguridad.*;

class SeguridadTest {
    private static final String SECRETO = "secreto-de-pruebas-con-32-bytes-minimo!!";
    private final Clock reloj = Fakes.reloj("2026-10-10T12:00:00Z");
    private final SesionUsuario base = new SesionUsuario(7, 21, "operador1", Rol.OPERADOR_PV, 0);

    // ---------- PBKDF2 ----------
    @Test void hashNoContieneLaContrasenaYEsDistintoCadaVez() {
        Pbkdf2PasswordHasher h = new Pbkdf2PasswordHasher(1000);
        String a = h.hash("Cambiar#2026"), b = h.hash("Cambiar#2026");
        assertFalse(a.contains("Cambiar#2026"));
        assertNotEquals(a, b, "la sal debe ser aleatoria");
    }
    @Test void verificaContrasenaCorrectaEIncorrecta() {
        Pbkdf2PasswordHasher h = new Pbkdf2PasswordHasher(1000);
        String hash = h.hash("Cambiar#2026");
        assertTrue(h.verificar("Cambiar#2026", hash));
        assertFalse(h.verificar("otra", hash));
    }
    @Test void hashMalformadoNuncaLanzaExcepcion() {
        assertFalse(new Pbkdf2PasswordHasher(1000).verificar("x", "basura"));
    }

    // ---------- JWT ----------
    @Test void tokenEmitidoSeValidaYConservaRol() {
        JwtHs256TokenService t = new JwtHs256TokenService(SECRETO, 3600, reloj);
        SesionUsuario s = t.validar(t.emitir(base));
        assertEquals("operador1", s.username());
        assertEquals(Rol.OPERADOR_PV, s.rol());
        assertEquals(21, s.idPersona());
    }
    @Test void tokenManipuladoEsRechazado() {
        JwtHs256TokenService t = new JwtHs256TokenService(SECRETO, 3600, reloj);
        String[] p = t.emitir(base).split("\\.");
        String payloadAdmin = Base64.getUrlEncoder().withoutPadding().encodeToString(
            new String(Base64.getUrlDecoder().decode(p[1]), StandardCharsets.UTF_8)
                .replace("OPERADOR_PV", "ADMINISTRADOR").getBytes(StandardCharsets.UTF_8));
        assertThrows(AutenticacionException.class, () -> t.validar(p[0] + "." + payloadAdmin + "." + p[2]));
    }
    @Test void tokenConAlgNoneEsRechazado() {
        JwtHs256TokenService t = new JwtHs256TokenService(SECRETO, 3600, reloj);
        Base64.Encoder e = Base64.getUrlEncoder().withoutPadding();
        String h = e.encodeToString("{\"alg\":\"none\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));
        String p = t.emitir(base).split("\\.")[1];
        assertThrows(AutenticacionException.class, () -> t.validar(h + "." + p + "."));
    }
    @Test void tokenExpiradoEsRechazado() {
        Clock pasado = Clock.fixed(Instant.parse("2026-10-10T12:00:00Z"), ZoneOffset.UTC);
        Clock futuro = Clock.fixed(Instant.parse("2026-10-10T14:00:01Z"), ZoneOffset.UTC);
        String token = new JwtHs256TokenService(SECRETO, 3600, pasado).emitir(base);
        assertThrows(AutenticacionException.class, () -> new JwtHs256TokenService(SECRETO, 3600, futuro).validar(token));
    }
    @Test void tokenFirmadoConOtroSecretoEsRechazado() {
        String token = new JwtHs256TokenService("otro-secreto-distinto-de-32-bytes-ok!!!", 3600, reloj).emitir(base);
        assertThrows(AutenticacionException.class, () -> new JwtHs256TokenService(SECRETO, 3600, reloj).validar(token));
    }
    @Test void basuraONuloSonRechazados() {
        JwtHs256TokenService t = new JwtHs256TokenService(SECRETO, 3600, reloj);
        assertThrows(AutenticacionException.class, () -> t.validar(null));
        assertThrows(AutenticacionException.class, () -> t.validar("a.b"));
        assertThrows(AutenticacionException.class, () -> t.validar("%%%.%%%.%%%"));
    }
    @Test void secretoCortoNoEsPermitido() {
        assertThrows(IllegalArgumentException.class, () -> new JwtHs256TokenService("corto", 60, reloj));
    }

    // ---------- Roles y permisos ----------
    @Test void operadorPuedeRecargarPeroPersonalDeControlNo() {
        PermisosAcceso p = new PermisosAcceso();
        assertTrue(p.permitido("/app/recargas", Rol.OPERADOR_PV));
        assertFalse(p.permitido("/app/recargas", Rol.PERSONAL_CONTROL));
        assertTrue(p.permitido("/app/dashboard", Rol.PERSONAL_CONTROL));
    }
    @Test void rutaNoRegistradaSeDeniegaPorDefecto() {
        assertFalse(new PermisosAcceso().permitido("/app/secreto", Rol.ADMINISTRADOR));
    }
    @Test void prefijoSimilarNoEscalaPrivilegios() {
        assertFalse(new PermisosAcceso().permitido("/app/recargasXYZ", Rol.ADMINISTRADOR));
    }

    // ---------- Fuerza bruta ----------
    @Test void tras5FallosElLimitadorBloquea() {
        LimitadorIntentos l = new LimitadorIntentos(5, Duration.ofMinutes(15), reloj);
        for (int i = 0; i < 4; i++) l.registrarFallo("u|ip");
        assertFalse(l.bloqueado("u|ip"));
        l.registrarFallo("u|ip");
        assertTrue(l.bloqueado("u|ip"));
        assertFalse(l.bloqueado("otro|ip"));
    }
    @Test void elBloqueoExpiraDespuesDeLaVentana() {
        Clock c1 = Fakes.reloj("2026-10-10T12:00:00Z"), c2 = Fakes.reloj("2026-10-10T12:16:00Z");
        LimitadorIntentos l1 = new LimitadorIntentos(1, Duration.ofMinutes(15), c1);
        l1.registrarFallo("k");
        assertTrue(l1.bloqueado("k"));
        // mismo estado visto 16 min después: simulamos con reloj mutable
        final Clock[] actual = {c1};
        Clock mutable = new Clock() {
            public ZoneOffset getZone() { return ZoneOffset.UTC; }
            public Clock withZone(java.time.ZoneId z) { return this; }
            public Instant instant() { return actual[0].instant(); }
        };
        LimitadorIntentos l = new LimitadorIntentos(1, Duration.ofMinutes(15), mutable);
        l.registrarFallo("k");
        assertTrue(l.bloqueado("k"));
        actual[0] = c2;
        assertFalse(l.bloqueado("k"));
    }
}

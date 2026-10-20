package test;

import static org.junit.jupiter.api.Assertions.*;

import excepcion.AutenticacionException;
import fakes.Fakes;
import java.time.Clock;
import java.time.Duration;
import modelo.Rol;
import modelo.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import seguridad.*;
import servicio.AutenticacionServicio;

class AutenticacionServicioTest {
    private final Clock reloj = Fakes.reloj("2026-10-10T12:00:00Z");
    private final Pbkdf2PasswordHasher hasher = new Pbkdf2PasswordHasher(1000);
    private final JwtHs256TokenService tokens = new JwtHs256TokenService("secreto-de-pruebas-con-32-bytes-minimo!!", 3600, reloj);
    private Fakes.UsuarioFake usuarios;
    private Fakes.BitacoraFake bitacora;
    private AutenticacionServicio servicio;

    @BeforeEach
    void preparar() {
        usuarios = new Fakes.UsuarioFake();
        usuarios.datos.put("operador1", new Usuario(7, 21, "operador1", hasher.hash("Cambiar#2026"), Rol.OPERADOR_PV, true));
        usuarios.datos.put("inactivo", new Usuario(8, 22, "inactivo", hasher.hash("Cambiar#2026"), Rol.OPERADOR_PV, false));
        bitacora = new Fakes.BitacoraFake();
        servicio = new AutenticacionServicio(usuarios, hasher, tokens, bitacora,
            new LimitadorIntentos(3, Duration.ofMinutes(15), reloj));
    }

    @Test void loginCorrectoEmiteTokenConRolYAudita() {
        String token = servicio.iniciarSesion("operador1", "Cambiar#2026", "10.0.0.1");
        assertEquals(Rol.OPERADOR_PV, tokens.validar(token).rol());
        assertTrue(bitacora.acciones.contains("LOGIN_OK"));
    }

    @Test void usuarioInexistenteYPasswordIncorrectaDanElMismoMensaje() {
        String m1 = assertThrows(AutenticacionException.class, () -> servicio.iniciarSesion("noexiste", "x", "ip")).getMessage();
        String m2 = assertThrows(AutenticacionException.class, () -> servicio.iniciarSesion("operador1", "mala", "ip")).getMessage();
        assertEquals(m1, m2, "no debe revelarse si el usuario existe");
    }

    @Test void usuarioInactivoNoPuedeIngresar() {
        assertThrows(AutenticacionException.class, () -> servicio.iniciarSesion("inactivo", "Cambiar#2026", "ip"));
    }

    @Test void entradaConInyeccionSqlEsRechazadaSinDetalles() {
        AutenticacionException e = assertThrows(AutenticacionException.class,
            () -> servicio.iniciarSesion("' OR '1'='1", "x", "ip"));
        assertEquals("Usuario o contraseña incorrectos.", e.getMessage());
    }

    @Test void tras3FallosSeBloqueaIncluidoElPasswordCorrecto() {
        for (int i = 0; i < 3; i++) {
            assertThrows(AutenticacionException.class, () -> servicio.iniciarSesion("operador1", "mala", "ip"));
        }
        AutenticacionException e = assertThrows(AutenticacionException.class,
            () -> servicio.iniciarSesion("operador1", "Cambiar#2026", "ip"));
        assertTrue(e.getMessage().contains("Demasiados intentos"));
    }
}

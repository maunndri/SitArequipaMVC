package controlador;

import dao.Transaccion;
import fakes.Fakes;
import modelo.Rol;
import modelo.Usuario;
import java.time.Clock;
import java.time.Duration;
import notificaciones.NotificadorEmail;
import seguridad.*;
import servicio.*;

/** Composition root (Fakes para ejecución en memoria sin DB). */
public class Contenedor {
    public final AutenticacionServicio autenticacion;
    public final EmisionServicio emision;
    public final RecargaServicio recarga;
    public final LectorServicio lector;
    public final TokenService tokens;
    public final PermisosAcceso permisos = new PermisosAcceso();
    public final boolean cookieSegura = false;

    public Contenedor() {
        Clock reloj = Clock.systemUTC();
        String secreto = "12345678901234567890123456789012";
        this.tokens = new JwtHs256TokenService(secreto, 8 * 3600, reloj);

        Transaccion tx = new Fakes.TransaccionDirecta();
        Fakes.BitacoraFake bitacora = new Fakes.BitacoraFake();
        Fakes.TarjetaFake tarjetas = new Fakes.TarjetaFake();
        Fakes.ComprobanteFake comprobantes = new Fakes.ComprobanteFake();
        GeneradorComprobante generador = new GeneradorComprobante(reloj);

        Fakes.UsuarioFake usuarios = new Fakes.UsuarioFake();
        usuarios.datos.put("admin", new Usuario(1, 1, "admin", new Pbkdf2PasswordHasher().hash("admin"), Rol.ADMINISTRADOR, true));

        this.autenticacion = new AutenticacionServicio(usuarios, new Pbkdf2PasswordHasher(),
            tokens, bitacora, new LimitadorIntentos(5, Duration.ofMinutes(15), reloj));
        this.emision = new EmisionServicio(new Fakes.PasajeroFake(), tarjetas, comprobantes,
            new Fakes.VentaFake(), bitacora, tx, generador);
        this.recarga = new RecargaServicio(tarjetas, new Fakes.RecargaFake(), comprobantes, bitacora, tx,
            generador, new NotificadorEmail());
        this.lector = new LectorServicio(tarjetas, new Fakes.RutaFake(), new Fakes.LectorFake(),
            new Fakes.ViajeFake(), bitacora, new PoliticaTarifaBase(), tx);
    }
}

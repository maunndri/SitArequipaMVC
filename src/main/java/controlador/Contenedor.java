package controlador;

import dao.*;
import dao.jdbc.*;
import fakes.Fakes;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import modelo.*;
import notificaciones.NotificadorEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import seguridad.*;
import servicio.*;

/** Composition root: Conecta a PostgreSQL con DAOs JDBC, o respaldo en memoria si la BD no está lista. */
public class Contenedor {
    private static final Logger LOG = LoggerFactory.getLogger(Contenedor.class);

    public final TarjetaDAO tarjetaDAO;
    public final PasajeroDAO pasajeroDAO;
    public final AutenticacionServicio autenticacion;
    public final EmisionServicio emision;
    public final RecargaServicio recarga;
    public final LectorServicio lector;
    public final TokenService tokens;
    public final PermisosAcceso permisos = new PermisosAcceso();
    public final boolean cookieSegura = false;
    private final boolean baseDatosActiva;

    public Contenedor() {
        Clock reloj = Clock.systemUTC();
        String secreto = "12345678901234567890123456789012";
        this.tokens = new JwtHs256TokenService(secreto, 8 * 3600, reloj);

        boolean usarBD = false;
        try (ConexionBD.Sesion s = ConexionBD.abrir()) {
            try (var ps = s.conexion().prepareStatement("SELECT 1 FROM usuario LIMIT 1")) {
                ps.executeQuery();
                usarBD = true;
                LOG.info("Conexión exitosa a PostgreSQL (sitdb). Modo persistente JDBC ACTIVO.");
            }
        } catch (Exception e) {
            LOG.warn("No se pudo conectar a PostgreSQL: {}. Se utilizará el modo en memoria hasta que la base de datos esté lista.", e.getMessage());
            usarBD = false;
        }

        this.baseDatosActiva = usarBD;

        if (usarBD) {
            Transaccion tx = new TransaccionJdbc();
            BitacoraDAO bitacora = new BitacoraDAOJdbc();
            TarjetaDAO tarjetas = new TarjetaDAOJdbc();
            PasajeroDAO pasajeros = new PasajeroDAOJdbc();
            this.tarjetaDAO = tarjetas;
            this.pasajeroDAO = pasajeros;

            ComprobanteDAO comprobantes = new ComprobanteDAOJdbc();
            GeneradorComprobante generador = new GeneradorComprobante(reloj);
            UsuarioDAO usuarios = new UsuarioDAOJdbc();
            PasswordHasher hasher = new Pbkdf2PasswordHasher();

            RutaDAO rutas = new RutaDAOJdbc();
            LectorBusDAO lectores = new LectorBusDAOJdbc();
            ViajeDAO viajes = new ViajeDAOJdbc();
            RecargaDAO recargas = new RecargaDAOJdbc();
            VentaTarjetaDAO ventas = new VentaTarjetaDAOJdbc();

            this.autenticacion = new AutenticacionServicio(usuarios, hasher,
                tokens, bitacora, new LimitadorIntentos(5, Duration.ofMinutes(15), reloj));
            this.emision = new EmisionServicio(pasajeros, tarjetas, comprobantes,
                ventas, bitacora, tx, generador);
            this.recarga = new RecargaServicio(tarjetas, recargas, comprobantes, bitacora, tx,
                generador, new NotificadorEmail());
            this.lector = new LectorServicio(tarjetas, rutas, lectores,
                viajes, bitacora, new PoliticaTarifaBase(), tx);
        } else {
            Transaccion tx = new Fakes.TransaccionDirecta();
            Fakes.BitacoraFake bitacora = new Fakes.BitacoraFake();
            Fakes.TarjetaFake tarjetas = new Fakes.TarjetaFake();
            Fakes.PasajeroFake pasajeros = new Fakes.PasajeroFake();
            this.tarjetaDAO = tarjetas;
            this.pasajeroDAO = pasajeros;

            Fakes.ComprobanteFake comprobantes = new Fakes.ComprobanteFake();
            GeneradorComprobante generador = new GeneradorComprobante(reloj);

            // Pasajeros y tarjetas iniciales de demostración
            Pasajero p1 = new Pasajero();
            p1.setTipoDocumento("DNI");
            p1.setDocumento("70123456");
            p1.setNombres("Carlos Alberto");
            p1.setApellidoPaterno("Quispe");
            p1.setApellidoMaterno("Mamani");
            p1.setEmail("carlos.quispe@gmail.com");
            p1.setTelefono("958123456");
            pasajeros.insertar(p1);
            tarjetas.insertar(new Tarjeta("SIT-100001", p1.getIdPersona(), PerfilTarifa.GENERAL, EstadoTarjeta.ACTIVA, new BigDecimal("15.50"), LocalDate.now().minusDays(3)));

            Pasajero p2 = new Pasajero();
            p2.setTipoDocumento("DNI");
            p2.setDocumento("72987654");
            p2.setNombres("Lucía Elena");
            p2.setApellidoPaterno("Flores");
            p2.setApellidoMaterno("Vargas");
            p2.setEmail("lucia.flores@utp.edu.pe");
            p2.setTelefono("984567890");
            pasajeros.insertar(p2);
            tarjetas.insertar(new Tarjeta("SIT-100002", p2.getIdPersona(), PerfilTarifa.UNIVERSITARIO, EstadoTarjeta.ACTIVA, new BigDecimal("8.00"), LocalDate.now().minusDays(1)));

            Fakes.UsuarioFake usuarios = new Fakes.UsuarioFake();
            PasswordHasher hasher = new Pbkdf2PasswordHasher();
            usuarios.datos.put("admin", new Usuario(1, 1, "admin", hasher.hash("admin"), Rol.ADMINISTRADOR, true));
            usuarios.datos.put("operador1", new Usuario(2, 2, "operador1", hasher.hash("operador1"), Rol.OPERADOR_PV, true));
            usuarios.datos.put("operador", new Usuario(3, 3, "operador", hasher.hash("operador"), Rol.OPERADOR_PV, true));

            Fakes.RutaFake rutas = new Fakes.RutaFake();
            rutas.datos.put(1, new modelo.Ruta(1, "TRONCAL-C1", "Río Seco - Socabaya", new java.math.BigDecimal("1.30"), true));
            Fakes.LectorFake lectores = new Fakes.LectorFake();
            lectores.ids.add("LECTOR-001");

            this.autenticacion = new AutenticacionServicio(usuarios, hasher,
                tokens, bitacora, new LimitadorIntentos(5, Duration.ofMinutes(15), reloj));
            this.emision = new EmisionServicio(pasajeros, tarjetas, comprobantes,
                new Fakes.VentaFake(), bitacora, tx, generador);
            this.recarga = new RecargaServicio(tarjetas, new Fakes.RecargaFake(), comprobantes, bitacora, tx,
                generador, new NotificadorEmail());
            this.lector = new LectorServicio(tarjetas, rutas, lectores,
                new Fakes.ViajeFake(), bitacora, new PoliticaTarifaBase(), tx);
        }
    }

    public boolean isBaseDatosActiva() {
        return baseDatosActiva;
    }

    public List<TarjetaDetalle> listarTarjetasConPasajeros() {
        List<Tarjeta> lista = tarjetaDAO.listarTodas();
        List<TarjetaDetalle> resultado = new ArrayList<>();
        for (Tarjeta t : lista) {
            Optional<Pasajero> pOpt = pasajeroDAO.buscarPorId(t.getIdPasajero());
            String nombre = "Sin asignar";
            String tipoDoc = "-";
            String doc = "-";
            String email = "-";
            String tel = "-";
            if (pOpt.isPresent()) {
                Pasajero p = pOpt.get();
                nombre = (p.getNombres() != null ? p.getNombres() : "") + " "
                       + (p.getApellidoPaterno() != null ? p.getApellidoPaterno() : "") + " "
                       + (p.getApellidoMaterno() != null ? p.getApellidoMaterno() : "");
                nombre = nombre.trim();
                tipoDoc = p.getTipoDocumento() != null ? p.getTipoDocumento() : "DNI";
                doc = p.getDocumento() != null ? p.getDocumento() : "-";
                email = p.getEmail() != null ? p.getEmail() : "-";
                tel = p.getTelefono() != null ? p.getTelefono() : "-";
            }
            resultado.add(new TarjetaDetalle(
                t.getNumero(), t.getIdPasajero(), nombre, tipoDoc, doc, email, tel,
                t.getPerfil(), t.getEstado(), t.getSaldo(), t.getFechaEmision()
            ));
        }
        return resultado;
    }
}

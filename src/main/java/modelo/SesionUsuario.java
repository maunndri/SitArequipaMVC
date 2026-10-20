package modelo;

/** Datos mínimos que viajan dentro del JWT. */
public record SesionUsuario(int idUsuario, int idPersona, String username, Rol rol, long expiraEpochSeg) {
}

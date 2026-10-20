package dao;

public interface BitacoraDAO {
    /** idUsuario puede ser null (p. ej. intento de login fallido). */
    void registrar(Integer idUsuario, String accion, String entidad, String ip);
}

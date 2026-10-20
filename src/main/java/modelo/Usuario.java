package modelo;

public class Usuario {
    private final int id;
    private final int idPersona;
    private final String username;
    private final String passwordHash;
    private final Rol rol;
    private final boolean activo;

    public Usuario(int id, int idPersona, String username, String passwordHash, Rol rol, boolean activo) {
        this.id = id; this.idPersona = idPersona; this.username = username;
        this.passwordHash = passwordHash; this.rol = rol; this.activo = activo;
    }

    public int getId() { return id; }
    public int getIdPersona() { return idPersona; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public Rol getRol() { return rol; }
    public boolean isActivo() { return activo; }
}

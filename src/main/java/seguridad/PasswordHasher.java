package seguridad;

public interface PasswordHasher {
    String hash(String passwordPlano);
    boolean verificar(String passwordPlano, String hashAlmacenado);
}

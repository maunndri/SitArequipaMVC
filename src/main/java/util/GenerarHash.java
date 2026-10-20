package util;

import seguridad.Pbkdf2PasswordHasher;

/** Utilidad de consola para crear el hash de una contraseña: java -cp classes util.GenerarHash "MiClave#2026" */
public final class GenerarHash {
    private GenerarHash() {}

    public static void main(String[] args) {
        if (args.length != 1) { System.err.println("Uso: GenerarHash <contraseña>"); System.exit(1); }
        Validador.passwordFuerte(args[0]);
        System.out.println(new Pbkdf2PasswordHasher().hash(args[0]));
    }
}

package seguridad;

import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import modelo.Rol;

/** Matriz de permisos ruta → roles. Denegar por defecto (principio de mínimo privilegio). */
public class PermisosAcceso {
    private final Map<String, Set<Rol>> reglas = new LinkedHashMap<>();

    public PermisosAcceso() {
        reglas.put("/app/dashboard", EnumSet.allOf(Rol.class));
        reglas.put("/app/emision",   EnumSet.of(Rol.ADMINISTRADOR, Rol.OPERADOR_PV));
        reglas.put("/app/recargas",  EnumSet.of(Rol.ADMINISTRADOR, Rol.OPERADOR_PV));
        reglas.put("/app/lector",    EnumSet.of(Rol.ADMINISTRADOR, Rol.OPERADOR_PV));
    }

    public boolean permitido(String rutaSinContexto, Rol rol) {
        for (Map.Entry<String, Set<Rol>> r : reglas.entrySet()) {
            if (rutaSinContexto.equals(r.getKey()) || rutaSinContexto.startsWith(r.getKey() + "/")) {
                return r.getValue().contains(rol);
            }
        }
        return false;   // ruta no registrada → denegada
    }
}

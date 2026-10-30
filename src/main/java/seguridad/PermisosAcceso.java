package seguridad;

import com.google.common.collect.ImmutableList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import modelo.Rol;

/** Matriz de permisos ruta → roles. Denegar por defecto (principio de mínimo privilegio). */
public class PermisosAcceso {
    /** 2.7.1. Google Guava: Lista inmutable de perfiles de usuario protegidos del sistema */
    public static final ImmutableList<String> ROLES_SISTEMA =
        ImmutableList.of("ADMINISTRADOR", "OPERADOR_PV", "PERSONAL_CONTROL");

    private final Map<String, Set<Rol>> reglas = new LinkedHashMap<>();

    public PermisosAcceso() {
        reglas.put("/app/dashboard", EnumSet.allOf(Rol.class));
        reglas.put("/app/emision",   EnumSet.of(Rol.ADMINISTRADOR, Rol.OPERADOR_PV));
        reglas.put("/app/recargas",  EnumSet.of(Rol.ADMINISTRADOR, Rol.OPERADOR_PV));
        reglas.put("/app/lector",    EnumSet.of(Rol.ADMINISTRADOR, Rol.OPERADOR_PV));
        reglas.put("/app/tarjetas",  EnumSet.allOf(Rol.class));
        reglas.put("/app/reporte",   EnumSet.allOf(Rol.class));
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

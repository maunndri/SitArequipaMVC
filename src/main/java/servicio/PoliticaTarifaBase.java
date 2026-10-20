package servicio;

import interfaces.PoliticaTarifa;
import java.math.BigDecimal;
import modelo.PerfilTarifa;
import modelo.Ruta;

/** Cobra la tarifa base de la ruta. Las tarifas oficiales por perfil están fuera del alcance. */
public class PoliticaTarifaBase implements PoliticaTarifa {
    @Override
    public BigDecimal calcular(Ruta ruta, PerfilTarifa perfil) {
        return ruta.getTarifaBase();
    }
}

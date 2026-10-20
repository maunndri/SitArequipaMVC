package interfaces;

import java.math.BigDecimal;
import modelo.PerfilTarifa;
import modelo.Ruta;

/** Estrategia de cálculo de tarifa (OCP): se agregan políticas sin tocar LectorServicio. */
public interface PoliticaTarifa {
    BigDecimal calcular(Ruta ruta, PerfilTarifa perfil);
}

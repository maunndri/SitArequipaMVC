package servicio;

import interfaces.PoliticaTarifa;
import java.math.BigDecimal;
import modelo.PerfilTarifa;
import modelo.Ruta;

/**
 * Cobra la tarifa con soporte de caché en memoria Google Guava (Sección 2.7.1 y 2.8.4).
 * Reduce la latencia de validación en lectores a menos de 50 ms.
 */
public class PoliticaTarifaBase implements PoliticaTarifa {
    private final TarifaCacheServicio cacheServicio;

    public PoliticaTarifaBase() {
        this(new TarifaCacheServicio());
    }

    public PoliticaTarifaBase(TarifaCacheServicio cacheServicio) {
        this.cacheServicio = cacheServicio;
    }

    @Override
    public BigDecimal calcular(Ruta ruta, PerfilTarifa perfil) {
        if (ruta == null) return BigDecimal.ZERO;
        return cacheServicio != null ? cacheServicio.obtenerTarifa(ruta, perfil) : ruta.getTarifaBase();
    }

    public TarifaCacheServicio getCacheServicio() {
        return cacheServicio;
    }
}

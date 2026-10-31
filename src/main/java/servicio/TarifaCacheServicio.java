package servicio;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;
import modelo.PerfilTarifa;
import modelo.Ruta;

/**
 * 2.7.1 y 2.8.4. Google Guava: Caché en Memoria con desalojo LRU y TTL de 15 minutos.
 * Mitiga la latencia en lectores simulados (&lt; 50 ms) y previene la saturación del pool JDBC.
 * Cuota fija maximumSize(500) para prevenir desbordamiento de Heap por ataques DoS.
 */
public class TarifaCacheServicio {

    private final Cache<String, BigDecimal> tarifaCache;

    public TarifaCacheServicio() {
        // Configuración de caché local para tarifas con desalojo LRU y TTL (Sección 2.7.1 y 2.8.4)
        this.tarifaCache = CacheBuilder.newBuilder()
            .maximumSize(500) // Evita desbordamiento de Heap
            .expireAfterWrite(15, TimeUnit.MINUTES) // Refresco automático
            .recordStats()
            .build();
    }

    public BigDecimal obtenerTarifa(Ruta ruta, PerfilTarifa perfil) {
        if (ruta == null) return BigDecimal.ZERO;
        String clave = ruta.getId() + ":" + (perfil != null ? perfil.name() : "GENERAL");
        try {
            return tarifaCache.get(clave, () -> calcularTarifa(ruta, perfil));
        } catch (Exception e) {
            return calcularTarifa(ruta, perfil);
        }
    }

    public BigDecimal calcularTarifa(Ruta ruta, PerfilTarifa perfil) {
        if (ruta == null) return BigDecimal.ZERO;
        if (perfil == PerfilTarifa.UNIVERSITARIO) {
            return PerfilTarifa.UNIVERSITARIO.getTarifa();
        }
        if (perfil == PerfilTarifa.ESCOLAR) {
            return PerfilTarifa.ESCOLAR.getTarifa();
        }
        return (ruta.getTarifaBase() != null && ruta.getTarifaBase().compareTo(BigDecimal.ZERO) > 0)
            ? ruta.getTarifaBase()
            : PerfilTarifa.GENERAL.getTarifa();
    }

    public Cache<String, BigDecimal> getCache() {
        return tarifaCache;
    }

    public void invalidar() {
        tarifaCache.invalidateAll();
    }
}

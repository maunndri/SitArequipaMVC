package test;

import static org.junit.jupiter.api.Assertions.*;

import com.google.common.collect.ImmutableList;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import modelo.PerfilTarifa;
import modelo.Ruta;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import seguridad.PermisosAcceso;
import servicio.PoliticaTarifaBase;
import servicio.ReporteLiquidacionServicio;
import servicio.ReporteLiquidacionServicio.ItemReporteViaje;
import servicio.TarifaCacheServicio;
import util.ArchivoSeguroUtil;
import util.LogSeguroUtil;
import util.Validador;

/**
 * Pruebas de integración y unitarias para las librerías complementarias (Sección 2.7)
 * y consideraciones de seguridad (Sección 2.8):
 * - 2.7.1 y 2.8.4: Google Guava (Colecciones Inmutables y Caché LRU)
 * - 2.7.2 y 2.8.3: Apache POI (SXSSFWorkbook y ZipSecureFile)
 * - 2.7.3 y 2.8.3: Apache Commons (lang3 defensivo e io con validación de magic bytes)
 * - 2.7.4 y 2.8.2: SLF4J / Logback (Zero-Leak Logging y sanitización CRLF)
 */
class LibreriasComplementariasTest {

    private static final Logger LOG = LoggerFactory.getLogger(LibreriasComplementariasTest.class);

    // =========================================================================
    // 2.7.1 y 2.8.4. GOOGLE GUAVA
    // =========================================================================
    @Test
    @DisplayName("Guava: Lista inmutable de roles del sistema no admite modificaciones en tiempo de ejecución")
    void testGuavaColeccionInmutable() {
        ImmutableList<String> roles = PermisosAcceso.ROLES_SISTEMA;
        assertNotNull(roles);
        assertTrue(roles.contains("ADMINISTRADOR"));
        assertTrue(roles.contains("OPERADOR_PV"));
        assertTrue(roles.contains("PERSONAL_CONTROL"));
        assertThrows(UnsupportedOperationException.class, () -> roles.add("HACKER"));
    }

    @Test
    @DisplayName("Guava: CacheBuilder almacena tarifas en memoria y registra estadísticas de acierto")
    void testGuavaCacheTarifas() {
        TarifaCacheServicio cacheServicio = new TarifaCacheServicio();
        Ruta rutaTroncal = new Ruta(1, "TRONCAL-C1", "Río Seco - Socabaya", new BigDecimal("1.30"), true);

        // Primera consulta: cálculo e inserción en caché
        BigDecimal tarifaGeneral = cacheServicio.obtenerTarifa(rutaTroncal, PerfilTarifa.GENERAL);
        assertEquals(new BigDecimal("1.30"), tarifaGeneral);

        // Segunda consulta: debe servirse desde la caché de Guava
        BigDecimal tarifaGeneralCacheada = cacheServicio.obtenerTarifa(rutaTroncal, PerfilTarifa.GENERAL);
        assertEquals(new BigDecimal("1.30"), tarifaGeneralCacheada);

        // Tarifa universitario: S/ 0.70
        BigDecimal tarifaUniv = cacheServicio.obtenerTarifa(rutaTroncal, PerfilTarifa.UNIVERSITARIO);
        assertEquals(new BigDecimal("0.70"), tarifaUniv);

        // Tarifa escolar: S/ 0.50
        BigDecimal tarifaEscolar = cacheServicio.obtenerTarifa(rutaTroncal, PerfilTarifa.ESCOLAR);
        assertEquals(new BigDecimal("0.50"), tarifaEscolar);

        // Verificar que la caché registra elementos
        assertTrue(cacheServicio.getCache().size() >= 3);
    }

    @Test
    @DisplayName("Guava: PoliticaTarifaBase resuelve tarifas a través de TarifaCacheServicio")
    void testPoliticaConGuavaCache() {
        PoliticaTarifaBase politica = new PoliticaTarifaBase();
        Ruta ruta = new Ruta(10, "ALIMENTADORA-A1", "Cayma - Centro", new BigDecimal("1.20"), true);
        BigDecimal tarifa = politica.calcular(ruta, PerfilTarifa.GENERAL);
        assertEquals(new BigDecimal("1.20"), tarifa);
    }

    // =========================================================================
    // 2.7.2 y 2.8.3. APACHE POI (SXSSFWorkbook)
    // =========================================================================
    @Test
    @DisplayName("Apache POI: Genera reporte de liquidación en streaming (SXSSF) con cabeceras y datos")
    void testApachePoiGeneracionExcel() throws IOException {
        ReporteLiquidacionServicio reporteServicio = new ReporteLiquidacionServicio();

        List<ItemReporteViaje> viajes = List.of(
            new ItemReporteViaje("SIT-100001", "BUS-ARE-101", new BigDecimal("1.00"), LocalDateTime.now(), "APROBADO"),
            new ItemReporteViaje("SIT-100002", "BUS-ARE-102", new BigDecimal("0.50"), LocalDateTime.now(), "APROBADO")
        );

        byte[] bytesExcel = reporteServicio.exportarBytes(viajes);
        assertNotNull(bytesExcel);
        assertTrue(bytesExcel.length > 0, "El archivo Excel generado no debe estar vacío");

        // Sección 2.8.3: Verificar que el archivo generado sea un ZIP / Office OpenXML válido (PK\x03\x04)
        assertTrue(ArchivoSeguroUtil.esFormatoXlsxValido(new ByteArrayInputStream(bytesExcel)));
    }

    @Test
    @DisplayName("Apache POI: ZipSecureFile tiene configurado el ratio mínimo de inflación (0.01) contra Zip-Bombs")
    void testApachePoiZipSecureFile() {
        assertEquals(0.01, ZipSecureFile.getMinInflateRatio(), 0.0001);
    }

    // =========================================================================
    // 2.7.3 y 2.8.3. APACHE COMMONS (lang3 e io)
    // =========================================================================
    @Test
    @DisplayName("Apache Commons Lang3: Validación defensiva de DNI con StringUtils")
    void testCommonsLang3StringUtils() {
        assertTrue(StringUtils.isBlank("   "));
        assertTrue(StringUtils.isNumeric("12345678"));
        assertFalse(StringUtils.isNumeric("1234567A"));
        assertEquals("Hola", StringUtils.trimToEmpty("  Hola  "));

        // Integrado en Validador.documento
        assertEquals("70123456", Validador.documento("DNI", "70123456"));
    }

    @Test
    @DisplayName("Apache Commons IO: ArchivoSeguroUtil detecta flujos no-XLSX y valida límites de cuota")
    void testCommonsIoValidaciones() throws IOException {
        byte[] bytesBasura = new byte[] {0x00, 0x01, 0x02, 0x03};
        assertFalse(ArchivoSeguroUtil.esFormatoXlsxValido(new ByteArrayInputStream(bytesBasura)));

        // No debe lanzar excepción dentro de 5 MB
        assertDoesNotThrow(() -> ArchivoSeguroUtil.validarTamano(1024 * 1024));
    }

    // =========================================================================
    // 2.7.4 y 2.8.2. SLF4J / LOGBACK Y ZERO-LEAK LOGGING
    // =========================================================================
    @Test
    @DisplayName("Zero-Leak Logging: Enmascara el número de tarjeta en registros de bitácora")
    void testZeroLeakEnmascaramiento() {
        assertEquals("SIT-****-0001", LogSeguroUtil.enmascararTarjeta("SIT-100001"));
        assertEquals("****-5678", LogSeguroUtil.enmascararTarjeta("12345678"));
        assertEquals("[VACIO]", LogSeguroUtil.enmascararTarjeta("   "));
    }

    @Test
    @DisplayName("Zero-Leak Logging: Neutraliza CRLF Injection en entradas del usuario")
    void testZeroLeakCrlfSanitizacion() {
        String entradaMaliciosa = "admin\r\n[CRITICAL] Intrusión detectada";
        String entradaSanitizada = LogSeguroUtil.sanitizarCRLF(entradaMaliciosa);
        assertFalse(entradaSanitizada.contains("\r"));
        assertFalse(entradaSanitizada.contains("\n"));
    }

    @Test
    @DisplayName("SLF4J: El Logger de la aplicación registra eventos correctamente")
    void testSlf4jLogging() {
        assertNotNull(LOG);
        assertDoesNotThrow(() -> LOG.info("Prueba unitaria de auditoría SLF4J completada exitosamente."));
    }
}

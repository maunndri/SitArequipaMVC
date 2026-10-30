package util;

import excepcion.ValidacionException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * 2.7.3 y 2.8.3. Apache Commons IO: Manejo seguro de archivos y validación de seguridad.
 * - Validación estricta por Magic Bytes (PK\x03\x04 para formatos Office OpenXML .xlsx).
 * - Control de cuota máxima de carga (5 MB) para evitar denegación de servicio.
 * - Cierre automático y lectura segura de plantillas con FileUtils / IOUtils.
 */
public final class ArchivoSeguroUtil {

    public static final long LIMITE_BYTES_CARGA = 5 * 1024 * 1024; // 5 MB (Sec 2.8.3)
    private static final byte[] MAGIC_BYTES_ZIP = new byte[] {0x50, 0x4B, 0x03, 0x04}; // PK..

    private ArchivoSeguroUtil() {}

    /**
     * Valida que la cabecera binaria del flujo corresponda al formato ZIP/XLSX legítimo.
     */
    public static boolean esFormatoXlsxValido(InputStream in) throws IOException {
        if (in == null) return false;
        byte[] encabezado = new byte[4];
        int leidos = IOUtils.read(in, encabezado);
        if (leidos < 4) return false;
        for (int i = 0; i < 4; i++) {
            if (encabezado[i] != MAGIC_BYTES_ZIP[i]) return false;
        }
        return true;
    }

    /**
     * Valida la cuota de tamaño de un archivo subido.
     */
    public static void validarTamano(long bytes) {
        if (bytes > LIMITE_BYTES_CARGA) {
            throw new ValidacionException("El archivo excede la cuota máxima permitida de 5 MB.");
        }
    }

    /**
     * Lectura segura de recursos de texto utilizando Apache Commons IO (FileUtils).
     */
    public static String leerTextoSeguro(File archivo) throws IOException {
        if (archivo == null || !archivo.exists()) {
            throw new IOException("El archivo solicitado no existe.");
        }
        validarTamano(archivo.length());
        return FileUtils.readFileToString(archivo, StandardCharsets.UTF_8);
    }
}

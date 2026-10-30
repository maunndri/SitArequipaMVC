package servicio;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

/**
 * 2.7.2 y 2.8.3. Apache POI: Generación de Reportes y Liquidaciones en modo Streaming (SXSSFWorkbook).
 * - Mantiene una ventana acotada de 100 filas en memoria RAM para evitar OutOfMemoryError.
 * - Mitiga ataques de descompresión (Zip-Bomb / XML Entity Expansion) fijando ZipSecureFile.setMinInflateRatio(0.01).
 */
public class ReporteLiquidacionServicio {

    static {
        // Sección 2.8.3: Protección contra Zip-Bombs y mitigación de descompresión maliciosa
        ZipSecureFile.setMinInflateRatio(0.01);
    }

    public record ItemReporteViaje(String codigoTarjeta, String unidadBus, BigDecimal monto, LocalDateTime fechaHora, String estado) {}

    public void exportarLiquidacionDiaria(List<ItemReporteViaje> listaViajes, OutputStream out) throws IOException {
        // SXSSFWorkbook con ventana deslizante de 100 filas en RAM
        try (SXSSFWorkbook wb = new SXSSFWorkbook(100)) {
            wb.setCompressTempFiles(true);
            Sheet sheet = wb.createSheet("Liquidacion_Diaria");

            // Estilos del encabezado
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());

            CellStyle headerStyle = wb.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // Estilo para moneda
            CellStyle moneyStyle = wb.createCellStyle();
            DataFormat df = wb.createDataFormat();
            moneyStyle.setDataFormat(df.getFormat("S/ #,##0.00"));

            // 1. Fila de Encabezados
            Row header = sheet.createRow(0);
            String[] titulos = {"ID Tarjeta", "Unidad Bus", "Monto (S/)", "Fecha y Hora", "Estado"};
            for (int i = 0; i < titulos.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(titulos[i]);
                cell.setCellStyle(headerStyle);
            }

            // 2. Filas de Datos
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            int rowIdx = 1;
            for (ItemReporteViaje v : listaViajes) {
                Row r = sheet.createRow(rowIdx++);
                r.createCell(0).setCellValue(v.codigoTarjeta() != null ? v.codigoTarjeta() : "-");
                r.createCell(1).setCellValue(v.unidadBus() != null ? v.unidadBus() : "-");

                Cell cMonto = r.createCell(2);
                cMonto.setCellValue(v.monto() != null ? v.monto().doubleValue() : 0.0);
                cMonto.setCellStyle(moneyStyle);

                r.createCell(3).setCellValue(v.fechaHora() != null ? v.fechaHora().format(dtf) : "-");
                r.createCell(4).setCellValue(v.estado() != null ? v.estado() : "APROBADO");
            }

            wb.write(out);
            wb.dispose(); // Libera archivos temporales en disco de SXSSF
        }
    }

    public byte[] exportarBytes(List<ItemReporteViaje> listaViajes) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        exportarLiquidacionDiaria(listaViajes, baos);
        return baos.toByteArray();
    }
}

package controlador;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import modelo.Tarjeta;
import servicio.ReporteLiquidacionServicio;
import servicio.ReporteLiquidacionServicio.ItemReporteViaje;

/**
 * 2.7.2. Servlet para exportar reporte de liquidación diaria en formato Excel (.xlsx) usando Apache POI en streaming.
 */
@WebServlet(name = "ReporteServlet", urlPatterns = {"/app/reporte"})
public class ReporteServlet extends BaseServlet {
    private final ReporteLiquidacionServicio reporteServicio = new ReporteLiquidacionServicio();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        resp.setHeader("Content-Disposition", "attachment; filename=\"Liquidacion_Diaria_SIT.xlsx\"");

        List<ItemReporteViaje> viajes = new ArrayList<>();
        List<Tarjeta> tarjetas = contenedor().tarjetaDAO.listarTodas();
        int busNum = 101;
        for (Tarjeta t : tarjetas) {
            viajes.add(new ItemReporteViaje(
                t.getNumero(),
                "BUS-ARE-" + busNum++,
                t.getSaldo(),
                LocalDateTime.now().minusHours(busNum % 10 + 1),
                t.getEstado().name()
            ));
        }

        reporteServicio.exportarLiquidacionDiaria(viajes, resp.getOutputStream());
    }
}

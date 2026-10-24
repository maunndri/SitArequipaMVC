package modelo;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TarjetaDetalle(
    String numero,
    int idPasajero,
    String nombrePasajero,
    String tipoDocumento,
    String documento,
    String email,
    String telefono,
    PerfilTarifa perfil,
    EstadoTarjeta estado,
    BigDecimal saldo,
    LocalDate fechaEmision
) {}

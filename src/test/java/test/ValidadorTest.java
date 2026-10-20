package test;

import static org.junit.jupiter.api.Assertions.*;

import excepcion.ValidacionException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import util.Validador;

class ValidadorTest {
    @Test void dniValidoTiene8Digitos() { assertEquals("12345678", Validador.documento("DNI", " 12345678 ")); }
    @Test void dniConLetrasEsRechazado() { assertThrows(ValidacionException.class, () -> Validador.documento("DNI", "1234567A")); }
    @Test void tipoDocumentoDesconocidoEsRechazado() { assertThrows(ValidacionException.class, () -> Validador.documento("XYZ", "12345678")); }

    @Test void nombreAceptaTildesYEspacios() { assertEquals("María José", Validador.nombre("María José", "Nombres")); }
    @Test void nombreConHtmlEsRechazado() { assertThrows(ValidacionException.class, () -> Validador.nombre("<script>alert(1)</script>", "Nombres")); }
    @Test void nombreConSqlEsRechazado() { assertThrows(ValidacionException.class, () -> Validador.nombre("Robert'); DROP TABLE tarjeta;--", "Nombres")); }

    @Test void emailOpcionalVacioEsNull() { assertNull(Validador.email("  ")); }
    @Test void emailInvalidoEsRechazado() { assertThrows(ValidacionException.class, () -> Validador.email("no-es-correo")); }

    @Test void montoValidoSeNormalizaADosDecimales() { assertEquals(new BigDecimal("10.50"), Validador.monto("10,5", false, Validador.MONTO_MAX_RECARGA)); }
    @Test void montoCeroEsRechazadoSiNoSePermite() { assertThrows(ValidacionException.class, () -> Validador.monto("0", false, Validador.MONTO_MAX_RECARGA)); }
    @Test void montoNegativoEsRechazado() { assertThrows(ValidacionException.class, () -> Validador.monto("-5", true, Validador.MONTO_MAX_RECARGA)); }
    @Test void montoConTresDecimalesEsRechazado() { assertThrows(ValidacionException.class, () -> Validador.monto("1.234", false, Validador.MONTO_MAX_RECARGA)); }
    @Test void montoSobreElTopeEsRechazado() { assertThrows(ValidacionException.class, () -> Validador.monto("500.01", false, Validador.MONTO_MAX_RECARGA)); }
    @Test void montoNoNumericoEsRechazado() { assertThrows(ValidacionException.class, () -> Validador.monto("abc", false, Validador.MONTO_MAX_RECARGA)); }

    @Test void tarjetaSeNormalizaAMayusculas() { assertEquals("04-A3-1F0", Validador.numeroTarjeta("04-a3-1f0")); }
    @Test void tarjetaConCaracteresRarosEsRechazada() { assertThrows(ValidacionException.class, () -> Validador.numeroTarjeta("04'; --")); }

    @Test void passwordDebilEsRechazada() { assertThrows(ValidacionException.class, () -> Validador.passwordFuerte("12345678")); }
    @Test void passwordFuerteEsAceptada() { assertDoesNotThrow(() -> Validador.passwordFuerte("Cambiar#2026x")); }
}

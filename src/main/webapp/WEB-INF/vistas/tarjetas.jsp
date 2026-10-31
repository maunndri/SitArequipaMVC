<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>SIT Arequipa - Tarjetas activas</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilos.css">
</head>
<body>
<div class="container wide">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px;">
        <h1>Tarjetas Activas y Pasajeros</h1>
        <div style="display: flex; gap: 8px;">
            <a href="${pageContext.request.contextPath}/app/reporte" class="btn-accion" style="padding: 9px 14px; background: #2563eb; color: #ffffff; font-weight: bold;">📊 Exportar Excel (POI)</a>
            <a href="${pageContext.request.contextPath}/app/emision" class="btn-accion" style="padding: 9px 14px; background: #10b981; color: #04130d; font-weight: bold;">+ Emitir nueva</a>
        </div>
    </div>
    <p><a href="${pageContext.request.contextPath}/app/dashboard">&larr; Volver al inicio</a></p>

    <div class="buscador-wrap" style="margin-top: 16px;">
        <p class="sub" style="margin: 0;">Consulta de tarjetas emitidas vinculadas a sus respectivos usuarios titulares.</p>
        <input type="text" id="filtroTarjetas" placeholder="🔍 Buscar por UID, nombre o DNI..." onkeyup="filtrarTabla()">
    </div>

    <c:choose>
        <c:when test="${empty tarjetas}">
            <div class="alerta" style="background: #171a21; border: 1px dashed #2b303b; text-align: center; color: #9aa3af; padding: 24px;">
                No hay tarjetas registradas aún.<br><br>
                <a href="${pageContext.request.contextPath}/app/emision" class="btn-accion" style="padding: 8px 16px; background: #10b981; color: #04130d; font-weight: bold;">Emitir la primera tarjeta</a>
            </div>
        </c:when>
        <c:otherwise>
            <div style="overflow-x: auto;">
                <table id="tablaTarjetas">
                    <thead>
                        <tr>
                            <th>Código UID</th>
                            <th>Pasajero (Titular)</th>
                            <th>Documento</th>
                            <th>Perfil</th>
                            <th>Saldo Actual</th>
                            <th>Estado</th>
                            <th>Fecha Emisión</th>
                            <th style="text-align: right;">Acciones</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="t" items="${tarjetas}">
                            <tr>
                                <td>
                                    <strong style="font-family: Consolas, monospace; color: #6ee7b7;"><c:out value="${t.numero()}"/></strong>
                                </td>
                                <td>
                                    <strong><c:out value="${t.nombrePasajero()}"/></strong>
                                    <c:if test="${t.email() ne '-'}">
                                        <br><small style="color: #64748b;"><c:out value="${t.email()}"/></small>
                                    </c:if>
                                </td>
                                <td>
                                    <c:out value="${t.tipoDocumento()}"/>: <c:out value="${t.documento()}"/>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${t.perfil() eq 'UNIVERSITARIO'}">
                                            <span class="badge universitario">Universitario</span>
                                        </c:when>
                                        <c:when test="${t.perfil() eq 'ESCOLAR'}">
                                            <span class="badge escolar">Escolar</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge general">General</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <span class="saldo-positivo">S/ <c:out value="${t.saldo()}"/></span>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${t.estado() eq 'ACTIVA'}">
                                            <span class="badge activa">ACTIVA</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge bloqueada"><c:out value="${t.estado()}"/></span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td style="color: #9aa3af; font-size: 12px;">
                                    <c:out value="${t.fechaEmision()}"/>
                                </td>
                                <td style="text-align: right; white-space: nowrap;">
                                    <a href="${pageContext.request.contextPath}/app/recargas" class="btn-accion">Recargar</a>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<script>
function filtrarTabla() {
    var input = document.getElementById("filtroTarjetas");
    var filter = input.value.toLowerCase().trim();
    var table = document.getElementById("tablaTarjetas");
    if (!table) return;
    var tr = table.getElementsByTagName("tr");

    for (var i = 1; i < tr.length; i++) {
        var textoFila = tr[i].innerText || tr[i].textContent;
        if (textoFila.toLowerCase().indexOf(filter) > -1) {
            tr[i].style.display = "";
        } else {
            tr[i].style.display = "none";
        }
    }
}
</script>
</body>
</html>

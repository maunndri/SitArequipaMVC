<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>SIT Arequipa - Venta y emisión</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilos.css">
</head>
<body>
<div class="container">
    <h1>Venta y Emisión de Tarjeta</h1>
    <p><a href="${pageContext.request.contextPath}/app/dashboard">&larr; Volver</a></p>
    <c:if test="${not empty error}"><div class="alerta error"><c:out value="${error}"/></div></c:if>
    <c:if test="${not empty exito}"><div class="alerta ok"><c:out value="${exito}"/></div></c:if>
    <form action="${pageContext.request.contextPath}/app/emision" method="post" autocomplete="off">
        <label for="tipoDocumento">Tipo de documento</label>
        <select id="tipoDocumento" name="tipoDocumento">
            <option value="DNI" ${param.tipoDocumento == 'DNI' ? 'selected' : ''}>DNI</option>
            <option value="CE" ${param.tipoDocumento == 'CE' ? 'selected' : ''}>Carné de extranjería</option>
        </select>
        <label for="documento">Número de documento</label>
        <input id="documento" type="text" name="documento" value="<c:out value='${param.documento}'/>" placeholder="Ej. 72345678" maxlength="12" required>
        <label for="nombres">Nombres</label>
        <input id="nombres" type="text" name="nombres" value="<c:out value='${param.nombres}'/>" placeholder="Ej. Juan Carlos" maxlength="100" required>
        <label for="apellidoPaterno">Apellido paterno</label>
        <input id="apellidoPaterno" type="text" name="apellidoPaterno" value="<c:out value='${param.apellidoPaterno}'/>" placeholder="Ej. Pérez" maxlength="50" required>
        <label for="apellidoMaterno">Apellido materno</label>
        <input id="apellidoMaterno" type="text" name="apellidoMaterno" value="<c:out value='${param.apellidoMaterno}'/>" placeholder="Ej. Quispe (opcional)" maxlength="50">
        <label for="email">Correo electrónico</label>
        <input id="email" type="email" name="email" value="<c:out value='${param.email}'/>" placeholder="Ej. usuario@correo.com (opcional)" maxlength="150">
        <label for="telefono">Teléfono</label>
        <input id="telefono" type="text" name="telefono" value="<c:out value='${param.telefono}'/>" placeholder="Ej. 987654321 (opcional)" maxlength="15">
        <label for="numeroTarjeta">Código UID de la tarjeta</label>
        <div style="display: flex; gap: 8px; align-items: stretch;">
            <input id="numeroTarjeta" type="text" name="numeroTarjeta" value="<c:out value='${not empty param.numeroTarjeta ? param.numeroTarjeta : uidSugerido}'/>" placeholder="Ej. SIT-100001" maxlength="30" style="flex: 1;" required>
            <button type="button" class="secundario" style="margin-top: 0; white-space: nowrap; padding: 0 16px;" onclick="generarNuevoUID()">🎲 Generar otro</button>
        </div>
        <small style="color: #9aa3af; display: block; margin-top: 4px;">Código autogenerado por el sistema. Puedes cambiarlo o generar uno nuevo con el botón.</small>
        <label for="perfil">Perfil de tarifa</label>
        <select id="perfil" name="perfil">
            <option value="GENERAL" ${empty param.perfil || param.perfil == 'GENERAL' ? 'selected' : ''}>General (S/ 1.30)</option>
            <option value="UNIVERSITARIO" ${param.perfil == 'UNIVERSITARIO' ? 'selected' : ''}>Universitario (S/ 0.70)</option>
            <option value="ESCOLAR" ${param.perfil == 'ESCOLAR' ? 'selected' : ''}>Escolar (S/ 0.50)</option>
        </select>
        <label for="montoInicial">Monto de carga inicial (S/)</label>
        <input id="montoInicial" type="text" name="montoInicial" value="<c:out value='${empty param.montoInicial ? "10.00" : param.montoInicial}'/>" maxlength="7" required>
        <button type="submit">Registrar y emitir tarjeta</button>
    </form>
</div>
<script>
function generarNuevoUID() {
    var num = Math.floor(100000 + Math.random() * 900000);
    document.getElementById('numeroTarjeta').value = 'SIT-' + num;
}
</script>
</body>
</html>

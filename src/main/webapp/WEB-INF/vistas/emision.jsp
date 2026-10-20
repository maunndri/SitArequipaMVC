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
            <option value="DNI">DNI</option>
            <option value="CE">Carné de extranjería</option>
        </select>
        <label for="documento">Número de documento</label>
        <input id="documento" type="text" name="documento" maxlength="12" required>
        <label for="nombres">Nombres</label>
        <input id="nombres" type="text" name="nombres" maxlength="100" required>
        <label for="apellidoPaterno">Apellido paterno</label>
        <input id="apellidoPaterno" type="text" name="apellidoPaterno" maxlength="50" required>
        <label for="apellidoMaterno">Apellido materno</label>
        <input id="apellidoMaterno" type="text" name="apellidoMaterno" maxlength="50">
        <label for="email">Correo electrónico</label>
        <input id="email" type="email" name="email" maxlength="150">
        <label for="telefono">Teléfono</label>
        <input id="telefono" type="text" name="telefono" maxlength="15">
        <label for="numeroTarjeta">Código UID de la tarjeta</label>
        <input id="numeroTarjeta" type="text" name="numeroTarjeta" maxlength="30" required>
        <label for="perfil">Perfil de tarifa</label>
        <select id="perfil" name="perfil">
            <option value="GENERAL">General</option>
            <option value="UNIVERSITARIO">Universitario</option>
            <option value="ESCOLAR">Escolar</option>
        </select>
        <label for="montoInicial">Monto de carga inicial (S/)</label>
        <input id="montoInicial" type="text" name="montoInicial" value="10.00" maxlength="7" required>
        <button type="submit">Registrar y emitir tarjeta</button>
    </form>
</div>
</body>
</html>

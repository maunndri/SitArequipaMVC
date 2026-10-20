<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>SIT Arequipa - Recargas</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilos.css">
</head>
<body>
<div class="container narrow">
    <h1>Recarga de Saldo</h1>
    <p><a href="${pageContext.request.contextPath}/app/dashboard">&larr; Volver</a></p>
    <c:if test="${not empty error}"><div class="alerta error"><c:out value="${error}"/></div></c:if>
    <c:if test="${not empty exito}"><div class="alerta ok"><c:out value="${exito}"/></div></c:if>
    <form action="${pageContext.request.contextPath}/app/recargas" method="post" autocomplete="off">
        <label for="numeroTarjeta">Código de tarjeta</label>
        <input id="numeroTarjeta" type="text" name="numeroTarjeta" maxlength="30" required>
        <label for="monto">Monto a recargar (S/)</label>
        <input id="monto" type="text" name="monto" maxlength="7" required>
        <button type="submit">Confirmar recarga</button>
    </form>
</div>
</body>
</html>

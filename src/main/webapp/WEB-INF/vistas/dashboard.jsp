<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>SIT Arequipa - Centro de control</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilos.css">
</head>
<body>
<div class="logo-dashboard-wrap" style="max-width: 760px; margin: 28px auto -18px auto; padding: 0 4px;">
    <a href="${pageContext.request.contextPath}/app/dashboard">
        <img src="${pageContext.request.contextPath}/img/logo-sit.png" alt="SIT Arequipa" class="logo-dashboard" height="65" width="116" style="height: 65px; width: auto; display: block;">
    </a>
</div>
<div class="container" style="margin-top: 14px;">
    <h1>Centro de Control SIT</h1>
    <p class="sub">Sesión: <c:out value="${nombreUsuario}"/> (<c:out value="${rolUsuario}"/>)</p>
    <c:if test="${rolUsuario ne 'PERSONAL_CONTROL'}">
        <ul class="menu">
            <li><a href="${pageContext.request.contextPath}/app/emision">Venta y emisión de tarjeta</a></li>
            <li><a href="${pageContext.request.contextPath}/app/recargas">Recargas</a></li>
            <li><a href="${pageContext.request.contextPath}/app/lector">Lector simulado</a></li>
            <li><a href="${pageContext.request.contextPath}/app/tarjetas">Ver tarjetas activas</a></li>
            <li><a href="${pageContext.request.contextPath}/app/reporte">Descargar Liquidación (Excel POI SXSSF)</a></li>
        </ul>
    </c:if>
    <form action="${pageContext.request.contextPath}/logout" method="post">
        <button type="submit" class="secundario">Cerrar sesión</button>
    </form>
</div>
</body>
</html>

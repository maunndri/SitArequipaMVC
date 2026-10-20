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
<div class="container">
    <h1>Centro de Control SIT</h1>
    <p class="sub">Sesión: <c:out value="${nombreUsuario}"/> (<c:out value="${rolUsuario}"/>)</p>
    <c:if test="${rolUsuario ne 'PERSONAL_CONTROL'}">
        <ul class="menu">
            <li><a href="${pageContext.request.contextPath}/app/emision">Venta y emisión de tarjeta</a></li>
            <li><a href="${pageContext.request.contextPath}/app/recargas">Recargas</a></li>
            <li><a href="${pageContext.request.contextPath}/app/lector">Lector simulado</a></li>
        </ul>
    </c:if>
    <form action="${pageContext.request.contextPath}/logout" method="post">
        <button type="submit" class="secundario">Cerrar sesión</button>
    </form>
</div>
</body>
</html>

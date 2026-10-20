<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>SIT Arequipa - Ingresar</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilos.css">
</head>
<body>
<div class="container narrow">
    <h1>SIT Arequipa</h1>
    <p class="sub">Ingreso al sistema</p>
    <c:if test="${not empty error}"><div class="alerta error"><c:out value="${error}"/></div></c:if>
    <form action="${pageContext.request.contextPath}/login" method="post" autocomplete="off">
        <label for="username">Usuario</label>
        <input id="username" type="text" name="username" maxlength="50" required>
        <label for="password">Contraseña</label>
        <input id="password" type="password" name="password" maxlength="100" required>
        <button type="submit">Ingresar</button>
    </form>
</div>
</body>
</html>

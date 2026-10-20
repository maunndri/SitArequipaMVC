<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>SIT Arequipa - Lector simulado</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilos.css">
</head>
<body>
<div class="container narrow">
    <h1>Lector Simulado</h1>
    <p><a href="${pageContext.request.contextPath}/app/dashboard">&larr; Volver</a></p>
    <c:if test="${not empty error}"><div class="alerta error"><c:out value="${error}"/></div></c:if>
    <c:if test="${not empty resultadoMensaje}">
        <div class="alerta ${resultadoAprobado ? 'ok' : 'error'}">
            <strong><c:out value="${resultadoMensaje}"/></strong><br>
            Tarifa: S/ <c:out value="${resultadoTarifa}"/> &middot; Saldo: S/ <c:out value="${resultadoSaldo}"/>
        </div>
    </c:if>
    <form action="${pageContext.request.contextPath}/app/lector" method="post" autocomplete="off">
        <label for="uid">UID de la tarjeta</label>
        <input id="uid" type="text" name="uid" maxlength="30" required autofocus>
        <label for="idRuta">Ruta (id)</label>
        <input id="idRuta" type="text" name="idRuta" value="1" maxlength="9" required>
        <label for="idLector">Lector de bus</label>
        <input id="idLector" type="text" name="idLector" value="LECTOR-001" maxlength="20" required>
        <button type="submit">Simular lectura</button>
    </form>
</div>
</body>
</html>

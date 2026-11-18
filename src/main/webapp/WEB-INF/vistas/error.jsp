<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%-- Security Misconfiguration / fuga de información (Semana 14):
     nunca se muestran mensajes de excepción, trazas ni errores SQL al usuario final.
     El detalle técnico queda solo en el log del servidor. --%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>SIT Arequipa - Error</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilos.css">
</head>
<body>
<div class="container narrow">
    <h1>No se pudo completar la solicitud</h1>
    <c:set var="codigo" value="${requestScope['javax.servlet.error.status_code']}"/>
    <c:choose>
        <c:when test="${codigo == 403}"><p>No tiene permiso para realizar esta acción o la solicitud no es válida. Recargue la página e intente de nuevo.</p></c:when>
        <c:when test="${codigo == 404}"><p>El recurso solicitado no existe.</p></c:when>
        <c:otherwise><p>Ocurrió un problema. Por favor, vuelva a intentarlo o contacte al administrador.</p></c:otherwise>
    </c:choose>
    <c:if test="${not empty codigo}">
        <p style="color:#ef4444; font-weight:bold; font-size:14px;">Código de error: <c:out value="${codigo}"/></p>
    </c:if>
    <p><a href="${pageContext.request.contextPath}/app/dashboard">&larr; Ir al inicio</a></p>
</div>
</body>
</html>

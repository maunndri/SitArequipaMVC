<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
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
    <p>Ocurrió un problema. Por favor, vuelva a intentarlo o contacte al administrador.</p>
    <c:if test="${not empty requestScope['javax.servlet.error.status_code']}">
        <p style="color:#ef4444; font-weight:bold; font-size:14px;">
            Código de error: <c:out value="${requestScope['javax.servlet.error.status_code']}"/>
            <c:if test="${not empty requestScope['javax.servlet.error.message']}">
                &middot; <c:out value="${requestScope['javax.servlet.error.message']}"/>
            </c:if>
        </p>
    </c:if>
    <c:if test="${not empty requestScope['javax.servlet.error.exception']}">
        <p style="color:#f87171; font-size:12px; background:#0f1115; padding:8px; border-radius:6px; word-break:break-all; text-align:left;">
            Detalle: <c:out value="${requestScope['javax.servlet.error.exception'].message}"/>
        </p>
    </c:if>
    <p><a href="${pageContext.request.contextPath}/app/dashboard">&larr; Ir al inicio</a></p>
</div>
</body>
</html>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title><c:out value="${formTitre}"/> - BiblioTech</title>
</head>
<body>
<h1><c:out value="${formTitre}"/></h1>

<form method="post" action="<c:out value='${formAction}'/>">
    <p>
        <label>Titre<br>
            <input type="text" name="titre" value="<c:out value='${form.titre}'/>">
        </label>
        <c:if test="${not empty errors.titre}">
            <br><span style="color:red"><c:out value="${errors.titre}"/></span>
        </c:if>
    </p>
    <p>
        <label>Auteur<br>
            <input type="text" name="auteur" value="<c:out value='${form.auteur}'/>">
        </label>
        <c:if test="${not empty errors.auteur}">
            <br><span style="color:red"><c:out value="${errors.auteur}"/></span>
        </c:if>
    </p>
    <p>
        <label>Année de publication<br>
            <input type="number" name="anneePublication" value="<c:out value='${form.anneePublication}'/>">
        </label>
        <c:if test="${not empty errors.anneePublication}">
            <br><span style="color:red"><c:out value="${errors.anneePublication}"/></span>
        </c:if>
    </p>
    <p>
        <label>Exemplaires (total)<br>
            <input type="number" name="exemplairesTotal" value="<c:out value='${form.exemplairesTotal}'/>">
        </label>
        <c:if test="${not empty errors.exemplairesTotal}">
            <br><span style="color:red"><c:out value="${errors.exemplairesTotal}"/></span>
        </c:if>
    </p>
    <p>
        <label>Exemplaires disponibles<br>
            <input type="number" name="exemplairesDisponibles" value="<c:out value='${form.exemplairesDisponibles}'/>">
        </label>
        <c:if test="${not empty errors.exemplairesDisponibles}">
            <br><span style="color:red"><c:out value="${errors.exemplairesDisponibles}"/></span>
        </c:if>
    </p>
    <button type="submit">Enregistrer</button>
    <a href="<c:url value='/livres'/>">Annuler</a>
</form>
</body>
</html>
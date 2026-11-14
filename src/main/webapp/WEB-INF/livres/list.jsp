<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Catalogue - BiblioTech</title>
</head>
<body>
<h1>Catalogue des livres</h1>

<p><a href="<c:url value='/emprunts'/>">Voir les emprunts en cours</a></p>

<p><a href="<c:url value='/livres/nouveau'/>">+ Ajouter un livre</a></p>


<form method="get" action="<c:url value='/livres'/>">
    <input type="text" name="q">
    <button type="submit">Rechercher</button>
</form>

<c:if test="${not empty param.q}">
    <p>Résultats pour : <c:out value="${param.q}"/></p>
</c:if>


<table border="1">
    <thead>
    <tr>
        <th>Titre</th>
        <th>Auteur</th>
        <th>Année</th>
        <th>Disponibles</th>
        <th>Actions</th>
    </tr>
    </thead>
    <tbody>
    <c:forEach var="livre" items="${livres}">
        <tr>
            <td><c:out value="${livre.titre()}"/></td>
            <td><c:out value="${livre.auteur()}"/></td>
            <td><c:out value="${livre.anneePublication()}"/></td>
            <td><c:out value="${livre.exemplairesDisponibles()}"/> / <c:out value="${livre.exemplairesTotal()}"/></td>
            <td>
                <a href="<c:url value='/livres/${livre.id()}'/>">Voir</a> |
                <a href="<c:url value='/livres/${livre.id()}/edit'/>">Modifier</a> |
                <form method="post" action="<c:url value='/livres/${livre.id()}/delete'/>" style="display:inline"
                      onsubmit="return confirm('Supprimer ce livre ?');">
                    <button type="submit">Supprimer</button>
                </form>
            </td>
        </tr>
    </c:forEach>
    </tbody>
</table>
</body>
</html>
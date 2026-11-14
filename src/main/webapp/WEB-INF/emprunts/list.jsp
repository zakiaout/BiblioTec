<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Emprunts en cours - BiblioTech</title>
</head>
<body>
<h1>Emprunts en cours</h1>

<p><a href="<c:url value='/livres'/>">Retour au catalogue</a></p>

<c:choose>
    <c:when test="${empty emprunts}">
        <p>Aucun emprunt en cours.</p>
    </c:when>
    <c:otherwise>
        <table border="1">
            <thead>
            <tr>
                <th>Livre</th>
                <th>Étudiant (n°)</th>
                <th>Emprunté le</th>
                <th>Retour prévu</th>
                <th>Statut</th>
                <th>Pénalité</th>
                <th>Action</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="ligne" items="${emprunts}">
                <tr>
                    <td><c:out value="${ligne.titreLivre()}"/></td>
                    <td><c:out value="${ligne.etudiantId()}"/></td>
                    <td><c:out value="${ligne.dateEmprunt()}"/></td>
                    <td><c:out value="${ligne.dateRetourPrevue()}"/></td>
                    <td><c:out value="${ligne.statut()}"/></td>
                    <td>
                        <fmt:formatNumber value="${ligne.penalite()}" type="number"
                                          minFractionDigits="2" maxFractionDigits="2"/> €
                    </td>
                    <td>
                        <form method="post" action="<c:url value='/emprunts/${ligne.id()}/retour'/>">
                            <button type="submit">Enregistrer le retour</button>
                        </form>
                    </td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </c:otherwise>
</c:choose>
</body>
</html>
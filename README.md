# BiblioTech

BiblioTech est une application web de gestion d'une bibliothèque universitaire. Elle permet de consulter et rechercher les livres, d'ajouter, modifier ou supprimer un livre, de consulter les emprunts et d'enregistrer les retours.

**Stack technique :** Java 21, Jakarta EE 10, Tomcat 10.1, PostgreSQL 16, Maven, JSP/JSTL 3.0.

## Prérequis

- Java 21
- Maven 3.9+
- Tomcat 10.1
- Docker (pour PostgreSQL)
- pgAdmin (facultatif, pour gérer la base de données)

## Structure du projet

```text
TP01/
├── pom.xml
├── README.md
├── .gitignore
├── sql/
│   └── schema.sql
├── Captures/
└── src/
    ├── main/
    │   ├── java/com/bibliotech/
    │   │   ├── HealthServlet.java
    │   │   ├── dao/
    │   │   │   ├── DatabaseConnection.java
    │   │   │   ├── GenericDAO.java
    │   │   │   ├── LivreDAO.java
    │   │   │   ├── EmpruntDAO.java
    │   │   │   ├── DaoException.java
    │   │   │   └── LivreIndisponibleException.java
    │   │   ├── model/
    │   │   │   ├── Livre.java
    │   │   │   ├── Etudiant.java
    │   │   │   ├── Emprunt.java
    │   │   │   ├── StatutEmprunt.java
    │   │   │   ├── PenaliteCalculator.java
    │   │   │   └── ChampInvalideException.java
    │   │   └── web/
    │   │       ├── LivreServlet.java
    │   │       ├── EmpruntServlet.java
    │   │       └── EmpruntLigne.java
    │   └── webapp/WEB-INF/
    │       ├── web.xml
    │       ├── livres/
    │       │   ├── list.jsp
    │       │   ├── detail.jsp
    │       │   └── form.jsp
    │       └── emprunts/
    │           └── list.jsp
    └── test/java/com/bibliotech/dao/
        └── RaceConditionDemo.java
```

J'ai utilisé l'architecture MVC pour séparer les responsabilités. Les Records représentent les données, les DAO gèrent les interactions avec la base de données, les Servlets traitent les requêtes HTTP et les JSP s'occupent de l'affichage.

Le fichier `sql/schema.sql` permet de créer les trois tables (livre, etudiant et emprunt) et d'insérer 5 livres et 2 étudiants de démonstration.

## Lancer le projet

1. Démarrer PostgreSQL avec Docker (remplacer `VOTRE_MOT_DE_PASSE` par un mot de passe local) :

   ```bash
   docker run --name pg-bibliotech -e POSTGRES_PASSWORD=VOTRE_MOT_DE_PASSE -p 5432:5432 -d postgres:16
   ```

   Si le conteneur existe déjà :

   ```bash
   docker start pg-bibliotech
   ```

2. Se connecter à PostgreSQL depuis pgAdmin et exécuter le fichier `sql/schema.sql` dans le Query Tool de la base `postgres`.

3. Configurer les variables d'environnement dans la configuration de lancement de Tomcat (Smart Tomcat dans IntelliJ par exemple) :

   ```text
   DB_URL=jdbc:postgresql://localhost:5432/postgres
   DB_USER=postgres
   DB_PASSWORD=VOTRE_MOT_DE_PASSE
   ```

4. Construire le projet :

   ```bash
   mvn clean package
   ```

   Le fichier WAR généré est `target/bibliotech.war`.

5. Déployer le WAR sur Tomcat 10.1, depuis l'IDE ou en le copiant dans le dossier `webapps/`. Configurer le context path `/bibliotech`.

6. Vérifier la connexion à la base de données : <http://localhost:8080/bibliotech/health>.

7. Accéder à l'application : <http://localhost:8080/bibliotech/livres>.

## Routes

Les routes suivantes sont relatives au context path `/bibliotech`.

| Méthode HTTP | Route | Action |
| --- | --- | --- |
| GET | `/health` | Vérifier le fonctionnement de l'application et la connexion à PostgreSQL |
| GET | `/livres` | Afficher le catalogue et rechercher un livre |
| GET | `/livres/{id}` | Afficher les détails d'un livre |
| GET | `/livres/nouveau` | Afficher le formulaire d'ajout |
| POST | `/livres` | Ajouter un livre |
| GET | `/livres/{id}/edit` | Afficher le formulaire de modification |
| POST | `/livres/{id}` | Modifier un livre |
| POST | `/livres/{id}/delete` | Supprimer un livre |
| GET | `/emprunts` | Afficher les emprunts |
| POST | `/emprunts/{id}/retour` | Enregistrer le retour d'un livre |

## Captures d'écran

### Catalogue des livres

![Catalogue des livres](Captures/Page%20d%E2%80%99accueil%20avec%20le%20catalogue.png)

### Formulaire d'ajout d'un livre

![Formulaire d'ajout](Captures/Formulaire%20d%E2%80%99ajout%20de%20livre.png)

### Liste des emprunts en cours

La capture montre un emprunt en retard de 11 jours, avec une pénalité de 5,50 €.

![Liste des emprunts](Captures/Liste%20des%20emprunts%20en%20cours.png)

## Choix techniques

- J'ai placé les JSP dans `WEB-INF` pour empêcher l'utilisateur d'y accéder directement. Il doit passer par une Servlet qui prépare les données avant de les envoyer à la JSP.
- J'ai créé `ChampInvalideException`, qui hérite de `IllegalArgumentException`, pour récupérer le nom du champ invalide et afficher le message d'erreur sous le champ correspondant. Les valeurs déjà saisies sont conservées dans le formulaire.
- J'ai utilisé des Records pour représenter les entités et valider les données dans leurs constructeurs compacts.
- J'ai choisi de recalculer le statut d'un emprunt lors de sa lecture en fonction de ses dates. Un emprunt peut devenir en retard avec le temps même si aucune modification n'a été faite dans la base.
- Pour accéder aux attributs des Records dans mes JSP, j'ai utilisé la syntaxe `${livre.titre()}` car les Records ne possèdent pas de getters classiques comme `getTitre()`.
- Pour un emprunt rendu en retard, j'ai fixé la pénalité à 0,50 € car le Record `Rendu` imposé dans le sujet contient seulement la date de retour et un booléen indiquant le retard, sans le nombre de jours de retard. C'est une simplification du calcul.
- Si un livre possède déjà des emprunts, sa suppression est refusée avec un code HTTP 409 pour respecter la contrainte de clé étrangère.
- J'ai utilisé des `PreparedStatement` pour les requêtes SQL et le `try-with-resources` pour fermer automatiquement les ressources JDBC.
- J'ai appliqué le pattern Post-Redirect-Get après les opérations POST réussies pour éviter les doubles soumissions.

### Limites et améliorations possibles

Actuellement, une nouvelle connexion JDBC est créée pour chaque opération qui nécessite un accès à la base. Une amélioration serait d'utiliser un pool de connexions comme HikariCP pour réutiliser les connexions.

Je n'ai pas ajouté de formulaire de création d'un emprunt car les routes demandées pour `EmpruntServlet` concernent la liste et le retour. Par contre, la méthode `enregistrerEmprunt` est implémentée dans `EmpruntDAO` et utilisée dans `RaceConditionDemo`.

## Réponses aux questions de réflexion

### Question 0.1 – Pourquoi utiliser `provided` au lieu de `compile` ?

J'ai utilisé `provided` car Tomcat fournit déjà l'API Servlet lors de l'exécution de l'application. Maven a besoin de cette dépendance pour compiler le projet, mais il n'est pas nécessaire de l'inclure dans le WAR.

Si on utilise `compile`, la dépendance sera intégrée dans le WAR, ce qui augmente sa taille et peut provoquer des conflits de classes ou de versions avec les bibliothèques du serveur (`ClassCastException`, `NoSuchMethodError`).

Pour vérifier cela, on peut ouvrir le WAR généré et vérifier que la dépendance `jakarta.jakartaee-web-api` n'est pas présente dans `WEB-INF/lib`.

### Question 0.2 – Pourquoi ne pas mettre les identifiants de la base dans le code ?

Si on met les identifiants directement dans le code et qu'on pousse le projet sur un repo Git public, n'importe quelle personne peut les récupérer. Même si on les supprime après, ils peuvent rester accessibles dans l'historique des commits.

En dehors des variables d'environnement, on peut utiliser une `DataSource` JNDI configurée côté serveur dans `context.xml` (hors du repo public), ou utiliser un gestionnaire de secrets comme Vault.

### Question A.1 – Immutabilité des Records et thread-safety

Un Record est immuable par défaut car ses attributs sont `final` et il ne possède pas de setters. Une fois l'objet créé, on ne peut plus réaffecter ses attributs.

Dans une application web, plusieurs threads peuvent utiliser la même Servlet en même temps. Dans mon cas, les Records contiennent des types immuables, donc plusieurs threads peuvent lire le même objet sans risque qu'un autre thread modifie ses valeurs. On n'a donc pas besoin de `synchronized` pour protéger ces valeurs. Par contre, cela ne rend pas toute la Servlet thread-safe automatiquement.

### Question A.2 – Différence entre une interface classique et une sealed interface

Une interface classique peut être implémentée par n'importe quelle classe, alors qu'une `sealed interface` limite les classes autorisées à l'implémenter en utilisant le mot clé `permits`.

Dans mon cas, `StatutEmprunt` ne peut prendre que trois formes : `EnCours`, `Rendu` et `EnRetard`. Le compilateur connaît donc tous les cas possibles et peut vérifier si mon `switch` traite tous ces cas.

### Question A.3 – Pourquoi le switch n'a pas besoin de default ?

Le compilateur connaît tous les types autorisés par `StatutEmprunt`. Si mon `switch` traite `EnCours`, `Rendu` et `EnRetard`, il est exhaustif et je n'ai pas besoin d'ajouter un `default`.

Si demain on ajoute un Record `Perdu` dans l'interface et dans la liste `permits`, les switch exhaustifs qui ne traitent pas ce nouveau cas ne compileront plus. Cela permet de retrouver les endroits à modifier. Avec un `default`, on risque de ne pas remarquer qu'un nouveau statut a été ajouté.

### Question B.1 – Pourquoi les formulaires HTML ne supportent que GET et POST ?

Les formulaires HTML classiques permettent d'envoyer des requêtes HTTP avec les méthodes `GET` et `POST`. Ils ne supportent pas directement `PUT` et `DELETE` (la valeur `dialog` existe aussi mais n'envoie pas de requête HTTP).

Pour envoyer une requête `PUT` ou `DELETE` depuis le navigateur, on peut utiliser JavaScript avec `fetch`. Dans mon projet, j'ai utilisé `POST /livres/{id}/delete` pour supprimer un livre depuis un formulaire HTML.

### Question B.2 – Différence entre EL et c:out

Si on utilise directement `${livre.titre()}` dans le HTML, la valeur est affichée sans échappement HTML. Si cette valeur contient du code HTML ou JavaScript, le navigateur peut l'interpréter comme du code.

Avec `<c:out value="${livre.titre()}"/>`, les caractères spéciaux sont échappés avant l'affichage. Par exemple, `<` devient `&lt;` et `>` devient `&gt;`.

| Valeur saisie | EL directement dans le HTML | Avec `c:out` |
| --- | --- | --- |
| `<b>Gras</b>` | Gras est affiché en gras | `<b>Gras</b>` est affiché comme texte |
| `<script>alert('XSS')</script>` | Le script peut être exécuté | Le script est affiché comme texte |

C'est pour cette raison que j'ai utilisé `c:out` pour afficher les données dynamiques dans les JSP.

### Question B.3 – Que se passe-t-il sans Post-Redirect-Get ?

L'utilisateur ajoute un livre et la Servlet effectue un INSERT puis un `forward` vers la JSP, donc la dernière requête reste un POST. Si l'utilisateur actualise la page avec F5 et confirme le renvoi du formulaire, le navigateur renvoie la même requête POST, ce qui peut créer un deuxième livre identique dans la base. Avec le PRG, la Servlet renvoie une redirection HTTP 302 vers la liste et l'actualisation ne renvoie qu'une requête GET.

### Question C.1 – Pourquoi créer une interface GenericDAO ?

La première raison est d'avoir un contrat commun pour tous les DAO. Grâce aux génériques, la même interface peut être utilisée pour plusieurs types d'entités et d'identifiants sans avoir besoin de redéfinir le contrat à chaque fois ni de faire des casts.

La deuxième raison est le découplage. Si une Servlet dépend de l'interface plutôt que d'une classe concrète, on peut changer l'implémentation du DAO ou utiliser une implémentation simulée pour les tests sans modifier sa logique.

### Question C.2 – Pourquoi utiliser PreparedStatement plutôt que Statement ?

Le premier avantage est la sécurité. Avec `PreparedStatement`, les valeurs saisies par l'utilisateur sont transmises comme des paramètres et ne sont pas interprétées comme du code SQL, ce qui permet d'éviter les injections SQL pour ces valeurs.

Le deuxième avantage est qu'une requête préparée peut être réutilisée et bénéficier de la précompilation selon le pilote et la base. Cela facilite aussi la gestion des types et des caractères spéciaux comme les apostrophes grâce aux méthodes `setString`, `setLong`, etc.

### Question C.3 – Que se passe-t-il si on oublie setAutoCommit(false) ?

Par défaut, JDBC fonctionne en auto-commit, donc chaque requête SQL réussie est validée automatiquement.

Si le stock est décrémenté de 1 et qu'une exception est levée avant l'INSERT de l'emprunt, la modification du stock est déjà validée dans la base. Aucun emprunt n'est créé et le `rollback()` ne peut plus annuler la modification déjà validée.

Par exemple, si le stock initial était de 1, il devient 0 alors qu'aucun étudiant n'a emprunté le livre. La base devient donc incohérente. C'est pour cela que les deux opérations doivent être effectuées dans une même transaction.

## Bonus 1 : Race condition

### Reproduction

Pour reproduire le problème, j'ai créé la classe `RaceConditionDemo` qui lance deux threads en parallèle représentant deux étudiants, Alice et Bob. Les deux threads essaient d'emprunter le même livre alors qu'il reste seulement un exemplaire disponible.

J'ai répété le test 100 fois pour observer le problème. Avant la correction, 99 rondes sur 100 étaient anormales : deux emprunts réussissaient pour un seul exemplaire, avec un stock final de -1.

![Race condition avant correction](Captures/avant%20bonus%201.png)

### Diagnostic

Le problème est que les deux transactions peuvent lire le même stock avant que l'une d'elles ne le modifie.

| Instant | Alice | Bob | Stock |
| --- | --- | --- | --- |
| T1 | SELECT : lit 1 | | 1 |
| T2 | | SELECT : lit 1 | 1 |
| T3 | UPDATE : décrémente et INSERT | Attend le verrou de mise à jour | 0 (non validé) |
| T4 | COMMIT | UPDATE : décrémente à nouveau | -1 (non validé) |
| T5 | | INSERT puis COMMIT | -1 |

Ce scénario illustre le cas d'un UPDATE de type `exemplaires_disponibles = exemplaires_disponibles - 1`, sans contrainte empêchant un stock négatif.

Le niveau d'isolation par défaut de PostgreSQL est `READ COMMITTED`. Il empêche de lire les modifications non validées des autres transactions. Par contre, il n'empêche pas deux transactions de lire la même valeur avant sa modification.

Le `SELECT` classique ne verrouille pas la ligne contre les mises à jour. Les deux étudiants passent donc la vérification du stock avant de le décrémenter. C'est un problème de type *check-then-act*.

### Correction

J'ai choisi d'utiliser `SELECT ... FOR UPDATE` dans la transaction pour verrouiller la ligne du livre au moment de la vérification du stock.

```sql
SELECT exemplaires_disponibles
FROM livre
WHERE id = ?
FOR UPDATE;
```

Le deuxième thread attend que le premier termine sa transaction. Une fois le verrou libéré, il lit le nouveau stock (0) et une `LivreIndisponibleException` est levée. Un seul emprunt peut donc réussir.

Une autre solution serait d'ajouter une contrainte `CHECK (exemplaires_disponibles >= 0)` dans la base. Elle permet d'empêcher un stock négatif, mais l'erreur arrive au moment de la mise à jour au lieu de la vérification métier. Cette contrainte reste utile comme protection supplémentaire.

On pourrait aussi utiliser le niveau d'isolation `SERIALIZABLE`, mais il faudrait gérer les échecs de sérialisation et éventuellement relancer les transactions. J'ai préféré `FOR UPDATE` car je peux verrouiller seulement la ligne concernée et gérer directement le cas d'un livre indisponible.

Après la correction, le test affiche 0 ronde anormale sur 100.

![Race condition après correction](Captures/apres%20correction%20du%20conflit%20des%20threads%20bonus%201.png)

## Bonus 2 : XSS

### Exploit

J'ai testé la vulnérabilité XSS dans le champ de recherche du catalogue en affichant directement le paramètre de recherche dans la JSP :

```jsp
Résultats pour : ${param.q}
```

Ensuite, j'ai saisi la valeur suivante dans le champ de recherche de mon application locale :

```html
<script>alert('XSS pwned')</script>
```

Le navigateur interprète la valeur comme du JavaScript et affiche une fenêtre d'alerte. Cela montre qu'une donnée saisie par l'utilisateur peut être exécutée comme du code si elle est réinjectée sans échappement dans la page.

![Exploit XSS avant correction](Captures/boite%20dialogue%20xss%20bonus%202%20avant.png)

### Correction

Pour corriger le problème, j'ai remplacé l'expression EL brute par `c:out` :

```jsp
Résultats pour : <c:out value="${param.q}" />
```

Maintenant les caractères spéciaux sont échappés. Le navigateur affiche le script comme du texte sans l'exécuter.

![XSS après correction](Captures/reslutat%20apres%20correction%20bonus%202.png)

Dans le HTML généré, les caractères sont échappés, par exemple `&lt;script&gt;` au lieu de `<script>`.


### Où d'autre ?

Le même problème peut se produire partout où une donnée venant de l'utilisateur ou de la base de données est affichée sans échappement dans une JSP.

| Page | Données à protéger |
| --- | --- |
| `livres/list.jsp` | Paramètre de recherche, titre et auteur des livres |
| `livres/detail.jsp` | Titre et auteur du livre |
| `livres/form.jsp` | Valeurs déjà saisies, titre, auteur et messages d'erreur |
| `emprunts/list.jsp` | Titre du livre, informations de l'étudiant et autres textes dynamiques affichés |

Il faut aussi protéger les valeurs dynamiques placées dans les attributs HTML des formulaires, en utilisant des attributs entre guillemets avec `c:out` ou `fn:escapeXml` lorsque c'est adapté.

Une donnée enregistrée dans la base n'est pas forcément une donnée de confiance. Par exemple, un titre contenant un script peut être enregistré puis exécuté chaque fois qu'un utilisateur consulte le catalogue. C'est le principe du XSS stocké, contrairement au XSS réfléchi testé avec le champ de recherche.

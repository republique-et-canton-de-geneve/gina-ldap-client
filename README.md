# 1. Propos

La bibliothèque gina-ldap-client est destinée aux applications Java de l'État de Genève.
Elle permet aux applications de se connecter via le protocole LDAP à l'annuaire de l'État et ainsi de récupérer :
- les utilisateurs
- les groupes (rôles)
- l'appartenance des utilisateurs aux groupes.

Cette bibliothèque ne traite que des droits d'accès, pas de l'authentification.

Cette bibliothèque est disponible sur
[Maven Central](http://central.maven.org/maven2/ch/ge/etat/securite/gina-ldap-client).

# 2. Description

L'annuaire de l'État de Genève s'appelle Gina. 
Pour une application Java, la manière la plus utilisée pour obtenir une information d'autorisation est d'utiliser
un JAR fourni en interne par l'équipe Gina. 
Ce JAR Gina interne implique que la communication entre l'application cliente et Gina se fait via EJB.

Ce projet-ci permet d'établir la communication via LDAP plutôt qu'EJB. Ses classes implémentent la même
interface ``gina.api.GinaApiLdapBaseAble`` que le JAR Gina interne. 
On notera cependant que, comme Gina n'expose via LDAP qu'une
partie de ses données, de nombreuses méthodes renvoient simplement une exception "méthode non implémentée" -
voir la classe ``gina.impl.GinaLdapAccess``.
A l'inverse, quelques méthodes ont été ajoutées.

Cette bibliothèque peut être intégrée à toute application tournant sur JRE 8+.
Elle est construite quasiment sans utilisation de librairies spécifiques, dans un souci de limiter les dépendances.

# 3. Construction

La bibliothèque peut être assemblée via Maven par la commande

```mvn -DskipTest clean install```

Il est cependant utile ne pas sauter les tests JUnit.
Ceux-ci peuvent être lancés selon deux modes : "Gina" et "local".

## Mode "Gina"

Dans ce mode, les classes JUnit de tests se connectent sur l'annuaire Gina. Ce mode nécessite la
connaissance de mots de passe de Gina.

Dans le POM, ce mode correspond au profil ``etat-de-geneve`` (le profil par défaut) :

```mvn -Dldap.gestrepo.password=<MOT DE PASSE> clean install```

 S'adresser au SMIL pour l'obtenir.

## Mode "local"

Dans ce mode, les classes JUnit de tests utilisent des fichiers .ldif fournis.
Pour faire fonctionner les tests, il faut créer des serveurs LDAP locaux, qui exposent les fichiers .ldif fournis ;
cette opération est décrite plus bas, en annexe.
Aucun mot de passe n'est nécessaire.

Dans le POM, ce mode correspond au profil ``local`` :

```mvn -P local,!etat-de-geneve clean install```

# 4. Intégration dans une application

## Maven 

```
<dependency>
	<groupId>ch.ge.cti.ct</groupId>
	<artifactId>gina-ldap-client</artifactId>
	<version> ... </version>
</dependency>
```

## Usage

L'API s'utilise en créant une ``GinaLdapConfiguration``, puis en obtenant un objet de type ``GinaApiLdapBaseAble``.
Par exemple :
```
String server = "ldaps://...";
String user = "cn= ... ... o=gina";
String password = <mot de passe>
String domain = "CTI"
String application = "GESTREPO"
int connectionTimeout = 5000;
int readTimeout = 3000;
GinaLdapConfiguration ldapConf = new GinaLdapConfiguration(
                server, base, user, password, domain, application, connectionTimeout, readTimeout);
GinaApiLdapBaseAble gina = new GinaLdapCommon(ldapConf);
List<String> roles = gina.getUserRoles(LAROCHEP);
```

Pour davantage d'exemples, se référer aux tests JUnit.

## Activation du pooling

Pour utilisation dans une application déployée sur Tomcat ou JBoss.

Il faut ajouter et adapter les propriétés suivantes dans le fichier ``catalina.properties`` :
```
com.sun.jndi.ldap.connect.pool.protocol=plain ssl
com.sun.jndi.ldap.connect.pool.authentication=simple
com.sun.jndi.ldap.connect.pool.maxsize=5
com.sun.jndi.ldap.connect.pool.prefsize=3
com.sun.jndi.ldap.connect.pool.timeout=240000
com.sun.jndi.ldap.connect.pool.initsize=2
#com.sun.jndi.ldap.connect.pool.debug=fine
```

Définition des propriétés : voir https://docs.oracle.com/javase/jndi/tutorial/ldap/connect/config.html.

## Exemples d'utilisation

```
GinaApiLdapConfiguration ldapConf = new GinaApiLdapConfiguration(server, base, user, password, DOMAIN, timeout);  // 1
try (GinaApiLdapBaseAble gina = new GinaLdapCommon(ldapConf)) {                                                   // 2
   List<String> roles = gina.getUserRoles("LAURENTJ");                                                            // 3
}
```
Légende :
1. Paramétrisation du serveur LDAP
2. Création de l'objet de connexion au VLDAP
3. Récupération d'informations (les rôles d'un utilisateur) sur le VLDAP

Pour un exemple d'utilisation de toutes les méthodes exposées, se référer :
- aux classes de tests de l'api : ./src/test/java/gina/api
- aux classes de l'appli de démo : http://scm.etat-ge.ch:21080/cti.composant/ct-gina-ldap-client-demo/trunk/ct-gina-ldap-client-demo-war/src/main/java/ch/ge/ael/gina/infos/InfosController.java

# Notes de la version

Chaque item annonce s'il respecte ou s'il rompt la compatibilité avec la version précédente.

## Version 4.0.0
- (incompatible) La version 3.0.0 nécessitait Java 7+. Désormais il faut Java 8+.
- (incompatible) Suppression de la classe `GinaLdapFactory`. Désormais, pour obtenir une connexion, on passe directement
  par le constructeur de `GinaLdapAccess`.
- (compatible) Améliorations des requêtes LDAP.

## Version 3.0.0
- (incompatible) La version 2.0.3 nécessitait Java 6+. Désormais il faut Java 7+.
- (incompatible) L'objet `GinaLdapApplication`ou `GinaLdapDomain` de connexion au serveur LDAP précédemment recréait la
  connexion LDAP à chaque requête.
  Désormais la connexion LDAP reste ouverte.
  C'est à l'utilisateur d'appeler la méthode `close` en fin d'utilisation de l'objet, par exemple avec un
  "try with resource".

## Version 2.0.3
- (compatible) ajout d'un paramètre `ldapConnexionTimeout` dans `GinaLdapConfiguration`.

# Annexe. Serveur LDAP sur un poste de développeur

## Serveur LDAP 

Pour effectuer les tests JUnit en mode local, c'est-à-dire autonome, sans appel à Gina, on utilise les
fichiers .ldif fournis. Pour exposer un fichier .ldif, il faut lancer un serveur LDAP sur ce fichier.
Pour cela, un serveur LDAP [UnboundID](https://ldap.com/unboundid-ldap-sdk-for-java) a été intégré dans 
les sources de ce projet.

Procédure :
- Ouvrir une fenêtre DOS et aller dans le répertoire du projet (le répertoire contenant le fichier `pom.xml`)
- Exécuter les commandes suivantes :

```
cd src\test\resources
unboundid-ldapsdk-4.0.8\tools\in-memory-directory-server --baseDN "o=gina" --port 30636 --ldifFile ldap_cti_gestrepo.ldif
```

Ceci doit afficher
```
Listening for client connections on port 30636.
```

Note. Le fichier `ldap_cti_gestrepo.ldif` est utilisable pour les classes de test suivantes :
``UserRoleAttributeTest``, ``UserRoleAttributeParallelTest`` et ``UserRoleAttributeTimeoutTest``.

## Explorateur LDAP

De façon facultative, on peut lancer un explorateur LDAP sur un serveur LDAP lancé (VLDAP Gina ou UnboundID).
L'explorateur, par exemple [Apache Directory Studio](https://directory.apache.org/studio)
ou [JXplorer](http://www.jxplorer.org), permet de parcourir de façon conviviale un annuaire LDAP.
Il n'est pas nécessaire pour lancer les tests JUnit.

### JXplorer
Exemple: branchement de JXplorer sur le VLDAP Gina :
- Installer JXplorer
- Lancer JXplorer
- Fichier > Se connecter
- Utiliser les paramètres de connexion fournis dans le POM de ce projet

![connexion LDAP](./doc/jxplorer_1.png)

![exploration LDAP](./doc/jxplorer_2.png)

### Apache Directory Studio
Exemple : branchement d'Apache Directory Studio sur un serveur UnboundID local pointant sur un fichier .ldif fourni :
- nom d'hôte : `lochalhost`
- port : `30636`
- méthode d'encryption : `Pas d'encrytption`
- authentification : `Pas d'authentification`

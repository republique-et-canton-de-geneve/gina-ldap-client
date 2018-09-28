# 1. Propos

La biblioth�que gina-ldap-client est destin�e aux applications Java de l'�tat de Gen�ve.
Elle permet aux applications de se connecter via le protocole LDAP � l'annuaire de l'�tat et ainsi de r�cup�rer :
- les utilisateurs
- les groupes (r�les)
- l'appartenance des utilisateurs aux groupes.

Cette biblioth�que ne traite que des droits d'acc�s, pas de l'authentification.

# 2. Description

L'annuaire de l'�tat de Gen�ve s'appeller Gina. 
Pour une application Java, la mani�re la plus utilis�e pour obtenir une information d'autorisation est d'utiliser
un JAR fourni en interne par l'�quipe Gina. 
Ce JAR Gina interne implique que la communication entre l'application cliente et Gina se fait via EJB.

Ce projet-ci permet d'�tablir la communication via LDAP plut�t qu'EJB. Ses classes impl�mentent la m�me
interface ``gina.api.GinaApiLdapBaseAble`` que le JAR Gina interne. 
On notera cependant que, comme Gina n'expose via LDAP qu'une
partie de ses donn�es, de nombreuses m�thodes renvoient simplement une exception "m�thode non impl�ment�e" -
voir la classe ``gina.impl.GinaLdapCommon``.
A l'inverse, quelques m�thodes ont �t� ajout�es.

Cette biblioth�que peut �tre int�gr�e � toute application tournant sur JRE 1.6+.
Elle est construite quasiment sans utilisation de librairies sp�cifiques, dans un souci de limiter les d�pendances.

# 3. Construction

La biblioth�que peut �tre assembl�e via Maven par la commande

```mvn -DskipTest clean install```

Il est cependant utile ne pas sauter les tests unitaires JUnit. 
Ceux-ci peuvent �tre lanc�s selon deux modes : "Gina" et "local".

## Mode "Gina"

Dans ce mode, les classes de tests se connectent sur l'annuaire Gina. Ce mode n�cessite la
connaissance de mots de passe de Gina.

Dans le POM, ce mode correspond au profil ``etat-de-geneve`` (le profil par d�faut) :

```mvn -Dldap.domain.password=<MOT DE PASSE 1> -Dldap.application.password=<MOT DE PASSE 2> -Dldap.gestrepo.password=<MOT DE PASSE 3> clean install```

Les mots de passe sont stock�s dans le Keypass du SMIL. S'adresser au SMIL pour les obtenir.

## Mode "local"

Dans ce mode, les classes de tests utilisent des fichiers .ldif fournis.
Pour faire fonctionner les tests, il faut cr�er des serveurs LDAP locaux, qui exposent les fichiers .ldif fournis ;
cette op�ration est d�crite plus bas, en annexe.
Aucun mot de passe n'est n�cessaire.

Dans le POM, ce mode correspond au profil ``local`` :

```mvn -P local,!etat-de-geneve clean install```

Attention : actuellement, certains tests unitaires ne fonctionnent pas dans ce mode.

# 4. Int�gration dans une application

## Maven 

```
<dependency>
	<groupId>ch.ge.cti.ct</groupId>
	<artifactId>gina-ldap-client</artifactId>
	<version>${gina-ldap-client.version}</version>
</dependency>
```

## Activation du pooling

Pour utilisation dans une application d�ploy�e sur Tomcat ou JBoss.

Il faut ajouter et adapter les propri�t�s suivantes dans le fichier ``catalina.properties`` :
```
com.sun.jndi.ldap.connect.pool.protocol=plain ssl
com.sun.jndi.ldap.connect.pool.authentication=simple
com.sun.jndi.ldap.connect.pool.maxsize=5
com.sun.jndi.ldap.connect.pool.prefsize=3
com.sun.jndi.ldap.connect.pool.timeout=240000
com.sun.jndi.ldap.connect.pool.initsize=2
#com.sun.jndi.ldap.connect.pool.debug=fine
```

D�finition des propri�t�s : voir https://docs.oracle.com/javase/jndi/tutorial/ldap/connect/config.html.

## Exemples d'utilisation

```
GinaApiLdapConfiguration ldapConf = new GinaApiLdapConfiguration(server, base, user, password, DOMAIN, timeout);
GinaApiLdapBaseAble api = GinaApiLdapBaseFactory.getInstance(ldapConf);
List<String> roles = api.getUserRoles("LAURENTJ");
```

Pour un exemple d'utilisation de toutes les m�thodes expos�es, se r�f�rer :
- aux classes de tests de l'api : ./src/test/java/gina/api
- aux classes de l'appli de d�mo : http://scm.etat-ge.ch:21080/cti.composant/ct-gina-ldap-client-demo/trunk/ct-gina-ldap-client-demo-war/src/main/java/ch/ge/ael/gina/infos/InfosController.java

# Annexe 1. Serveur LDAP sur un poste de d�veloppeur

## Serveur LDAP 

Pour effectuer les tests unitaires en mode local, c'est-�-dire autonome, sans appel � Gina, on utilise les
fichiers .ldif fournis. Pour exposer un fichier .ldif, il faut lancer un serveur LDAP sur ce fichier.
Pour cela, un serveur LDAP [UnboundID](https://ldap.com/unboundid-ldap-sdk-for-java) a �t� int�gr� dans 
les sources de ce projet.

Proc�dure :
- Ouvrir une fen�tre DOS et aller dans le r�pertoire du projet
- Ex�cuter les commandes suivantes :

```
cd src\test\resources
unboundid-ldapsdk-4.0.8\tools\in-memory-directory-server --baseDN "ou=CSBUGTRACK,o=gina" --port 30636 --ldifFile ldap_csbugtrack_full.ldif
```

Ceci doit afficher
```
Listening for client connections on port 30636.
```

Note. A ce jour, seule la classe de test ``GinaLdapDomainTest`` peut ainsi �tre test�e. Les autres classes 
``GinaLdapApplicationCtiGestrepoTest``, ``GinaLdapApplicationParallelTest`` et ``GinaLdapApplicationTest``
n'ont pas de fichier .ldif disponible.

## Explorateur LDAP

De fa�on facultative, on peut lancer un explorateur LDAP sur un serveur LDAP lanc� (Gina ou UnboundID).
L'explorateur, par exemple [ApacheDS](http://directory.apache.org/apacheds/downloads.html)
ou [JXplorer](http://www.jxplorer.org), permet de parcourir de fa�on conviviale un r�pertoire LDAP.

Proc�dure pour brancher JXplorer sur Gina :
- Installer JXplorer
- Lancer JXplorer
- Fichier > Se connecter
- Utiliser les param�tres de connexion fournis dans le POM de ce projet

![connexion LDAP](./doc/jxplorer_1.png)

![exploration LDAP](./doc/jxplorer_2.png)

# Annexe 2. Note sur l'usage d'IntelliJ

Pour les d�veloppeurs. IntelliJ ne sait pas interpr�ter ``<testSource>`` dans le POM et l�ve des erreurs
```
Error:(198, 48) java: lambda expressions are not supported in -source 1.6 (use -source 8 or higher to enable lambda expressions)
```
sur les classes de test, bien que ``<testSource>`` ait été initialis� � ``1.8``.
Pour �viter ces erreurs, aller dans ``Project Structure`` (Ctrl+Alt+Shift+S), puis  dans ``Modules``, puis mettre
``Language level`` � ``8``.

Ces erreurs sont sans cons�quence sur le ``mvn install``.

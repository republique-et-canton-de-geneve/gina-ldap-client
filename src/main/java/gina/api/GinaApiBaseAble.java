/*
 * GINA LDAP client
 *
 * Copyright 2016-2019 Republique et canton de Genève
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package gina.api;

import gina.impl.GinaException;

import javax.naming.NamingException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

public interface GinaApiBaseAble {

    /**
     * Donne le nom de l'utilisateur courant.
     *
     * @return nom de l'utilisateur courant
     */
    String getUser() throws RemoteException;

    /**
     * Indique si l'utilisateur est valide ou non.
     *
     * @param username nom de l'utilisateur
     * @return true si l'utilisateur existe, false sinon
     */
    boolean isValidUser(String username) throws RemoteException;

    /**
     * Donne la liste de tous les utilisateurs dans l'annuaire, avec les attributs demandes.
     *
     * @param filter debut du nom de l'utilisateur (min. 3 caractères)
     * @param attrs tableau des attributs, ou null pour obtenir tous les attributs disponibles
     * @return liste de Maps contenant les attributs demandés pour chaque utilisateur
     */
    List<Map<String, String>> getAllUsers(String filter, String[] attrs) throws RemoteException;

    /**
     * Donne les valeurs de certaines informations concernant un utilisateur.
     *
     * @param username nom de l'utilisateur
     * @param attrs tableau des noms des attributs, ou null pour obtenir tous les attributs disponibles
     * @return attributs sous la forme d'une Map <cle, valeur> (les cles sont les
     *         noms des attributs) ou null si l'utilisateur n'existe pas
     */
    Map<String, String> getUserAttrs(String username, String[] attrs) throws RemoteException;

    /**
     * Donne les valeurs de certaines informations concernant l'utilisateur.
     * Equivaut a : getUserAttrs(getUser(), attrs)
     *
     * @param attrs tableau des noms des attributs, ou null pour obtenir tous les attributs disponibles
     * @return attributs demandes sous la forme d'une Map. Les cles sont les noms des attributs
     */
    Map<String,String> getUserAttrs(String[] attrs) throws RemoteException;

    /**
     * Donne la langue de l'utilisateur courant.
     *
     * @return langue au format ISO (2 char majuscules)
     */
    String getLanguage() throws RemoteException;

    /**
     * Donne l'environnement courant.
     *
     * @return environnement courant (DEV/REC/PROD)
     */
    String getEnvironment() throws RemoteException;

    /**
     * Indique si l'utilisateur courant a le role donne' pour l'application donnee.
     *
     * @param application nom de l'application (ex: CTI.COMPOSANTSECURITE)
     * @param role nom du role
     * @return true si l'utilisateur courant a le role, false sinon
     */
    boolean hasRole(String application, String role) throws RemoteException;

    /**
     * Indique si l'utilisateur donne' a le role donne' pour l'application donnee.
     *
     * @param user nom de l'utilisateur
     * @param application nom de l'application (ex: CTI.COMPOSANTSECURITE)
     * @param role nom du role
     * @return true si l'utilisateur courant a le role, false sinon
     */
    boolean hasUserRole(String user, String application, String role) throws RemoteException;

    /**
     * Donne tous les roles de l'utilisateur courant, pour l'application donnee.
     *
     * @param application nom de l'application (ex: CTI.COMPOSANTSECURITE)
     * @return liste des noms des roles
     */
    List<String> getRoles(String application) throws RemoteException;

    /**
     * Donne tous les roles de l'utilisateur donne', pour l'application donnee.
     *
     * @param user nom de l'utilisateur.
     * @param application nom de l'application (ex: CTI.COMPOSANTSECURITE)
     * @return liste des noms des roles
     */
    List<String> getUserRoles(String user, String application) throws RemoteException;

    /**
     * Donne les roles d'un utilisateur dans un domaine donne'.
     * @param domain domaine de recherche des roles
     * @param applicationPrefix filtre sur les applications (filtre), minimum 3 caracteres
     * @return liste de roles en notation longue
     */
    List<String> getIntegrationUserRoles(String domain, String applicationPrefix) throws RemoteException;

    /**
     *
     * @param domain nom du domaine
     * @param application nom court de l'application
     * @return Liste d'attributs sous forme [nom attribut]=[valuer attribut]:
     * 		Si utilisateur n'est pas dans le role UTILISATEUR de l'application, ne retourne que "validUser=false"
     */
    @Deprecated
    List<String> getIntegrationUserAttributes(String domain, String application) throws RemoteException;

    /**
     * Donne tous les roles de l'application donnee.
     *
     * @param application nom long de l'application (ex: CTI.COMPOSANTSECURITE)
     * @return liste des noms des roles metier
     */
    List<String> getAppRoles(String application) throws RemoteException;

    /**
     * Donne tous les roles metier (RM-*) d'une application donnée
     *
     * @param application nom long de l'application (ex: CTI.COMPOSANTSECURITE)
     * @return liste des noms des roles metier
     */
     List<String> getBusinessRoles(String application) throws RemoteException;

    /**
     * Donne la liste des utilisateurs ayant acces a une application donnee.
     *
     * @param application nom de l'application (ex: CTI.COMPOSANTSECURITE)
     * @param attrs tableau des noms des attributs, ou null pour obtenir tous les attributs disponibles
     * @return liste de Maps contenant les attributs demandes pour chaque
     *         utilisateur de l'application
     * @deprecated Cette methode ne doit plus être utilisee
     */
    @Deprecated
    List<Map<String, String>> getUsers(String application, String[] attrs) throws RemoteException;

    /**
     * Donne la liste des utilisateurs ayant acces a l'application donnee, pour le role donne, avec les attributs
     * donnes.
     *
     * @param application nom de l'application (ex: CTI.COMPOSANTSECURITE)
     * @param role nom du role
     * @param attrs tableau des noms des attributs, ou null pour obtenir tous les attributs disponibles
     * @return liste de Maps contenant les attributs demandes pour chaque utilisateur ayant le role
     */
    List<Map<String, String>> getUsers(String application, String role, String[] attrs) throws RemoteException;

    /**
     * Donne la liste des utilisateurs par numéro de telephone.
     *
     * @param phoneNumber numero de telephone
     * @param activUser Status du compte utilisateur (actif/inactif)
     * @return liste des utilisateurs
     */
    List<Map<String, String>> getUsersByPhone(String phoneNumber, Boolean activUser, String[] attrs)
   			throws RemoteException;

   /**
    * Donne la liste des utilisateurs par numero SIRH.
    *
    * @param sirhNumber numero SIRH
    * @param activUser etat du compte utilisateur (actif/inactif)
    * @return liste des utilisateurs
    */
   List<Map<String,String>> getUsersBySIRHNumber(String sirhNumber, Boolean activUser, String[] attrs)
  			throws RemoteException;

   /**
    * Donne la liste des utilisateurs par nom de famille.
    *
    * @param name nom de famille
    * @param activUser etat du compte utilisateur (actif/inactif)
    * @return liste des utilisateurs
    */
   List<Map<String, String>> getUsersByName(String name, Boolean activUser, String[] attrs)
  			throws RemoteException;

    /**
     * Donne la liste des roles heritant (directement) du rôle donné
     * @param application
     * @param role
     * @return liste des roles
     */
    @Deprecated
    List<String> getInheritingRoles(String application, String role) throws RemoteException;

    /**
     * Donne les proprietes metier associees aux UO.
     * @param name nom de la propriete
     * @return Map dont les cles sont les chemins des UOs et les valeurs sont les listes des
     * valeurs de la propriete pour l'UO correspondante
     */
     List<String> getPMProprieteMetier(String name) throws RemoteException;

    /**
     *
     * @return Un identifiant métier unique pour l'utilisatuer courant s'il est de type PP ou PseudoPP
     * @throws GinaException : En cas de type de compte incorrect
     * @throws RemoteException
     */
    String getOwnIDUniqueForPPorPseudo()	throws RemoteException, NamingException;

    /**
     * Donne les proprietes metier associees à l'UO de l'utilisateur courant.
     * @param name nom de la propriete
     * @return Map dont les cles sont les chemins des UOs et les valeurs les listes des
     * valeurs de la propriete pour l'UO correspondante
     */
    List<String> getOwnPMProprieteMetier(String name) throws RemoteException;

    /**
     * Donne les proprietes metier associees a l'utilisateur courant.
     * @param name nom de la propriete
     * @return liste des valeurs de la propriete
     */
    @Deprecated
    List<String> getPPProprieteMetier(String name) throws RemoteException;

    /**
     * Donne les proprietes metier associees à l'UO de l'utilisateur courant.
     * @param name nom de la propriete
     * @return Map dont les cles sont les chemins des UOs et les valeurs les listes des
     * valeurs de la propriete pour l'UO correspondante
     */
    List<String> getOwnPPProprieteMetier(String name) throws RemoteException;

    /**
     * Envoie un e-mail.
     * @param from adresse de l'expediteur
     * @param to adresse(s) du(des) destinataire(s)
     * @param cc adresse(s) du(des) personne(s) en copie
     * @param subject sujet
     * @param text texte (corps) du message
     * @param mimeType type mime du texte (text/plain ou text/html)
     * @deprecated Cette methode ne doit plus etre utilisee
     */
    @Deprecated
    void sendMail(String from, String[] to, String[] cc, String subject, String text, String mimeType)
            throws RemoteException;
}

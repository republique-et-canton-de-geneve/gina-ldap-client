package gina.api;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import org.apache.log4j.Logger;

import gina.api.util.Configuration;



/*
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;*/

import javax.naming.NamingException;

public class GinaApiLdapBaseAbleDomainImpl implements GinaApiLdapBaseAble {
    
    private DirContext ctxtDir = null;
    // LOGGER

    private static Logger logger = Logger.getLogger(GinaApiLdapBaseAbleDomainImpl.class);
    
    private void init() throws GinaException {
	if (ctxtDir == null) {
	    System.out.println("init()");
	    Configuration conf = new Configuration();
	    conf.init();
	    ctxtDir = conf.getCtxtDir();
	    if (ctxtDir == null) {
		throw new GinaException("initialisation impossible");
	    }
	 }
    }

    /* (non-Javadoc)
     * retourne le user name de l'utilisateur courant
     * @see gina.api.GinaApiLdapBaseAble#getUser()
     */
    @Override 
    public String getUser() throws GinaException, RemoteException {
	init();
	return System.getProperty("user.name");
    }

    /* (non-Javadoc)
     * retourne boolean pour savoir si le user est valide
     * @see gina.api.GinaApiLdapBaseAble#isValidUser(java.lang.String)
     */
    @Override
    public boolean isValidUser(String user) throws GinaException, RemoteException {
	
	init();
	try {
		SearchControls searchControls = new SearchControls();
		searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		searchControls.setTimeLimit(30000);
		NamingEnumeration<?> answer = ctxtDir.search("ou=Users",  "(&(cn=*))" ,searchControls);

		if (answer != null) {
			while (answer.hasMoreElements()) {
				SearchResult sr = (SearchResult) answer.next();
				String name =  sr.getName().replace("cn=", "");  //.substring(0, sr.getName().indexOf(":")).replace("cn=", "");
				if (user.equalsIgnoreCase(name)){
				    return true;			    
				}
			}
		}
	}
	catch (NamingException e) {
	    logger.error(e);
	}
	
	return false;

    }

    @Override
    public List<Map<String, String>> getAllUsers(String paramString, String[] paramArrayOfString)
	    throws GinaException, RemoteException {

	return null;
    }

    /* (non-Javadoc)
     * Donne les valeurs des attributs passé en paramètre pour l'utilisateur passé en paramètre
     * @see gina.api.GinaApiLdapBaseAble#getUserAttrs(java.lang.String, java.lang.String[])
     */
    @Override
    public Map<String, String> getUserAttrs(String user, String[] paramArrayOfString)
	    throws GinaException, RemoteException {
	
	List<Map<String, String>> data = new ArrayList<Map<String, String>>();
	Arrays.asList(paramArrayOfString).contains("param");
	Map<String, String> myMap = new HashMap<String, String>();
	
	init();
	try {
	    SearchControls searchControls = new SearchControls();
	    searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
	    searchControls.setTimeLimit(30000);
	    NamingEnumeration<?> answer = ctxtDir.search("ou=Users",  "(&(cn=" + user + "))" ,searchControls);

	    if (answer != null) {
		while (answer.hasMoreElements()) {
		    SearchResult sr = (SearchResult) answer.next();
		   // String name =  sr.getName().substring(0, sr.getName().indexOf(",")).replace("cn=", "");
		   // if (user.equalsIgnoreCase(name)){

			Attributes attrs = sr.getAttributes();
			System.out.println("sr : " + sr);
			if (attrs != null) {
			    for (int i = 0; i < paramArrayOfString.length; i++) {
				Attribute attribute = attrs.get(paramArrayOfString[i]);   //Attribute attmember = attrs.get("member");
				
				if (attribute != null) {
				    for (int j = 0; j < attribute.size(); j++) {
					String member = (String) attribute.get(j);
					if (member != null) {
					    myMap.put(paramArrayOfString[i], attribute.toString().substring(attribute.toString().indexOf(":") + 2 ));
					}
				    }
				}
			    } 
			}
		   // }
		}
	    }
	}
	catch (NamingException e) {
	    logger.error(e);
	}

	return myMap;

    }

    /* (non-Javadoc)
     * Donne les valeurs des attributs passé en paramètre pour l'utilisateur courant
     * @see gina.api.GinaApiLdapBaseAble#getUserAttrs(java.lang.String[])
     */
    @Override
    public Map<String, String> getUserAttrs(String[] paramArrayOfString) throws GinaException, RemoteException {
	
	List<Map<String, String>> data = new ArrayList<Map<String, String>>();
	Arrays.asList(paramArrayOfString).contains("param");
	Map<String, String> myMap = new HashMap<String, String>();
	String user = System.getProperty("user.name");
	
	init();
	try {
	    SearchControls searchControls = new SearchControls();
	    searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
	    searchControls.setTimeLimit(30000);
	    NamingEnumeration<?> answer = ctxtDir.search("ou=Users",  "(&(cn=" + user + "))" ,searchControls);

	    if (answer != null) {
		while (answer.hasMoreElements()) {
		    SearchResult sr = (SearchResult) answer.next();
		   // String name =  sr.getName().substring(0, sr.getName().indexOf(",")).replace("cn=", "");
		   // if (user.equalsIgnoreCase(name)){

			Attributes attrs = sr.getAttributes();
			System.out.println("sr : " + sr);
			if (attrs != null) {
			    for (int i = 0; i < paramArrayOfString.length; i++) {
				Attribute attribute = attrs.get(paramArrayOfString[i]);   //Attribute attmember = attrs.get("member");
				
				if (attribute != null) {
				    for (int j = 0; j < attribute.size(); j++) {
					String member = (String) attribute.get(j);
					if (member != null) {
					    myMap.put(paramArrayOfString[i], attribute.toString().substring(attribute.toString().indexOf(":") + 2 ));
					}
				    }
				}
			    } 
			}
		   // }
		}
	    }
	}
	catch (NamingException e) {
	    logger.error(e);
	}

	return myMap;


    }

    @Override @Deprecated 
    public String getLanguage() throws GinaException, RemoteException {
	return null;
    }

    @Override @Deprecated 
    public String getEnvironment() throws GinaException, RemoteException {
	return null;
    }

    /* (non-Javadoc)
     * Retourne vrai si l'utilisateur courant à le role donné pour l'application donnée 
     * @see gina.api.GinaApiLdapBaseAble#hasUserRole(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public boolean hasRole(String appli, String role) throws GinaException, RemoteException {
	
	init();
	List<String> users = new ArrayList<String>();
	String user = System.getProperty("user.name");
	try {
		SearchControls searchControls = new SearchControls();
		searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		searchControls.setTimeLimit(30000);;

		NamingEnumeration<?> answer = ctxtDir.search("ou=" + appli,  "(&(cn=*))" ,searchControls);

		if (answer != null) {
			while (answer.hasMoreElements()) {
				SearchResult sr = (SearchResult) answer.next();
				System.out.println("name : " +  sr.getName().substring(0, sr.getName().indexOf(",")).replace("cn=", ""));
				
				Attributes attrs = sr.getAttributes();
			        System.out.println("sr : " + sr);
				if (attrs != null) {
					Attribute attmember = attrs.get("member");
				
					if (attmember != null) {
						for (int j = 0; j < attmember.size(); j++) {
							String member = (String) attmember.get(j);
							if (member != null) {
							    String username = member.substring(0, member.indexOf(",")).replace("cn=", "").toLowerCase();
							    if (user.equalsIgnoreCase(username)) {
								return true;
							    }
							}
						}
					}
				}
			}
		}
	}
	catch (NamingException e) {
	    logger.error(e);
	}
	
	return false;
    }

    /* (non-Javadoc)
     * Retourne vrai si l'utilisateur donné à le role donné pour l'application donnée 
     * @see gina.api.GinaApiLdapBaseAble#hasUserRole(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public boolean hasUserRole(String user, String appli, String role)
	    throws GinaException, RemoteException {
	
	init();
	List<String> users = new ArrayList<String>();

	try {
		SearchControls searchControls = new SearchControls();
		searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		searchControls.setTimeLimit(30000);;

		NamingEnumeration<?> answer = ctxtDir.search("ou=" + appli,  "(&(cn=*))" ,searchControls);

		if (answer != null) {
			while (answer.hasMoreElements()) {
				SearchResult sr = (SearchResult) answer.next();
				System.out.println("name : " +  sr.getName().substring(0, sr.getName().indexOf(",")).replace("cn=", ""));
				
				Attributes attrs = sr.getAttributes();
			        System.out.println("sr : " + sr);
				if (attrs != null) {
					Attribute attmember = attrs.get("member");
				
					if (attmember != null) {
						for (int j = 0; j < attmember.size(); j++) {
							String member = (String) attmember.get(j);
							if (member != null) {
							    String username = member.substring(0, member.indexOf(",")).replace("cn=", "").toLowerCase();
							    if (user.equalsIgnoreCase(username)) {
								return true;
							    }
							}
						}
					}
				}
			}
		}
	}
	catch (NamingException e) {
	    logger.error(e);
	}
	
	return false;
    }

    /* (non-Javadoc)
     *  Donne tous les rôles de l'utilisateur courant pour l'application passée en paramètre
     * @see gina.api.GinaApiLdapBaseAble#getRoles(java.lang.String)
     */
    @Override
    public List<String> getRoles(String appli) throws GinaException, RemoteException {
	init();
	List<String> roles = new ArrayList<String>();
	String user = System.getProperty("user.name");
	String role = "";
	try {

		SearchControls searchControls = new SearchControls();
		searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		searchControls.setTimeLimit(30000);
		NamingEnumeration<?> answer = ctxtDir.search("ou=" + appli,  "(&(cn=*))" ,searchControls);

		if (answer != null) {
			while (answer.hasMoreElements()) {
				SearchResult sr = (SearchResult) answer.next();
				System.out.println("sr : " + sr);
				role = sr.getName().substring(0, sr.getName().indexOf(",")).replace("cn=", "");
				Attributes attrs = sr.getAttributes();
				if (attrs != null) {
					Attribute attmember = attrs.get("member");

					if (attmember != null) {
						for (int j = 0; j < attmember.size(); j++) {
							String member = (String) attmember.get(j);
							if (member != null) {
							    String username = member.substring(0, member.indexOf(",")).replace("cn=", "").toLowerCase();
							    if (username.equalsIgnoreCase(user)) {
								roles.add(role);								
							    }

							}
						}
					}
				}
			}
		}
	}
	catch (NamingException e) {
	    logger.error(e);
	}
	
	return roles;
    }

    /* (non-Javadoc)
     * Donne tous les rôles de l'utilisateur passé en paramètre pour l'application passée en paramètre.
     * @see gina.api.GinaApiLdapBaseAble#getUserRoles(java.lang.String, java.lang.String)
     */
    @Override
    public List<String> getUserRoles(String user, String appli) throws GinaException, RemoteException {
	
	init();
	List<String> roles = new ArrayList<String>();
	String role = "";
	try {

		SearchControls searchControls = new SearchControls();
		searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		searchControls.setTimeLimit(30000);
		NamingEnumeration<?> answer = ctxtDir.search("ou=" + appli,  "(&(cn=*))" ,searchControls);

		if (answer != null) {
			while (answer.hasMoreElements()) {
				SearchResult sr = (SearchResult) answer.next();
				System.out.println("sr : " + sr);
				role = sr.getName().substring(0, sr.getName().indexOf(",")).replace("cn=", "");
				Attributes attrs = sr.getAttributes();
				if (attrs != null) {
					Attribute attmember = attrs.get("member");

					if (attmember != null) {
						for (int j = 0; j < attmember.size(); j++) {
							String member = (String) attmember.get(j);
							if (member != null) {
							    String username = member.substring(0, member.indexOf(",")).replace("cn=", "").toLowerCase();
							    if (username.equalsIgnoreCase(user)) {
								roles.add(role);								
							    }

							}
						}
					}
				}
			}
		}
	}
	catch (NamingException e) {
	    logger.error(e);
	}
	
	return roles;
    }

    @Override
    public List<String> getIntegrationUserRoles(String paramString1, String paramString2)
	    throws GinaException, RemoteException {
	
	return null;
    }

    @Override
    public List<String> getIntegrationUserAttributes(String paramString1, String paramString2)
	    throws GinaException, RemoteException {
	
	return null;
    }

    /* (non-Javadoc)
     * retoune les roles d'une application
     * @see gina.api.GinaApiLdapBaseAble#getAppRoles(java.lang.String)
     */
    @Override
    public List<String> getAppRoles(String appli) throws GinaException, RemoteException {
	
	init();
	List<String> roles = new ArrayList<String>();
	try {

	    SearchControls searchControls = new SearchControls();
	    searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
	    searchControls.setTimeLimit(30000);
	    NamingEnumeration<?> answer = ctxtDir.search("ou=" + appli,  "(&(cn=*))" ,searchControls);

	    if (answer != null) {
		while (answer.hasMoreElements()) {
		    SearchResult sr = (SearchResult) answer.next();
		    System.out.println("name : " +  sr.getName().substring(0, sr.getName().indexOf(",")).replace("cn=", ""));
		    roles.add(sr.getName().substring(0, sr.getName().indexOf(",")).replace("cn=", ""));
		   
		}
	    }
	}
	catch (NamingException e) {
	    logger.error(e);
	}

	return roles;
    }

    @Override
    public List<String> getBusinessRoles(String paramString) throws GinaException, RemoteException {
	
	return null;
    }

    /* (non-Javadoc)
     * Donne la liste des utilisateurs ayant accès à l'application passée en paramètre, avec les attributs demandés 
     * @see gina.api.GinaApiLdapBaseAble#getUsers(java.lang.String)
     */
    @Override
    public List<Map<String, String>> getUsers(String appli, String[] paramArrayOfString)
	    throws GinaException, RemoteException {

	init();
	List<String> users = new ArrayList<String>();
	List<Map<String, String>> list = new ArrayList<Map<String, String>>();
	try {

		SearchControls searchControls = new SearchControls();
		searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		searchControls.setTimeLimit(30000);
		NamingEnumeration<?> answer = ctxtDir.search("ou=" + appli,  "(&(cn=*))" ,searchControls);

		if (answer != null) {
			while (answer.hasMoreElements()) {
				SearchResult sr = (SearchResult) answer.next();
				System.out.println("name : " +  sr.getName().substring(0, sr.getName().indexOf(",")).replace("cn=", ""));
				
				Attributes attrs = sr.getAttributes();
			        System.out.println("sr : " + sr);
				if (attrs != null) {
					Attribute attmember = attrs.get("member");

					if (attmember != null) {
						for (int j = 0; j < attmember.size(); j++) {
							String member = (String) attmember.get(j);
							
							if (member != null) {
							    
							    String username = member.substring(0, member.indexOf(",")).replace("cn=", "").toLowerCase();
							    if (!users.contains(username)) {
								Map<String, String> map = new HashMap<String, String>();
								users.add(username);
								map = this.getUserAttrs(username, paramArrayOfString);
								  /*  for (int i = 0; i < paramArrayOfString.length; i++) {
									Attribute attribute = attrs.get(paramArrayOfString[i]);   //Attribute attmember = attrs.get("member");
									
									if (attribute != null) {
									    
									    for (int k = 0; k < attribute.size(); k++) {
										String tribute = (String) attribute.get(k);
										if (tribute != null) {
										    map.put(paramArrayOfString[i], tribute.toString());
										}
									    }
									}
								    } */
								    
								list.add(map);
							    }							   
							}
						}
					}
				}
			}
		}
	}
	catch (NamingException e) {
	    logger.error(e);
	}
	
	return list;
    }

    /* (non-Javadoc)
     * Donne la liste des utilisateurs ayant accès à l'application passée en paramètre pour le rôle donné, avec les attributs demandés  
     * @see gina.api.GinaApiLdapBaseAble#getUsers(java.lang.String, java.lang.String, java.lang.String[])
     */
    @Override
    public List<Map<String, String>> getUsers(String appli, String role, String[] paramArrayOfString)
	    throws GinaException, RemoteException {
	

	init();
	List<String> users = new ArrayList<String>();
	List<Map<String, String>> list = new ArrayList<Map<String, String>>();
	try {

		SearchControls searchControls = new SearchControls();
		searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		searchControls.setTimeLimit(30000);
		NamingEnumeration<?> answer = ctxtDir.search("ou=" + appli,  "(&(cn=" + role + "))" ,searchControls);

		if (answer != null) {
			while (answer.hasMoreElements()) {
				SearchResult sr = (SearchResult) answer.next();
				System.out.println("name : " +  sr.getName().substring(0, sr.getName().indexOf(",")).replace("cn=", ""));
				
				Attributes attrs = sr.getAttributes();
			        System.out.println("sr : " + sr);
				if (attrs != null) {
					Attribute attmember = attrs.get("member");

					if (attmember != null) {
						for (int j = 0; j < attmember.size(); j++) {
							String member = (String) attmember.get(j);
							
							if (member != null) {
							    
							    String username = member.substring(0, member.indexOf(",")).replace("cn=", "").toLowerCase();
							    if (!users.contains(username)) {
								Map<String, String> map = new HashMap<String, String>();
								users.add(username);
								map = this.getUserAttrs(username, paramArrayOfString);
								  /*  for (int i = 0; i < paramArrayOfString.length; i++) {
									Attribute attribute = attrs.get(paramArrayOfString[i]);   //Attribute attmember = attrs.get("member");
									
									if (attribute != null) {
									    
									    for (int k = 0; k < attribute.size(); k++) {
										String tribute = (String) attribute.get(k);
										if (tribute != null) {
										    map.put(paramArrayOfString[i], tribute.toString());
										}
									    }
									}
								    } */
								    
								list.add(map);
							    }							   
							}
						}
					}
				}
			}
		}
	}
	catch (NamingException e) {
	    logger.error(e);
	}
	
	return list;
    }

    @Override
    public List<Map<String, String>> getUsersByPhone(String paramString, Boolean paramBoolean,
	    String[] paramArrayOfString) throws GinaException, RemoteException {
	
	return null;
    }

    @Override
    public List<Map<String, String>> getUsersBySIRHNumber(String paramString, Boolean paramBoolean,
	    String[] paramArrayOfString) throws GinaException, RemoteException {
	
	return null;
    }

    @Override
    public List<Map<String, String>> getUsersByName(String paramString, Boolean paramBoolean,
	    String[] paramArrayOfString) throws GinaException, RemoteException {
	
	return null;
    }

    @Override
    public List<String> getInheritingRoles(String paramString1, String paramString2) {
	
	return null;
    }

    @Override
    public List<String> getPMProprieteMetier(String paramString) {
	
	return null;
    }

    @Override
    public String getOwnIDUniqueForPPorPseudo() {
	
	return null;
    }

    @Override
    public List<String> getOwnPMProprieteMetier(String paramString)  {
	
	return null;
    }

    @Override
    public List<String> getPPProprieteMetier(String paramString)  {
	
	return null;
    }

    @Override
    public List<String> getOwnPPProprieteMetier(String paramString) {
	
	return null;
    }


  /*  @Override
    public void sendMail(String paramString1, String[] paramArrayOfString1, String[] paramArrayOfString2,
	    String paramString2, String paramString3, String paramString4) throws GinaException, RemoteException {
    }*/

}

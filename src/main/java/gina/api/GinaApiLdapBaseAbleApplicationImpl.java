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
import javax.xml.crypto.Data;

import org.apache.log4j.Logger;

import gina.api.util.Configuration;



/*
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;*/

import javax.naming.NamingException;

public class GinaApiLdapBaseAbleApplicationImpl implements GinaApiLdapBaseAble {
    
    private DirContext ctxtDir = null;


    private static Logger logger = Logger.getLogger(GinaApiLdapBaseAbleApplicationImpl.class);
    
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
	// new version
	init();
	try {
	    SearchControls searchControls = new SearchControls();
	    searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
	    searchControls.setTimeLimit(30000);
	    Attributes matchAttrs = new BasicAttributes(true);
	    matchAttrs.put(new BasicAttribute("cn", user));
	    String searchFilter = "(&(objectClass=user)(cn=" + user + "))";
	    NamingEnumeration<?> answer = ctxtDir.search("",  searchFilter ,searchControls);

	    logger.info("answer : ");
	    while (answer.hasMoreElements()) {
		SearchResult sr = (SearchResult) answer.next();
		logger.info("sr : " + sr);
		Attributes  attrs = sr.getAttributes();
		NamingEnumeration<? extends Attribute> attributEnum = attrs.getAll();
		while (attributEnum.hasMoreElements()) {
		    Attribute at = attributEnum.next();
		    NamingEnumeration<?> nameEnum = sr.getAttributes().get(at.getID()).getAll();
		    while (nameEnum.hasMoreElements()) {
			String  s = (String) nameEnum.next();
			logger.info("value: " + s);
		    }
		    //logger.info("attribut: " + at.getID() + ":" + sr.getAttributes().get(at.getID()).get());
		}
		if (attrs != null) {
		    Attribute attmember = attrs.get("cn");
		    logger.info(attmember.getID());
		}
		//String name =  sr.getName().substring(0, sr.getName().indexOf(",")).replace("cn=", "");
		String name =  (String) sr.getAttributes().get("cn").get();
		logger.info("name:" + name);
		if (user.equalsIgnoreCase(name)){
		    return true;			    
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
     * Donne les valeurs des attributs pass� en param�tre pour l'utilisateur pass� en param�tre
     * @see gina.api.GinaApiLdapBaseAble#getUserAttrs(java.lang.String, java.lang.String[])
     */
    @Override
    public Map<String, String> getUserAttrs(String user, String[] paramArrayOfString)
	    throws GinaException, RemoteException {
	// new version
	Arrays.asList(paramArrayOfString).contains("param");
	Map<String, String> myMap = new HashMap<String, String>();
	
	init();
	try {
	    SearchControls searchControls = new SearchControls();
	    searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
	    searchControls.setTimeLimit(30000);
	    String searchFilter = "(&(objectClass=user)(cn=" + user + "))";
	    NamingEnumeration<?> answer = ctxtDir.search("", searchFilter,searchControls);
	    
		while (answer.hasMoreElements()) {
		    SearchResult sr = (SearchResult) answer.next();
		    Attributes  attrs = sr.getAttributes();
		    for (int i = 0; i < paramArrayOfString.length; i++) {		
			NamingEnumeration<?> nameEnum = sr.getAttributes().get(paramArrayOfString[i]).getAll();
			String  value = "";
			while (nameEnum.hasMoreElements()) {
			    if (value.isEmpty()) {
				value = (String) nameEnum.next();
			    } else {
				value = value + ":" + (String) nameEnum.next();
			    }									
			}
			logger.info("value: " + value);
			myMap.put(paramArrayOfString[i], value);
			
			
		    }

		}
	    
	}
	catch (NamingException e) {
	    logger.error(e);
	}

	return myMap;

    }

    /* (non-Javadoc)
     * Donne les valeurs des attributs pass� en param�tre pour l'utilisateur courant
     * @see gina.api.GinaApiLdapBaseAble#getUserAttrs(java.lang.String[])
     */
    @Override
    public Map<String, String> getUserAttrs(String[] paramArrayOfString) throws GinaException, RemoteException {
	// new version
	List<Map<String, String>> data = new ArrayList<Map<String, String>>();
	Arrays.asList(paramArrayOfString).contains("param");
	Map<String, String> myMap = new HashMap<String, String>();
	String user = System.getProperty("user.name");
	
	init();
	try {
	    SearchControls searchControls = new SearchControls();
	    searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
	    String searchFilter = "(&(objectClass=user)(cn=" + user + "))";
	    NamingEnumeration<?> answer = ctxtDir.search("", searchFilter,searchControls);

		while (answer.hasMoreElements()) {
		    SearchResult sr = (SearchResult) answer.next();
		    Attributes  attrs = sr.getAttributes();
		    for (int i = 0; i < paramArrayOfString.length; i++) {		
			NamingEnumeration<?> nameEnum = sr.getAttributes().get(paramArrayOfString[i]).getAll();
			String  value = "";
			while (nameEnum.hasMoreElements()) {
			    if (value.isEmpty()) {
				value = (String) nameEnum.next();
			    } else {
				value = value + ":" + (String) nameEnum.next();
			    }									
			}
			logger.info("value: " + value);
			myMap.put(paramArrayOfString[i], value);

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
     * Retourne vrai si l'utilisateur courant � le role donn� pour l'application donn�e 
     * @see gina.api.GinaApiLdapBaseAble#hasUserRole(java.lang.String, java.lang.String, java.lang.String)
     */

    public boolean hasRole(String role) throws GinaException, RemoteException {
	// new version
	init();

	String user = System.getProperty("user.name");
	try {
	    SearchControls searchControls = new SearchControls();
	    searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
	    searchControls.setTimeLimit(30000);

	    String searchFilter = "(&(objectClass=groups)(cn=" + role + ")(cn=" + user + "))";
	    NamingEnumeration<?> answer = ctxtDir.search("",  searchFilter ,searchControls);

	    while (answer.hasMoreElements()) {
		SearchResult sr = (SearchResult) answer.next();
		logger.info("sr : " + sr);
		return true;
	    }

	}
	catch (NamingException e) {
	    logger.error(e);
	}

	return false;
    }

    /* (non-Javadoc)
     * Retourne vrai si l'utilisateur donn� � le role donn� pour l'application donn�e 
     * @see gina.api.GinaApiLdapBaseAble#hasUserRole(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public boolean hasUserRole(String user,  String role)
	    throws GinaException, RemoteException {
	// new version
	init();
	List<String> users = new ArrayList<String>();

	try {
		SearchControls searchControls = new SearchControls();
		searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		searchControls.setTimeLimit(30000);

		String searchFilter = "(&(objectClass=groups)(cn=" + role + ")(cn=" + user + "))";
		    NamingEnumeration<?> answer = ctxtDir.search("",  searchFilter ,searchControls);

		    while (answer.hasMoreElements()) {
			SearchResult sr = (SearchResult) answer.next();
			logger.info("sr : " + sr);
			return true;
		    }
		    
	}
	catch (NamingException e) {
	    logger.error(e);
	}
	
	return false;
    }

    /* (non-Javadoc)
     *  Donne tous les r�les de l'utilisateur courant pour l'application pass�e en param�tre
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
				//System.out.println("sr : " + sr);
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
     * Donne tous les r�les de l'utilisateur pass� en param�tre pour l'application pass�e en param�tre.
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
				//System.out.println("sr : " + sr);
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
	    System.out.println("getAppRoles");	
	    SearchControls searchControls = new SearchControls();
	    searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
	    searchControls.setTimeLimit(30000);
	    NamingEnumeration<?> answer = ctxtDir.search("ou="  + appli ,  "(&(cn=*))" ,searchControls);
	    
	    if (answer != null) {
		while (answer.hasMoreElements()) {
		    SearchResult sr = (SearchResult) answer.next();
		    //System.out.println("sr : " + sr);
		    //System.out.println("name : " +  sr.getName().substring(0, sr.getName().indexOf(",")).replace("cn=", ""));
		    if (sr.getName().indexOf("ou=Groups") > 0) {
			roles.add(sr.getName().substring(0, sr.getName().indexOf(",")).replace("cn=", ""));
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
    public List<String> getBusinessRoles(String paramString) throws GinaException, RemoteException {
	
	return null;
    }

    /* (non-Javadoc)
     * Donne la liste des utilisateurs ayant acc�s � l'application pass�e en param�tre, avec les attributs demand�s 
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
     * Donne la liste des utilisateurs ayant acc�s � l'application pass�e en param�tre pour le r�le donn�, avec les attributs demand�s  
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

    @Override
    public boolean hasRole(String paramString1, String paramString2) throws GinaException, RemoteException {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public boolean hasUserRole(String paramString1, String paramString2, String paramString3)
	    throws GinaException, RemoteException {
	// TODO Auto-generated method stub
	return false;
    }


  /*  @Override
    public void sendMail(String paramString1, String[] paramArrayOfString1, String[] paramArrayOfString2,
	    String paramString2, String paramString3, String paramString4) throws GinaException, RemoteException {
    }*/

}

package gina.api;

import gina.api.util.Configuration;


import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import org.apache.log4j.Logger;


public class GinaApiLdapBaseFactory {
    
    private static final Logger LOG = Logger.getLogger(GinaApiLdapBaseFactory.class);
    
    public static GinaApiLdapBaseAble getInstanceApplication()
    {
	try {
	    ClassLoader cl = Thread.currentThread().getContextClassLoader();
            Class implClass = cl.loadClass("gina.api.GinaApiLdapBaseAbleApplicationImpl");
            return (GinaApiLdapBaseAble)implClass.newInstance();
        
      }
      catch (RuntimeException e) {
	LOG.error(e);
        throw e;
      } catch (Exception e) {
        e.printStackTrace();
        throw new GinaException(e.getMessage());
      }
    }
    
    

    public static GinaApiLdapBaseAble getInstanceDomain()
    {
	try {
	    ClassLoader cl = Thread.currentThread().getContextClassLoader();
            Class implClass = cl.loadClass("gina.api.GinaApiLdapBaseAbleDomainImpl");
            return (GinaApiLdapBaseAble)implClass.newInstance();
        
      }
      catch (RuntimeException e) {
	LOG.error(e);
        throw e;
      } catch (Exception e) {
	LOG.error(e);
        throw new GinaException(e.getMessage());
      }
    }

    public static GinaApiLdapConfig getInstanceConfigApplication()
    {
	try {
	    ClassLoader cl = Thread.currentThread().getContextClassLoader();
            Class implClass = cl.loadClass("gina.api.GinaApiLdapBaseAbleApplicationImpl");
            return (GinaApiLdapConfig)implClass.newInstance();
        
      }
      catch (RuntimeException e) {
	LOG.error(e);
        throw e;
      } catch (Exception e) {
	LOG.error(e);
        throw new GinaException(e.getMessage());
      }
    }
    
    

    public static GinaApiLdapConfig getInstanceConfigDomain()
    {
	try {
	    ClassLoader cl = Thread.currentThread().getContextClassLoader();
            Class implClass = cl.loadClass("gina.api.GinaApiLdapBaseAbleDomainImpl");
            return (GinaApiLdapConfig)implClass.newInstance();
        
      }
      catch (RuntimeException e) {
        e.printStackTrace();
        throw e;
      } catch (Exception e) {
        e.printStackTrace();
        throw new GinaException(e.getMessage());
      }
    }


/*    public static GinaApiLdapAppsAble getInstance()
    {
      try
      {
        String packageName = GinaApiLdapAppsAble.class.getPackage().getName();
        String urlNaming = packageName + ".im.UrlNaming";
        String urlDummy = packageName + ".im.Dummy";
        String url = Configuration.getString(urlNaming, null);
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (url == null) {
          boolean dummy = Boolean.parseBoolean(Configuration.getString(urlDummy, null));
          if (dummy)
          {
            String dummyPackage = packageName + ".dummy.GinaApiAppsImpl";
            LOG.warn("Dummy mode enabled, instanciating class [" + dummyPackage + "]");
            Class implClass = cl.loadClass(dummyPackage);
            return (GinaApiLdapAppsAble)implClass.newInstance();
          }
          throw new GinaException("Unable to instanciate GinaApiApps (parameter '" + urlNaming + "' not defined in properties)");
        }

        Hashtable props = new Hashtable();
        props.put("java.naming.provider.url", url);
        props.put("java.naming.factory.initial", Configuration.getString("java.naming.factory.initial", "org.objectweb.carol.jndi.spi.MultiOrbInitialContextFactory"));

        String s = Configuration.getString("java.naming.factory.url.pkgs", null);
        if (s != null) {
          props.put("java.naming.factory.url.pkgs", s);
        }
        Context cxt = new InitialContext(props);
        Class homeClass = cl.loadClass(packageName + ".im.GinaApiAppsHome");
        Object home = PortableRemoteObject.narrow(cxt.lookup("ejb/" + packageName.replace('.', '/') + "/im/GinaApiApps"), homeClass);

        Method create = homeClass.getMethod("create", new Class[0]);
        return (GinaApiLdapAppsAble)create.invoke(home, new Object[0]);
      }
      catch (RuntimeException e) {
        e.printStackTrace();
        throw e;
      } catch (Exception e) {
        e.printStackTrace();
        throw new GinaException(e.getMessage());
      }
    }*/

}

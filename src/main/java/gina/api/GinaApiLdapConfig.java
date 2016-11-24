package gina.api;

import javax.naming.directory.DirContext;

public abstract interface GinaApiLdapConfig
{
  
  public abstract void setInitTest(DirContext dir)
    throws GinaException;

 
  
}// End of source file
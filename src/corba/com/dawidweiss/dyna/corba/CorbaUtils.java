package com.dawidweiss.dyna.corba;

import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;

/**
 * OpenORB related utilities.
 */
public final class CorbaUtils
{
    private CorbaUtils()
    {
        // no instances.
    }
    
    /*
     * 
     */
    public static ORB initORB(String iiop_host, int iiop_port)
    {
        Properties props = new Properties();
        props.put("org.omg.CORBA.ORBSingletonClass", "org.openorb.orb.core.ORBSingleton");
        props.put("org.omg.CORBA.ORBClass", "org.openorb.orb.core.ORB");
        if (!StringUtils.isEmpty(iiop_host))
        {
            props.put("iiop.host", iiop_host);
        }
        if (iiop_port > 0)
        {
            props.put("iiop.port", Integer.toString(iiop_port));
        }
        return org.omg.CORBA.ORB.init(new String[0], props);
    }

    /*
     * 
     */
    public static POA rootPOA(ORB orb)
    {
        try
        {
            POA rootPOA = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootPOA.the_POAManager().activate();
            return rootPOA;
        }
        catch (org.omg.CORBA.ORBPackage.InvalidName ex)
        {
            throw new RuntimeException("RootPOA missing?");
        }
        catch (AdapterInactive e)
        {
            throw new RuntimeException(e);
        }
    }

}

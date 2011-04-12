// **********************************************************************
//
// Copyright (c) 2003-2010 ZeroC, Inc. All rights reserved.
//
// This copy of Ice is licensed to you under the terms described in the
// ICE_LICENSE file included in this distribution.
//
// **********************************************************************

// Ice version 3.4.1

package Ice;

// <auto-generated>
//
// Generated from file `Properties.ice'
//
// Warning: do not edit this file.
//
// </auto-generated>


/**
 * Get all properties whose keys begins with
 * <em>prefix</em>. If
 * <em>prefix</em> is an empty string,
 * then all properties are returned.
 * 
 **/

public abstract class Callback_PropertiesAdmin_getPropertiesForPrefix extends Ice.TwowayCallback
{
    public abstract void response(java.util.Map<java.lang.String, java.lang.String> __ret);

    public final void __completed(Ice.AsyncResult __result)
    {
        PropertiesAdminPrx __proxy = (PropertiesAdminPrx)__result.getProxy();
        java.util.Map<java.lang.String, java.lang.String> __ret = null;
        try
        {
            __ret = __proxy.end_getPropertiesForPrefix(__result);
        }
        catch(Ice.LocalException __ex)
        {
            exception(__ex);
            return;
        }
        response(__ret);
    }
}

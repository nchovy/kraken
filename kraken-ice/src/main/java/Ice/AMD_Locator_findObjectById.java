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
// Generated from file `Locator.ice'
//
// Warning: do not edit this file.
//
// </auto-generated>


/**
 * Find an object by identity and return its proxy.
 * 
 **/

public interface AMD_Locator_findObjectById extends Ice.AMDCallback
{
    /**
     * ice_response indicates that
     * the operation completed successfully.
     * @param __ret (return value) The proxy, or null if the object is not active.
     * 
     **/
    void ice_response(Ice.ObjectPrx __ret);
}

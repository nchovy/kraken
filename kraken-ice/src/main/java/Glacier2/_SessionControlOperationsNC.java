// **********************************************************************
//
// Copyright (c) 2003-2010 ZeroC, Inc. All rights reserved.
//
// This copy of Ice is licensed to you under the terms described in the
// ICE_LICENSE file included in this distribution.
//
// **********************************************************************

// Ice version 3.4.1

package Glacier2;

// <auto-generated>
//
// Generated from file `Session.ice'
//
// Warning: do not edit this file.
//
// </auto-generated>


/**
 * An administrative session control object, which is tied to the
 * lifecycle of a {@link Session}.
 * 
 * @see Session
 * 
 **/
public interface _SessionControlOperationsNC
{
    /**
     * Access the object that manages the allowable categories
     * for object identities for this session. 
     * 
     * @return A StringSet object.
     * 
     **/
    StringSetPrx categories();

    /**
     * Access the object that manages the allowable adapter identities
     * for objects for this session. 
     * 
     * @return A StringSet object.
     * 
     **/
    StringSetPrx adapterIds();

    /**
     * Access the object that manages the allowable object identities
     * for this session. 
     * 
     * @return An IdentitySet object.
     * 
     **/
    IdentitySetPrx identities();

    /**
     * Get the session timeout.
     * 
     * @return The timeout.
     * 
     **/
    int getSessionTimeout();

    /**
     * Destroy the associated session.
     * 
     **/
    void destroy();
}

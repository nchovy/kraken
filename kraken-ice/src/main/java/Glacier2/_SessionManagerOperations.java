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
 * The session manager for username/password authenticated users that
 * is responsible for managing {@link Session} objects. New session objects
 * are created by the {@link Router} object calling on an application-provided
 * session manager. If no session manager is provided by the application,
 * no client-visible sessions are passed to the client.
 * 
 * @see Router
 * @see Session
 * 
 **/
public interface _SessionManagerOperations
{
    /**
     * Create a new session.
     * 
     * @param userId The user id for the session.
     * 
     * @param control A proxy to the session control object.
     * 
     * @param __current The Current object for the invocation.
     * @return A proxy to the newly created session.
     * 
     * @throws CannotCreateSessionException Raised if the session
     * cannot be created.
     * 
     **/
    SessionPrx create(String userId, SessionControlPrx control, Ice.Current __current)
        throws CannotCreateSessionException;
}

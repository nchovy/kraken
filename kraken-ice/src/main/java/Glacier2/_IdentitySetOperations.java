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
 * An object for managing the set of object identity constraints on a
 * {@link Session}. 
 * 
 * @see Session
 * @see SessionControl
 * 
 **/
public interface _IdentitySetOperations
{
    /**
     * Add a sequence of Ice identities to this set of constraints. Order is
     * not preserved and duplicates are implicitly removed.
     * 
     * @param additions The sequence of Ice identities to be added.
     * 
     * @param __current The Current object for the invocation.
     **/
    void add(Ice.Identity[] additions, Ice.Current __current);

    /**
     * Remove a sequence of identities from this set of constraints. No
     * errors are returned if an entry is not found.
     * 
     * @param deletions The sequence of Ice identities to be removed.
     * 
     * @param __current The Current object for the invocation.
     **/
    void remove(Ice.Identity[] deletions, Ice.Current __current);

    /**
     * Returns a sequence of identities describing the constraints in this
     * set.
     * 
     * @param __current The Current object for the invocation.
     * @return The sequence of Ice identities for this set. 
     * 
     **/
    Ice.Identity[] get(Ice.Current __current);
}

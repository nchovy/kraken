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
// Generated from file `LocalException.ice'
//
// Warning: do not edit this file.
//
// </auto-generated>


/**
 * This exception is raised if an operation call on a server raises an
 * unknown exception. For example, for C++, this exception is raised
 * if the server throws a C++ exception that is not directly or
 * indirectly derived from <tt>Ice::LocalException</tt> or
 * <tt>Ice::UserException</tt>.
 * 
 **/
public class UnknownException extends Ice.LocalException
{
    public UnknownException()
    {
    }

    public UnknownException(String unknown)
    {
        this.unknown = unknown;
    }

    public String
    ice_name()
    {
        return "Ice::UnknownException";
    }

    /**
     * This field is set to the textual representation of the unknown 
     * exception if available.
     * 
     **/
    public String unknown;
}

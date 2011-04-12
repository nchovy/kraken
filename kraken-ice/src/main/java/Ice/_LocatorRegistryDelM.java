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


public final class _LocatorRegistryDelM extends Ice._ObjectDelM implements _LocatorRegistryDel
{
    public void
    setAdapterDirectProxy(String id, Ice.ObjectPrx proxy, java.util.Map<String, String> __ctx)
        throws IceInternal.LocalExceptionWrapper,
               AdapterAlreadyActiveException,
               AdapterNotFoundException
    {
        IceInternal.Outgoing __og = __handler.getOutgoing("setAdapterDirectProxy", Ice.OperationMode.Idempotent, __ctx);
        try
        {
            try
            {
                IceInternal.BasicStream __os = __og.os();
                __os.writeString(id);
                __os.writeProxy(proxy);
            }
            catch(Ice.LocalException __ex)
            {
                __og.abort(__ex);
            }
            boolean __ok = __og.invoke();
            try
            {
                if(!__ok)
                {
                    try
                    {
                        __og.throwUserException();
                    }
                    catch(AdapterAlreadyActiveException __ex)
                    {
                        throw __ex;
                    }
                    catch(AdapterNotFoundException __ex)
                    {
                        throw __ex;
                    }
                    catch(Ice.UserException __ex)
                    {
                        throw new Ice.UnknownUserException(__ex.ice_name());
                    }
                }
                __og.is().skipEmptyEncaps();
            }
            catch(Ice.LocalException __ex)
            {
                throw new IceInternal.LocalExceptionWrapper(__ex, false);
            }
        }
        finally
        {
            __handler.reclaimOutgoing(__og);
        }
    }

    public void
    setReplicatedAdapterDirectProxy(String adapterId, String replicaGroupId, Ice.ObjectPrx p, java.util.Map<String, String> __ctx)
        throws IceInternal.LocalExceptionWrapper,
               AdapterAlreadyActiveException,
               AdapterNotFoundException,
               InvalidReplicaGroupIdException
    {
        IceInternal.Outgoing __og = __handler.getOutgoing("setReplicatedAdapterDirectProxy", Ice.OperationMode.Idempotent, __ctx);
        try
        {
            try
            {
                IceInternal.BasicStream __os = __og.os();
                __os.writeString(adapterId);
                __os.writeString(replicaGroupId);
                __os.writeProxy(p);
            }
            catch(Ice.LocalException __ex)
            {
                __og.abort(__ex);
            }
            boolean __ok = __og.invoke();
            try
            {
                if(!__ok)
                {
                    try
                    {
                        __og.throwUserException();
                    }
                    catch(AdapterAlreadyActiveException __ex)
                    {
                        throw __ex;
                    }
                    catch(AdapterNotFoundException __ex)
                    {
                        throw __ex;
                    }
                    catch(InvalidReplicaGroupIdException __ex)
                    {
                        throw __ex;
                    }
                    catch(Ice.UserException __ex)
                    {
                        throw new Ice.UnknownUserException(__ex.ice_name());
                    }
                }
                __og.is().skipEmptyEncaps();
            }
            catch(Ice.LocalException __ex)
            {
                throw new IceInternal.LocalExceptionWrapper(__ex, false);
            }
        }
        finally
        {
            __handler.reclaimOutgoing(__og);
        }
    }

    public void
    setServerProcessProxy(String id, ProcessPrx proxy, java.util.Map<String, String> __ctx)
        throws IceInternal.LocalExceptionWrapper,
               ServerNotFoundException
    {
        IceInternal.Outgoing __og = __handler.getOutgoing("setServerProcessProxy", Ice.OperationMode.Idempotent, __ctx);
        try
        {
            try
            {
                IceInternal.BasicStream __os = __og.os();
                __os.writeString(id);
                ProcessPrxHelper.__write(__os, proxy);
            }
            catch(Ice.LocalException __ex)
            {
                __og.abort(__ex);
            }
            boolean __ok = __og.invoke();
            try
            {
                if(!__ok)
                {
                    try
                    {
                        __og.throwUserException();
                    }
                    catch(ServerNotFoundException __ex)
                    {
                        throw __ex;
                    }
                    catch(Ice.UserException __ex)
                    {
                        throw new Ice.UnknownUserException(__ex.ice_name());
                    }
                }
                __og.is().skipEmptyEncaps();
            }
            catch(Ice.LocalException __ex)
            {
                throw new IceInternal.LocalExceptionWrapper(__ex, false);
            }
        }
        finally
        {
            __handler.reclaimOutgoing(__og);
        }
    }
}

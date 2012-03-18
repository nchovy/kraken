// **********************************************************************
//
// Copyright (c) 2003-2010 ZeroC, Inc. All rights reserved.
//
// This copy of Ice is licensed to you under the terms described in the
// ICE_LICENSE file included in this distribution.
//
// **********************************************************************

// Ice version 3.4.1

package IceBox;

public class SliceChecksums
{
    public static final java.util.Map<String, String> checksums;

    static
    {
        java.util.Map<String, String> map = new java.util.HashMap<String, String>();
        map.put("::IceBox::AlreadyStartedException", "d5b097af3221b37482d5f175502abf62");
        map.put("::IceBox::AlreadyStoppedException", "281d493a84d674b3a4335d6afc2c16");
        map.put("::IceBox::NoSuchServiceException", "5957f1c582d9aebad557cbdb7820d4");
        map.put("::IceBox::ServiceManager", "df3a42670c3ce4ef67d6125a5d04d4c");
        map.put("::IceBox::ServiceObserver", "f657781cda7438532a6c33e95988479c");
        checksums = java.util.Collections.unmodifiableMap(map);
    }
}

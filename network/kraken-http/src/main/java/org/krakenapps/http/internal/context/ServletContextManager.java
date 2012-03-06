/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.krakenapps.http.internal.context;

import org.osgi.framework.Bundle;
import org.osgi.service.http.HttpContext;
import javax.servlet.ServletContext;
import java.util.Map;
import java.util.HashMap;

public final class ServletContextManager
{
    private final Bundle bundle;
    private final ServletContext context;
    private final Map<HttpContext, KrakenServletContext> contextMap;

    public ServletContextManager(Bundle bundle, ServletContext context)
    {
        this.bundle = bundle;
        this.context = context;
        this.contextMap = new HashMap<HttpContext, KrakenServletContext>();
    }

    public KrakenServletContext getServletContext(HttpContext httpContext)
    {
        synchronized (this.contextMap) {
            KrakenServletContext context = this.contextMap.get(httpContext);
            if (context == null) {
                context = addServletContext(httpContext);
            }

            return context;
        }
    }

    private KrakenServletContext addServletContext(HttpContext httpContext)
    {
        KrakenServletContext context = new ServletContextImpl(this.bundle, this.context, httpContext);
        this.contextMap.put(httpContext, context);
        return context;
    }
}
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alipay.sofa.runtime.spi.service;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * @author xuanbei 18/2/28
 */
public abstract class ServiceProxy implements MethodInterceptor {
    private ClassLoader serviceClassLoader;

    public ServiceProxy(ClassLoader serviceClassLoader) {
        this.serviceClassLoader = serviceClassLoader;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        long startTime = System.currentTimeMillis();
        try {
            Thread.currentThread().setContextClassLoader(serviceClassLoader);
            return doInvoke(invocation);
        } catch (Throwable e) {
            do_catch(invocation, e, startTime);
            throw e;
        } finally {
            do_finally(invocation, startTime);
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
    }

    protected void pushThreadContextClassLoader(ClassLoader newContextClassLoader) {
        if (newContextClassLoader != null) {
            Thread.currentThread().setContextClassLoader(newContextClassLoader);
        }
    }

    protected void popThreadContextClassLoader(ClassLoader tcl) {
        Thread.currentThread().setContextClassLoader(tcl);
    }

    protected String getCommonInvocationLog(String start, MethodInvocation invocation,
                                            long startTime) {
        String appStart = "";

        if (appStart != null && appStart.length() > 0) {
            appStart = "-" + start;
        }

        long endTime = System.currentTimeMillis();

        StringBuffer sb = new StringBuffer("SOFA-Reference" + appStart + "(");

        sb.append(invocation.getMethod().getName());
        sb.append(",");
        for (Object o : invocation.getArguments()) {
            sb.append(o);
            sb.append(",");
        }
        sb.append((endTime - startTime) + "ms");
        sb.append(")");

        return sb.toString();
    }

    public ClassLoader getServiceClassLoader() {
        return serviceClassLoader;
    }

    protected abstract Object doInvoke(MethodInvocation invocation) throws Throwable;

    protected abstract void do_catch(MethodInvocation invocation, Throwable e, long startTime);

    protected abstract void do_finally(MethodInvocation invocation, long startTime);
}
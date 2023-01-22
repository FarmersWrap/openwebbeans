/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.webbeans.context.control;

import org.apache.webbeans.annotation.EmptyAnnotationLiteral;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.util.AnnotationUtil;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.enterprise.context.control.RequestContextController;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.InterceptionType;
import jakarta.enterprise.inject.spi.Interceptor;
import jakarta.interceptor.InvocationContext;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;

public class ActivateRequestContextInterceptorBean
        implements Interceptor<ActivateRequestContextInterceptorBean.InterceptorClass>, Serializable
{
    private static final Set<Annotation> BINDING = singleton(new ActivateRequestContextLiteral());
    private static final Set<Type> TYPES = singleton(Object.class);
    private static final InterceptorClass INSTANCE = new InterceptorClass();

    private final WebBeansContext webBeansContext;

    public ActivateRequestContextInterceptorBean(final WebBeansContext webBeansContext)
    {
        this.webBeansContext = webBeansContext;
    }

    @Override
    public Set<Annotation> getInterceptorBindings()
    {
        return BINDING;
    }

    @Override
    public boolean intercepts(final InterceptionType type)
    {
        return true;
    }

    @Override
    public Object intercept(final InterceptionType type, final InterceptorClass instance,
                            final InvocationContext ctx) throws Exception
    {
        final RequestContextController contextController = new OwbRequestContextController(webBeansContext);
        contextController.activate();
        try
        {
            return ctx.proceed();
        }
        finally
        {
            contextController.deactivate();
        }
    }

    @Override
    public InterceptorClass create(final CreationalContext<InterceptorClass> context)
    {
        return INSTANCE;
    }

    @Override
    public void destroy(final InterceptorClass instance, final CreationalContext<InterceptorClass> context)
    {
        // no-op
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints()
    {
        return emptySet();
    }

    @Override
    public Class<?> getBeanClass()
    {
        return InterceptorClass.class;
    }

    @Override
    public Set<Type> getTypes()
    {
        return TYPES;
    }

    @Override
    public Set<Annotation> getQualifiers()
    {
        return AnnotationUtil.DEFAULT_AND_ANY_ANNOTATION_SET;
    }

    @Override
    public Class<? extends Annotation> getScope()
    {
        return Dependent.class;
    }

    @Override
    public String getName()
    {
        return null;
    }

    @Override
    public Set<Class<? extends Annotation>> getStereotypes()
    {
        return emptySet();
    }

    @Override
    public boolean isAlternative()
    {
        return false;
    }

    public static class ActivateRequestContextLiteral
            extends EmptyAnnotationLiteral<ActivateRequestContext>
            implements ActivateRequestContext
    {
    }

    public static class InterceptorClass implements Serializable
    {
    }
}

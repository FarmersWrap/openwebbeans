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
package org.apache.webbeans.component;

import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.BeanAttributes;
import jakarta.enterprise.inject.spi.InterceptionType;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

import org.apache.webbeans.config.WebBeansContext;


/**
 * <p>{@link jakarta.enterprise.inject.spi.Interceptor}
 * Bean implementation for CDI-style Beans.
 * This is Interceptors which got defined using
 * &#064;{@link jakarta.interceptor.InterceptorBinding}.</p>
 */
public class CdiInterceptorBean<T> extends InterceptorBean<T>
{
    
    private Set<Annotation> interceptorBindings;

    public CdiInterceptorBean(WebBeansContext webBeansContext,
                              AnnotatedType<T> annotatedType,
                              BeanAttributes<T> beanAttributes,
                              Class<T> beanClass,
                              Set<Annotation> interceptorBindings,
                              boolean enabled,
                              Map<InterceptionType, Method[]> interceptionMethods)
    {
        super(webBeansContext, annotatedType, beanAttributes, beanClass, interceptionMethods);
        this.interceptorBindings = interceptorBindings;
        this.setEnabled(enabled);
    }

    @Override
    public Set<Annotation> getInterceptorBindings()
    {
        return interceptorBindings;
    }
}

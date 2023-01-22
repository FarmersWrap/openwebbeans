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
package org.apache.webbeans.event;

import org.apache.webbeans.component.AbstractOwbBean;
import org.apache.webbeans.exception.WebBeansConfigurationException;
import org.apache.webbeans.portable.events.AfterObserver;
import org.apache.webbeans.portable.events.discovery.ExtensionAware;

import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.AnnotatedParameter;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;
import jakarta.enterprise.inject.spi.WithAnnotations;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class ContainerEventObserverMethodImpl<T> extends ObserverMethodImpl<T>
{
    private final Class[] withAnnotations;

    public ContainerEventObserverMethodImpl(AbstractOwbBean<?> bean, AnnotatedMethod<T> annotatedObserverMethod,
                                            AnnotatedParameter<T> annotatedObservesParameter)
    {
        // we do NOT fire any further events while building this ObserverMethod, e.g for ProcessInjectionPoint...
        super(bean, annotatedObserverMethod, annotatedObservesParameter, false);

        WithAnnotations withAnnotationsAnn = annotatedObservesParameter.getAnnotation(WithAnnotations.class);
        if (withAnnotationsAnn != null)
        {
            Type baseType = annotatedObservesParameter.getBaseType();
            if (ParameterizedType.class.isInstance(baseType))
            {
                baseType = ParameterizedType.class.cast(baseType).getRawType();
            }
            if (baseType.equals(ProcessAnnotatedType.class))
            {
                withAnnotations = withAnnotationsAnn.value();
            }
            else
            {
                throw new WebBeansConfigurationException("WithAnnotations must only be used for ProcessAnnotatedType");
            }
        }
        else
        {
            withAnnotations = null;
        }
    }

    @Override
    protected void checkObserverCondition(AnnotatedParameter<T> annotatedObservesParameter)
    {
        // all fine
    }

    @Override
    protected void invoke(Object object, Object[] args) throws IllegalAccessException, InvocationTargetException
    {
        ExtensionAware extensionAware = null;
        if (args.length > 0)
        {
            if (ExtensionAware.class.isInstance(args[0]))
            {
                extensionAware = ExtensionAware.class.cast(args[0]);
                extensionAware.setExtension((Extension) object);
            }
        }
        super.invoke(object, args);
        if (extensionAware != null)
        {
            ExtensionAware.class.cast(extensionAware).setExtension(null);
        }
        if (args.length > 0 && AfterObserver.class.isInstance(args[0]))
        {
            AfterObserver.class.cast(args[0]).afterObserver();
        }
    }

    public Class[] getWithAnnotations()
    {
        return withAnnotations;
    }
}

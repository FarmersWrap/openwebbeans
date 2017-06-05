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
package org.apache.webbeans.configurator;

import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.portable.AnnotatedMethodImpl;
import org.apache.webbeans.portable.AnnotatedTypeImpl;

import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.configurator.AnnotatedConstructorConfigurator;
import javax.enterprise.inject.spi.configurator.AnnotatedFieldConfigurator;
import javax.enterprise.inject.spi.configurator.AnnotatedMethodConfigurator;
import javax.enterprise.inject.spi.configurator.AnnotatedTypeConfigurator;
import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class AnnotatedTypeConfiguratorImpl<T> implements AnnotatedTypeConfigurator<T>
{

    private final AnnotatedTypeImpl<T> annotatedType;
    private Set<AnnotatedMethodConfigurator<? super T>> annotatedMethodConfigurators;


    public AnnotatedTypeConfiguratorImpl(WebBeansContext webBeansContext, AnnotatedType<T> originalAnnotatedType)
    {
        this.annotatedType = new AnnotatedTypeImpl<>(webBeansContext, originalAnnotatedType);

        annotatedMethodConfigurators = annotatedType.getMethods().stream()
            .map(m -> new AnnotatedMethodConfiguratorImpl<>((AnnotatedMethodImpl<T>) m))
            .collect(Collectors.toSet());
    }


    @Override
    public AnnotatedType<T> getAnnotated()
    {
        return annotatedType;
    }

    @Override
    public AnnotatedTypeConfigurator<T> add(Annotation annotation)
    {
        annotatedType.addAnnotation(annotation);
        return this;
    }

    @Override
    public AnnotatedTypeConfigurator<T> remove(Predicate predicate)
    {
        annotatedType.getAnnotations().removeIf(predicate);
        return this;
    }

    @Override
    public AnnotatedTypeConfigurator<T> removeAll()
    {
        annotatedType.clearAnnotations();
        return this;
    }

    @Override
    public Set<AnnotatedMethodConfigurator<? super T>> methods()
    {
        return annotatedMethodConfigurators;
    }

    @Override
    public Set<AnnotatedFieldConfigurator<? super T>> fields()
    {
        throw new UnsupportedOperationException("TODO implement CDI 2.0");
    }

    @Override
    public Set<AnnotatedConstructorConfigurator<T>> constructors()
    {
        throw new UnsupportedOperationException("TODO implement CDI 2.0");
    }

}

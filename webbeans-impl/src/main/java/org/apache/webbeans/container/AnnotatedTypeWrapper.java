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
package org.apache.webbeans.container;

import jakarta.enterprise.inject.spi.AnnotatedConstructor;
import jakarta.enterprise.inject.spi.AnnotatedField;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.Extension;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

// totally useless but TCKs check AT references creating a new instance for each call...
public class AnnotatedTypeWrapper<T> implements AnnotatedType<T>
{
    private final AnnotatedType<T> original;
    private final Extension source;
    private final String id;

    public AnnotatedTypeWrapper(Extension source, AnnotatedType<T> annotatedType, String id)
    {
        this.source = source;
        this.original = annotatedType;
        this.id = id;
    }

    @Override
    public Class<T> getJavaClass()
    {
        return original.getJavaClass();
    }

    public String getId()
    {
        return id;
    }

    @Override
    public Set<AnnotatedConstructor<T>> getConstructors()
    {
        return original.getConstructors();
    }

    @Override
    public Set<AnnotatedMethod<? super T>> getMethods()
    {
        return original.getMethods();
    }

    @Override
    public Set<AnnotatedField<? super T>> getFields()
    {
        return original.getFields();
    }

    @Override
    public Type getBaseType()
    {
        return original.getBaseType();
    }

    @Override
    public Set<Type> getTypeClosure()
    {
        return original.getTypeClosure();
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> tClass)
    {
        return original.getAnnotation(tClass);
    }

    @Override
    public Set<Annotation> getAnnotations()
    {
        return original.getAnnotations();
    }

    @Override
    public boolean isAnnotationPresent(Class<? extends Annotation> aClass)
    {
        return original.isAnnotationPresent(aClass);
    }

    public Extension getSource()
    {
        return source;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null)
        {
            return false;
        }
        if (getClass() == o.getClass())
        {
            return false; // == should have worked
        }
        if (AnnotatedType.class.isInstance(o))
        {
            return original.equals(o);
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return original.hashCode();
    }
}

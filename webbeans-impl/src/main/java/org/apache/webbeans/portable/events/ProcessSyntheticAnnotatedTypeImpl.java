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
package org.apache.webbeans.portable.events;

import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessSyntheticAnnotatedType;
import org.apache.webbeans.config.WebBeansContext;


/**
 * Default implementation of the {@link jakarta.enterprise.inject.spi.ProcessSyntheticAnnotatedType}.
 *
 * @param <X> bean class info
 */
public class ProcessSyntheticAnnotatedTypeImpl<X> extends ProcessAnnotatedTypeImpl<X> implements ProcessSyntheticAnnotatedType<X>
{

    private Extension source;


    /**
     * This field gets set to <code>true</code> when a custom AnnotatedType
     * got set in an Extension. In this case we must now take this modified
     * AnnotatedType for our further processing!
     */
    private boolean modifiedAnnotatedType;


    public ProcessSyntheticAnnotatedTypeImpl(WebBeansContext webBeansContext, AnnotatedType<X> annotatedType, Extension source)
    {
        super(webBeansContext, annotatedType);

        this.source = source;
    }

    @Override
    public Extension getSource()
    {
        checkState();
        return source;
    }

    /**
     * Returns sets or not.
     *
     * @return set or not
     */
    public boolean isModifiedAnnotatedType()
    {
        return modifiedAnnotatedType;
    }

}

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
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.Producer;

import jakarta.enterprise.inject.spi.ProducerFactory;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.portable.ExtensionProducer;

/**
 * A factory for {@link jakarta.enterprise.inject.spi.Producer}s that produce CDI {@link jakarta.enterprise.inject.spi.Extension}s.
 * 
 * @version $Rev: 1440403 $ $Date: 2013-01-30 14:27:15 +0100 (Mi, 30 Jan 2013) $
 */
public class ExtensionProducerFactory<T> implements ProducerFactory<T>
{

    private AnnotatedType<?> annotatedType;
    private WebBeansContext webBeansContext;

    public ExtensionProducerFactory(AnnotatedType<T> annotatedType, WebBeansContext webBeansContext)
    {
        this.annotatedType = annotatedType;
        this.webBeansContext = webBeansContext;
    }

    @Override
    public <P> Producer<P> createProducer(Bean<P> bean)
    {
        return new ExtensionProducer<>((AnnotatedType<P>) annotatedType, bean, webBeansContext);
    }
}

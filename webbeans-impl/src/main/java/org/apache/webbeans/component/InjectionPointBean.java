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

import java.lang.reflect.Type;

import jakarta.enterprise.inject.spi.InjectionPoint;

import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.portable.InjectionPointProducer;
import org.apache.webbeans.util.CollectionUtil;

public class InjectionPointBean extends BuiltInOwbBean<InjectionPoint>
{
    private static final Class<?> PROXY_CLASS;
    static
    {
        try
        {
            PROXY_CLASS = BuiltInOwbBean.class.getClassLoader().loadClass("org.apache.webbeans.inject.impl.InjectionPointImpl");
        }
        catch (ClassNotFoundException e)
        {
            throw new IllegalStateException(e);
        }
    }
    public InjectionPointBean(WebBeansContext webBeansContext)
    {
        super(webBeansContext,
              WebBeansType.INJECTIONPOINT,
              new BeanAttributesImpl<>(CollectionUtil.<Type>unmodifiableSet(InjectionPoint.class, Object.class)),
              InjectionPoint.class,
              new SimpleProducerFactory<>(new InjectionPointProducer()));
    }

    /* (non-Javadoc)
     * @see org.apache.webbeans.component.AbstractOwbBean#isPassivationCapable()
     */
    @Override
    public boolean isPassivationCapable()
    {
        return true;
    }

    @Override
    public Class<?> proxyableType()
    {
        return PROXY_CLASS;
    }
}

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

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.util.TypeLiteral;
import jakarta.inject.Provider;

import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.inject.instance.InstanceImpl;
import org.apache.webbeans.portable.InstanceProducer;
import org.apache.webbeans.util.CollectionUtil;

public class InstanceBean<T> extends BuiltInOwbBean<Instance<T>>
{
    @SuppressWarnings("serial")
    public InstanceBean(WebBeansContext webBeansContext)
    {
        super(webBeansContext,
            WebBeansType.INSTANCE,
            new BeanAttributesImpl(CollectionUtil.unmodifiableSet(Instance.class, Provider.class, Object.class)),
            new TypeLiteral<Instance<T>>(){}.getRawType(),
            new SimpleProducerFactory(new InstanceProducer(webBeansContext)));
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
        return InstanceImpl.class;
    }
}

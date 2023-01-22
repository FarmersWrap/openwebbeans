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
package org.apache.webbeans.component.third;

import java.util.Set;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.Producer;

import org.apache.webbeans.component.AbstractOwbBean;
import org.apache.webbeans.component.WebBeansType;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.context.creational.CreationalContextImpl;

public class ThirdpartyBeanImpl<T> extends AbstractOwbBean<T>
{
    private Bean<T> bean;

    public ThirdpartyBeanImpl(WebBeansContext webBeansContext, Bean<T> bean)
    {
        super(webBeansContext,
              WebBeansType.THIRDPARTY,
              new ThirdpartyBeanAttributesImpl<>(bean),
              bean.getBeanClass());
        
        this.bean = bean;
    }

    @Override
    public Producer<T> getProducer()
    {
        return new Producer<T>()
        {
            @Override
            public T produce(CreationalContext<T> creationalContext)
            {
                return bean.create(creationalContext);
            }

            @Override
            public void dispose(T instance)
            {
                bean.destroy(instance, null);
            }

            @Override
            public Set<InjectionPoint> getInjectionPoints()
            {
                return bean.getInjectionPoints();
            }
        };
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints()
    {
        return bean.getInjectionPoints();
    }

    @Override
    public T create(CreationalContext<T> context)
    {
        CreationalContextImpl<T> contextImpl;
        if(!CreationalContextImpl.class.isInstance(context))
        {
            contextImpl = webBeansContext.getCreationalContextFactory().wrappedCreationalContext(context, this);
        }
        else
        {
            contextImpl = CreationalContextImpl.class.cast(context);
        }

        T t;
        Bean<T> oldBean = contextImpl.putBean(this);
        try
        {
            t = bean.create(contextImpl);
        }
        finally
        {
            contextImpl.putBean(oldBean);
        }
        if (getScope().equals(Dependent.class))
        {
            contextImpl.addDependent(this, t);
        }
        return t;
    }

    @Override
    public void destroy(T instance, CreationalContext<T> context)
    {
        bean.destroy(instance,context);
        
    }

    /* (non-Javadoc)
     * @see org.apache.webbeans.component.AbstractBean#getId()
     */
    @Override
    public String getId()
    {
        return null;
    }

    @Override
    public boolean isPassivationCapable()
    {
        return false;
    }

    @Override
    public Class<?> getBeanClass()
    {
        return bean.getBeanClass();
    }

    @Override
    public boolean isAlternative()
    {
        return bean.isAlternative();
    }

    /**
     * We need to override the hash code from the AbstractOwbBean
     * and delegate to the shaded instance.
     *
     * @return the hash mixed with the shadowed bean.
     */
    @Override
    public int hashCode()
    {
        return 29 * bean.hashCode();
    }

    @Override
    public boolean equals(Object other)
    {
        if (other instanceof ThirdpartyBeanImpl)
        {
            return ((ThirdpartyBeanImpl) other).bean.equals(bean);
        }

        return bean.equals(other);
    }
}

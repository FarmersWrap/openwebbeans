/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.webbeans.test.promethods;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.InjectionTarget;

import org.junit.Assert;

import org.apache.webbeans.component.ManagedBean;
import org.apache.webbeans.test.AbstractUnitTest;
import org.apache.webbeans.test.promethods.beans.InjectorofMethod1Bean;
import org.apache.webbeans.test.promethods.beans.MethodTypeProduces1;
import org.apache.webbeans.test.promethods.beans.ProducerBean;
import org.junit.Test;

public class MethodProducer1Test extends AbstractUnitTest
{
    public MethodProducer1Test()
    {
        
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testPersonProducer()
    {
        Collection<String> beanXmls = new ArrayList<String>();
        
        Collection<Class<?>> beanClasses = new ArrayList<Class<?>>();
        beanClasses.add(MethodTypeProduces1.class);
        beanClasses.add(InjectorofMethod1Bean.class);
        
        startContainer(beanClasses, beanXmls);      
        
        Set<Bean<?>> beans = getBeanManager().getBeans("injectorofMethod1Bean");
        Assert.assertNotNull(beans);        
        Bean<InjectorofMethod1Bean> bean = (Bean<InjectorofMethod1Bean>)beans.iterator().next();
        
        Assert.assertTrue(bean instanceof ManagedBean);
        
        CreationalContext<InjectorofMethod1Bean> ctx = getBeanManager().createCreationalContext(bean);
        
        Object reference = getBeanManager().getReference(bean, InjectorofMethod1Bean.class, ctx);
        Assert.assertNotNull(reference);
        
        Assert.assertTrue(reference instanceof InjectorofMethod1Bean);


        ArrayList<String> value = ((InjectorofMethod1Bean) reference).getPt3();
        Assert.assertNotNull(value);

        
        shutDownContainer();
    }
    
    @Test
    @SuppressWarnings("unchecked")
    /**
     * Tests the code path of adding a bean containing producer methods through 
     * WebBeansAnnotatedTypeUtil.defineManagedBean
     */
    public void testProducerAddedByWebBeansAnnotatedTypeUtil()
    {
        Collection<String> beanXmls = new ArrayList<String>();
        
        Collection<Class<?>> beanClasses = new ArrayList<Class<?>>();
        
        startContainer(beanClasses, beanXmls);  
        
        AnnotatedType<ProducerBean> at = getBeanManager().createAnnotatedType(ProducerBean.class);
        InjectionTarget<ProducerBean> it = getBeanManager().getInjectionTargetFactory(at).createInjectionTarget(null);
        
        Assert.assertNotNull(it);
        
        shutDownContainer();
    }
}

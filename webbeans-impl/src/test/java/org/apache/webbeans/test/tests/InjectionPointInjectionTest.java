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
package org.apache.webbeans.test.tests;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Collection;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.AnnotatedField;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.InjectionTarget;
import jakarta.inject.Inject;

import org.apache.webbeans.annotation.DefaultLiteral;
import org.apache.webbeans.test.AbstractUnitTest;
import org.apache.webbeans.test.injection.injectionpoint.beans.AbstractInjectionPointOwner;
import org.apache.webbeans.test.injection.injectionpoint.beans.ConstructorInjectionPointOwner;
import org.apache.webbeans.test.injection.injectionpoint.beans.FieldInjectionPointOwner;
import org.apache.webbeans.test.injection.injectionpoint.beans.InjectionPointBeansOwner;
import org.apache.webbeans.test.injection.injectionpoint.beans.InjectionPointObserver;
import org.apache.webbeans.test.injection.injectionpoint.beans.InjectionPointOwnerInstance;
import org.apache.webbeans.test.injection.injectionpoint.beans.InjectionPointOwnerProducer;
import org.apache.webbeans.test.injection.injectionpoint.beans.MethodInjectionPointOwner;
import org.apache.webbeans.test.injection.injectionpoint.beans.ProducerInjectionPointInstanceOwner;
import org.apache.webbeans.test.injection.injectionpoint.beans.ProducerMethodInjectionPointOwner;
import org.apache.webbeans.test.injection.injectionpoint.beans.UnmanagedClassWithInjectionPoints;
import org.apache.webbeans.test.util.Serializations;
import org.junit.Assert;
import org.junit.Test;

public class InjectionPointInjectionTest extends AbstractUnitTest {

    @Test
    public void testInjectionPointInjection()
    {
        Collection<Class<?>> beanClasses = new ArrayList<Class<?>>();
        beanClasses.add(ConstructorInjectionPointOwner.class);
        beanClasses.add(FieldInjectionPointOwner.class);
        beanClasses.add(MethodInjectionPointOwner.class);
        beanClasses.add(InjectionPointObserver.class);
        beanClasses.add(InjectionPointBeansOwner.class);
        beanClasses.add(InjectionPointOwnerProducer.class);
        beanClasses.add(ProducerMethodInjectionPointOwner.class);
        beanClasses.add(ConstructorInjectionPointOwner.SomeInnerClassWithInstructorInjectionPoint.class);
        startContainer(beanClasses, null);

        Bean<InjectionPointBeansOwner> bean = (Bean<InjectionPointBeansOwner>) getBeanManager().getBeans(InjectionPointBeansOwner.class).iterator().next();
        CreationalContext<InjectionPointBeansOwner> cc = getBeanManager().createCreationalContext(bean);
        InjectionPointBeansOwner owner = (InjectionPointBeansOwner) getBeanManager().getReference(bean, InjectionPointBeansOwner.class, cc);

        ConstructorInjectionPointOwner.SomeInnerClassWithInstructorInjectionPoint innerClass
                = getInstance(ConstructorInjectionPointOwner.SomeInnerClassWithInstructorInjectionPoint.class);
        assertThat(innerClass, is(notNullValue()));

        assertThat(owner.getConstructorInjectionName(), is("constructorInjection"));
        assertThat(owner.getFieldInjectionName(), is("fieldInjection"));
        assertThat(owner.getMethodInjectionName(), is("methodInjection"));
        assertThat(owner.getProducerMethodInjectionName(), is("producerMethodInjection"));
        assertThat(owner.getConstructorInjectionInstanceName(), is("constructorInjectionInstance"));
        assertThat(owner.getFieldInjectionInstanceName(), is("fieldInjectionInstance"));
        assertThat(owner.getMethodInjectionInstanceName(), is("methodInjectionInstance"));
        assertThat(owner.getProducerMethodInjectionInstanceName(), is("producerMethodInjectionInstance"));
        assertThat(owner.getObserverInjectionName(), is("observerInjection"));
        assertThat(owner.getParameterizedObserverInjectionName(), is("observeParameterizedInjectionPoint"));

        shutDownContainer();
    }

    @Test
    public void testPackagePrivateInjectionPoint()
    {
        startContainer(PackageMethodInjectionPointOwner.class, PackageInjectionPointOwner.class);

        PackageInjectionPointOwner pipo = getInstance(PackageInjectionPointOwner.class);
        assertThat(pipo, is(notNullValue()));
        assertThat(pipo.getName(), is("pimp"));
    }

    @Test
    public void testDynamicResolvingInjectionPoint()
    {
        startContainer(InjectionPointOwnerProducer.class, ProducerMethodInjectionPointOwner.class, AbstractInjectionPointOwner.class);

        ProducerMethodInjectionPointOwner producedInstance = getInstance(ProducerMethodInjectionPointOwner.class);
        assertThat(producedInstance, notNullValue());
        InjectionPoint ip = producedInstance.getInjectionPoint();
        assertThat(ip, nullValue());
    }


    @Test
    public void testDynamicResolvingInstanceInjectionPoint()
    {
        startContainer(InjectionPointOwnerInstance.class, InjectionPointOwnerProducer.class,
                       ProducerMethodInjectionPointOwner.class, AbstractInjectionPointOwner.class,
                       MethodInjectionPointOwner.class, ProducerInjectionPointInstanceOwner.class);

        InjectionPointOwnerInstance producedInstanceOwner = getInstance(InjectionPointOwnerInstance.class);
        assertThat(producedInstanceOwner, notNullValue());

        MethodInjectionPointOwner ipOwner = producedInstanceOwner.getIpOwnerInstance().select(new DefaultLiteral()).get();
        assertThat(ipOwner, notNullValue());

        InjectionPoint ip = ipOwner.getInjectionPoint();
        assertNotNull(ip);
        assertNotNull(ip.getAnnotated());
        assertThat(ip.getAnnotated(), instanceOf(AnnotatedField.class));


        ProducerInjectionPointInstanceOwner pipo = getInstance(ProducerInjectionPointInstanceOwner.class);
        assertNotNull(pipo);
        Instance<ProducerMethodInjectionPointOwner> ipOwnerInstance = pipo.getIpOwnerInstance();
        assertNotNull(ipOwnerInstance);
        ProducerMethodInjectionPointOwner pmp = ipOwnerInstance.select(DefaultLiteral.INSTANCE).get();
        assertNotNull(pmp);
        assertNotNull(pmp.getInjectionPoint());
    }

    @Test
    public void testManualConstructorInjectionPoint() throws Exception
    {
        startContainer(ConstructorInjectionPointOwner.class);
        ConstructorInjectionPointOwner owner = getInstance(ConstructorInjectionPointOwner.class);
        Assert.assertNotNull(owner);
        Assert.assertNull(owner.getInjectionPoint());
    }

    @Test
    public void testIndirectConstructorInjectionPoint() throws Exception
    {
        startContainer(ConstructorInjectionPointOwner.class, ConstructorInjectionOwner.class);
        ConstructorInjectionOwner owner = getInstance(ConstructorInjectionOwner.class);
        Assert.assertNotNull(owner);
        Assert.assertNotNull(owner.getConstructorInjectionPointOwner());
        Assert.assertNotNull(owner.getConstructorInjectionPointOwner().getInjectionPoint());
        Assert.assertNotNull(owner.getConstructorInjectionPointOwner().getName());
    }

    @Test
    public void testManualCreateInjectionTargetWithInjectionPointsSerialisation() throws Exception
    {
        startContainer(FieldInjectionPointOwner.class);
        AnnotatedType<UnmanagedClassWithInjectionPoints> at = getBeanManager().createAnnotatedType(UnmanagedClassWithInjectionPoints.class);
        InjectionTarget<UnmanagedClassWithInjectionPoints> injectionTarget
            = getBeanManager().getInjectionTargetFactory(at).createInjectionTarget(null);

        UnmanagedClassWithInjectionPoints instance = new UnmanagedClassWithInjectionPoints();
        Assert.assertNull(instance.getSomeField());

        CreationalContext cc = getBeanManager().createCreationalContext(null);
        injectionTarget.inject(instance, cc);

        Assert.assertNotNull(instance.getSomeField());
        Assert.assertNotNull(instance.getSomeField().getInjectionPoint());

        byte[] bytes = Serializations.serialize(instance);
        Assert.assertNotNull(bytes);
        UnmanagedClassWithInjectionPoints instance2 = (UnmanagedClassWithInjectionPoints) Serializations.deserialize(bytes);
        Assert.assertNotNull(instance2);
    }

    @Dependent
    public static class ConstructorInjectionOwner
    {
        private @Inject ConstructorInjectionPointOwner constructorInjectionPointOwner;

        public ConstructorInjectionPointOwner getConstructorInjectionPointOwner()
        {
            return constructorInjectionPointOwner;
        }
    }

}

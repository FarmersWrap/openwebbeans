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
package org.apache.webbeans.test.unittests.inject;

import java.util.Set;

import jakarta.enterprise.inject.spi.Bean;

import org.junit.Assert;

import org.apache.webbeans.annotation.DefaultLiteral;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.container.InjectionResolver;
import org.apache.webbeans.test.AbstractUnitTest;
import org.apache.webbeans.test.component.service.ITyped;
import org.apache.webbeans.test.component.service.TypedComponent;
import org.junit.Test;

public class TypedComponentTest extends AbstractUnitTest
{
    private ITyped<String> s = null;

    @Test
    public void testTypedComponent() throws Throwable
    {
        startContainer(TypedComponent.class);

        InjectionResolver injectionResolver = WebBeansContext.getInstance().getBeanManagerImpl().getInjectionResolver();

        Set<Bean<?>> beans
                = injectionResolver.implResolveByType(false, TypedComponentTest.class.getDeclaredField("s").getGenericType(),
                                                      DefaultLiteral.INSTANCE);

        Assert.assertTrue(beans.size() == 1);
    }

}

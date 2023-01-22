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
package org.apache.webbeans.el22;

import jakarta.el.ELResolver;
import jakarta.el.ExpressionFactory;

import org.apache.webbeans.spi.adaptor.ELAdaptor;

/**
 * OWB {@link ELAdaptor} implementation for EL22
 */
public class EL22Adaptor implements ELAdaptor
{
    /**
     * Default constructor.
     */
    public EL22Adaptor()
    {
        
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ELResolver getOwbELResolver()
    {        
        return new WebBeansELResolver();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExpressionFactory getOwbWrappedExpressionFactory(ExpressionFactory expressionFactroy)
    {
        return new WrappedExpressionFactory(expressionFactroy);
    }

}

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
package org.apache.webbeans.component.creation;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.BeanAttributes;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.inject.Inject;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.webbeans.component.DecoratorBean;
import org.apache.webbeans.component.WebBeansType;
import org.apache.webbeans.config.OWBLogConst;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.exception.WebBeansConfigurationException;
import org.apache.webbeans.logger.WebBeansLoggerFacade;
import org.apache.webbeans.util.Asserts;
import org.apache.webbeans.util.ClassUtil;
import org.apache.webbeans.util.GenericsUtil;

/**
 * Bean builder for {@link org.apache.webbeans.component.InterceptorBean}s.
 */
public class DecoratorBeanBuilder<T> extends AbstractBeanBuilder
{
    private static Logger logger = WebBeansLoggerFacade.getLogger(DecoratorBeanBuilder.class);

    protected final WebBeansContext webBeansContext;
    protected final AnnotatedType<T> annotatedType;
    protected final BeanAttributes<T> beanAttributes;

    /**
     * The Types the decorator itself implements
     */
    private Set<Type> decoratedTypes;

    /**
     * The Type of the &#064;Delegate injection point.
     */
    private Type delegateType;

    /**
     * The Qualifiers of the &#064;Delegate injection point.
     */
    private Set<Annotation> delegateQualifiers;

    private final Set<String> ignoredDecoratorInterfaces;

    public DecoratorBeanBuilder(WebBeansContext webBeansContext, AnnotatedType<T> annotatedType, BeanAttributes<T> beanAttributes)
    {
        Asserts.assertNotNull(webBeansContext, Asserts.PARAM_NAME_WEBBEANSCONTEXT);
        Asserts.assertNotNull(annotatedType, "annotated type");
        Asserts.assertNotNull(beanAttributes, "beanAttributes");
        this.webBeansContext = webBeansContext;
        this.annotatedType = annotatedType;
        this.beanAttributes = beanAttributes;
        decoratedTypes = new HashSet<>(beanAttributes.getTypes());
        ignoredDecoratorInterfaces = getIgnoredDecoratorInterfaces();
    }

    private Set<String> getIgnoredDecoratorInterfaces()
    {
        return webBeansContext.getOpenWebBeansConfiguration().getIgnoredInterfaces();
    }

    /**
     * If this method returns <code>false</code> the {@link #getBean()} method must not get called.
     *
     * @return <code>true</code> if the Decorator is enabled and a Bean should get created
     */
    public boolean isDecoratorEnabled()
    {
        return webBeansContext.getDecoratorsManager().isDecoratorEnabled(annotatedType.getJavaClass());
    }

    protected void checkDecoratorConditions()
    {
        if (beanAttributes.getScope() != Dependent.class)
        {
            logger.log(Level.WARNING, OWBLogConst.WARN_0005_1, annotatedType.getJavaClass().getName());
        }

        if (beanAttributes.getName() != null)
        {
            logger.log(Level.WARNING, OWBLogConst.WARN_0005_2, annotatedType.getJavaClass().getName());
        }

        if (annotatedType.isAnnotationPresent(Alternative.class))
        {
            logger.log(Level.WARNING, OWBLogConst.WARN_0005_3, annotatedType.getJavaClass().getName());
        }


        if (logger.isLoggable(Level.FINE))
        {
            logger.log(Level.FINE, "Configuring decorator class : [{0}]", annotatedType.getJavaClass());
        }

        // make sure that CDI Decorators do not have any Producer methods or a method with @Observes
        validateNoProducerOrObserverMethod(annotatedType);

        // make sure that CDI Decorator do not have a Disposes method
        validateNoDisposerWithoutProducer(
                webBeansContext.getAnnotatedElementFactory().getFilteredAnnotatedMethods(annotatedType),
                Collections.emptySet(), Collections.emptySet(), Collections.emptySet());

    }

    public void defineDecoratorRules()
    {
        checkDecoratorConditions();

        defineDecoratedTypes();
    }

    private void defineDecoratedTypes()
    {
        decoratedTypes.remove(Serializable.class); /* 8.1 */
        decoratedTypes.removeIf(t -> !ClassUtil.getClass(t).isInterface() || (t instanceof Class<?> && ignoredDecoratorInterfaces.contains(((Class) t).getName())));
    }

    private void defineDelegate(Set<InjectionPoint> injectionPoints)
    {
        boolean found = false;
        InjectionPoint ipFound = null;
        for (InjectionPoint ip : injectionPoints)
        {
            if (ip.isDelegate())
            {
                if (!found)
                {
                    found = true;
                    ipFound = ip;
                }
                else
                {
                    throw new WebBeansConfigurationException("Decorators must have a one @Delegate injection point. " +
                            "But the decorator bean : " + toString() + " has more than one");
                }
            }
        }


        if(ipFound == null)
        {
            throw new WebBeansConfigurationException("Decorators must have a one @Delegate injection point." +
                        "But the decorator bean : " + toString() + " has none");
        }

        if(!(ipFound.getMember() instanceof Constructor))
        {
            AnnotatedElement element = (AnnotatedElement)ipFound.getMember();
            if(!element.isAnnotationPresent(Inject.class))
            {
                String message = "Error in decorator : " + annotatedType + ". The delegate injection point must be an injected field, " +
                        "initializer method parameter or bean constructor method parameter.";

                throw new WebBeansConfigurationException(message);
            }
        }

        delegateType = GenericsUtil.resolveType(ipFound.getType(), annotatedType.getJavaClass(), ipFound.getMember());
        delegateQualifiers = ipFound.getQualifiers();

        for (Type decType : decoratedTypes)
        {
            if (!(ClassUtil.getClass(decType)).isAssignableFrom(ClassUtil.getClass(delegateType)))
            {
                throw new WebBeansConfigurationException("Decorator : " + toString() + " delegate attribute must implement all of the decorator decorated types" +
                        ", but decorator type " + decType + " is not assignable from delegate type of " + delegateType);
            }
            else
            {
                if(ClassUtil.isParametrizedType(decType) && ClassUtil.isParametrizedType(delegateType))
                {
                    checkParametrizedType();
                }
                else if (ClassUtil.isTypeVariable(decType))
                {
                    if (!decType.equals(delegateType))
                    {
                        throw new WebBeansConfigurationException("Decorator : " + toString() + " generic delegate attribute must be same with decorated type : " + decType);
                    }
                }

            }
        }
    }

    /**
     * Checks recursive, if the ParameterizedTypes are equal
     */
    private void checkParametrizedType()
    {
        Type[] delegeteTypes = ((ParameterizedType) delegateType).getActualTypeArguments();
        Type[] interfaceTypes = annotatedType.getJavaClass().getGenericInterfaces();

        for (Type interfaceType : interfaceTypes)
        {
            if (!ClassUtil.isClassAssignableFrom(ClassUtil.getClass(delegateType), ClassUtil.getClass(interfaceType)))
            {
                // only check the interface from the decorated type
                continue;
            }

            Type[] arguments = ClassUtil.getActualTypeArguments(interfaceType);
            if (arguments.length != delegeteTypes.length)
            {
                throw new WebBeansConfigurationException("Decorator: " + toString() + " Number of TypeArguments must match - Decorated Type:  " + arguments.length +
                                              " Delegate Type: " + delegeteTypes.length);
            }

            for (int i = 0; i < delegeteTypes.length; i++)
            {
                if (!delegeteTypes[i].equals(arguments[i]))
                {
                    throw new WebBeansConfigurationException("Decorator: " + toString() + " delegate attribute must match decorated type: " + delegeteTypes[i]);
                }
            }
        }
    }

    public DecoratorBean<T> getBean()
    {
        DecoratorBean<T> decorator = new DecoratorBean<>(webBeansContext, WebBeansType.MANAGED, annotatedType, beanAttributes, annotatedType.getJavaClass());
        decorator.setEnabled(webBeansContext.getDecoratorsManager().isDecoratorEnabled(annotatedType.getJavaClass()));

        // we can only do this after the bean injection points got scanned
        defineDelegate(decorator.getInjectionPoints());
        decorator.setDecoratorInfo(decoratedTypes, delegateType, delegateQualifiers);

        return decorator;
    }

    protected List<AnnotatedMethod<?>> getPostConstructMethods()
    {
        List<AnnotatedMethod<?>> postConstructMethods = new ArrayList<>();
        collectPostConstructMethods(annotatedType.getJavaClass(), postConstructMethods);
        return postConstructMethods;
    }

    private void collectPostConstructMethods(Class<?> type, List<AnnotatedMethod<?>> postConstructMethods)
    {
        if (type == null)
        {
            return;
        }
        collectPostConstructMethods(type.getSuperclass(), postConstructMethods);
        for (AnnotatedMethod<?> annotatedMethod: webBeansContext.getAnnotatedElementFactory().getFilteredAnnotatedMethods(annotatedType))
        {
            if (annotatedMethod.getJavaMember().getDeclaringClass() == type
                && annotatedMethod.isAnnotationPresent(PostConstruct.class)
                && annotatedMethod.getParameters().isEmpty())
            {
                postConstructMethods.add(annotatedMethod);
            }
        }
    }

    protected List<AnnotatedMethod<?>> getPreDestroyMethods()
    {
        List<AnnotatedMethod<?>> preDestroyMethods = new ArrayList<>();
        collectPreDestroyMethods(annotatedType.getJavaClass(), preDestroyMethods);
        return preDestroyMethods;
    }

    private void collectPreDestroyMethods(Class<?> type, List<AnnotatedMethod<?>> preDestroyMethods)
    {
        if (type == null)
        {
            return;
        }
        collectPreDestroyMethods(type.getSuperclass(), preDestroyMethods);
        for (AnnotatedMethod<?> annotatedMethod: webBeansContext.getAnnotatedElementFactory().getFilteredAnnotatedMethods(annotatedType))
        {
            if (annotatedMethod.getJavaMember().getDeclaringClass() == type
                && annotatedMethod.isAnnotationPresent(PreDestroy.class)
                && annotatedMethod.getParameters().isEmpty())
            {
                preDestroyMethods.add(annotatedMethod);
            }
        }
    }
}

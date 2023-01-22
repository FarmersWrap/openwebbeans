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
package org.apache.webbeans.inject;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.annotation.Priority;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.Prioritized;

import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.exception.WebBeansConfigurationException;
import org.apache.webbeans.util.AnnotationUtil;
import org.apache.webbeans.util.PriorityClasses;

/**
 * This class has 2 responsibilities.
 *
 * 1.) to collect information about configured &#064;Alternatives at boot time
 *
 * 2.) answer if a class is an enabled Alternative.
 * This is needed for {@link org.apache.webbeans.container.BeanManagerImpl#resolve(java.util.Set)}
 *
 * The boot order for 1.) is to first register all the alternatives and stereotypes
 * from the XML.
 * After that the AnnotatedType scanning is performed and all &#064;Alternatives with
 * &#064;Priority get added as well. We will also add classes which have an Alternative stereotype.
 *
 * After the AnnotatedTypes got scanned we have to fire the {@link jakarta.enterprise.inject.spi.AfterTypeDiscovery}
 * event with the collected <i>effective</i> alternative classes sorted by their priority.
 * Any extension can re-order the alternatives which then form the base of the resolve() handling
 * at runtime.
 */
public class AlternativesManager
{

    private final WebBeansContext webBeansContext;

    /**
     * All the stereotypes which are configured via XML &lt;class&gt;
     */
    private final Set<Class<?>> configuredAlternatives = new HashSet<>();

    /**
     * All Stereotypes which are declared as &#064;Alternative in a beans.xml.
     * Please note that &#064;Priority on a stereotype does <b>not</b> automatically enable it!
     */
    private final Set<Class<? extends Annotation>> configuredStereotypeAlternatives = new HashSet<>();

    /**
     * Contains all Alternative Stereotypes which are NOT enabled via beans.xml
     * We need those for classes which  have a @Priority.
     */
    private final Map<Class<? extends Annotation>, Boolean> notEnabledStereotypeAlternatives = new HashMap<>();


    private final PriorityClasses priorityAlternatives = new PriorityClasses();

    public AlternativesManager(WebBeansContext webBeansContext)
    {
        this.webBeansContext = webBeansContext;
    }

    /**
     * This methods gets called while scanning the various beans.xml files.
     * It registers a &lt;stereotype&gt; alternative.
     */
    public void addXmlStereoTypeAlternative(Class<?> alternative)
    {                
        if(Annotation.class.isAssignableFrom(alternative))
        {
            Class<? extends Annotation> stereo = (Class<? extends Annotation>)alternative;
            boolean ok = false;
            if(webBeansContext.getAnnotationManager().isStereoTypeAnnotation(stereo))
            {
                if(AnnotationUtil.hasClassAnnotation(stereo, Alternative.class))
                {
                    ok = true;

                    configuredStereotypeAlternatives.add(stereo);
                }
            }
            
            if(!ok)
            {
                throw new WebBeansConfigurationException("Given stereotype class : " + alternative.getName() + " is not an annotation" );
            }
        }
        else
        {
            throw new WebBeansConfigurationException("Given stereotype class : " + alternative.getName() + " is not an annotation" );
        }        
    }

    /**
     * This methods gets called while scanning the various beans.xml files.
     * It registers a &lt;class&gt; alternative.
     */
    public void addXmlClazzAlternative(Class<?> alternative)
    {
        configuredAlternatives.add(alternative);
    }

    /**
     * This method is used to add Alternatives which have a &#064;Priority annotation.
     * This is performed after the ProcessAnnotatedType events got fired.
     */
    public void addPriorityClazzAlternative(Class<?> clazz, Priority priority)
    {
        priorityAlternatives.add(clazz, priority);
    }

    /**
     * This method is used to add Alternative Beans which implement the {@link Prioritized} interface
     * This is performed after the Bean get's added.
     */
    public void addPrioritizedAlternativeBean(Bean<?> prioritizedBean)
    {
        if (prioritizedBean instanceof Prioritized)
        {
            priorityAlternatives.add(prioritizedBean.getBeanClass(), ((Prioritized) prioritizedBean).getPriority());
        }
        else
        {
            throw new WebBeansConfigurationException("Given Bean : " + prioritizedBean + " doesn't implement " +
                        Prioritized.class.getName());
        }
    }

    /**
     * Alternatives get ordered by their priority and as lowest priority all
     * the alternatives added via XML get added.
     * @return the list of sorted alternatives
     */
    public List<Class<?>> getPrioritizedAlternatives()
    {
        return priorityAlternatives.getSorted();
    }


    /**
     * @return <code>true</code> if the given bean is a configured alternative
     */
    public boolean isAlternative(Bean<?> bean)
    {
        return isAlternative(bean.getBeanClass(), bean.getStereotypes());
    }

    /**
     * @return <code>true</code> if the given bean is a configured alternative
     */
    public boolean isAlternative(Class<?> beanType, Set<Class<? extends Annotation>> stereotypes)
    {
        if(configuredAlternatives.contains(beanType) ||
            priorityAlternatives.contains(beanType))
        {
            return true;
        }

        if (stereotypes != null && !stereotypes.isEmpty())
        {
            for (Class<? extends Annotation> ann : stereotypes)
            {
                if (configuredStereotypeAlternatives.contains(ann))
                {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * @return all the alternative classes declared in beans.xml files
     */
    public Set<Class<?>> getXmlConfiguredAlternatives()
    {
        return configuredAlternatives;
    }

    public boolean isAlternativeStereotype(Class<? extends Annotation> stereo)
    {
        Boolean isAlternative = notEnabledStereotypeAlternatives.get(stereo);
        if (isAlternative == null)
        {
            isAlternative = AnnotationUtil.hasClassAnnotation(stereo, Alternative.class);
            notEnabledStereotypeAlternatives.putIfAbsent(stereo, isAlternative);
        }

        return isAlternative;
    }

    public void clear()
    {
        configuredAlternatives.clear();
        configuredStereotypeAlternatives.clear();
        priorityAlternatives.clear();
        notEnabledStereotypeAlternatives.clear();
    }

}

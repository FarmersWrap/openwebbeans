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
package org.apache.webbeans.corespi.se;

import java.lang.annotation.Annotation;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.BusyConversationException;
import jakarta.enterprise.context.ContextException;
import jakarta.enterprise.context.ConversationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.NonexistentConversationException;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.context.spi.Context;
import jakarta.inject.Singleton;

import org.apache.webbeans.annotation.BeforeDestroyedLiteral;
import org.apache.webbeans.annotation.DestroyedLiteral;
import org.apache.webbeans.annotation.InitializedLiteral;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.context.AbstractContextsService;
import org.apache.webbeans.context.ApplicationContext;
import org.apache.webbeans.context.ConversationContext;
import org.apache.webbeans.context.DependentContext;
import org.apache.webbeans.context.RequestContext;
import org.apache.webbeans.context.SessionContext;
import org.apache.webbeans.conversation.ConversationImpl;
import org.apache.webbeans.conversation.ConversationManager;
import org.apache.webbeans.intercept.RequestScopedBeanInterceptorHandler;
import org.apache.webbeans.intercept.SessionScopedBeanInterceptorHandler;


public abstract class BaseSeContextsService extends AbstractContextsService
{
    private static ThreadLocal<RequestContext> requestContext;

    private static ThreadLocal<SessionContext> sessionContext;

    private static ThreadLocal<ConversationContext> conversationContext;

    private static ThreadLocal<DependentContext> dependentContext;

    private ApplicationContext applicationContext;

    static
    {
        requestContext = new ThreadLocal<>();
        sessionContext = new ThreadLocal<>();
        conversationContext = new ThreadLocal<>();
        dependentContext = new ThreadLocal<>();
    }

    protected BaseSeContextsService(final WebBeansContext webBeansContext)
    {
        super(webBeansContext);
    }

    protected abstract void destroySingletonContext();

    protected abstract Context getCurrentSingletonContext();

    protected abstract void createSingletonContext();

    /**
     * {@inheritDoc}
     */
    @Override
    public void endContext(Class<? extends Annotation> scopeType, Object endParameters)
    {
        
        if(scopeType.equals(RequestScoped.class))
        {
            stopRequestContext();
        }
        else if(scopeType.equals(SessionScoped.class))
        {
            stopSessionContext();
        }
        else if(scopeType.equals(ApplicationScoped.class))
        {
            stopApplicationContext();
        }
        else if(scopeType.equals(ConversationScoped.class))
        {
            stopConversationContext();
        }
        else if(scopeType.equals(Singleton.class))
        {
            stopSingletonContext();
        }

        // do nothing for Dependent.class

    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Context getCurrentContext(Class<? extends Annotation> scopeType)
    {
        if(scopeType.equals(RequestScoped.class))
        {
            return getCurrentRequestContext();
        }
        else if(scopeType.equals(SessionScoped.class))
        {
            return getCurrentSessionContext();
        }
        else if(scopeType.equals(ApplicationScoped.class))
        {
            return applicationContext;
        }
        else if(scopeType.equals(ConversationScoped.class) && supportsConversation)
        {
            return getCurrentConversationContext();
        }
        else if(scopeType.equals(Dependent.class))
        {
            return getCurrentDependentContext();
        }
        else if(scopeType.equals(Singleton.class))
        {
            return getCurrentSingletonContext();
        }

        return null;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void startContext(Class<? extends Annotation> scopeType, Object startParameter) throws ContextException
    {
        try
        {
            if(scopeType.equals(RequestScoped.class))
            {
                startRequestContext();
            }
            else if(scopeType.equals(SessionScoped.class))
            {
                startSessionContext();
            }
            else if(scopeType.equals(ApplicationScoped.class))
            {
                startApplicationContext();
            }
            else if(scopeType.equals(ConversationScoped.class))
            {
                startConversationContext();
            }
            else if(scopeType.equals(Singleton.class))
            {
                startSingletonContext();
            }

            // do nothing for Dependent.class

        }
        catch (ContextException ce)
        {
            throw ce;
        }
        catch (Exception e)
        {
            throw new ContextException(e);
        }        
    }

    @Override
    public void destroy(Object destroyObject)
    {
        RequestContext requestCtx = requestContext.get();
        if (requestCtx != null)
        {
            requestCtx.destroy();
            RequestScopedBeanInterceptorHandler.removeThreadLocals();
            requestContext.set(null);
            requestContext.remove();
        }

        SessionContext sessionCtx = sessionContext.get();
        if (sessionCtx != null)
        {
            sessionCtx.destroy();
            SessionScopedBeanInterceptorHandler.removeThreadLocals();
            sessionContext.set(null);
            sessionContext.remove();
        }

        ConversationContext conversationCtx = conversationContext.get();
        if (conversationCtx != null)
        {
            conversationCtx.destroy();
            conversationContext.set(null);
            conversationContext.remove();
        }

        dependentContext.set(null);
        dependentContext.remove();

        destroyGlobalContexts();
    }

    protected void destroyGlobalContexts()
    {
        if (applicationContext != null)
        {
            applicationContext.destroy();
            applicationContext.destroySystemBeans();
        }
    }


    private Context getCurrentConversationContext()
    {
        ConversationContext conversationCtx = conversationContext.get();
        if (conversationCtx == null)
        {
            conversationCtx = webBeansContext.getConversationManager().getConversationContext(getCurrentSessionContext());
            conversationContext.set(conversationCtx);

            // check for busy and non-existing conversations
            String conversationId = webBeansContext.getConversationService().getConversationId();
            if (conversationId != null && conversationCtx.getConversation().isTransient())
            {
                throw new NonexistentConversationException("Propogated conversation with cid=" + conversationId +
                        " cannot be restored. It creates a new transient conversation.");
            }

            if (conversationCtx.getConversation().iUseIt() > 1)
            {
                //Throw Busy exception
                throw new BusyConversationException("Propogated conversation with cid=" + conversationId +
                        " is used by other request. It creates a new transient conversation");
            }
        }

        return conversationCtx;
    }

    
    private Context getCurrentDependentContext()
    {        
        if(dependentContext.get() == null)
        {
            dependentContext.set(new DependentContext());
        }
        
        return dependentContext.get();
    }

    
    private Context getCurrentRequestContext()
    {        
        return requestContext.get();
    }

    
    private Context getCurrentSessionContext()
    {
        return sessionContext.get();
    }
    
    private void startApplicationContext()
    {
        if (applicationContext != null && !applicationContext.isDestroyed())
        {
            // applicationContext is already started
            return;
        }

        ApplicationContext ctx = new ApplicationContext();
        ctx.setActive(true);

        applicationContext = ctx;

        // We do ALSO send the @Initialized(ApplicationScoped.class) at this
        // location but this is WAY to early for userland apps
        // This also gets sent in the application startup code after AfterDeploymentValidation got fired.
        // see AbstractLifecycle#afterStartApplication
        webBeansContext.getBeanManagerImpl().fireContextLifecyleEvent(
            new Object(), InitializedLiteral.INSTANCE_APPLICATION_SCOPED);
    }

    
    private void startConversationContext()
    {
        if (!supportsConversation)
        {
            return;
        }
        ConversationManager conversationManager = webBeansContext.getConversationManager();
        ConversationContext ctx = conversationManager.getConversationContext(getCurrentSessionContext());
        ctx.setActive(true);
        conversationContext.set(ctx);

        final ConversationImpl conversation = ctx.getConversation();
        if (conversation.isTransient())
        {
            webBeansContext.getBeanManagerImpl().fireContextLifecyleEvent(
                conversationManager.getLifecycleEventPayload(ctx), InitializedLiteral.INSTANCE_CONVERSATION_SCOPED);
        }
        else
        {
            conversation.updateLastAccessTime();
        }
    }

    
    private void startRequestContext()
    {
        
        RequestContext ctx = new RequestContext();
        ctx.setActive(true);
        
        requestContext.set(ctx);
        if (shouldFireRequestLifecycleEvents())
        {
            webBeansContext.getBeanManagerImpl().fireContextLifecyleEvent(
                    ctx, InitializedLiteral.INSTANCE_REQUEST_SCOPED);
        }
    }

    
    private void startSessionContext()
    {
        SessionContext ctx = new SessionContext();
        ctx.setActive(true);
        
        sessionContext.set(ctx);
        webBeansContext.getBeanManagerImpl().fireContextLifecyleEvent(
            new Object(), InitializedLiteral.INSTANCE_SESSION_SCOPED);
    }

    
    private void startSingletonContext()
    {

        createSingletonContext();
        if (fireApplicationScopeEvents())
        {
            webBeansContext.getBeanManagerImpl().fireContextLifecyleEvent(
                    new Object(), InitializedLiteral.INSTANCE_SINGLETON_SCOPED);
        }
    }

    private void stopApplicationContext()
    {
        if(applicationContext != null && !applicationContext.isDestroyed())
        {
            final boolean fireApplicationScopeEvents = fireApplicationScopeEvents();
            if (fireApplicationScopeEvents)
            {
                webBeansContext.getBeanManagerImpl().fireContextLifecyleEvent(
                        new Object(), BeforeDestroyedLiteral.INSTANCE_APPLICATION_SCOPED);
            }

            applicationContext.destroy();

            // this is needed to get rid of ApplicationScoped beans which are cached inside the proxies...
            WebBeansContext.currentInstance().getBeanManagerImpl().clearCacheProxies();
            if (fireApplicationScopeEvents)
            {
                webBeansContext.getBeanManagerImpl().fireContextLifecyleEvent(
                        new Object(), DestroyedLiteral.INSTANCE_APPLICATION_SCOPED);
            }
        }
    }

    public boolean fireApplicationScopeEvents()
    {
        return Boolean.parseBoolean(webBeansContext.getOpenWebBeansConfiguration()
                .getProperty("org.apache.webbeans.lifecycle.standalone.fireApplicationScopeEvents", "true"));
    }
    
    private void stopConversationContext()
    {
        if (!supportsConversation)
        {
            return;
        }
        if(conversationContext.get() != null)
        {
            conversationContext.get().destroy();   
        }

        conversationContext.set(null);
        conversationContext.remove();
    }

    
    private void stopRequestContext()
    {
        // cleanup open conversations first
        if (supportsConversation)
        {
            destroyOutdatedConversations(conversationContext.get());
            conversationContext.set(null);
            conversationContext.remove();
        }


        final RequestContext ctx = BaseSeContextsService.requestContext.get();
        if (ctx != null && shouldFireRequestLifecycleEvents())
        {
            webBeansContext.getBeanManagerImpl().fireContextLifecyleEvent(
                    new Object(), BeforeDestroyedLiteral.INSTANCE_REQUEST_SCOPED);
        }

        if (ctx != null)
        {
            ctx.destroy();
        }

        BaseSeContextsService.requestContext.set(null);
        BaseSeContextsService.requestContext.remove();
        RequestScopedBeanInterceptorHandler.removeThreadLocals();

        if (ctx != null && shouldFireRequestLifecycleEvents())
        {
            webBeansContext.getBeanManagerImpl().fireContextLifecyleEvent(
                    ctx, DestroyedLiteral.INSTANCE_REQUEST_SCOPED);
        }
    }

    
    private void stopSessionContext()
    {
        webBeansContext.getBeanManagerImpl().fireContextLifecyleEvent(
                new Object(), BeforeDestroyedLiteral.INSTANCE_SESSION_SCOPED);
        SessionContext activeContext = sessionContext.get();
        if(activeContext != null)
        {
            activeContext.destroy();   
        }

        sessionContext.set(null);
        sessionContext.remove();
        SessionScopedBeanInterceptorHandler.removeThreadLocals();
        if (activeContext != null)
        {
            webBeansContext.getBeanManagerImpl().fireContextLifecyleEvent(
                new Object(), DestroyedLiteral.INSTANCE_SESSION_SCOPED);
        }
    }

    
    private void stopSingletonContext()
    {
        webBeansContext.getBeanManagerImpl().fireContextLifecyleEvent(
            new Object(), BeforeDestroyedLiteral.INSTANCE_SINGLETON_SCOPED);
        destroySingletonContext();
        webBeansContext.getBeanManagerImpl().fireContextLifecyleEvent(
            new Object(), DestroyedLiteral.INSTANCE_SINGLETON_SCOPED);
    }
}

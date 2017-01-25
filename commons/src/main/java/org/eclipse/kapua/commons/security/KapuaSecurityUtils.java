/*******************************************************************************
 * Copyright (c) 2011, 2016 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *
 *******************************************************************************/
package org.eclipse.kapua.commons.security;

import java.util.concurrent.Callable;

import org.eclipse.kapua.commons.model.id.KapuaEid;
import org.eclipse.kapua.locator.KapuaLocator;
import org.eclipse.kapua.service.authorization.subject.SubjectFactory;
import org.eclipse.kapua.service.authorization.subject.SubjectType;

/**
 * Kapua security utility to handle the bind/unbind operation of the Kapua session into the thread context.
 *
 * @since 1.0.0
 */
public class KapuaSecurityUtils {

    public static String MDC_USER_ID = "userId";

    private static final ThreadLocal<KapuaSession> threadSession = new ThreadLocal<>();

    /**
     * Returns the {@link KapuaSession} associated to the current thread session.
     * 
     * @return
     */
    public static KapuaSession getSession() {
        return threadSession.get();
    }

    /**
     * Bounds the {@link KapuaSession} to the current thread session.
     * 
     * @param session
     */
    public static void setSession(KapuaSession session) {
        threadSession.set(session);
    }

    /**
     * Clears the {@link KapuaSession} from the current thread session.
     */
    public static void clearSession() {
        threadSession.remove();
    }

    /**
     * Executes the {@link Callable} in a privileged context.<br>
     * Trusted mode means that no checks for permissions and rights will fail.
     * 
     * @param privilegedAction
     * @return
     * @throws Exception
     */
    public static <T> T doPriviledge(Callable<T> privilegedAction)
            throws Exception {
        T result = null;

        KapuaSession session = getSession();

        boolean created = false;
        if (session == null) {
            KapuaLocator locator = KapuaLocator.getInstance();
            SubjectFactory subjectFactory = locator.getFactory(SubjectFactory.class);

            session = new KapuaSession(null, KapuaEid.ONE, subjectFactory.newSubject(SubjectType.USER, KapuaEid.ONE));
            setSession(session);
            created = true;
        }

        session.setTrustedMode(true);
        try {
            result = privilegedAction.call();
        } finally {
            session.setTrustedMode(false);

            if (created) {
                clearSession();
                session = null;
            }
        }

        return result;
    }

}

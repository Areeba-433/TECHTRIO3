/*******************************************************************************
 * Copyright (c) 2016, 2022 Eurotech and/or its affiliates and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.service.user.internal;

import org.eclipse.kapua.commons.jpa.AbstractEntityManagerFactory;
import org.eclipse.kapua.commons.jpa.EntityManagerFactory;

/**
 * {@link UserServiceImpl} {@link EntityManagerFactory} implementation.
 *
 * @since 1.0.0
 */
public class UserEntityManagerFactory extends AbstractEntityManagerFactory implements EntityManagerFactory {

    private static final String PERSISTENCE_UNIT_NAME = "kapua-user";

    /**
     * Constructor.
     *
     * @since 1.0.0
     */
    public UserEntityManagerFactory() {
        super(PERSISTENCE_UNIT_NAME);
    }

    //TODO: remove me along with deprecated singleton access point
    private final static UserEntityManagerFactory INSTANCE = new UserEntityManagerFactory();

    /**
     * Returns the {@link EntityManagerFactory} instance.
     *
     * @return The {@link EntityManagerFactory} instance.
     * @since 1.0.0
     * @deprecated Since 2.0.0 - Please use {@link UserEntityManagerFactory#UserEntityManagerFactory()} instead. This may be removed in future releases.
     */
    @Deprecated
    public static UserEntityManagerFactory getInstance() {
        return INSTANCE;
    }
}

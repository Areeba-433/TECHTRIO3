/*******************************************************************************
 * Copyright (c) 2020, 2022 Eurotech and/or its affiliates and others
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
package org.eclipse.kapua.service.authentication.credential.mfa;

import org.eclipse.kapua.locator.KapuaLocator;

import javax.xml.bind.annotation.XmlRegistry;

@XmlRegistry
public class MfaOptionXmlRegistry {

    private final MfaOptionFactory mfaOptionFactory = KapuaLocator.getInstance().getFactory(MfaOptionFactory.class);

    /**
     * Instantiates a new {@link MfaOptionCreator} instance
     *
     * @return The newly instantiated {@link MfaOptionCreator}
     * @since 1.3.0
     */
    public MfaOptionCreator newMfaOptionCreator() {
        return mfaOptionFactory.newCreator(null, null);
    }

    /**
     * Instantiates a new {@link MfaOption} instance
     *
     * @return The newly instantiated {@link MfaOption}
     * @since 1.3.0
     */
    public MfaOption newMfaOption() {
        return mfaOptionFactory.newEntity(null);
    }

    /**
     * Instantiates a new {@link MfaOptionListResult} instance
     *
     * @return The newly instantiated {@link MfaOptionListResult}
     * @since 1.3.0
     */
    public MfaOptionListResult newMfaOptionListResult() {
        return mfaOptionFactory.newListResult();
    }

    /**
     * Instantiates a new {@link MfaOptionQuery} instance.
     *
     * @return The newly instantiated {@link MfaOptionQuery}
     * @since 1.3.0
     */
    public MfaOptionQuery newQuery() {
        return mfaOptionFactory.newQuery(null);
    }
}

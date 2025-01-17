/*******************************************************************************
 * Copyright (c) 2019, 2022 Eurotech and/or its affiliates and others
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
package org.eclipse.kapua.model.xml.adapters;

public class StringPropertyAdapter extends ClassBasedXmlPropertyAdapterBase<String> {

    public StringPropertyAdapter() {
        super(String.class);
    }

    /**
     * Yes, definitely String can be empty.
     *
     * @return {@code true}
     * @since 2.1.0
     */
    @Override
    public boolean canUnmarshallEmptyString() {
        return true;
    }

    @Override
    public String unmarshallValue(String property) {
        return property;
    }
}

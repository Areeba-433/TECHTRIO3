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

import org.eclipse.kapua.model.xml.XmlPropertyAdapted;

public interface XmlPropertyAdapter {
    boolean canMarshall(Class<?> objectClass);

    boolean doesEncrypt();

    void marshallValues(XmlPropertyAdapted<?> property, Object value);

    Object unmarshallValues(XmlPropertyAdapted<?> property);
}

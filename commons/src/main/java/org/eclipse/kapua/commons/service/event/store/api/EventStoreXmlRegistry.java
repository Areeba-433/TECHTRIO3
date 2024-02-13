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
package org.eclipse.kapua.commons.service.event.store.api;

import org.eclipse.kapua.commons.service.event.store.internal.EventStoreFactoryImpl;

import javax.xml.bind.annotation.XmlRegistry;

@XmlRegistry
public class EventStoreXmlRegistry {

    private final EventStoreFactory kapuaEventFactory = new EventStoreFactoryImpl();

    /**
     * Creates a new kapuaEvent instance
     *
     * @return
     */
    public EventStoreRecord newEventStoreRecord() {
        return kapuaEventFactory.newEntity(null);
    }

    /**
     * Creates a new kapuaEvent creator instance
     *
     * @return
     */
    public EventStoreRecordCreator newEventStoreRecordCreator() {
        return kapuaEventFactory.newCreator(null);
    }

    /**
     * Creates a new kapuaEvent list result instance
     *
     * @return
     */
    public EventStoreRecordListResult newEventStoreRecordListResult() {
        return kapuaEventFactory.newListResult();
    }

    public EventStoreRecordQuery newQuery() {
        return kapuaEventFactory.newQuery(null);
    }

}

/*******************************************************************************
 * Copyright (c) 2011, 2017 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *     Red Hat Inc
 *******************************************************************************/
package org.eclipse.kapua.service.authorization.permission;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import org.eclipse.kapua.model.KapuaObjectFactory;
import org.eclipse.kapua.model.id.KapuaId;
import org.eclipse.kapua.service.authorization.domain.Domain;
import org.eclipse.kapua.service.authorization.group.Group;

/**
 * {@link Permission} object factory.
 */
public interface PermissionFactory extends KapuaObjectFactory {

    /**
     * Instantiate a new {@link Permission} implementing object with the provided parameters.
     * 
     * @param domain
     *            The {@link Domain} of the new {@link Permission}.
     * @param action
     *            The {@link Action} of the new {@link Permission}.
     * @param targetScopeId
     *            The target scope id of the new {@link Permission}.
     * @return A instance of the implementing class of {@link Permission}.
     */
    public default Permission newPermission(Domain domain, Actions action, KapuaId targetScopeId) {
        return newPermission(domain, action, targetScopeId, null);
    }

    /**
     * Instantiate a new {@link Permission} implementing object with the provided parameters.
     * 
     * @param domain
     *            The {@link Domain} of the new {@link Permission}.
     * @param action
     *            The {@link Action} of the new {@link Permission}.
     * @param targetScopeId
     *            The target scope id of the new {@link Permission}.
     * @param groupId
     *            The {@link Group} id that this {@link Permission} gives access.
     * @return A instance of the implementing class of {@link Permission}.
     */
    public default Permission newPermission(Domain domain, Actions action, KapuaId targetScopeId, KapuaId groupId) {
        return newPermission(domain, action, targetScopeId, groupId, false);
    }

    /**
     * Instantiate a new {@link Permission} implementing object with the provided parameters.
     * 
     * @param domain
     *            The {@link Domain} of the new {@link Permission}.
     * @param action
     *            The {@link Action} of the new {@link Permission}.
     * @param targetScopeId
     *            The target scope id of the new {@link Permission}.
     * @param groupId
     *            The {@link Group} id that this {@link Permission} gives access.
     * @param forwardable
     *            If the {@link Permission} is forward-able to children scopeIds
     * @return A instance of the implementing class of {@link Permission}.
     */
    public Permission newPermission(Domain domain, Actions action, KapuaId targetScopeId, KapuaId groupId, boolean forwardable);

    /**
     * Instantiate new {@link Permission}s implementing object with the provided parameters.
     * 
     * @param domain
     *            The {@link Domain} of the new {@link Permission}.
     * @param targetScopeId
     *            The target scope id of the new {@link Permission}.
     * @param actions
     *            The {@link Action}s of the new {@link Permission}s.
     * @return A collection of instances of the implementing class of {@link Permission}.
     */
    public default Collection<Permission> newPermissions(Domain domain, KapuaId targetScopeId, Actions... actions) {
        return newPermissions(domain, targetScopeId, null, actions);
    }

    /**
     * Instantiate new {@link Permission}s implementing object with the provided parameters.
     * 
     * @param domain
     *            The {@link Domain} of the new {@link Permission}.
     * @param targetScopeId
     *            The target scope id of the new {@link Permission}.
     * @param groupId
     *            The {@link Group} id that this {@link Permission} gives access.
     * @param actions
     *            The {@link Action}s of the new {@link Permission}s.
     * @return A collection of instances of the implementing class of {@link Permission}.
     */
    public default Collection<Permission> newPermissions(Domain domain, KapuaId targetScopeId, KapuaId groupId, Actions... actions) {
        return newPermissions(domain, targetScopeId, groupId, false, actions);
    }

    /**
     * Instantiate new {@link Permission}s implementing object with the provided parameters.
     * 
     * @param domain
     *            The {@link Domain} of the new {@link Permission}.
     * @param targetScopeId
     *            The target scope id of the new {@link Permission}.
     * @param groupId
     *            The {@link Group} id that this {@link Permission} gives access.
     * @param forwardable
     *            If the {@link Permission} is forward-able to children scopeIds
     * @param actions
     *            The {@link Action}s of the new {@link Permission}s.
     * @return A collection of instances of the implementing class of {@link Permission}.
     */
    public default Collection<Permission> newPermissions(Domain domain, KapuaId targetScopeId, KapuaId groupId, boolean forwardable, Actions... actions) {
        return Arrays.stream(actions)
                .map(action -> newPermission(domain, action, targetScopeId, groupId, forwardable))
                .collect(Collectors.toList());
    }
}

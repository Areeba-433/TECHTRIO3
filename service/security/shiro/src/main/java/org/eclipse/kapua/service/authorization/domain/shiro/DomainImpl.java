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
 *******************************************************************************/
package org.eclipse.kapua.service.authorization.domain.shiro;

import org.eclipse.kapua.commons.model.AbstractKapuaEntity;
import org.eclipse.kapua.model.domain.Actions;
import org.eclipse.kapua.model.id.KapuaId;
import org.eclipse.kapua.service.authorization.domain.Domain;

import javax.persistence.Basic;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import java.util.Objects;
import java.util.Set;

/**
 * {@link Domain} implementation.
 *
 * @since 1.0
 */
@Entity(name = "Domain")
@Table(name = "athz_domain")
public class DomainImpl extends AbstractKapuaEntity implements Domain {

    private static final long serialVersionUID = 3878607249074632729L;

    @Basic
    @Column(name = "name", nullable = false, updatable = false)
    private String name;

    @Basic
    @Column(name = "serviceName", nullable = false, updatable = false)
    private String serviceName;

    @ElementCollection
    @CollectionTable(name = "athz_domain_actions", joinColumns = @JoinColumn(name = "domain_id", referencedColumnName = "id"))
    @Column(name = "action", nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<Actions> actions;

    @Basic
    @Column(name = "groupable", nullable = false, updatable = false)
    private boolean groupable;

    /**
     * Constructor
     */
    public DomainImpl() {
        super();
    }

    public DomainImpl(KapuaId scopeId) {
        super(scopeId);
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getServiceName() {
        return serviceName;
    }

    @Override
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    public Set<Actions> getActions() {
        return actions;
    }

    @Override
    public void setGroupable(boolean groupable) {
        this.groupable = groupable;
    }

    @Override
    public boolean getGroupable() {
        return groupable;
    }

    @Override
    public void setActions(Set<Actions> actions) {
        this.actions = actions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DomainImpl domain = (DomainImpl) o;
        return groupable == domain.groupable &&
                Objects.equals(name, domain.name) &&
                Objects.equals(serviceName, domain.serviceName) &&
                Objects.equals(actions, domain.actions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, serviceName, actions, groupable);
    }
}

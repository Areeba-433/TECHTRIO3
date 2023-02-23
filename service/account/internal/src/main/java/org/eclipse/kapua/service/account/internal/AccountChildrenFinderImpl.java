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
package org.eclipse.kapua.service.account.internal;

import org.eclipse.kapua.KapuaException;
import org.eclipse.kapua.commons.configuration.AccountChildrenFinder;
import org.eclipse.kapua.model.KapuaEntityAttributes;
import org.eclipse.kapua.model.id.KapuaId;
import org.eclipse.kapua.model.query.KapuaListResult;
import org.eclipse.kapua.model.query.predicate.AttributePredicate;
import org.eclipse.kapua.service.KapuaService;
import org.eclipse.kapua.service.account.Account;
import org.eclipse.kapua.service.account.AccountFactory;
import org.eclipse.kapua.service.account.AccountQuery;
import org.eclipse.kapua.service.account.AccountRepository;

import javax.inject.Inject;
import java.util.Optional;

public class AccountChildrenFinderImpl implements AccountChildrenFinder, KapuaService {

    private final AccountFactory accountFactory;
    private final AccountRepository accountRepository;

    @Inject
    public AccountChildrenFinderImpl(AccountFactory accountFactory, AccountRepository accountRepository) {
        this.accountFactory = accountFactory;
        this.accountRepository = accountRepository;
    }

    @Override
    public KapuaListResult<Account> findChildren(KapuaId scopeId, Optional<KapuaId> excludeTargetScopeId) throws KapuaException {
        final AccountQuery childAccountsQuery = accountFactory.newQuery(scopeId);
        // Exclude the scope that is under config update
        if (excludeTargetScopeId.isPresent()) {
            childAccountsQuery.setPredicate(
                    childAccountsQuery.attributePredicate(
                            KapuaEntityAttributes.ENTITY_ID,
                            excludeTargetScopeId.get(),
                            AttributePredicate.Operator.NOT_EQUAL)
            );
        }

        return accountRepository.query(childAccountsQuery);
    }
}
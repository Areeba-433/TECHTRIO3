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
package org.eclipse.kapua.commons.storage.memory.adapters;

import org.eclipse.kapua.model.query.predicate.QueryPredicate;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public interface QueryPredicateResolver {
    <E> Optional<Predicate<E>> mapToPredicate(QueryPredicate queryPredicate, Map<String, Function<E, Object>> pluckers, BiFunction<QueryPredicate, Map<String, Function<E, Object>>, Predicate<E>> innerVisitor);
}

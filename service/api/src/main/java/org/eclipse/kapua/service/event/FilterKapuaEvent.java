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
 *******************************************************************************/
package org.eclipse.kapua.service.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME) 
@Target(ElementType.METHOD)
public @interface FilterKapuaEvent {

    String[] service() default {"*"};
    String[] entityType() default {"*"};
    String[] operation() default {"*"};
    KapuaEvent.OperationStatus[] operationStatus() default {KapuaEvent.OperationStatus.OK, KapuaEvent.OperationStatus.FAIL}; 
}

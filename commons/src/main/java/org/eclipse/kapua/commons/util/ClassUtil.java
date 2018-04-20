/*******************************************************************************
 * Copyright (c) 2018 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.commons.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.kapua.KapuaErrorCodes;
import org.eclipse.kapua.KapuaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to instantiate object through reflection
 * 
 * @since 1.0
 *
 */
public class ClassUtil {

    protected final static Logger logger = LoggerFactory.getLogger(ClassUtil.class);

    private final static String CANNOT_LOAD_INSTANCE_ERROR_MSG = "Cannot load instance %s for %s. Please check the configuration file!";
    private final static String PARAMETER_ERROR_MSG = "Invalid parameters. Parameters types and values differ!";

    private ClassUtil() {
    }

    /**
     * Create a class new instance (fallback to the default instance if something goes wrong)
     * 
     * @param clazz
     * @param defaultInstance
     * @return
     * @throws KapuaException
     */
    public static <T> T newInstance(String clazz, Class<T> defaultInstance) throws KapuaException {
        return newInstance(clazz, defaultInstance, null, null);
    }

    @SuppressWarnings("unchecked")
    /**
     * Create a class new instance by invoking the proper constructor (fallback to the default instance if something goes wrong)
     * 
     * @param clazz
     * @param defaultInstance
     * @param parameterTypes
     *            constructor parameters type
     * @param parameters
     *            constructor parameters value
     * @return
     * @throws KapuaException
     */
    public static <T> T newInstance(String clazz, Class<T> defaultInstance, Class<?>[] parameterTypes, Object[] parameters) throws KapuaException {
        logger.info("Initializing instance...");
        T instance;
        Class<T> clazzToInstantiate = defaultInstance;
        if (!StringUtils.isEmpty(clazz)) {
            logger.info("Initializing instance of {}...", clazz);
            // prepare parameters class type list
            try {
                clazzToInstantiate = (Class<T>) Class.forName(clazz);
            } catch (ClassNotFoundException e) {
                throw new KapuaException(KapuaErrorCodes.INTERNAL_ERROR, e, String.format(CANNOT_LOAD_INSTANCE_ERROR_MSG, clazz, clazzToInstantiate));
            }
        }
        else {
            logger.info("Initializing instance of. Instantiate default instance {} ...", defaultInstance);
        }
        if (parameterTypes == null || parameterTypes.length <= 0) {
            try {
                instance = clazzToInstantiate.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new KapuaException(KapuaErrorCodes.INTERNAL_ERROR, e, String.format(CANNOT_LOAD_INSTANCE_ERROR_MSG, clazz, clazzToInstantiate));
            }
        }
        else {
            if (parameters == null || parameters.length != parameterTypes.length) {
                throw new KapuaException(KapuaErrorCodes.INTERNAL_ERROR, PARAMETER_ERROR_MSG);
            }
            try {
                Constructor<T> constructor = clazzToInstantiate.getDeclaredConstructor(parameterTypes);
                instance = constructor.newInstance(parameters);
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                throw new KapuaException(KapuaErrorCodes.INTERNAL_ERROR, e, String.format(CANNOT_LOAD_INSTANCE_ERROR_MSG, clazz, clazzToInstantiate));
            }
        }
        logger.info("Initializing broker ip resolver... DONE");
        return instance;
    }
}

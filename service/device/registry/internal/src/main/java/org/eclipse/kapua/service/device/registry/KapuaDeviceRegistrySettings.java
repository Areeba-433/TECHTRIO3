/*******************************************************************************
 * Copyright (c) 2017, 2022 Eurotech and/or its affiliates and others
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
package org.eclipse.kapua.service.device.registry;

import org.eclipse.kapua.commons.setting.AbstractBaseKapuaSetting;
import org.eclipse.kapua.commons.setting.AbstractKapuaSetting;

import javax.inject.Inject;

/**
 * {@link AbstractBaseKapuaSetting} for `kapua-device-registry-internal` module.
 *
 * @since 1.0.0
 */
public class KapuaDeviceRegistrySettings extends AbstractKapuaSetting<KapuaDeviceRegistrySettingKeys> {

    private static final String DEVICE_REGISTRY_SETTING_RESOURCE = "kapua-device-registry-setting.properties";

    /**
     * Constructor.
     *
     * @since 1.0.0
     */
    @Inject
    public KapuaDeviceRegistrySettings() {
        super(DEVICE_REGISTRY_SETTING_RESOURCE);
    }

}

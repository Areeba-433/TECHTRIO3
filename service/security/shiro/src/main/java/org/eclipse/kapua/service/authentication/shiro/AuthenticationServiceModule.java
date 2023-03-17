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
package org.eclipse.kapua.service.authentication.shiro;

import org.eclipse.kapua.commons.event.ServiceEventClientConfiguration;
import org.eclipse.kapua.commons.event.ServiceEventModuleTransactionalConfiguration;
import org.eclipse.kapua.commons.event.ServiceEventTransactionalModule;
import org.eclipse.kapua.commons.event.ServiceInspector;
import org.eclipse.kapua.commons.jpa.JpaTxManager;
import org.eclipse.kapua.commons.jpa.KapuaEntityManagerFactory;
import org.eclipse.kapua.service.authentication.credential.CredentialService;
import org.eclipse.kapua.service.authentication.shiro.setting.KapuaAuthenticationSetting;
import org.eclipse.kapua.service.authentication.shiro.setting.KapuaAuthenticationSettingKeys;
import org.eclipse.kapua.service.authentication.token.AccessTokenService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class AuthenticationServiceModule extends ServiceEventTransactionalModule {

    @Inject
    private CredentialService credentialService;
    @Inject
    private AccessTokenService accessTokenService;

    @Override
    protected ServiceEventModuleTransactionalConfiguration initializeConfiguration() {
        KapuaAuthenticationSetting kas = KapuaAuthenticationSetting.getInstance();
        List<ServiceEventClientConfiguration> selc = new ArrayList<>();
        selc.addAll(ServiceInspector.getEventBusClients(credentialService, CredentialService.class));
        selc.addAll(ServiceInspector.getEventBusClients(accessTokenService, AccessTokenService.class));
        return new ServiceEventModuleTransactionalConfiguration(
                kas.getString(KapuaAuthenticationSettingKeys.AUTHENTICATION_EVENT_ADDRESS),
                new JpaTxManager(new KapuaEntityManagerFactory("kapua-authentication")),
                selc.toArray(new ServiceEventClientConfiguration[0]));
    }

}

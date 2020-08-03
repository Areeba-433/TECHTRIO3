/*******************************************************************************
 * Copyright (c) 2020 Eurotech and/or its affiliates and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.sso.provider.generic.jwt;

import org.eclipse.kapua.sso.exception.SsoException;
import org.eclipse.kapua.sso.exception.SsoIllegalArgumentException;
import org.eclipse.kapua.sso.provider.jwt.AbstractJwtProcessor;
import org.eclipse.kapua.sso.provider.generic.setting.GenericSsoSetting;
import org.eclipse.kapua.sso.provider.generic.setting.GenericSsoSettingKeys;
import org.eclipse.kapua.sso.provider.setting.SsoSetting;
import org.eclipse.kapua.sso.provider.setting.SsoSettingKeys;

import java.util.List;

/**
 * The generic JWT Processor.
 */
public class GenericJwtProcessor extends AbstractJwtProcessor {

    public GenericJwtProcessor() throws SsoException {
    }

    @Override
    protected List<String> getJwtExpectedIssuers() throws SsoIllegalArgumentException {
        List<String> jwtExpectedIssuers = GenericSsoSetting.getInstance().getList(String.class, GenericSsoSettingKeys.SSO_OPENID_JWT_ISSUER_ALLOWED);
        if (jwtExpectedIssuers == null || jwtExpectedIssuers.isEmpty()) {
            throw new SsoIllegalArgumentException(GenericSsoSettingKeys.SSO_OPENID_JWT_ISSUER_ALLOWED.key(), (jwtExpectedIssuers == null ? null : ""));
        }
        return jwtExpectedIssuers;
    }

    @Override
    protected List<String> getJwtAudiences() throws SsoIllegalArgumentException {
        List<String> jwtAudiences = SsoSetting.getInstance().getList(String.class, SsoSettingKeys.SSO_OPENID_CLIENT_ID);
        if (jwtAudiences == null || jwtAudiences.isEmpty()) {
            throw new SsoIllegalArgumentException(SsoSettingKeys.SSO_OPENID_CLIENT_ID.key(), (jwtAudiences == null ? null : ""));
        }
        return jwtAudiences;
    }
}

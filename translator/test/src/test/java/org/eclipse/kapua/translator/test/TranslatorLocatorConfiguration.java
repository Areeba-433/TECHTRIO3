/*******************************************************************************
 * Copyright (c) 2020, 2022 Eurotech and/or its affiliates and others
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
package org.eclipse.kapua.translator.test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import io.cucumber.java.Before;
import org.eclipse.kapua.KapuaException;
import org.eclipse.kapua.commons.configuration.metatype.KapuaMetatypeFactoryImpl;
import org.eclipse.kapua.commons.crypto.CryptoUtil;
import org.eclipse.kapua.commons.crypto.CryptoUtilImpl;
import org.eclipse.kapua.commons.crypto.setting.CryptoSettings;
import org.eclipse.kapua.commons.jpa.KapuaJpaRepositoryConfiguration;
import org.eclipse.kapua.commons.metric.MetricsService;
import org.eclipse.kapua.commons.metric.MetricsServiceImpl;
import org.eclipse.kapua.locator.KapuaLocator;
import org.eclipse.kapua.message.KapuaMessageFactory;
import org.eclipse.kapua.message.device.data.KapuaDataMessageFactory;
import org.eclipse.kapua.message.internal.KapuaMessageFactoryImpl;
import org.eclipse.kapua.model.config.metatype.KapuaMetatypeFactory;
import org.eclipse.kapua.model.id.KapuaIdFactory;
import org.eclipse.kapua.qa.common.MockedLocator;
import org.eclipse.kapua.service.account.AccountService;
import org.eclipse.kapua.service.authorization.AuthorizationService;
import org.eclipse.kapua.service.authorization.permission.Permission;
import org.eclipse.kapua.service.authorization.permission.PermissionFactory;
import org.eclipse.kapua.service.device.management.asset.DeviceAssetFactory;
import org.eclipse.kapua.service.device.management.bundle.DeviceBundleFactory;
import org.eclipse.kapua.service.device.management.configuration.DeviceConfigurationFactory;
import org.eclipse.kapua.service.device.management.inventory.DeviceInventoryManagementFactory;
import org.eclipse.kapua.service.device.management.keystore.DeviceKeystoreManagementFactory;
import org.eclipse.kapua.service.device.management.packages.DevicePackageFactory;
import org.eclipse.kapua.service.device.management.request.GenericRequestFactory;
import org.eclipse.kapua.service.device.management.snapshot.DeviceSnapshotFactory;
import org.eclipse.kapua.service.device.registry.DeviceRegistryService;
import org.eclipse.kapua.translator.KapuaKuraTranslatorsModule;
import org.eclipse.kapua.translator.KuraMqttTranslatorsModule;
import org.eclipse.kapua.translator.TranslatorHubModule;
import org.eclipse.kapua.translator.jms.kura.JmsKuraTranslatorsModule;
import org.mockito.Matchers;
import org.mockito.Mockito;

@Singleton
public class TranslatorLocatorConfiguration {

    /**
     * Setup DI with Google Guice DI.
     * Create mocked and non mocked service under test and bind them with Guice.
     * It is based on custom MockedLocator locator that is meant for sevice unit tests.
     */
    @Before(value = "@setup", order = 1)
    public void setupDI() {
        MockedLocator mockedLocator = (MockedLocator) KapuaLocator.getInstance();

        AbstractModule module = new AbstractModule() {

            @Override
            protected void configure() {
                bind(CryptoUtil.class).toInstance(new CryptoUtilImpl(new CryptoSettings()));
                bind(String.class).annotatedWith(Names.named("metricModuleName")).toInstance("tests");
                bind(MetricsService.class).to(MetricsServiceImpl.class).in(Singleton.class);
                bind(KapuaMessageFactory.class).to(KapuaMessageFactoryImpl.class).in(Singleton.class);
                // Inject mocked Authorization Service method checkPermission
                AuthorizationService mockedAuthorization = Mockito.mock(AuthorizationService.class);
                bind(KapuaJpaRepositoryConfiguration.class).toInstance(new KapuaJpaRepositoryConfiguration());

                try {
                    Mockito.doNothing().when(mockedAuthorization).checkPermission(Matchers.any(Permission.class));
                } catch (KapuaException e) {
                    // skip
                }
                bind(AuthorizationService.class).toInstance(mockedAuthorization);
                // Inject mocked Permission Factory
                bind(PermissionFactory.class).toInstance(Mockito.mock(PermissionFactory.class));
                bind(AccountService.class).toInstance(Mockito.mock(AccountService.class));
                bind(DeviceRegistryService.class).toInstance(Mockito.mock(DeviceRegistryService.class));
                bind(GenericRequestFactory.class).toInstance(Mockito.mock(GenericRequestFactory.class));
                bind(DeviceAssetFactory.class).toInstance(Mockito.mock(DeviceAssetFactory.class));
                bind(DeviceBundleFactory.class).toInstance(Mockito.mock(DeviceBundleFactory.class));
                bind(KapuaIdFactory.class).toInstance(Mockito.mock(KapuaIdFactory.class));
                bind(DevicePackageFactory.class).toInstance(Mockito.mock(DevicePackageFactory.class));
                bind(DeviceSnapshotFactory.class).toInstance(Mockito.mock(DeviceSnapshotFactory.class));
                bind(KapuaDataMessageFactory.class).toInstance(Mockito.mock(KapuaDataMessageFactory.class));
                bind(DeviceConfigurationFactory.class).toInstance(Mockito.mock(DeviceConfigurationFactory.class));
                bind(DeviceInventoryManagementFactory.class).toInstance(Mockito.mock(DeviceInventoryManagementFactory.class));
                bind(DeviceKeystoreManagementFactory.class).toInstance(Mockito.mock(DeviceKeystoreManagementFactory.class));
                // Set KapuaMetatypeFactory for Metatype configuration
                bind(KapuaMetatypeFactory.class).toInstance(new KapuaMetatypeFactoryImpl());
            }
        };

        Injector injector = Guice.createInjector(module, new TranslatorHubModule(), new KapuaKuraTranslatorsModule(), new KuraMqttTranslatorsModule(), new JmsKuraTranslatorsModule());
        mockedLocator.setInjector(injector);
    }
}

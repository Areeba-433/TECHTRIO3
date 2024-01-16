/*******************************************************************************
 * Copyright (c) 2021, 2022 Eurotech and/or its affiliates and others
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
package org.eclipse.kapua.job.engine.app.web;

import org.eclipse.kapua.commons.metric.CommonsMetric;
import org.eclipse.kapua.commons.populators.DataPopulatorRunner;
import org.eclipse.kapua.commons.rest.errors.ExceptionConfigurationProvider;
import org.eclipse.kapua.commons.util.xml.XmlUtil;
import org.eclipse.kapua.job.engine.app.web.jaxb.JobEngineJAXBContextProvider;
import org.eclipse.kapua.locator.KapuaLocator;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.filter.UriConnegFilter;
import org.glassfish.jersey.server.spi.Container;
import org.glassfish.jersey.server.spi.ContainerLifecycleListener;

import javax.inject.Singleton;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;

public class JobEngineApplication extends ResourceConfig {

    private static final String MODULE = "job-engine";

    public JobEngineApplication() {
        register(new AbstractBinder() {
            @Override
            protected void configure() {
                this.bind(ExceptionConfigurationProviderImpl.class)
                        .to(ExceptionConfigurationProvider.class)
                        .in(Singleton.class);
            }
        });
        packages("org.eclipse.kapua.commons.rest", "org.eclipse.kapua.job.engine.app", "org.eclipse.kapua.app.api.core");

        // Bind media type to resource extension
        HashMap<String, MediaType> mappedMediaTypes = new HashMap<>();
        mappedMediaTypes.put("json", MediaType.APPLICATION_JSON_TYPE);

        property(ServerProperties.MEDIA_TYPE_MAPPINGS, mappedMediaTypes);
        property(ServerProperties.WADL_FEATURE_DISABLE, true);
        register(UriConnegFilter.class);
        register(JacksonFeature.class);

        register(new ContainerLifecycleListener() {

            @Override
            public void onStartup(Container container) {
                //TODO to be injected!!!
                CommonsMetric.module = MODULE;
                ServiceLocator serviceLocator = container.getApplicationHandler().getInjectionManager().getInstance(ServiceLocator.class);
                JobEngineJAXBContextProvider provider = serviceLocator.createAndInitialize(JobEngineJAXBContextProvider.class);
                XmlUtil.setContextProvider(provider);
                KapuaLocator.getInstance().getService(DataPopulatorRunner.class).runPopulators();
            }

            @Override
            /**
             * Nothing to do
             */
            public void onReload(Container container) {
            }

            @Override
            /**
             * Nothing to do
             */
            public void onShutdown(Container container) {
            }
        });

    }

}

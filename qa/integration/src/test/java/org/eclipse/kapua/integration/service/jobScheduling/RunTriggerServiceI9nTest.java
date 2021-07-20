/*******************************************************************************
 * Copyright (c) 2019, 2021 Eurotech and/or its affiliates and others
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
package org.eclipse.kapua.integration.service.jobScheduling;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;

import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = {
                "classpath:features/jobScheduling/TriggerServiceI9n.feature",
        },
        glue = { "org.eclipse.kapua.service.job.steps",
                "org.eclipse.kapua.service.user.steps",
                "org.eclipse.kapua.qa.common",
                "org.eclipse.kapua.service.account.steps",
                "org.eclipse.kapua.service.device.registry.steps",
                "org.eclipse.kapua.service.scheduler.steps"
        },
        plugin = { "pretty",
                "html:target/cucumber",
                "json:target/cucumber.json" },
        monochrome = true)
public class RunTriggerServiceI9nTest {
}

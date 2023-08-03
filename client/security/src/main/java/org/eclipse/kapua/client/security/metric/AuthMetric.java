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
package org.eclipse.kapua.client.security.metric;

import com.codahale.metrics.Timer;

import org.eclipse.kapua.commons.metric.CommonsMetric;
import org.eclipse.kapua.commons.metric.MetricServiceFactory;
import org.eclipse.kapua.commons.metric.MetricsLabel;
import org.eclipse.kapua.commons.metric.MetricsService;

public class AuthMetric {

    private static final AuthMetric AUTH_METRIC = new AuthMetric();

    public static final String DISCONNECT = "disconnect";
    public static final String USER = "user";
    public static final String ADMIN = "admin";
    public static final String LOGOUT = "logout";

    private static final String REMOVE_CONNECTION = "remove_connection";

    private AuthLoginMetric adminLogin;
    private AuthLoginMetric userLogin;
    private AuthTimeMetric extConnectorTime;
    private AuthFailureMetric failure;
    private Timer removeConnection;

    public static AuthMetric getInstance() {
        return AUTH_METRIC;
    }

    private AuthMetric() {
        adminLogin = new AuthLoginMetric(ADMIN);
        userLogin = new AuthLoginMetric(USER);
        extConnectorTime = new AuthTimeMetric();
        failure = new AuthFailureMetric();
        MetricsService metricsService = MetricServiceFactory.getInstance();
        removeConnection = metricsService.getTimer(CommonsMetric.module, AuthMetric.USER, REMOVE_CONNECTION, MetricsLabel.TIME, MetricsLabel.SECONDS);
    }

    public AuthLoginMetric getAdminLogin() {
        return adminLogin;
    }

    public AuthLoginMetric getUserLogin() {
        return userLogin;
    }

    public AuthTimeMetric getExtConnectorTime() {
        return extConnectorTime;
    }

    public AuthFailureMetric getFailure() {
        return failure;
    }

    /**
     * Remove connection total time
     * @return
     */
    public Timer getRemoveConnection() {
        return removeConnection;
    }
}

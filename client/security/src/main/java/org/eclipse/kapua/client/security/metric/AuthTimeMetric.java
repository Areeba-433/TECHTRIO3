/*******************************************************************************
 * Copyright (c) 2022 Eurotech and/or its affiliates and others
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
import org.eclipse.kapua.commons.metric.MetricsLabel;
import org.eclipse.kapua.commons.metric.MetricsService;

import javax.inject.Inject;

public class AuthTimeMetric {

    private static final String ADD_CONNECTION = "add_connection";
    private static final String CHECK_ACCESS = "check_access";
    private static final String FIND_DEVICE = "find_device";
    private static final String LOGIN = "login";
    private static final String UPDATE_DEVICE = "update_device";
    private static final String RAISE_LIFECYCLE_EVENT = "raise_lifecycle_event";
    private static final String LOGOUT_ON_CONNECTION = "logout_on_connection";
    private static final String REMOVE_CONNECTION = "remove_connection";

    private Timer userAddConnection;
    private Timer userCheckAccess;
    private Timer userFindDevice;
    private Timer userUpdateDevice;
    private Timer userRemoveConnection;
    private Timer logoutOnLogin;
    private Timer adminAddConnection;
    private Timer raiseLifecycleEvent;

    @Inject
    public AuthTimeMetric(MetricsService metricsService) {
        userAddConnection = metricsService.getTimer(CommonsMetric.module, AuthMetric.USER, ADD_CONNECTION, MetricsLabel.TIME, MetricsLabel.SECONDS);
        userCheckAccess = metricsService.getTimer(CommonsMetric.module, AuthMetric.USER, CHECK_ACCESS, MetricsLabel.TIME, MetricsLabel.SECONDS);
        userFindDevice = metricsService.getTimer(CommonsMetric.module, AuthMetric.USER, FIND_DEVICE, MetricsLabel.TIME, MetricsLabel.SECONDS);
        userUpdateDevice = metricsService.getTimer(CommonsMetric.module, AuthMetric.USER, UPDATE_DEVICE, MetricsLabel.TIME, MetricsLabel.SECONDS);
        userRemoveConnection = metricsService.getTimer(CommonsMetric.module, AuthMetric.USER, REMOVE_CONNECTION, MetricsLabel.TIME, MetricsLabel.SECONDS);
        logoutOnLogin = metricsService.getTimer(CommonsMetric.module, AuthMetric.USER, LOGOUT_ON_CONNECTION, LOGIN, MetricsLabel.TIME, MetricsLabel.SECONDS);
        adminAddConnection = metricsService.getTimer(CommonsMetric.module, AuthMetric.ADMIN, ADD_CONNECTION, MetricsLabel.TIME, MetricsLabel.SECONDS);
        raiseLifecycleEvent = metricsService.getTimer(CommonsMetric.module, AuthMetric.USER, RAISE_LIFECYCLE_EVENT, MetricsLabel.TIME, MetricsLabel.SECONDS);
    }

    /**
     * Add connection user login total time
     *
     * @return
     */
    public Timer getUserAddConnection() {
        return userAddConnection;
    }

    /**
     * Add connection user login check access time
     *
     * @return
     */
    public Timer getUserCheckAccess() {
        return userCheckAccess;
    }

    /**
     * Add connection user login find device time
     *
     * @return
     */
    public Timer getUserFindDevice() {
        return userFindDevice;
    }

    /**
     * Add connection user login update device time
     *
     * @return
     */
    public Timer getUserUpdateDevice() {
        return userUpdateDevice;
    }

    /**
     * Remove connection total time
     *
     * @return
     */
    public Timer getUserRemoveConnection() {
        return userRemoveConnection;
    }

    /**
     * Add connection after connect logout time
     *
     * @return
     */
    public Timer getLogoutOnLogin() {
        return logoutOnLogin;
    }

    /**
     * Add connection admin total time
     *
     * @return
     */
    public Timer getAdminAddConnection() {
        return adminAddConnection;
    }

    /**
     * Raise lifecycle event time (could be on connect or disconnect event)
     *
     * @return
     */
    public Timer getRaiseLifecycleEvent() {
        return raiseLifecycleEvent;
    }


}
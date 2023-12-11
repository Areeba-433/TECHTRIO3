/*******************************************************************************
 * Copyright (c) 2018, 2022 Eurotech and/or its affiliates and others
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
package org.eclipse.kapua.consumer.lifecycle.listener;

import com.google.common.base.MoreObjects;
import org.apache.camel.spi.UriEndpoint;
import org.eclipse.kapua.KapuaException;
import org.eclipse.kapua.consumer.lifecycle.MetricsLifecycle;
import org.eclipse.kapua.locator.KapuaLocator;
import org.eclipse.kapua.service.camel.message.CamelKapuaMessage;
import org.eclipse.kapua.service.device.management.job.manager.JobDeviceManagementOperationManagerService;
import org.eclipse.kapua.service.device.management.message.notification.KapuaNotifyChannel;
import org.eclipse.kapua.service.device.management.message.notification.KapuaNotifyMessage;
import org.eclipse.kapua.service.device.management.message.notification.KapuaNotifyPayload;
import org.eclipse.kapua.service.device.management.registry.manager.DeviceManagementRegistryManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link DeviceManagementNotificationMessageProcessor}
 *
 * @since 1.0.0
 */
@UriEndpoint(title = "Device management notification storage message processor", syntax = "bean:deviceManagementNotificationMessageProcessor", scheme = "bean")
public class DeviceManagementNotificationMessageProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(DeviceManagementNotificationMessageProcessor.class);

    private static final DeviceManagementRegistryManagerService DEVICE_MANAGEMENT_REGISTRY_MANAGER_SERVICE = KapuaLocator.getInstance().getService(DeviceManagementRegistryManagerService.class);
    private static final JobDeviceManagementOperationManagerService JOB_DEVICE_MANAGEMENT_OPERATION_MANAGER_SERVICE = KapuaLocator.getInstance().getService(JobDeviceManagementOperationManagerService.class);

    //TODO inject!!!
    private MetricsLifecycle metrics;

    public DeviceManagementNotificationMessageProcessor() {
        metrics = MetricsLifecycle.getInstance();
    }

    /**
     * Process a device management {@link KapuaNotifyMessage}.
     *
     * @throws KapuaException
     */
    public void processMessage(CamelKapuaMessage<?> message) throws KapuaException {
        LOG.debug("Received notification message from device channel: client id '{}' - {}", message.getMessage().getClientId(), message.getMessage().getChannel());

        KapuaNotifyMessage notifyMessage = (KapuaNotifyMessage) message.getMessage();
        KapuaNotifyPayload notifyPayload = notifyMessage.getPayload();
        KapuaNotifyChannel notifyChannel = notifyMessage.getChannel();

        try {
            DEVICE_MANAGEMENT_REGISTRY_MANAGER_SERVICE.processOperationNotification(
                    notifyMessage.getScopeId(),
                    notifyPayload.getOperationId(),
                    MoreObjects.firstNonNull(notifyMessage.getSentOn(), notifyMessage.getReceivedOn()),
                    notifyPayload.getResource() != null ? notifyPayload.getResource() : notifyChannel.getResources()[0],
                    notifyPayload.getStatus(),
                    notifyPayload.getProgress(),
                    notifyPayload.getMessage());

            //TODO EXT-CAMEL only for test remove when jobs will be defined in their own container
            JOB_DEVICE_MANAGEMENT_OPERATION_MANAGER_SERVICE.processJobTargetOnNotification(
                    notifyMessage.getScopeId(),
                    notifyPayload.getOperationId(),
                    MoreObjects.firstNonNull(notifyMessage.getSentOn(), notifyMessage.getReceivedOn()),
                    notifyPayload.getResource() != null ? notifyPayload.getResource() : notifyChannel.getResources()[0],
                    notifyPayload.getStatus());
        } catch (Exception e) {
            LOG.error("Error while processing Device Management Operation Notification message!", e);
            throw e;
        }
    }

}

/*******************************************************************************
 * Copyright (c) 2016, 2019 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Eurotech - initial API and implementation
 *      Red Hat Inc
 *******************************************************************************/
package org.eclipse.kapua.service.device.call.kura;

import org.eclipse.kapua.KapuaException;
import org.eclipse.kapua.commons.security.KapuaSecurityUtils;
import org.eclipse.kapua.locator.KapuaLocator;
import org.eclipse.kapua.message.Message;
import org.eclipse.kapua.service.account.Account;
import org.eclipse.kapua.service.account.AccountService;
import org.eclipse.kapua.service.device.call.DeviceCall;
import org.eclipse.kapua.service.device.call.exception.DeviceCallSendException;
import org.eclipse.kapua.service.device.call.exception.DeviceCallTimeoutException;
import org.eclipse.kapua.service.device.call.kura.exception.KuraMqttDeviceCallErrorCodes;
import org.eclipse.kapua.service.device.call.kura.exception.KuraMqttDeviceCallException;
import org.eclipse.kapua.service.device.call.message.kura.KuraMessage;
import org.eclipse.kapua.service.device.call.message.kura.app.request.KuraRequestChannel;
import org.eclipse.kapua.service.device.call.message.kura.app.request.KuraRequestMessage;
import org.eclipse.kapua.service.device.call.message.kura.app.request.KuraRequestPayload;
import org.eclipse.kapua.service.device.call.message.kura.app.response.KuraResponseMessage;
import org.eclipse.kapua.service.device.registry.Device;
import org.eclipse.kapua.service.device.registry.DeviceRegistryService;
import org.eclipse.kapua.translator.Translator;
import org.eclipse.kapua.translator.exception.TranslatorNotFoundException;
import org.eclipse.kapua.transport.TransportClientFactory;
import org.eclipse.kapua.transport.TransportFacade;
import org.eclipse.kapua.transport.exception.TransportTimeoutException;
import org.eclipse.kapua.transport.message.TransportMessage;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * {@link DeviceCall} {@link Kura} implementation.
 *
 * @since 1.0.0
 */
public class KuraDeviceCallImpl implements DeviceCall<KuraRequestMessage, KuraResponseMessage> {

    private static final KapuaLocator LOCATOR = KapuaLocator.getInstance();

    private static final AccountService ACCOUNT_SERVICE = LOCATOR.getService(AccountService.class);

    private static final DeviceRegistryService DEVICE_REGISTRY_SERVICE = LOCATOR.getService(DeviceRegistryService.class);

    private static final TransportClientFactory TRANSPORT_CLIENT_FACTORY = LOCATOR.getFactory(TransportClientFactory.class);


    @Override
    public KuraResponseMessage create(@NotNull KuraRequestMessage requestMessage, @Nullable Long timeout)
            throws DeviceCallTimeoutException, DeviceCallSendException {
        return sendInternal(requestMessage, timeout);
    }

    @Override
    public KuraResponseMessage read(@NotNull KuraRequestMessage requestMessage, @Nullable Long timeout)
            throws DeviceCallTimeoutException, DeviceCallSendException {
        return sendInternal(requestMessage, timeout);
    }

    @Override
    public KuraResponseMessage options(@NotNull KuraRequestMessage requestMessage, @Nullable Long timeout)
            throws DeviceCallTimeoutException, DeviceCallSendException {
        return sendInternal(requestMessage, timeout);
    }

    @Override
    public KuraResponseMessage delete(@NotNull KuraRequestMessage requestMessage, @Nullable Long timeout)
            throws DeviceCallTimeoutException, DeviceCallSendException {
        return sendInternal(requestMessage, timeout);
    }

    @Override
    public KuraResponseMessage execute(@NotNull KuraRequestMessage requestMessage, @Nullable Long timeout)
            throws DeviceCallTimeoutException, DeviceCallSendException {
        return sendInternal(requestMessage, timeout);
    }

    @Override
    public KuraResponseMessage write(@NotNull KuraRequestMessage requestMessage, @Nullable Long timeout)
            throws DeviceCallTimeoutException, DeviceCallSendException {
        return sendInternal(requestMessage, timeout);
    }

    @Override
    public Class<KuraMessage> getBaseMessageClass() {
        return KuraMessage.class;
    }

    //
    // Private methods
    //

    /**
     * Sends the {@link KuraRequestMessage} and waits for the response if the {@code timeout} is given.
     *
     * @param requestMessage The {@link KuraRequestMessage} to send.
     * @param timeout        The timeout of waiting the {@link KuraResponseMessage}.
     * @return The {@link KuraResponseMessage} received.
     * @throws DeviceCallTimeoutException if waiting of the response goes on timeout.
     * @throws DeviceCallSendException    if sending the request produces any error.
     * @since 1.0.0
     */
    private KuraResponseMessage sendInternal(@NotNull KuraRequestMessage requestMessage, @Nullable Long timeout) throws DeviceCallTimeoutException, DeviceCallSendException {

        KuraResponseMessage response;
        TransportFacade transportFacade = null;
        try {
            Account account = KapuaSecurityUtils.doPrivileged(() -> ACCOUNT_SERVICE.findByName(requestMessage.getChannel().getScope()));
            Device device = DEVICE_REGISTRY_SERVICE.findByClientId(account.getId(), requestMessage.getChannel().getClientId());
            String serverIp = device.getConnection().getServerIp();

            //
            // Borrow a KapuaClient
            transportFacade = borrowClient(serverIp);

            //
            // Get Kura to transport translator for the request and vice versa
            Translator<KuraRequestMessage, TransportMessage> translatorKuraTransport = getTranslator(KuraRequestMessage.class, transportFacade.getMessageClass());
            Translator<TransportMessage, KuraResponseMessage> translatorTransportKura = getTranslator(transportFacade.getMessageClass(), KuraResponseMessage.class);

            //
            // Make the request
            // Add requestId and requesterClientId to both payload and channel if response is expected
            // Note: Adding to both payload and channel to let the translator choose what to do base on the transport used.
            KuraRequestChannel requestChannel = requestMessage.getChannel();
            KuraRequestPayload requestPayload = requestMessage.getPayload();
            if (timeout != null) {
                // FIXME: create an utilty class to use the same synchronized random instance to avoid duplicates
                Random r = new Random();
                String requestId = String.valueOf(r.nextLong());

                requestChannel.setRequestId(requestId);
                requestChannel.setRequesterClientId(transportFacade.getClientId());

                requestPayload.setRequestId(requestId);
                requestPayload.setRequesterClientId(transportFacade.getClientId());
            }

            //
            // Do send
            // Set current timestamp
            requestMessage.setTimestamp(new Date());

            // Send
            TransportMessage transportRequestMessage = translatorKuraTransport.translate(requestMessage);
            TransportMessage transportResponseMessage = transportFacade.sendSync(transportRequestMessage, timeout);

            // Translate response
            response = translatorTransportKura.translate(transportResponseMessage);

        } catch (TransportTimeoutException te) {
            throw new DeviceCallTimeoutException(te, timeout);
        } catch (KapuaException se) {
            throw new DeviceCallSendException(se, requestMessage);
        } finally {
            if (transportFacade != null) {
                transportFacade.clean();
            }
        }

        return response;
    }

    /**
     * Picks a {@link TransportFacade} to send the {@link KuraResponseMessage}.
     *
     * @param serverIp
     * @return The {@link TransportFacade} to use to send the {@link KuraResponseMessage}.
     * @throws KuraMqttDeviceCallException If getting the {@link TransportFacade} causes an {@link Exception}.
     * @since 1.0.0
     */
    private TransportFacade borrowClient(String serverIp) throws KuraMqttDeviceCallException {
        TransportFacade transportFacade;
        Map<String, Object> configParameters = new HashMap<>();
        configParameters.put("serverAddress", serverIp);
        try {
            transportFacade = TRANSPORT_CLIENT_FACTORY.getFacade(configParameters);
        } catch (Exception e) {
            throw new KuraMqttDeviceCallException(KuraMqttDeviceCallErrorCodes.CALL_ERROR, e);
        }
        return transportFacade;
    }

    /**
     * Gets the translator for the given {@link Message} types.
     *
     * @param from The {@link Message} type from which to translate.
     * @param to   The {@link Message} type to which to translate.
     * @param <F>  The {@link Message} {@code class}from which to translate.
     * @param <T>  The {@link Message} {@code class} to which to translate.
     * @return The {@link Translator} found.
     * @throws KuraMqttDeviceCallException If error occurs while searching the {@link Translator}.
     * @since 1.0.0
     */
    private <F extends Message, T extends Message> Translator getTranslator(Class<F> from, Class<T> to) throws KuraMqttDeviceCallException {
        Translator<F, T> translator;
        try {
            translator = Translator.getTranslatorFor(from, to);
        } catch (TranslatorNotFoundException e) {
            throw new KuraMqttDeviceCallException(KuraMqttDeviceCallErrorCodes.CALL_ERROR, e);
        }
        return translator;
    }
}

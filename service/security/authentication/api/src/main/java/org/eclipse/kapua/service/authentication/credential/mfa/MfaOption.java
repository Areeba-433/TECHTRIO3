/*******************************************************************************
 * Copyright (c) 2020 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *******************************************************************************/
package org.eclipse.kapua.service.authentication.credential.mfa;

import org.eclipse.kapua.model.KapuaUpdatableEntity;
import org.eclipse.kapua.model.id.KapuaId;
import org.eclipse.kapua.model.id.KapuaIdAdapter;
import org.eclipse.kapua.model.xml.DateXmlAdapter;
import org.eclipse.kapua.service.user.User;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Date;

/**
 * {@link MfaOption} definition.<br>
 * Used to handle {@link MfaOption} needed by the various authentication algorithms.
 */
@XmlRootElement(name = "mfaOption")
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(propOrder = {
        "userId",
        "mfaSecretKey",
        "trustKey",
        "trustExpirationDate"}, //
        factoryClass = MfaOptionXmlRegistry.class, //
        factoryMethod = "newMfaOption") //
public interface MfaOption extends KapuaUpdatableEntity {

    String TYPE = "mfaOption";

    @Override
    default String getType() {
        return TYPE;
    }

    /**
     * Return the user identifier
     *
     * @return The user identifier.
     */
    @XmlElement(name = "userId")
    @XmlJavaTypeAdapter(KapuaIdAdapter.class)
    KapuaId getUserId();

    /**
     * Sets the {@link User} id of this {@link MfaOption}
     *
     * @param userId The {@link User} id to set.
     */
    void setUserId(KapuaId userId);

    /**
     * Return the {@link MfaOption} key
     *
     * @return
     */
    @XmlElement(name = "mfaSecretKey")
    String getMfaSecretKey();

    /**
     * Sets the {@link MfaOption} key
     *
     * @param mfaSecretKey
     */
    void setMfaSecretKey(String mfaSecretKey);

    /**
     * Return the trust key
     *
     * @return
     */
    @XmlElement(name = "trustKey")
    String getTrustKey();

    /**
     * Sets the trust key
     *
     * @param trustKey
     */
    void setTrustKey(String trustKey);

    /**
     * Gets the current trust key expiration date
     *
     * @return the current trust key expiration date
     */
    @XmlElement(name = "trustExpirationDate")
    @XmlJavaTypeAdapter(DateXmlAdapter.class)
    Date getTrustExpirationDate();

    /**
     * Sets the current trust expiration date
     *
     * @param trustExpirationDate the current trust expiration date
     */
    void setTrustExpirationDate(Date trustExpirationDate);

    /**
     * Gets the Mfa secret key in the form of a base64 QR code image
     *
     * @return the QR code image in base64
     */
    @XmlElement(name = "qrCodeImage")
    String getQRCodeImage();

    /**
     * Sets the Mfa QR code image
     *
     * @param qrCodeImage the QR code image in base64
     */
    void setQRCodeImage(String qrCodeImage);
}

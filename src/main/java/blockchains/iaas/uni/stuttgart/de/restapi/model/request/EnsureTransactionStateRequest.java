/********************************************************************************
 * Copyright (c) 2018-2024 Institute for the Architecture of Application System -
 * University of Stuttgart
 * Author: Ghareeb Falazi
 *
 * This program and the accompanying materials are made available under the
 * terms the Apache Software License 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/

package blockchains.iaas.uni.stuttgart.de.restapi.model.request;

import jakarta.xml.bind.annotation.*;
import lombok.Setter;


@Setter
@XmlRootElement(name = "EnsureTransactionStateRequest")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class EnsureTransactionStateRequest {

    private String blockchainId;
    private String subscriptionId;
    private String epUrl;
    private String txId;
    private double requiredConfidence;


    @XmlElement(name="EndPointUrl")
    public String getEpUrl() {
        return epUrl;
    }

    @XmlElement(name="TransactionId")
    public String getTxId() {
        return txId;
    }

    @XmlElement(name="BlockchainId")
    public String getBlockchainId() {
        return blockchainId;
    }

    @XmlElement(name="SubscriptionId")
    public String getSubscriptionId() {
        return subscriptionId;
    }

    @XmlElement(name="RequiredConfidence")
    public double getRequiredConfidence() {
        return requiredConfidence;
    }

}

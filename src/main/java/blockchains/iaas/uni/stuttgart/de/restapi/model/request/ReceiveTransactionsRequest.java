/********************************************************************************
 * Copyright (c) 2019-2024 Institute for the Architecture of Application System -
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
@XmlRootElement(name = "ReceiveTransactionsRequest")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class ReceiveTransactionsRequest {

    private String blockchainId;
    private String subscriptionId;
    private double requiredConfidence;
    private String epUrl;
    private String from;

    @XmlElement(name = "EndPointUrl")
    public String getEpUrl() {
        return epUrl;
    }

    @XmlElement(name = "From")
    public String getFrom() {
        return from;
    }

    @XmlElement(name = "BlockchainId")
    public String getBlockchainId() {
        return blockchainId;
    }

    @XmlElement(name = "SubscriptionId")
    public String getSubscriptionId() {
        return subscriptionId;
    }

    @XmlElement(name = "RequiredConfidence")
    public double getRequiredConfidence() {
        return requiredConfidence;
    }

}

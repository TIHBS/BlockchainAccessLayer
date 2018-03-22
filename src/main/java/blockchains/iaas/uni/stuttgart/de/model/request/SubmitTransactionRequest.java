package blockchains.iaas.uni.stuttgart.de.model.request;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.math.BigInteger;

/********************************************************************************
 * Copyright (c) 2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache Software License 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 ********************************************************************************/
@XmlRootElement(name = "SubmitTransactionRequest")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class SubmitTransactionRequest {

    private String to;

    private String valueAsString;

    private String blockchainId;

    private String subscriptionId;

    private long waitFor;

    private long timeout;

    private String epUrl;

    @XmlElement(name="EndPointUrl")
    public String getEpUrl() {
        return epUrl;
    }

    public void setEpUrl(String epUrl) {
        this.epUrl = epUrl;
    }

    @XmlElement(name="To")
    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    @XmlElement(name="BlockchainId")
    public String getBlockchainId() {
        return blockchainId;
    }

    public void setBlockchainId(String blockchainId) {
        this.blockchainId = blockchainId;
    }

    @XmlElement(name="SubscriptionId")
    public String getSubscriptionId() {
        return subscriptionId;
    }


    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    @XmlElement(name="WaitFor")
    public long getWaitFor() {
        return waitFor;
    }


    public void setWaitFor(long waitFor) {
        this.waitFor = waitFor;
    }

    @XmlElement(name="Timeout")
    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    @XmlElement(name="Value")
    public BigInteger getValue(){
        return new BigInteger(valueAsString);
    }

    public void setValue(BigInteger value){
        valueAsString = value.toString();
    }


    public String getValueAsString() {
        return valueAsString;
    }

    public void setValueAsString(String value) {
        this.valueAsString = value;
    }


}

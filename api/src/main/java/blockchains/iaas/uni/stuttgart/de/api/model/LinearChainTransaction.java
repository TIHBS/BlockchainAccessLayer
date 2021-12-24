package blockchains.iaas.uni.stuttgart.de.api.model;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.math.BigInteger;
import java.util.List;

/********************************************************************************
 * Copyright (c) 2018 Institute for the Architecture of Application System -
 * University of Stuttgart
 * Author: Ghareeb Falazi
 *
 * This program and the accompanying materials are made available under the
 * terms the Apache Software License 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/

@XmlRootElement(name="Transaction")
@XmlAccessorType(XmlAccessType.NONE)
public class LinearChainTransaction extends Transaction {

    @XmlElement(name="TransactionHash")
    private String transactionHash;

    @XmlElement
    private Block block;

    @XmlElement(name="From")
    private String from;

    @XmlElement(name="To")
    private String to;

    private BigInteger value;

    public LinearChainTransaction() {
        super();
    }

    public LinearChainTransaction(String transactionHash,
                                  Block block,
                                  String from, String to, BigInteger value, TransactionState state, List<Parameter> returnValues) {
        this.setReturnValues(returnValues);
        this.setState(state);
        this.transactionHash = transactionHash;
        this.block = block;
        this.from = from;
        this.to = to;
        this.value = value;
    }

    public String getTransactionHash() {
        return transactionHash;
    }

    public void setTransactionHash(String transactionHash) {
        this.transactionHash = transactionHash;
    }

    public Block getBlock() {
        return block;
    }

    public void setBlock(Block block) {
        this.block = block;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public BigInteger getValue() {
        return value;
    }

    public void setValue(BigInteger value) {
        this.value = value;
    }

    @XmlElement(name="Value")
    public String getValueAsString(){
        return value.toString();
    }

    public void setValueAsString(String value){
        this.value = new BigInteger(value);
    }
}

package blockchains.iaas.uni.stuttgart.de.restapi.model.response;

import blockchains.iaas.uni.stuttgart.de.model.Transaction;
import blockchains.iaas.uni.stuttgart.de.model.TransactionState;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

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
@XmlRootElement(name="TransactionCorrelatedResponse")
@XmlAccessorType(XmlAccessType.FIELD)
public class TransactionCorrelatedResponse extends CorrelatedResponse {
    @XmlElement(name="State")
    private TransactionState state;

    @XmlElement(name="Transaction")
    private Transaction transaction;

    public TransactionCorrelatedResponse(){

    }

    private TransactionCorrelatedResponse(final String correlationId, final TransactionState state, final Transaction transaction){
        super(correlationId);
        this.state = state;
        this.transaction = transaction;
    }

    public TransactionCorrelatedResponse(final String correlationId, final TransactionState state){
        this(correlationId, state, null);
    }

    /**
     * Transaction state is determined using the passed transaction object
     * @param correlationId the correlation id
     * @param transaction the transaction
     */
    public TransactionCorrelatedResponse(final String correlationId, final Transaction transaction){
        this(correlationId, transaction.getState(), transaction);
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public TransactionState getState() {
        return state;
    }

    public void setState(TransactionState state) {
        this.state = state;
    }

}

package blockchains.iaas.uni.stuttgart.de.management.callback;

import blockchains.iaas.uni.stuttgart.de.model.Transaction;
import blockchains.iaas.uni.stuttgart.de.model.TransactionState;
import blockchains.iaas.uni.stuttgart.de.restapi.model.response.CallbackMessage;

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
public abstract class MessageTranslator {
    public abstract CallbackMessage convert(String subscriptionId, Transaction transaction, TransactionState state, boolean isErrorMessage, int errorCode);

    public CallbackMessage convert(String subscriptionId, Transaction transaction, boolean isErrorMessage) {
        return this.convert(subscriptionId, transaction, transaction.getState(), isErrorMessage, 0);
    }

    public CallbackMessage convert(String subscriptionId, TransactionState state, boolean isErrorMessage, int errorCode) {
        return this.convert(subscriptionId, null, state, isErrorMessage, errorCode);
    }
}

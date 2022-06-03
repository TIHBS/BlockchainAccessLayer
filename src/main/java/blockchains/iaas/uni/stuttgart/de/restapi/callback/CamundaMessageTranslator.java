/*******************************************************************************
 * Copyright (c) 2022 Institute for the Architecture of Application System - University of Stuttgart
 * Author: Ghareeb Falazi
 *
 * This program and the accompanying materials are made available under the
 * terms the Apache Software License 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0
 *******************************************************************************/
package blockchains.iaas.uni.stuttgart.de.restapi.callback;

import java.util.Map;

import blockchains.iaas.uni.stuttgart.de.model.LinearChainTransaction;
import blockchains.iaas.uni.stuttgart.de.model.Transaction;
import blockchains.iaas.uni.stuttgart.de.model.TransactionState;
import blockchains.iaas.uni.stuttgart.de.restapi.model.response.CallbackMessage;
import blockchains.iaas.uni.stuttgart.de.restapi.model.response.CamundaMessage;
import blockchains.iaas.uni.stuttgart.de.restapi.model.response.CamundaVariable;

public class CamundaMessageTranslator {
    public static CallbackMessage convert(String subscriptionId, Transaction transaction, TransactionState state, boolean isErrorMessage, int errorCode) {
        final CamundaMessage result = new CamundaMessage();
        final String processInstanceId = subscriptionId.substring(subscriptionId.indexOf('_') + 1);
        final String msgName = (isErrorMessage) ? "error_" : "message_" + subscriptionId;
        result.setMessageName(msgName);
        result.setProcessInstanceId(processInstanceId);
        Map<String, CamundaVariable> variables = result.getProcessVariables();
        variables.put("status", new CamundaVariable(state.toString(), "String"));

        // todo handle communication between Camunda and Fabric
        if (transaction instanceof LinearChainTransaction) {
            LinearChainTransaction tx = (LinearChainTransaction) transaction;

            if (state != TransactionState.RETURN_VALUE) {
                variables.put("from", new CamundaVariable(tx.getFrom(), "String"));
                variables.put("to", new CamundaVariable(tx.getTo(), "String"));
                variables.put("value", new CamundaVariable(tx.getValueAsString(), "Long"));
                variables.put("transactionId", new CamundaVariable(tx.getTransactionHash(), "String"));
            } else {
                for (int i = 0; i < transaction.getReturnValues().size(); i++) {
                    variables.put("returnValue_" + i, new CamundaVariable(transaction.getReturnValues().get(0).getValue(), "String"));
                }
            }

            if (tx.getBlock() != null) { //it could be null if we are accepting transactions with 0 confirmations
                variables.put("blockId", new CamundaVariable(tx.getBlock().getHash(), "String"));
                variables.put("blockNumber", new CamundaVariable(String.valueOf(tx.getBlock().getNumberAsLong()), "Long"));
            } else {
                variables.put("blockId", new CamundaVariable("", "String"));
                variables.put("blockNumber", new CamundaVariable("-1", "Long"));
            }
        }

        return result;
    }

    public static CallbackMessage convert(String subscriptionId, Transaction transaction, boolean isErrorMessage) {
        return convert(subscriptionId, transaction, transaction.getState(), isErrorMessage, 0);
    }

    public static CallbackMessage convert(String subscriptionId, TransactionState state, boolean isErrorMessage, int errorCode) {
        return convert(subscriptionId, null, state, isErrorMessage, errorCode);
    }
}

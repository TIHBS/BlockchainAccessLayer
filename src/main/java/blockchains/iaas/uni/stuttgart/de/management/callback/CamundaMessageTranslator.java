/********************************************************************************
 * Copyright (c) 2019 Institute for the Architecture of Application System -
 * University of Stuttgart
 * Author: Ghareeb Falazi
 *
 * This program and the accompanying materials are made available under the
 * terms the Apache Software License 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package blockchains.iaas.uni.stuttgart.de.management.callback;

import java.util.Map;

import blockchains.iaas.uni.stuttgart.de.model.Transaction;
import blockchains.iaas.uni.stuttgart.de.model.TransactionState;
import blockchains.iaas.uni.stuttgart.de.restapi.model.response.CallbackMessage;
import blockchains.iaas.uni.stuttgart.de.restapi.model.response.CamundaMessage;
import blockchains.iaas.uni.stuttgart.de.restapi.model.response.CamundaVariable;

public class CamundaMessageTranslator extends MessageTranslator {
    @Override
    public CallbackMessage convert(String subscriptionId, boolean isErrorMessage, Transaction transaction, TransactionState state) {
        final CamundaMessage result = new CamundaMessage();
        final String processInstanceId = subscriptionId.substring(subscriptionId.indexOf('_') + 1);
        final String msgName = (isErrorMessage) ? "error_" : "message_" + subscriptionId;
        result.setMessageName(msgName);
        result.setProcessInstanceId(processInstanceId);
        Map<String, CamundaVariable> variables = result.getProcessVariables();

        variables.put("status", new CamundaVariable(state.toString(), "String"));

        if (transaction != null) {
            if (state != TransactionState.RETURN_VALUE) {
                variables.put("from", new CamundaVariable(transaction.getFrom(), "String"));
                variables.put("to", new CamundaVariable(transaction.getTo(), "String"));
                variables.put("value", new CamundaVariable(transaction.getValueAsString(), "Long"));
                variables.put("transactionId", new CamundaVariable(transaction.getTransactionHash(), "String"));
            } else {
                variables.put("returnValue", new CamundaVariable(transaction.getReturnValue(), "String"));
            }

            if (transaction.getBlock() != null) { //it could be null if we are accepting transactions with 0 confirmations
                variables.put("blockId", new CamundaVariable(transaction.getBlock().getHash(), "String"));
                variables.put("blockNumber", new CamundaVariable(String.valueOf(transaction.getBlock().getNumberAsLong()), "Long"));
            } else {
                variables.put("blockId", new CamundaVariable("", "String"));
                variables.put("blockNumber", new CamundaVariable("-1", "Long"));
            }
        }

        return result;
    }
}

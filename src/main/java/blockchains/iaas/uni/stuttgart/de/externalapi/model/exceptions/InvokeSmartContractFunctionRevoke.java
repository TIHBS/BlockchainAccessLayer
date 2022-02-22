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

package blockchains.iaas.uni.stuttgart.de.externalapi.model.exceptions;

import com.github.arteam.simplejsonrpc.core.annotation.JsonRpcError;

@JsonRpcError(code = ExceptionCode.ExecutionError, message = "The execution of the smart contract function resulted in an error.")
public class InvokeSmartContractFunctionRevoke extends  BalException{
    public InvokeSmartContractFunctionRevoke() {
    }

    public InvokeSmartContractFunctionRevoke(String message) {
        super(message);
    }

    @Override
    public int getCode() {
        return ExceptionCode.ExecutionError;
    }
}

/*******************************************************************************
 * Copyright (c) 2019-2024 Institute for the Architecture of Application System - University of Stuttgart
 * Author: Ghareeb Falazi
 *
 * This program and the accompanying materials are made available under the
 * terms the Apache Software License 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0
 *******************************************************************************/
package blockchains.iaas.uni.stuttgart.de.restapi.Controllers;

import blockchains.iaas.uni.stuttgart.de.management.BlockchainManager;
import blockchains.iaas.uni.stuttgart.de.management.model.SubscriptionKey;
import blockchains.iaas.uni.stuttgart.de.management.model.SubscriptionType;
import blockchains.iaas.uni.stuttgart.de.restapi.model.request.InvokeSmartContractFunctionRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController()
@RequestMapping("invoke-smart-contract-function")
@Log4j2
public class InvokeSmartContractFunctionController extends SubscriptionController {

    public InvokeSmartContractFunctionController(BlockchainManager manager) {
        super(manager);
    }

    @GetMapping
    public Collection<SubscriptionKey> get() {
        return getSubscriptions(SubscriptionType.INVOKE_SMART_CONTRACT_FUNCTION);
    }

    // todo try if still working
    @PostMapping(consumes = MediaType.APPLICATION_XML_VALUE)
    public void invokeSCFunction(@RequestBody InvokeSmartContractFunctionRequest request) {
        log.info("Received an invokeSCFunction request via REST API");
        manager.invokeSmartContractFunction(
                request.getBlockchainId(),
                request.getSmartContractPath(),
                request.getFunctionIdentifier(),
                request.getInputs().getArguments(),
                request.getOutputs().getArguments(),
                request.getConfidence(),
                request.getEpUrl(),
                request.getTimeoutMillis(),
                request.getSubscriptionId(),
                request.getSignature()
        );
    }
}

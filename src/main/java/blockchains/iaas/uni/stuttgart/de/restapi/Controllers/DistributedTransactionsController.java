/********************************************************************************
 * Copyright (c) 2023-2024 Institute for the Architecture of Application System -
 * University of Stuttgart
 * 
 * Author: Ghareeb Falazi
 *
 * This program and the accompanying materials are made available under the
 * terms the Apache Software License 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/

package blockchains.iaas.uni.stuttgart.de.restapi.Controllers;

import blockchains.iaas.uni.stuttgart.de.management.model.DistributedTransaction;
import blockchains.iaas.uni.stuttgart.de.management.tccsci.DistributedTransactionRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController()
@RequestMapping("distributed-transactions")
@Log4j2
public class DistributedTransactionsController {

    @GetMapping()
    public List<DistributedTransaction> get() {
        return DistributedTransactionRepository.getInstance().getAll();
    }

    @GetMapping(path = "/{dtxId}")
    public ResponseEntity<DistributedTransaction> getSubscriptionDetails(@PathVariable("dtxId") final String dtxId) {
        UUID uuid = UUID.fromString(dtxId);
        DistributedTransaction dtx = DistributedTransactionRepository.getInstance().getById(uuid);
        if (dtx != null) {
            return ResponseEntity.ok(dtx);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}

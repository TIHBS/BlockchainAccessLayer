/********************************************************************************
 * Copyright (c) 2023 Institute for the Architecture of Application System -
 * University of Stuttgart
 * Author: Ghareeb Falazi
 *
 * This program and the accompanying materials are made available under the
 * terms the Apache Software License 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/

package blockchains.iaas.uni.stuttgart.de.tccsci;

import blockchains.iaas.uni.stuttgart.de.tccsci.model.DistributedTransaction;
import blockchains.iaas.uni.stuttgart.de.tccsci.model.DistributedTransactionState;
import blockchains.iaas.uni.stuttgart.de.tccsci.model.DistributedTransactionVerdict;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class DistributedTransactionRepository {
    private final List<DistributedTransaction> distributedTransactions = new ArrayList<>();
    private static DistributedTransactionRepository instance;

    private DistributedTransactionRepository() {

    }

    public static DistributedTransactionRepository getInstance() {
        if(instance == null) {
            instance = new DistributedTransactionRepository();
        }

        return instance;
    }

    public void addDistributedTransaction(DistributedTransaction tx) {
        if(getById(tx.getId()) == null) {
            this.distributedTransactions.add(tx);
        } else {
            throw new IllegalArgumentException("A distributed transaction with the same id " + tx.getId() + "already exists!");
        }
    }

    public DistributedTransaction getById(UUID txId) {
        return distributedTransactions.stream().filter(tx->tx.getId().equals(txId)).findFirst().orElse(null);
    }

    public List<DistributedTransaction> getAll() {
        return this.distributedTransactions;
    }

    public Collection<DistributedTransaction> getByState(DistributedTransactionState state) {
        return distributedTransactions.stream().filter(tx->tx.getState().equals(state)).collect(Collectors.toList());
    }

    public Collection<DistributedTransaction> getByVerdict(DistributedTransactionVerdict verdict) {
        return distributedTransactions.stream().filter(tx->tx.getVerdict().equals(verdict)).collect(Collectors.toList());
    }

    public Collection<DistributedTransaction> getByBlockchainId(String bcId) {
        return distributedTransactions.stream().filter(tx->tx.getBlockchainIds().contains(bcId)).collect(Collectors.toList());
    }


}

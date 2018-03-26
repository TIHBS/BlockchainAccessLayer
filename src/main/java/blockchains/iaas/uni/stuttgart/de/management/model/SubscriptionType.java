package blockchains.iaas.uni.stuttgart.de.management.model;

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
public enum SubscriptionType {
    SUBMIT_TRANSACTION,
    RECEIVE_TRANSACTION,
    RECEIVE_TRANSACTIONS,
    DETECT_ORPHANED_TRANSACTION,
    ENSURE_TRANSACTION_STATE
}

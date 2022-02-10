/********************************************************************************
 * Copyright (c) 2019-2022 Institute for the Architecture of Application System -
 * University of Stuttgart
 * Author: Ghareeb Falazi
 *
 * This program and the accompanying materials are made available under the
 * terms the Apache Software License 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package blockchains.iaas.uni.stuttgart.de.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "State")
public enum TransactionState {
    UNKNOWN,
    PENDING,
    CONFIRMED,
    NOT_FOUND,
    INVALID,
    // When a smart contract function fails although a transaction is recorded (relevant for Ethereum-like BCs)
    ERRORED,
    // indicates that a successful read-only function invocation occurred
    RETURN_VALUE
}

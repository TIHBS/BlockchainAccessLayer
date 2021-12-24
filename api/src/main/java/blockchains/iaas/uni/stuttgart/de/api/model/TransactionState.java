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
package blockchains.iaas.uni.stuttgart.de.api.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "State")
public enum TransactionState {
    UNKNOWN,
    PENDING,
    CONFIRMED,
    NOT_FOUND,
    INVALID,
    // transactions which are only place holders for read-only function invocation result
    RETURN_VALUE
}

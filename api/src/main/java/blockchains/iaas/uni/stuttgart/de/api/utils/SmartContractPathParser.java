/*******************************************************************************
 * Copyright (c) 2019 Institute for the Architecture of Application System - University of Stuttgart
 * Author: Ghareeb Falazi
 *
 * This program and the accompanying materials are made available under the
 * terms the Apache Software License 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0
 *******************************************************************************/
package blockchains.iaas.uni.stuttgart.de.api.utils;

import lombok.Data;

@Data
public class SmartContractPathParser {
    private final String path;
    private String[] smartContractPathSegments;

    private SmartContractPathParser(String path) {
        this.path = path;
    }

    private void parse() {
        this.smartContractPathSegments = path.split("/");
    }

    public static SmartContractPathParser parse(String smartContractPath) {
        final SmartContractPathParser result = new SmartContractPathParser(smartContractPath);
        result.parse();

        return result;
    }
}

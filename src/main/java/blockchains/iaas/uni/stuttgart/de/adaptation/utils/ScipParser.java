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
package blockchains.iaas.uni.stuttgart.de.adaptation.utils;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import blockchains.iaas.uni.stuttgart.de.model.SmartContractFunctionParameter;

public class ScipParser {
    private final String scip;
    private String blockchainId;
    private String functionName;
    private String returnType;
    private String[] functionPathSegments;
    private List<SmartContractFunctionParameter> parameterTypes;

    public String getBlockchainId() {
        return blockchainId;
    }

    public String getFunctionName() {
        return functionName;
    }

    public String[] getFunctionPathSegments() {
        return functionPathSegments;
    }

    public String getReturnType() {
        return returnType;
    }

    public List<SmartContractFunctionParameter> getParameterTypes() {
        return parameterTypes;
    }

    private ScipParser(String scip) {
        this.scip = scip;
    }

    private void parse() {
        URI uri = URI.create(scip);
        this.blockchainId = uri.getAuthority();
        this.parsePath(uri);
        this.parseQuery(uri);
    }

    private void parsePath(URI uri) {
        String[] segments = uri.getPath().split("/");
        this.functionName = segments[segments.length - 1];
        // path looks like: /blabla/functionName
        this.functionPathSegments = new String[segments.length - 2];
        System.arraycopy(segments, 1, functionPathSegments, 0, functionPathSegments.length);
    }

    private void parseQuery(URI uri) {
        String query = uri.getQuery();
        this.parameterTypes = new ArrayList<>();

        if (query != null && !query.isEmpty()) {
            String[] paramsReturn = query.split(":");

            if(paramsReturn.length != 2) {
                throw new IllegalArgumentException("query section should have the following syntax: " +
                        "?<param name-type pairs>:<returnType>");
            }

            this.returnType = paramsReturn[1];

            if(paramsReturn[0].length() > 0) {
                String[] segments = paramsReturn[0].split("&");

                for (String segment : segments) {
                    String[] parameter = segment.split("=");

                    if (parameter.length != 2) {
                        throw new IllegalArgumentException("The passed uri is not a valid scip. Reason: " +
                                "parameters are not well defined (" + query + ")");
                    }

                    this.parameterTypes.add(new SmartContractFunctionParameter(parameter[0], parameter[1]));
                }
            }
        } else {
            throw new IllegalArgumentException("The return type is not specified for the function!");
        }

    }

    public static ScipParser parse(String scip) {
        final ScipParser result = new ScipParser(scip);
        result.parse();

        return result;
    }
}

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

package blockchains.iaas.uni.stuttgart.de.scip.bindings;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages a map between endpoints and bindings. The goal is to allow clients to specify the binding to be
 * used when sending asynchronous responses to different endpoints.
 * Endpoints are specified using urls with possible wildcards (*).
 * For example: http://my-awsome-endpoint.com/*
 *
 * When sending an asynchronous response to some endpoint, this map is checked. If any entry (rule) is matched to
 * the endpoint the corresponding binding is used.
 * If no rules were matched, a default binding, i.e., "json-rpc" is used.
 */
public class EndpointBindings {
    private static EndpointBindings instance = null;
    private static final Logger log = LoggerFactory.getLogger(EndpointBindings.class);
    private static final String DEFAULT_BINDING_IDENTIFIER = "json-rpc";
    private Map<String, String> rules;

    private EndpointBindings() {
        rules = new HashMap<>();
    }

    public static EndpointBindings getInstance() {
        if (instance == null)
            instance = new EndpointBindings();

        return instance;
    }

    /**
     * Add a new endpoint binding rule.
     *
     * @param url               the regex to match the endpoints
     * @param bindingIdentifier the identifier of the binding to match to the provided endpoint
     */
    public void addRule(String url, String bindingIdentifier) {
        if (BindingsManager.getInstance().getAvailableBindingIdentifiers().contains(bindingIdentifier)) {
            rules.put(url, bindingIdentifier);
        } else {
            throw new IllegalArgumentException("The specified binding does not exist!");
        }
    }

    public AbstractBinding getBindingForEndpoint(String endpointUrl) {
        final String bindingId = getBindingIdentifierForEndpoint(endpointUrl);

        return BindingsManager.getInstance().getBinding(bindingId);
    }

    /**
     * FOR INTERNAL USE AND TESTING!
     *
     * @param endpointUrl an endpoint url with potential wildcards, i.e., *.
     * @return the binding identifier associated with this url, or the default binding identifier.
     */
    protected String getBindingIdentifierForEndpoint(String endpointUrl) {
        String binding = DEFAULT_BINDING_IDENTIFIER; ;

        for(String key: rules.keySet()) {
            String url = preprocess(key);

            if (endpointUrl.matches(url)) {
                binding = rules.get(key);
                break;
            }
        }

        return binding;
    }

    /**
     * ONLY FOR TESTING!
     * Please do not use this method for any production code.
     */
    protected void resetRules() {
        rules.clear();
    }

    /**
     * FOR INTERNAL USE AND TESTING!
     * @param url a url in clear text with possible wildcards, i.e., *.
     * @return a regex that escapes special characters and properly handles the wildcard
     */
    protected String preprocess(String url) {
        return url
                .replaceAll("\\*", "\\\\S*")
                .replaceAll("\\+", "\\\\+")
                .replaceAll("\\.", "\\\\.")
                .replaceAll("\\?", "\\\\?");
    }
}

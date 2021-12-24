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

import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import blockchains.iaas.uni.stuttgart.de.api.model.Parameter;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BooleanExpressionEvaluator {
    private static final Logger log = LoggerFactory.getLogger(BooleanExpressionEvaluator.class);

    public static boolean evaluate(String expression, List<Parameter> parameters) throws Exception {
        if (Strings.isNullOrEmpty(expression)) {
            return true;
        }
        
        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine jsEngine = mgr.getEngineByName("JavaScript");

        for (Parameter param : parameters) {
            jsEngine.put(param.getName(), JsonSchemaToJavaTypeMapper.map(param));
        }

        log.info("Executing in script environment...");

        try {
            Object result = jsEngine.eval(expression);
            if (result instanceof Boolean) {
                return (Boolean) result;
            } else {
                throw new RuntimeException(String.format("The expression evaluated to type: %s, but a boolean value was expected!", result.getClass().getName()));
            }
        } catch (ScriptException ex) {
            log.error("Failed to execute boolean expression {}. Reason: {}.", expression, ex.getMessage());
            throw ex;
        }
    }
}

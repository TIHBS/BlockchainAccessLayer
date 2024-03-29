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

package blockchains.iaas.uni.stuttgart.de.restapi.model.request;

import java.io.StringWriter;
import java.util.ArrayList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import blockchains.iaas.uni.stuttgart.de.api.model.Parameter;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


class InvokeSmartContractFunctionRequestTest {
    private static final Logger log = LoggerFactory.getLogger(InvokeSmartContractFunctionRequestTest.class);

    @Test
    void serializeObject() throws JAXBException {
        InvokeSmartContractFunctionRequest theObject = new InvokeSmartContractFunctionRequest();
        theObject.setConfidence(99.0);
        theObject.setEpUrl("http://localhost/test");
        theObject.setSmartContractPath("mysc");
        theObject.setSignature("!!!!!");
        theObject.setTimeoutMillis(120021);
        theObject.setFunctionIdentifier("invokeMePlease");
        theObject.setBlockchainId("eth-0");
        theObject.setSubscriptionId("1200");
        ParameterList myList = new ParameterList();
        myList.setArguments(new ArrayList<>());
        myList.getArguments().add(new Parameter("aaa", "type", "aaaaa"));
        theObject.setInputs(myList);
        theObject.setOutputs(myList);

        //Create JAXB Context
        JAXBContext jaxbContext = JAXBContext.newInstance(InvokeSmartContractFunctionRequest.class);

        //Create Marshaller
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

        //Required formatting??
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

        //Print XML String to Console
        StringWriter sw = new StringWriter();

        //Write XML to StringWriter
        jaxbMarshaller.marshal(theObject, sw);

        //Verify XML Content
        String xmlContent = sw.toString();

        log.debug(xmlContent);
    }
}
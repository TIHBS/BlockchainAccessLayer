/*******************************************************************************
 * Copyright (c) 2023 Institute for the Architecture of Application System - University of Stuttgart
 * Author: Akshay Patel
 * This program and the accompanying materials are made available under the
 * terms the Apache Software License 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0
 *******************************************************************************/
package blockchains.iaas.uni.stuttgart.de.restapi.model.request;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

// todo rethink type
@XmlRootElement(name = "signatures")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class SignaturesList {
    private List<ImmutablePair<String, String>> signatures;

    @XmlElement(name = "signature")
    public List<ImmutablePair<String, String>> getSignatures() {
        return signatures;
    }

    public void setSignatures(List<ImmutablePair<String, String>> signatures) {
        this.signatures = signatures;
    }
}

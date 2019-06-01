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
package blockchains.iaas.uni.stuttgart.de.model;

import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Argument")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class SmartContractFunctionArgument {
    private String name;
    private String value;

    @XmlElement(name = "Name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlElement(name = "Value")
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public SmartContractFunctionArgument() {
    }

    public SmartContractFunctionArgument(String name, String value) {
        this.name = name;
        this.value = value;
    }

//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//        SmartContractFunctionArgument that = (SmartContractFunctionArgument) o;
//        return name.equals(that.name) &&
//                value.equals(that.value);
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(name, value);
//    }
}

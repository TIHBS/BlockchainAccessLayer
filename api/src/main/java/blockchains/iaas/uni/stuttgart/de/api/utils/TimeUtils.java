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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.google.common.base.Strings;

public class TimeUtils {

    public static LocalDateTime getTimestampObject(String isoTimestamp) {
        if (!Strings.isNullOrEmpty(isoTimestamp)) {
            return LocalDateTime.parse(isoTimestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }

        return null;
    }
}

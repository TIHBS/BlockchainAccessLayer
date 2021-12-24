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

package blockchains.iaas.uni.stuttgart.de.adaptation.adapters.bitcoin;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

public class BitcoinUtils {
    private static final long SATOSHIS_PER_BITCCOIN = 100000000L;
    private static final int LARGEST_SCALE = 8;

    public static BigDecimal satoshiToBitcoin(BigDecimal satoshis){
        return satoshis.divide(new BigDecimal(SATOSHIS_PER_BITCCOIN), LARGEST_SCALE, RoundingMode.UNNECESSARY);
    }

    public static BigDecimal bitcoinsToSatoshi(BigDecimal bitcoins){
        return bitcoins.multiply(new BigDecimal(SATOSHIS_PER_BITCCOIN));
    }

}

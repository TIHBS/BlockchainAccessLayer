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

package blockchains.iaas.uni.stuttgart.de.subscription;

import java.util.Collections;

import blockchains.iaas.uni.stuttgart.de.subscription.model.MonitorOccurrencesSubscription;
import blockchains.iaas.uni.stuttgart.de.subscription.model.SubscriptionType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SubscriptionManagerTest {
    @Test
    void testReplacingSubscription() {
        SubscriptionManager manager = SubscriptionManager.getInstance();
        manager.getAllSubscriptions().clear();
        manager.createSubscription(
                "abc",
                "cba",
                "a/a",
                new MonitorOccurrencesSubscription(null, SubscriptionType.EVENT_OCCURRENCES, "myId", Collections.emptyList()));
        assertEquals(1, manager.getAllSubscriptions().size());
        manager.createSubscription(
                "abc",
                "cba",
                "a/a",
                new MonitorOccurrencesSubscription(null, SubscriptionType.EVENT_OCCURRENCES, "myId", Collections.emptyList()));
        assertEquals(1, manager.getAllSubscriptions().size());
    }
}
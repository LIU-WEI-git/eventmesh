/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.eventmesh.connector.dledger.config;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;

public class DLedgerConfigurationTest {

    private static DLedgerConfiguration config;

    @BeforeClass
    public static void init() {
        config = DLedgerConfiguration.getInstance();
    }

    @Test
    public void getGroup() {
        assertEquals("default", config.getGroup());
    }

    @Test
    public void getPeers() {
        assertEquals("n0-localhost:20911;n1-localhost:20912;n2-localhost:20913", config.getPeers());
    }

    @Test
    public void getClientPoolSize() {
        assertEquals(8, config.getClientPoolSize());
    }
}
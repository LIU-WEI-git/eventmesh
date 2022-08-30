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

package org.apache.eventmesh.trace.skywalking.config;

import com.google.common.base.Preconditions;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.eventmesh.common.Constants;
import org.apache.eventmesh.trace.skywalking.common.SkywalkingConstants;

import java.io.*;
import java.net.URL;
import java.util.Properties;

/**
 * to load the properties form skywalking.properties
 */
@Slf4j
@UtilityClass
public class SkywalkingConfiguration {

    private static final String CONFIG_FILE = "skywalking.properties";
    private static final Properties properties = new Properties();

    private String eventMeshSkywalkingHost = "localhost";
    private int eventMeshSkywalkingGrpcPort = 11800;

    static {
        loadProperties();
        initializeConfig();
    }

    public static String getEventMeshSkywalkingHost() {
        return eventMeshSkywalkingHost;
    }

    public static int getEventMeshSkywalkingGrpcPort() {
        return eventMeshSkywalkingGrpcPort;
    }

    private void initializeConfig() {
        String eventMeshSkywalkingHostStr = properties.getProperty(SkywalkingConstants.KEY_SKYWALKING_HOST);
        Preconditions.checkState(StringUtils.isNotEmpty(eventMeshSkywalkingHostStr),
                String.format("%s error", SkywalkingConstants.KEY_SKYWALKING_HOST));
        eventMeshSkywalkingHost = StringUtils.deleteWhitespace(eventMeshSkywalkingHostStr);

        String eventMeshSkywalkingGrpcPortStr = properties.getProperty(SkywalkingConstants.KEY_SKYWALKING_GRPC_PORT);
        if (StringUtils.isNotEmpty(eventMeshSkywalkingGrpcPortStr)) {
            eventMeshSkywalkingGrpcPort = Integer.parseInt(StringUtils.deleteWhitespace(eventMeshSkywalkingGrpcPortStr));
        }
    }

    private void loadProperties() {
        URL resource = SkywalkingConfiguration.class.getClassLoader().getResource(CONFIG_FILE);
        if (resource != null) {
            try (InputStream inputStream = resource.openStream()) {
                if (inputStream.available() > 0) {
                    properties.load(new BufferedReader(new InputStreamReader(inputStream)));
                }
            } catch (IOException e) {
                throw new RuntimeException("Load skywalking.properties file from classpath error");
            }
        }
        // get from config home
        try {
            String configPath = Constants.EVENTMESH_CONF_HOME + File.separator + CONFIG_FILE;
            if (new File(configPath).exists()) {
                properties.load(new BufferedReader(new FileReader(configPath)));
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot load skywalking.properties file from conf");
        }
    }
}

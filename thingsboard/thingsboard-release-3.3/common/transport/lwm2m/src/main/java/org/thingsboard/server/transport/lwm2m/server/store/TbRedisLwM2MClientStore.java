/**
 * Copyright © 2016-2021 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thingsboard.server.transport.lwm2m.server.store;

import lombok.extern.slf4j.Slf4j;
import org.nustaq.serialization.FSTConfiguration;
import org.springframework.data.redis.connection.RedisClusterConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.thingsboard.server.transport.lwm2m.server.client.LwM2MClientState;
import org.thingsboard.server.transport.lwm2m.server.client.LwM2mClient;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
public class TbRedisLwM2MClientStore implements TbLwM2MClientStore {

    private static final String CLIENT_EP = "CLIENT#EP#";
    private final RedisConnectionFactory connectionFactory;
    private final FSTConfiguration serializer;

    public TbRedisLwM2MClientStore(RedisConnectionFactory redisConnectionFactory) {
        this.connectionFactory = redisConnectionFactory;
        this.serializer = FSTConfiguration.createDefaultConfiguration();
    }

    @Override
    public LwM2mClient get(String endpoint) {
        try (var connection = connectionFactory.getConnection()) {
            byte[] data = connection.get(getKey(endpoint));
            if (data == null) {
                return null;
            } else {
                return (LwM2mClient) serializer.asObject(data);
            }
        }
    }

    @Override
    public Set<LwM2mClient> getAll() {
        try (var connection = connectionFactory.getConnection()) {
            Set<LwM2mClient> clients = new HashSet<>();
            ScanOptions scanOptions = ScanOptions.scanOptions().count(100).match(CLIENT_EP + "*").build();
            List<Cursor<byte[]>> scans = new ArrayList<>();
            if (connection instanceof RedisClusterConnection) {
                ((RedisClusterConnection) connection).clusterGetNodes().forEach(node -> {
                    scans.add(((RedisClusterConnection) connection).scan(node, scanOptions));
                });
            } else {
                scans.add(connection.scan(scanOptions));
            }

            scans.forEach(scan -> {
                scan.forEachRemaining(key -> {
                    byte[] element = connection.get(key);
                    clients.add((LwM2mClient) serializer.asObject(element));
                });
            });
            return clients;
        }
    }

    @Override
    public void put(LwM2mClient client) {
        if (client.getState().equals(LwM2MClientState.UNREGISTERED)) {
            log.error("[{}] Client is in invalid state: {}!", client.getEndpoint(), client.getState(), new Exception());
        } else {
            byte[] clientSerialized = serializer.asByteArray(client);
            try (var connection = connectionFactory.getConnection()) {
                connection.getSet(getKey(client.getEndpoint()), clientSerialized);
            }
        }
    }

    @Override
    public void remove(String endpoint) {
        try (var connection = connectionFactory.getConnection()) {
            connection.del(getKey(endpoint));
        }
    }

    private byte[] getKey(String endpoint) {
        return (CLIENT_EP + endpoint).getBytes();
    }
}

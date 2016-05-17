/**
 * Copyright (c) 2015 Intel Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.trustedanalytics.servicebroker.framework.store.zookeeper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.trustedanalytics.cfbroker.store.zookeeper.service.ZookeeperClient;

import com.google.common.collect.ImmutableMap;

@RunWith(MockitoJUnitRunner.class)
public class ZookeeperCredentialsStoreTest {

  private static final String CREDENTIALS = "/%s/metadata";

  @Mock
  private ZookeeperClient zookeeperClient;

  @Autowired
  @InjectMocks
  private ZookeeperCredentialsStore zookeeperCredentialsStore;

  @Test
  public void createServiceInstanceCredentials_validRequest_credentialsStoreSaveMethodCalled()
      throws Exception {
    UUID instanceId = UUID.randomUUID();
    ImmutableMap<String, Object> credentials =
        ImmutableMap.of("key", "secret", "key2", "another_secret");

    zookeeperCredentialsStore.save(credentials, instanceId);
    verify(zookeeperClient).addZNode(String.format(CREDENTIALS, instanceId.toString()),
        zookeeperCredentialsStore.convertToBytes(credentials));
  }

  @Test
  public void getServiceInstanceCredentials_validRequest_credentialsStoreGetMethodCalled()
      throws Exception {
    UUID instanceId = UUID.randomUUID();
    ImmutableMap<String, Object> credentials =
        ImmutableMap.of("key", "secret", "key2", "another_secret");
    when(zookeeperClient.getZNode(String.format(CREDENTIALS, instanceId.toString()))).thenReturn(
        zookeeperCredentialsStore.convertToBytes(credentials));

    zookeeperCredentialsStore.save(credentials, instanceId);
    Map<String, Object> deserialized = zookeeperCredentialsStore.get(instanceId);

    verify(zookeeperClient).getZNode(String.format(CREDENTIALS, instanceId.toString()));
    assertThat(credentials.get("key"), equalTo(deserialized.get("key")));
    assertThat(credentials.get("key2"), equalTo(deserialized.get("key2")));
  }

}

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

import java.io.*;
import java.util.Map;
import java.util.UUID;

import com.google.common.annotations.VisibleForTesting;
import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.trustedanalytics.cfbroker.store.zookeeper.service.ZookeeperClient;
import org.trustedanalytics.servicebroker.framework.Qualifiers;
import org.trustedanalytics.servicebroker.framework.store.CredentialsStore;

@Component
public class ZookeeperCredentialsStore implements CredentialsStore {

  private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperCredentialsStore.class);
  private static final String CREDENTIALS = "/%s/metadata";

  @Autowired
  @Qualifier(value = Qualifiers.BROKER_STORE)
  private ZookeeperClient zkClient;

  @Override
  public void save(Map<String, Object> credentials, UUID instanceId) throws ServiceBrokerException {
    String path = String.format(CREDENTIALS, instanceId.toString());
    try {
      LOGGER.info("credentials save(" + path + ")");
      zkClient.addZNode(path, convertToBytes(credentials));
    } catch (IOException e)  {
      throw new ServiceBrokerException("Can't save credentials to: " + path, e);
    }
  }

  @Override
  public boolean exists(UUID instanceId) throws ServiceBrokerException {
    String path = String.format(CREDENTIALS, instanceId.toString());
    try {
      LOGGER.info("credentials exists(" + path + ")");
      return zkClient.exists(path);
    } catch (IOException e)  {
      throw new ServiceBrokerException("Can't read credentials form: " + path, e);
    }
  }

  @Override
  public Map<String, Object> get(UUID instanceId) throws ServiceBrokerException {
    String path = String.format(CREDENTIALS, instanceId.toString());
    try {
      LOGGER.info("credentials get(" + path + ")");
      return (Map<String, Object>)convertFromBytes(zkClient.getZNode(path));
    } catch (IOException | ClassNotFoundException e) {
      throw new ServiceBrokerException("Can't get credentials from: " + path, e);
    }
  }

  @VisibleForTesting
  byte[] convertToBytes(Object object) throws IOException {
    try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
         ObjectOutput out = new ObjectOutputStream(bos)) {
      out.writeObject(object);
      return bos.toByteArray();
    }
  }

  @VisibleForTesting
  Object convertFromBytes(byte[] bytes) throws IOException, ClassNotFoundException {
    try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
         ObjectInput in = new ObjectInputStream(bis)) {
      return in.readObject();
    }
  }
}

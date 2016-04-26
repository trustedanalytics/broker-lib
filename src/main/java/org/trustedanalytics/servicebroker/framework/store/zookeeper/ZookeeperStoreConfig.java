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

import java.io.IOException;
import java.util.Optional;

import javax.security.auth.login.LoginException;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.trustedanalytics.cfbroker.store.zookeeper.service.ZookeeperClient;
import org.trustedanalytics.cfbroker.store.zookeeper.service.ZookeeperClientBuilder;
import org.trustedanalytics.hadoop.config.client.*;
import org.trustedanalytics.hadoop.kerberos.KrbLoginManager;
import org.trustedanalytics.hadoop.kerberos.KrbLoginManagerFactory;
import org.trustedanalytics.servicebroker.framework.Profiles;
import org.trustedanalytics.servicebroker.framework.Qualifiers;
import org.trustedanalytics.servicebroker.framework.kerberos.KerberosProperties;

@Configuration
public class ZookeeperStoreConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperStoreConfig.class);

  @Value("${store.cluster:}")
  String zkClusterHosts;

  @Value("${store.path}")
  @NotNull
  private String brokerStoreNode;

  @Value("${store.user}")
  @NotNull
  private String user;

  @Value("${store.password}")
  @NotNull
  private String password;

  @Autowired
  private KerberosProperties kerberosProperties;

  @Bean(initMethod = "init", destroyMethod = "destroy")
  @Profile(Profiles.CLOUD)
  @Qualifier(Qualifiers.BROKER_STORE)
  public ZookeeperClient getZkClientForBrokerStore() throws IOException, LoginException {
    return getZKClient(brokerStoreNode, zkClusterHosts);
  }

  private ZookeeperClient getZKClient(String brokerNode, String zkClusterHosts) throws IOException, LoginException {
    AppConfiguration confHelper = Configurations.newInstanceFromEnv();
    Optional<ServiceInstanceConfiguration> zkConf = confHelper.getServiceConfigIfExists(ServiceType.ZOOKEEPER_TYPE);
    if(zkConf.isPresent()) {
      zkClusterHosts = zkConf.get().getProperty(Property.ZOOKEPER_URI).get();
    }

    if (kerberosProperties.isKerberosEnabled()) {
      LOGGER.info("Found kerberos configuration - trying to authenticate");
      KrbLoginManager loginManager =
          KrbLoginManagerFactory.getInstance().getKrbLoginManagerInstance(
              kerberosProperties.getKdc(), kerberosProperties.getRealm());
      loginManager.loginWithCredentials(user, password.toCharArray());
    } else {
      LOGGER.warn("kerberos configuration empty or invalid - will not try to authenticate.");
    }

    return new ZookeeperClientBuilder(zkClusterHosts, user, password, brokerStoreNode).build();
  }

}

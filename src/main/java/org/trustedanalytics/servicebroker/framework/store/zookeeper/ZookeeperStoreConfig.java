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

import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;
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
import org.trustedanalytics.hadoop.kerberos.KrbLoginManager;
import org.trustedanalytics.hadoop.kerberos.KrbLoginManagerFactory;
import org.trustedanalytics.servicebroker.framework.Profiles;
import org.trustedanalytics.servicebroker.framework.Qualifiers;
import org.trustedanalytics.servicebroker.framework.kerberos.KerberosProperties;
import sun.security.krb5.KrbException;

import javax.security.auth.login.LoginException;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

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

  @Value("${store.keytabPath}")
  @NotNull
  private String keytabPath;

  @Autowired
  private KerberosProperties kerberosProperties;

  @Bean(initMethod = "init", destroyMethod = "destroy")
  @Profile(Profiles.SIMPLE)
  @Qualifier(Qualifiers.BROKER_STORE)
  public ZookeeperClient getInsecureZkClientForBrokerStore() throws IOException, NoSuchAlgorithmException {
    return new ZookeeperClientBuilder(zkClusterHosts, user, password, brokerStoreNode).withRootCreation(getAcl()).build();
  }

  @Bean(initMethod = "init", destroyMethod = "destroy")
  @Profile(Profiles.KERBEROS)
  @Qualifier(Qualifiers.BROKER_STORE)
  public ZookeeperClient getSecureZkClientForBrokerStore() throws LoginException, KrbException, IOException, NoSuchAlgorithmException {
    this.loginToKerberos();
    return new ZookeeperClientBuilder(zkClusterHosts, user, password, brokerStoreNode).withRootCreation(getAcl()).build();
  }

  private void loginToKerberos() throws LoginException, KrbException {
    LOGGER.info("Found kerberos configuration - trying to authenticate");
    KrbLoginManager loginManager =
        KrbLoginManagerFactory.getInstance().getKrbLoginManagerInstance(
            kerberosProperties.getKdc(), kerberosProperties.getRealm());
    loginManager.loginWithKeyTab(user, keytabPath);
  }


  private List<ACL> getAcl() throws NoSuchAlgorithmException {
    String digest = DigestAuthenticationProvider.generateDigest(
        String.format("%s:%s", user, password));
    return Arrays.asList(new ACL(ZooDefs.Perms.ALL, new Id("digest", digest)));
  }

}

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
package org.trustedanalytics.servicebroker.framework.integration;

import static org.mockito.Mockito.mock;

import java.io.IOException;

import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.trustedanalytics.cfbroker.store.api.BrokerStore;
import org.trustedanalytics.servicebroker.framework.Qualifiers;
import org.trustedanalytics.servicebroker.framework.kerberos.KerberosProperties;
import org.trustedanalytics.servicebroker.framework.service.ServicePlanDefinition;

@Configuration
@EnableConfigurationProperties
public class TestConfig {

  @Bean
  public KerberosProperties getKerberosProperties() throws IOException {
    return new KerberosProperties("kdc", "realm");
  }

  @Bean
  @Qualifier(value = Qualifiers.SERVICE_INSTANCE)
  public BrokerStore<ServiceInstance> serviceStore() {
    return mock(BrokerStore.class);
  }

  @Bean
  @Qualifier(value = Qualifiers.SERVICE_INSTANCE_BINDING)
  public BrokerStore<CreateServiceInstanceBindingRequest> bindingStore() {
    return mock(BrokerStore.class);
  }

  @Bean
  public ServicePlanDefinition planFirst() {
    return mock(ServicePlanDefinition.class);
  }

  @Bean
  public ServicePlanDefinition planSecond() {
    return mock(ServicePlanDefinition.class);
  }
}

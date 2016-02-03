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
package org.trustedanalytics.servicebroker.framework.service;

import static org.trustedanalytics.servicebroker.framework.service.ServicePlanDefinitionAndCatalogMerger.merge;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.cloudfoundry.community.servicebroker.model.Catalog;
import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.cloudfoundry.community.servicebroker.service.ServiceInstanceBindingService;
import org.cloudfoundry.community.servicebroker.service.ServiceInstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.trustedanalytics.cfbroker.store.api.BrokerStore;
import org.trustedanalytics.cfbroker.store.impl.ServiceInstanceBindingServiceStore;
import org.trustedanalytics.cfbroker.store.impl.ServiceInstanceServiceStore;
import org.trustedanalytics.servicebroker.framework.Qualifiers;

@Configuration
public class DefaultServicesConfig {

  @Autowired
  private Optional<Map<String, ServicePlanDefinition>> planNameToPlanDefinition;

  @Autowired
  private Catalog catalog;

  private Map<String, ServicePlanDefinition> planIdToPlanDefinition;

  @Autowired
  @Qualifier(value = Qualifiers.SERVICE_INSTANCE)
  private BrokerStore<ServiceInstance> serviceStore;

  @Autowired
  @Qualifier(value = Qualifiers.SERVICE_INSTANCE_BINDING)
  private BrokerStore<CreateServiceInstanceBindingRequest> bindingStore;

  @PostConstruct
  public void convertPlanDefinitionMap() {
    planIdToPlanDefinition =
        merge(planNameToPlanDefinition.orElseGet(HashMap::new), catalog).getPlanIdToPlanDefinitionMap();
  }

  @ConditionalOnProperty(value = "brokerFramework.useDefaultServiceInstanceService", matchIfMissing = true)
  @Bean
  public ServiceInstanceService defaultSIS() {
    return new DefaultServiceInstanceService(new ServiceInstanceServiceStore(serviceStore), planIdToPlanDefinition);
  }

  @ConditionalOnProperty(value = "brokerFramework.useDefaultServiceInstanceBindingService", matchIfMissing = true)
  @Bean
  public ServiceInstanceBindingService defaultSIBS(ServiceInstanceService serviceInstanceService) {
    return new DefaultServiceInstanceBindingService(new ServiceInstanceBindingServiceStore(bindingStore),
        serviceInstanceService, planIdToPlanDefinition);
  }
}

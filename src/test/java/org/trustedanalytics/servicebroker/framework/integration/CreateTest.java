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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.trustedanalytics.servicebroker.test.cloudfoundry.CfModelsAssert.deeplyEqualTo;
import static org.trustedanalytics.servicebroker.test.cloudfoundry.CfModelsFactory.getCreateBindingRequest;
import static org.trustedanalytics.servicebroker.test.cloudfoundry.CfModelsFactory.getCreateInstanceRequest;
import static org.trustedanalytics.servicebroker.test.cloudfoundry.CfModelsFactory.getServiceBinding;
import static org.trustedanalytics.servicebroker.test.cloudfoundry.CfModelsFactory.getServiceInstance;

import java.util.Optional;
import java.util.UUID;

import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.cloudfoundry.community.servicebroker.model.ServiceInstanceBinding;
import org.cloudfoundry.community.servicebroker.service.ServiceInstanceBindingService;
import org.cloudfoundry.community.servicebroker.service.ServiceInstanceService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.trustedanalytics.cfbroker.store.api.BrokerStore;
import org.trustedanalytics.cfbroker.store.api.Location;
import org.trustedanalytics.servicebroker.framework.Qualifiers;
import org.trustedanalytics.servicebroker.framework.catalog.CatalogConfig;
import org.trustedanalytics.servicebroker.framework.service.DefaultServicesConfig;
import org.trustedanalytics.servicebroker.framework.service.ServicePlanDefinition;

import com.google.common.collect.ImmutableMap;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {DefaultServicesConfig.class, CatalogConfig.class, TestConfig.class})
@IntegrationTest
public class CreateTest {

  @Autowired
  private ServiceInstanceService instanceService;

  @Autowired
  private ServiceInstanceBindingService bindingService;

  @Autowired
  @Qualifier(value = Qualifiers.SERVICE_INSTANCE)
  private BrokerStore<ServiceInstance> serviceStore;

  @Autowired
  @Qualifier(value = Qualifiers.SERVICE_INSTANCE_BINDING)
  private BrokerStore<CreateServiceInstanceBindingRequest> bindingStore;

  @Autowired
  private ServicePlanDefinition planFirst;

  @Autowired
  private ServicePlanDefinition planSecond;

  @Test
  public void createServiceInstance_validRequest_brokerStoreAndPlanDefinitionCalled() throws Exception {
    //arrange
    String serviceInstanceId = UUID.randomUUID().toString();
    ServiceInstance expectedInstance = getServiceInstance(serviceInstanceId, "baseId-planFirstID");
    CreateServiceInstanceRequest request = getCreateInstanceRequest(expectedInstance);

    //return empty -> instance not exist -> it can be created
    when(serviceStore.getById(Location.newInstance(serviceInstanceId))).thenReturn(Optional.empty());

    //act
    ServiceInstance actualInstance = instanceService.createServiceInstance(request);

    //assert
    verify(serviceStore).save(any(), eq(actualInstance));
    verify(planFirst).provision(actualInstance);
    assertThat(actualInstance, deeplyEqualTo(expectedInstance));
  }

  @Test
  public void createServiceInstanceBinding_validRequest_brokerStoreAndPlanDefinitionCalled() throws Exception {
    //arrange
    String serviceInstanceId = UUID.randomUUID().toString();
    ServiceInstance expectedInstance = getServiceInstance(serviceInstanceId, "baseId-planSecondID");
    CreateServiceInstanceRequest request = getCreateInstanceRequest(expectedInstance);

    //@formatter:off
    when(serviceStore.getById(Location.newInstance(serviceInstanceId)))
        .thenReturn(Optional.empty())                   //return empty -> instance not exist -> it can be created
        .thenReturn(Optional.of(expectedInstance));     //next time return that it's created
    instanceService.createServiceInstance(request);     //create instance
    //@formatter:on

    CreateServiceInstanceBindingRequest bindReq = getCreateBindingRequest(serviceInstanceId, "baseId-planSecondID");

    //return empty -> binding not exist -> it can be created
    when(bindingStore.getById(any())).thenReturn(Optional.empty());
    ImmutableMap<String, Object> credentials = ImmutableMap.of("key", "value");
    when(planSecond.bind(expectedInstance)).thenReturn(credentials);

    //act
    ServiceInstanceBinding actualBinding = bindingService.createServiceInstanceBinding(bindReq);

    //assert
    verify(bindingStore).save(any(), eq(bindReq));
    verify(planSecond).bind(expectedInstance);
    ServiceInstanceBinding expectedBinding = getServiceBinding(bindReq, credentials);
    assertThat(actualBinding, deeplyEqualTo(expectedBinding));
  }
}

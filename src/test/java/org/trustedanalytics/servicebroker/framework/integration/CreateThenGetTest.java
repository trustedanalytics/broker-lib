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
import static org.mockito.Mockito.when;
import static org.trustedanalytics.servicebroker.test.cloudfoundry.CfModelsAssert.deeplyEqualTo;
import static org.trustedanalytics.servicebroker.test.cloudfoundry.CfModelsFactory.getCreateInstanceRequest;
import static org.trustedanalytics.servicebroker.test.cloudfoundry.CfModelsFactory.getServiceInstance;

import java.util.Optional;
import java.util.UUID;

import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
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

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {DefaultServicesConfig.class, CatalogConfig.class, TestConfig.class})
@IntegrationTest
public class CreateThenGetTest {

  @Autowired
  private ServiceInstanceService instanceService;

  @Autowired
  @Qualifier(value = Qualifiers.SERVICE_INSTANCE)
  private BrokerStore<ServiceInstance> serviceStore;

  @Test
  public void getServiceInstance_instanceCreated_returnsInstance() throws Exception {

    //arrange
    String serviceInstanceId = UUID.randomUUID().toString();
    ServiceInstance expectedInstance = getServiceInstance(serviceInstanceId, "baseId-planFirstID");
    CreateServiceInstanceRequest request = getCreateInstanceRequest(expectedInstance);

    //@formatter:off
    when(serviceStore.getById(Location.newInstance(serviceInstanceId)))
        .thenReturn(Optional.empty())                   //return empty -> instance not exist -> it can be created
        .thenReturn(Optional.of(expectedInstance));     //next time return that it's created
    instanceService.createServiceInstance(request);     //create instance
    //@formatter:on

    //act
    ServiceInstance savedInstance = instanceService.getServiceInstance(serviceInstanceId);

    //assert
    assertThat(savedInstance, deeplyEqualTo(expectedInstance));
  }
}

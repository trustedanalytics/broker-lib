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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.trustedanalytics.servicebroker.test.cloudfoundry.CfModelsAssert.deeplyEqualTo;
import static org.trustedanalytics.servicebroker.test.cloudfoundry.CfModelsFactory.getCreateBindingRequest;
import static org.trustedanalytics.servicebroker.test.cloudfoundry.CfModelsFactory.getServiceBinding;
import static org.trustedanalytics.servicebroker.test.cloudfoundry.CfModelsFactory.getServiceInstance;

import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.cloudfoundry.community.servicebroker.model.ServiceInstanceBinding;
import org.cloudfoundry.community.servicebroker.service.ServiceInstanceBindingService;
import org.cloudfoundry.community.servicebroker.service.ServiceInstanceService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.ImmutableMap;

@RunWith(MockitoJUnitRunner.class)
public class DefaultServiceInstanceBindingServiceTest {

  private static final String INSTANCE_ID = "instanceId";
  private static final String PLAN_ID_1 = "planId1";
  private static final String PLAN_ID_2 = "planId2";

  @Mock
  private ServiceInstanceBindingService superSIBS;

  @Mock
  private ServiceInstanceService superSIS;

  @Mock
  private ServicePlanDefinition spd1;

  @Mock
  private ServicePlanDefinition spd2;

  private DefaultServiceInstanceBindingService service;

  @Before
  public void setup() {
    ImmutableMap<String, ServicePlanDefinition> idToPlanDefinition = ImmutableMap.of(PLAN_ID_1, spd1, PLAN_ID_2, spd2);
    service = new DefaultServiceInstanceBindingService(superSIBS, superSIS, idToPlanDefinition);
  }

  @Test
  public void createServiceInstanceBinding_instanceAndPlanExists_bindingCreated() throws Exception {
    //arrange
    ServiceInstance instance = getServiceInstance(INSTANCE_ID, PLAN_ID_2);
    when(superSIS.getServiceInstance(INSTANCE_ID)).thenReturn(instance);

    CreateServiceInstanceBindingRequest bindingRequest = getCreateBindingRequest(INSTANCE_ID, PLAN_ID_2);
    ServiceInstanceBinding binding = getServiceBinding(INSTANCE_ID);
    when(superSIBS.createServiceInstanceBinding(bindingRequest)).thenReturn(binding);

    ImmutableMap<String, Object> credentials = ImmutableMap.of("key", "value");
    when(spd2.bind(instance)).thenReturn(credentials);

    ServiceInstanceBinding expectedBinding = getServiceBinding(binding, credentials);

    //act
    ServiceInstanceBinding actualBinding = service.createServiceInstanceBinding(bindingRequest);

    //assert
    verify(spd2).bind(instance);
    assertThat(actualBinding, deeplyEqualTo(expectedBinding));
  }



  @Test(expected = ServiceBrokerException.class)
  public void createServiceInstanceBinding_planNotExist_exceptionThrown() throws Exception {
    //arrange
    CreateServiceInstanceBindingRequest bindingRequest = getCreateBindingRequest(INSTANCE_ID);
    ServiceInstance instance = getServiceInstance(INSTANCE_ID, "planIdUNKNOWN");
    when(superSIS.getServiceInstance(INSTANCE_ID)).thenReturn(instance);

    ServiceInstanceBinding binding = getServiceBinding(INSTANCE_ID);
    when(superSIBS.createServiceInstanceBinding(bindingRequest)).thenReturn(binding);

    //act
    service.createServiceInstanceBinding(bindingRequest);
  }

  @Test(expected = ServiceBrokerException.class)
  public void createServiceInstanceBinding_instanceNotExist_exceptionThrown() throws Exception {
    //arrange
    CreateServiceInstanceBindingRequest bindingRequest = getCreateBindingRequest(INSTANCE_ID);
    when(superSIS.getServiceInstance(INSTANCE_ID)).thenReturn(null);

    //act
    service.createServiceInstanceBinding(bindingRequest);
  }

  @After
  public void verifyNoMoreInteractionsWithServicePlanDefinitions() {
    verifyNoMoreInteractions(spd1, spd2);
  }
}

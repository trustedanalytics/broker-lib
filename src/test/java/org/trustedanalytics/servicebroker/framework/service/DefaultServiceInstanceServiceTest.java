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
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.*;
import static org.trustedanalytics.servicebroker.test.cloudfoundry.CfModelsFactory.getCreateInstanceRequest;
import static org.trustedanalytics.servicebroker.test.cloudfoundry.CfModelsFactory.getServiceInstance;

import java.util.Optional;

import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.cloudfoundry.community.servicebroker.service.ServiceInstanceService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.ImmutableMap;

@RunWith(MockitoJUnitRunner.class)
public class DefaultServiceInstanceServiceTest {

  private static final String INSTANCE_ID = "instanceId";
  private static final String PLAN_ID_1 = "planId1";
  private static final String PLAN_ID_2 = "planId2";

  @Mock
  private ServiceInstanceService superSIS;

  @Mock
  private ServicePlanDefinition spd1;

  @Mock
  private ServicePlanDefinition spd2;

  private DefaultServiceInstanceService service;

  @Before
  public void setup() {
    ImmutableMap<String, ServicePlanDefinition> idToPlanDefinition = ImmutableMap.of(PLAN_ID_1, spd1, PLAN_ID_2, spd2);
    service = new DefaultServiceInstanceService(superSIS, idToPlanDefinition);
  }

  @Test
  public void createServiceInstance_planExists_properServicePlanDefinitionCalled() throws Exception {
    //arrange
    ServiceInstance instance = getServiceInstance(INSTANCE_ID, PLAN_ID_1);
    CreateServiceInstanceRequest createRequest = getCreateInstanceRequest(instance);
    when(superSIS.createServiceInstance(createRequest)).thenReturn(instance);

    //act
    ServiceInstance actualInstance = service.createServiceInstance(createRequest);

    //assert
    verify(spd1).provision(instance, Optional.ofNullable(createRequest.getParameters()));
    assertThat(actualInstance, equalTo(instance));
  }

  @Test(expected = ServiceBrokerException.class)
  public void createServiceInstance_planNotExist_exceptionThrown() throws Exception {
    //arrange
    ServiceInstance instance = getServiceInstance(INSTANCE_ID, "planIdUNKNOWN");
    CreateServiceInstanceRequest createRequest = getCreateInstanceRequest(instance);
    when(superSIS.createServiceInstance(createRequest)).thenReturn(instance);

    //act
    service.createServiceInstance(createRequest);
  }

  @After
  public void verifyNoMoreInteractionsWithServicePlanDefinitions() {
    verifyNoMoreInteractions(spd1, spd2);
  }
}

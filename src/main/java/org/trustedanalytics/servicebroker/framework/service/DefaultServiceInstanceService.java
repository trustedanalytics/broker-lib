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

import java.util.Map;
import java.util.Optional;

import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceExistsException;
import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceRequest;
import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.cloudfoundry.community.servicebroker.service.ServiceInstanceService;
import org.trustedanalytics.cfbroker.store.impl.ForwardingServiceInstanceServiceStore;

public class DefaultServiceInstanceService extends ForwardingServiceInstanceServiceStore {

  private final Map<String, ServicePlanDefinition> planIdToPlanDefinition;

  public DefaultServiceInstanceService(ServiceInstanceService instanceService,
      Map<String, ServicePlanDefinition> planIdToPlanDefinition) {
    super(instanceService);
    this.planIdToPlanDefinition = planIdToPlanDefinition;
  }

  @Override
  public ServiceInstance createServiceInstance(CreateServiceInstanceRequest request)
      throws ServiceInstanceExistsException, ServiceBrokerException {

    ServiceInstance serviceInstance = super.createServiceInstance(request);
    String planId = serviceInstance.getPlanId();
    Optional.ofNullable(planIdToPlanDefinition.get(planId))
        .orElseThrow(() -> new ServiceBrokerException("Service plan '" + planId + "' definition not found"))
        .provision(serviceInstance);
    return serviceInstance;
  }
}

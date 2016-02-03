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
import org.cloudfoundry.community.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.cloudfoundry.community.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.cloudfoundry.community.servicebroker.model.ServiceInstanceBinding;
import org.cloudfoundry.community.servicebroker.service.ServiceInstanceBindingService;
import org.cloudfoundry.community.servicebroker.service.ServiceInstanceService;
import org.trustedanalytics.cfbroker.store.impl.ForwardingServiceInstanceBindingServiceStore;

public class DefaultServiceInstanceBindingService extends ForwardingServiceInstanceBindingServiceStore {

  private final ServiceInstanceService instanceService;
  private final Map<String, ServicePlanDefinition> planIdToPlanDefinition;

  public DefaultServiceInstanceBindingService(ServiceInstanceBindingService bindingService,
      ServiceInstanceService instanceService, Map<String, ServicePlanDefinition> planIdToPlanDefinition) {
    super(bindingService);
    this.instanceService = instanceService;
    this.planIdToPlanDefinition = planIdToPlanDefinition;
  }

  @Override
  public ServiceInstanceBinding createServiceInstanceBinding(CreateServiceInstanceBindingRequest request)
      throws ServiceInstanceBindingExistsException, ServiceBrokerException {
    ServiceInstance instance = instanceService.getServiceInstance(request.getServiceInstanceId());
    if (instance == null) {
      throw new ServiceBrokerException(
          String.format("Service instance not found: [%s]", request.getServiceInstanceId()));
    }
    return withCredentials(super.createServiceInstanceBinding(request), instance);
  }

  private ServiceInstanceBinding withCredentials(ServiceInstanceBinding serviceInstanceBinding,
      ServiceInstance instance) throws ServiceBrokerException {
    return new ServiceInstanceBinding(serviceInstanceBinding.getId(), serviceInstanceBinding.getServiceInstanceId(),
        getCredentialsFor(instance), serviceInstanceBinding.getSyslogDrainUrl(), serviceInstanceBinding.getAppGuid());
  }

  private Map<String, Object> getCredentialsFor(ServiceInstance instance) throws ServiceBrokerException {
    String planId = instance.getPlanId();
    return Optional.ofNullable(planIdToPlanDefinition.get(planId))
        .orElseThrow(() -> new ServiceBrokerException("Service plan '" + planId + "' definition not found"))
        .bind(instance);
  }
}

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
import java.util.stream.Collectors;

import org.cloudfoundry.community.servicebroker.model.Catalog;
import org.cloudfoundry.community.servicebroker.model.Plan;

class ServicePlanDefinitionAndCatalogMerger {

  private final Map<String, ServicePlanDefinition> planNameToPlanDefinition;
  private final Catalog catalog;

  private ServicePlanDefinitionAndCatalogMerger(Map<String, ServicePlanDefinition> planNameToPlanDefinition,
                                                Catalog catalog) {
    this.planNameToPlanDefinition = planNameToPlanDefinition;
    this.catalog = catalog;
  }

  public static ServicePlanDefinitionAndCatalogMerger merge(Map<String, ServicePlanDefinition> planNameToPlanDefinition,
      Catalog catalog) {
    return new ServicePlanDefinitionAndCatalogMerger(planNameToPlanDefinition, catalog);
  }

  public Map<String, ServicePlanDefinition> getPlanIdToPlanDefinitionMap() {
    return catalog.getServiceDefinitions().stream().flatMap(serviceDefinition -> serviceDefinition.getPlans().stream())
        .collect(Collectors.toMap(Plan::getId, this::getServicePlanDefinition));
  }

  private ServicePlanDefinition getServicePlanDefinition(Plan plan) {
    return Optional.ofNullable(planNameToPlanDefinition.get(plan.getName()))
        .orElseThrow(() -> getPlanNotFoundError(plan));
  }

  private IllegalStateException getPlanNotFoundError(Plan plan) {
    return new IllegalStateException(String.format(
        "Service plan [id='%s', name='%s'] is defined in cf.catalog.plans, but Spring can't find '%s' bean of that name.",
        plan.getId(), plan.getName(), ServicePlanDefinition.class.getName()));
  }
}

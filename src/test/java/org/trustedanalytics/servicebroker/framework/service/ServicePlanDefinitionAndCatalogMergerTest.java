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
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.isIn;
import static org.trustedanalytics.servicebroker.framework.service.ServicePlanDefinitionAndCatalogMerger.merge;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.cloudfoundry.community.servicebroker.exception.ServiceBrokerException;
import org.cloudfoundry.community.servicebroker.model.Catalog;
import org.cloudfoundry.community.servicebroker.model.Plan;
import org.cloudfoundry.community.servicebroker.model.ServiceDefinition;
import org.cloudfoundry.community.servicebroker.model.ServiceInstance;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

public class ServicePlanDefinitionAndCatalogMergerTest {

  private final String P1_ID = "p1ID";
  private final String P2_ID = "p2ID";
  private final String P3_ID = "p3ID";
  private final String P1_NAME = "p1NAME";
  private final String P2_NAME = "p2NAME";
  private final String P3_NAME = "p3NAME";
  private final Plan P1 = new Plan(P1_ID, P1_NAME, "desc");
  private final Plan P2 = new Plan(P2_ID, P2_NAME, "desc");
  private final Plan P3 = new Plan(P3_ID, P3_NAME, "desc");
  private final ServicePlanDefinition P1_DEFINITION = new TestServicePlanDefinition();
  private final ServicePlanDefinition P2_DEFINITION = new TestServicePlanDefinition();

  @Test
  public void getPlanIdToPlanDefinitionMap_emptyCatalog_returnsEmptyMap() {
    final Map<String, ServicePlanDefinition> EMPTY_PLAN_NAME_TO_DEFINITION = null;
    final Catalog EMPTY_CATALOG = new Catalog();

    Map<String, ServicePlanDefinition> planIdToDefinition =
        merge(EMPTY_PLAN_NAME_TO_DEFINITION, EMPTY_CATALOG).getPlanIdToPlanDefinitionMap();

    assertThat(planIdToDefinition.entrySet(), empty());
  }

  @Test
  public void getPlanIdToPlanDefinitionMap_everyPlanFromCatalogIsInTheInputMap_returnsMapWithAllPlans() {
    final Map<String, ServicePlanDefinition> P1P2_PLAN_NAME_TO_DEFINITION =
        ImmutableMap.of(P1_NAME, P1_DEFINITION, P2_NAME, P2_DEFINITION);
    final Catalog P1P2_CATALOG = getCatalogWithPlans(Arrays.asList(P1, P2));

    Map<String, ServicePlanDefinition> planIdToDefinition =
        merge(P1P2_PLAN_NAME_TO_DEFINITION, P1P2_CATALOG).getPlanIdToPlanDefinitionMap();

    assertThat(planIdToDefinition.entrySet(),
        everyItem(isIn(Maps.newHashMap(ImmutableMap.<String, ServicePlanDefinition>builder().put(P1_ID, P1_DEFINITION)
            .put(P2_ID, P2_DEFINITION).build()).entrySet())));
  }

  @Test(expected = IllegalStateException.class)
  public void getPlanIdToPlanDefinitionMap_onePlanFromCatalogIsNotInTheInputMap_throwsExceptionPlanDefinitionNotFound() {
    final Map<String, ServicePlanDefinition> P1P2_PLAN_NAME_TO_DEFINITION =
        ImmutableMap.of(P1_NAME, P1_DEFINITION, P2_NAME, P2_DEFINITION);
    final Catalog P1P2P3_CATALOG = getCatalogWithPlans(Arrays.asList(P1, P2, P3));

    merge(P1P2_PLAN_NAME_TO_DEFINITION, P1P2P3_CATALOG).getPlanIdToPlanDefinitionMap();
  }

  private Catalog getCatalogWithPlans(List<Plan> servicePlans) {
    return new Catalog(Collections.singletonList(new ServiceDefinition("id", "name", "desc", true, servicePlans)));
  }

  private class TestServicePlanDefinition implements ServicePlanDefinition {
    @Override
    public Map<String, Object> bind(ServiceInstance serviceInstance) throws ServiceBrokerException {
      return null;
    }
  }
}

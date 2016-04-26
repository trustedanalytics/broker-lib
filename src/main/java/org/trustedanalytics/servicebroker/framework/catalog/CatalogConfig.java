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
package org.trustedanalytics.servicebroker.framework.catalog;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.cloudfoundry.community.servicebroker.model.Catalog;
import org.cloudfoundry.community.servicebroker.model.Plan;
import org.cloudfoundry.community.servicebroker.model.ServiceDefinition;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.collect.ImmutableMap;

@Configuration
@ConfigurationProperties(prefix="cf.catalog")
public class CatalogConfig {

  @NotNull private Metadata metadata;
  @NotNull private List<ServicePlan> plans = new ArrayList<>();
  @NotNull private String serviceName;
  @NotNull private String serviceId;
  @NotNull private String serviceDescription;
  @NotNull private String baseId;

  private String SYSLOG_DRAIN = "syslog_drain";

  private static final String IMAGE_URL = "imageUrl";

  public Metadata getMetadata() {
    return metadata;
  }

  public void setMetadata(Metadata metadata) {
    this.metadata = metadata;
  }

  public String getServiceName() {
    return serviceName;
  }

  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }

  public String getServiceId() {
    return serviceId;
  }

  public void setServiceId(String serviceId) {
    this.serviceId = serviceId;
  }

  public String getServiceDescription() {
    return serviceDescription;
  }

  public void setServiceDescription(String serviceDescription) {
    this.serviceDescription = serviceDescription;
  }

  public String getBaseId() {
    return baseId;
  }

  public void setBaseId(String baseId) {
    this.baseId = baseId;
  }

  public List<ServicePlan> getPlans() {
    return plans;
  }

  //TODO: probably we can delete this method - it was not invoked during integration tests (jacoco report)
  public void setPlans(List<ServicePlan> plans) {
    this.plans = plans;
  }

  @Bean
  public Catalog catalog() {
    return new Catalog(Arrays.asList(
        new ServiceDefinition(serviceId, serviceName,
            serviceDescription, true, true, getBrokerPlans(), null, getServiceDefinitionMetadata(),
            Arrays.asList(SYSLOG_DRAIN),null)));
  }

  private List<Plan> getBrokerPlans() {
    plans.stream().forEach(ServicePlan::validate);
    return plans.stream()
        .map(plan -> new Plan(
            baseId + "-" + plan.getId(),
            plan.getName(),
            plan.getDescription(),
            null,
            plan.isFree()
        ))
        .collect(toList());
  }

  private Map<String, Object> getServiceDefinitionMetadata() {
    return ImmutableMap.of(IMAGE_URL, metadata.getImageUrl());
  }
}

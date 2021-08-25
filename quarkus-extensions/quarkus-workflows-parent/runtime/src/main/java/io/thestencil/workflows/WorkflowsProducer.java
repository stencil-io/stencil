package io.thestencil.workflows;

/*-
 * #%L
 * quarkus-stencil-workflows
 * %%
 * Copyright (C) 2021 Copyright 2021 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import io.thestencil.workflows.core.api.ImmutableRemoteIntegration;
import io.thestencil.workflows.core.mock.WorkflowsClientMock;
import io.thestencil.workflows.core.spi.WorkflowsClientDefault;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.client.WebClient;

@ApplicationScoped
public class WorkflowsProducer {

  private RuntimeConfig runtimeConfig;
  private boolean mockEnabled;
  private String mockFormId; 
  private String mockApiKey;
  private String servicePath;
  private String reviewPath;
  private String fillPath;
  
  public WorkflowsProducer setRuntimeConfig(RuntimeConfig runtimeConfig) {
    this.runtimeConfig = runtimeConfig;
    return this;
  }
  public WorkflowsProducer setMockEndabled(boolean mockEnabled, String apiKey, String formId) {
    this.mockEnabled = mockEnabled;
    this.mockFormId = formId;
    this.mockApiKey = apiKey;
    return this;
  }  
  public WorkflowsProducer setServicePath(String servicePath) {
    this.servicePath = servicePath;
    return this;
  }
  public WorkflowsProducer setReviewPath(String reviewPath) {
    this.reviewPath = reviewPath;
    return this;
  }
  public WorkflowsProducer setFillPath(String fillPath) {
    this.fillPath = fillPath;
    return this;
  }
  @Produces
  @ApplicationScoped
  public WorkflowsContext userActionsContext(Vertx vertx) {
    final var webClient = WebClient.create(vertx, new WebClientOptions());
    if(mockEnabled) {
      final var mockClient = WorkflowsClientMock.builder()
          .webClient(webClient)
          .setApiKey(mockApiKey)
          .setFormId(mockFormId)
          .config(b -> b
            .webClient(webClient)
            .defaultLanguage(runtimeConfig.defaultLocale)
            
            .servicePath(servicePath)
            .fillPath(fillPath)
            .reviewPath(reviewPath)
            
            .processes(ImmutableRemoteIntegration.builder().host(cleanPath(runtimeConfig.processes.host)).path(cleanPath(runtimeConfig.processes.path)).build())
            .fill(ImmutableRemoteIntegration.builder().host(cleanPath(runtimeConfig.fill.host)).path(cleanPath(runtimeConfig.fill.path)).build())
            .review(ImmutableRemoteIntegration.builder().host(cleanPath(runtimeConfig.review.host)).path(cleanPath(runtimeConfig.review.path)).build())
          ).build();
      return new WorkflowsContext(mockClient);
    }
      
    final var client = WorkflowsClientDefault.builder()
      .config(b -> b
        .webClient(webClient)
        .defaultLanguage(runtimeConfig.defaultLocale)
        
        .servicePath(servicePath)
        .fillPath(fillPath)
        .reviewPath(reviewPath)
        
        .processes(ImmutableRemoteIntegration.builder().host(cleanPath(runtimeConfig.processes.host)).path(cleanPath(runtimeConfig.processes.path)).build())
        .fill(ImmutableRemoteIntegration.builder().host(cleanPath(runtimeConfig.fill.host)).path(cleanPath(runtimeConfig.fill.path)).build())
        .review(ImmutableRemoteIntegration.builder().host(cleanPath(runtimeConfig.review.host)).path(cleanPath(runtimeConfig.review.path)).build())
        
        ).build();
    return new WorkflowsContext(client);
  }
  
  public static String cleanPath(String value) {
    final var start = value.startsWith("/") ? value.substring(1) : value;
    return start.endsWith("/") ? value.substring(0, start.length() -2) : start;
  }
}

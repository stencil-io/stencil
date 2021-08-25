package io.thestencil.workflows.core.spi;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.smallrye.mutiny.Multi;
import io.thestencil.workflows.core.api.ImmutableWorkflow;
import io.thestencil.workflows.core.api.WorkflowsClient.ClientConfig;
import io.thestencil.workflows.core.api.WorkflowsClient.QueryWorkflows;
import io.thestencil.workflows.core.api.WorkflowsClient.Workflow;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.ext.web.client.HttpResponse;



public class QueryWorkflowsDefault extends BuilderTemplate implements QueryWorkflows {
  private static final Logger LOGGER = LoggerFactory.getLogger(QueryWorkflowsDefault.class);
  private final ClientConfig config;
  private String userId;
  private String processId;
  private Integer limit;
  
  public QueryWorkflowsDefault(RequestOptions init, ClientConfig config) {
    super(config.getWebClient(), init);
    this.config = config;
  }
  @Override
  public QueryWorkflows userId(String userId) {
    this.userId = userId;
    return this;
  }
  @Override
  public QueryWorkflows processId(String processId) {
    this.processId = processId;
    return this;
  }
  @Override
  public QueryWorkflows limit(Integer limit) {
    this.limit = limit;
    return this;
  }
  @Override
  public Multi<Workflow> list() {
    PortalAssert.notEmpty(userId, () -> "userId must be defined!");
    
    if(processId != null) {
      final var process = super.get(getUri("/processes/" + processId)).send();
      return process.onItem()
          .transformToMulti(item -> {
            final var action = mapToElement(item, config.getFillPath(), config.getReviewPath());
            if(action == null) {
              return Multi.createFrom().empty();
            }
            return Multi.createFrom().item(action);
          });
    } else {
      final var processes  = super.get(getUri("/processesSearch"))
          .addQueryParam("unpaged", "true")
          .addQueryParam("size", limit == null ? "300" : limit + "")
          .addQueryParam("userId", userId)
          .send();  
      
      return processes.onItem()
          .transformToMulti(item -> 
            mapToList(item, config.getFillPath(), config.getReviewPath())
          );
    }
  }

  private Workflow mapToElement(HttpResponse<?> resp, String fillUri, String reviewUri) {
    if (resp.statusCode() != 200) {
      String error = "USER ACTIONS: Can't create response, e = " + resp.statusCode() + " | " + resp.statusMessage() + " | " + resp.headers();
      LOGGER.error(error);
      return null;
    }
    
    final JsonObject data = resp.bodyAsJsonObject();
    if(data == null) {
      return null;
    }
    return mapToUserAction(data, fillUri, reviewUri);
  }
  
  private Multi<Workflow> mapToList(HttpResponse<?> resp, String fillUri, String reviewUri) {
    if (resp.statusCode() != 200) {
      String error = "USER ACTIONS: Can't create response, e = " + resp.statusCode() + " | " + resp.statusMessage() + " | " + resp.headers();
      LOGGER.error(error);
      return Multi.createFrom().empty();
    }
    
    final JsonObject paged = resp.bodyAsJsonObject();
    final var content = paged.getJsonObject("_embedded");
    if(content == null) {
      return Multi.createFrom().empty();
    }
    
    final var datalist = content.getJsonArray("processDataList");
    if(datalist == null) {
      return Multi.createFrom().empty();
    }
    return Multi.createFrom()
        .items(datalist.stream().map(e -> mapToUserAction((JsonObject) e, fillUri, reviewUri)));
  }
  

  public static Workflow mapToUserAction(JsonObject entity, String fillUri, String reviewUri) {
    final var workflow = entity.getJsonObject("workflow");
    final var status = entity.getString("status");
    final var formInProgress = "ANSWERING".equalsIgnoreCase(status) || "CREATED".equalsIgnoreCase(status);
    final var formId = entity.getString("questionnaire");
    return ImmutableWorkflow.builder()
        .id(entity.getLong("id") + "")
        .status(status)
        .name(workflow.getString("name"))
        .taskId(entity.getString("task"))
        .reviewUri(reviewUri)
        .formUri(fillUri)
        .formId(formId)
        .formInProgress(formInProgress)
        .build();
  }
}

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

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.smallrye.mutiny.Uni;
import io.thestencil.workflows.core.api.ImmutableWorkflow;
import io.thestencil.workflows.core.api.WorkflowsClient.ClientConfig;
import io.thestencil.workflows.core.api.WorkflowsClient.CreateWorkflows;
import io.thestencil.workflows.core.api.WorkflowsClient.Workflow;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.ext.web.client.HttpResponse;


public class CreateWorkflowsDefault extends BuilderTemplate implements CreateWorkflows {
  private static final Logger LOGGER = LoggerFactory.getLogger(CreateWorkflowsDefault.class);
  private final ClientConfig config;
  private String actionName;
  private String userId;
  private String language;
  private JsonObject body;
  
  public CreateWorkflowsDefault(RequestOptions init, ClientConfig config) {
    super(config.getWebClient(), init);
    this.config = config;
  }
  @Override
  public CreateWorkflows actionName(String actionName) {
    this.actionName = actionName;
    return this;
  }
  @Override
  public CreateWorkflows userId(String userId) {
    this.userId = userId;
    return this;
  }
  @Override
  public CreateWorkflows language(String language) {
    this.language = language;
    return this;
  }
  @Override
  public CreateWorkflows body(JsonObject body) {
    this.body = body;
    return this;
  }
  @Override
  public Uni<Workflow> build() {
    //PortalAssert.notEmpty(userId, () -> "userId must be defined!");
    PortalAssert.notNull(body, () -> "body must be defined!");
    PortalAssert.notEmpty(actionName, () -> "actionName must be defined!");

    final var lang = language == null ? config.getDefaultLanguage() : language;
    //final var userId = this.userId;
    
    final var body = new HashMap<String, Object>();
    body.putAll(this.body.getMap());
    body.put("language", lang);
    
    return post(getUri("/createProcess/"))
        .putHeader(HttpHeaderNames.CONTENT_TYPE.toString(), "application/json")
        .sendBuffer(io.vertx.mutiny.core.buffer.Buffer.newInstance(JsonObject.mapFrom(body).toBuffer()))
        .onItem().transform(resp -> map(resp, config.getFillPath(), config.getReviewPath()));
  }
  
  private Workflow map(HttpResponse<?> resp, String fillUri, String reviewUri) {
    if (resp.statusCode() != 201) {
      String error = 
          "Stencil Workflows: Can't create response"
          + ", uri: " + getUri("/createProcess/")
          + ", code: " + resp.statusCode() 
          + ", msg: " + resp.statusMessage();
      LOGGER.error(error);
      return ImmutableWorkflow.builder()
          .id("").name("").status("")
          .formId("")
          .reviewUri("")
          .formUri(fillUri)
          .formInProgress(false)
          .build();
    }
    final var body = resp.bodyAsJsonObject();
    if(LOGGER.isDebugEnabled()) {
      LOGGER.debug("Stencil Workflows: response body: " + body.encodePrettily());
    }
    return QueryWorkflowsDefault.mapToUserAction(body, fillUri, reviewUri);
  }
}

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

import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.smallrye.mutiny.Uni;
import io.thestencil.workflows.core.api.ImmutableWorkflow;
import io.thestencil.workflows.core.api.WorkflowsClient.CancelWorkflows;
import io.thestencil.workflows.core.api.WorkflowsClient.ClientConfig;
import io.thestencil.workflows.core.api.WorkflowsClient.QueryWorkflows;
import io.thestencil.workflows.core.api.WorkflowsClient.Workflow;
import io.vertx.core.http.RequestOptions;
import io.vertx.mutiny.ext.web.client.HttpResponse;


public class CancelWorkflowsDefault extends BuilderTemplate implements CancelWorkflows {
  private static final Logger LOGGER = LoggerFactory.getLogger(CancelWorkflowsDefault.class);
  private final ClientConfig config;
  private final Supplier<QueryWorkflows> query;
  private String userId;
  private String processId;
  

  public CancelWorkflowsDefault(RequestOptions init, ClientConfig config, Supplier<QueryWorkflows> query) {
    super(config.getWebClient(), init);
    this.config = config;
    this.query = query;
  }
  @Override
  public CancelWorkflowsDefault userId(String userId) {
    this.userId = userId;
    return this;
  }
  @Override
  public CancelWorkflowsDefault processId(String processId) {
    this.processId = processId;
    return this;
  }
  @Override
  public Uni<Workflow> build() {
    PortalAssert.notEmpty(userId, () -> "userId must be defined!");
    PortalAssert.notEmpty(processId, () -> "processId must be defined!");

    return query.get().processId(processId).userId(userId).limit(1).list().collect()
        .asList().onItem().ifNotNull()
        .transformToUni(src -> delete(getUri("/processes/" + processId))
            .putHeader(HttpHeaderNames.CONTENT_TYPE.toString(), "application/json")
            .send())
        .onItem().transform(resp -> map(resp, config.getFillPath(), config.getReviewPath()));
  }
  
  private static Workflow map(HttpResponse<?> resp, String fillUri, String reviewUri) {
    int code = resp.statusCode();
    if (code < 200 || code >= 300) {
      String error = "USER ACTIONS CANCEL: Can't create response, e = " + resp.statusCode() + " | " + resp.statusMessage() + " | " + resp.headers();
      LOGGER.error(error);
      return ImmutableWorkflow.builder()
          .id("").name("").status("")
          .formId("")
          .reviewUri("")
          .formUri(fillUri)
          .formInProgress(false)
          .build();
    }
    return ImmutableWorkflow.builder()
        .id("").name("").status("")
        .formId("")
        .reviewUri("")
        .formUri(fillUri)
        .formInProgress(false)
        .build();
  }
}

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

import java.util.function.Function;

import io.thestencil.workflows.core.api.ImmutableClientConfig;
import io.thestencil.workflows.core.api.WorkflowsClient;
import io.vertx.core.http.RequestOptions;


public class WorkflowsClientDefault implements WorkflowsClient {
  private final RequestOptions process;
  private final RequestOptions fill;
  private final RequestOptions review;
  private final ClientConfig config;
  
  public WorkflowsClientDefault(ClientConfig config) {
    super();
    this.process = new RequestOptions()
        .setURI(config.getProcesses().getPath())
        .setHost(config.getProcesses().getHost());
    this.fill = new RequestOptions()
        .setURI(config.getFill().getPath())
        .setHost(config.getFill().getHost());
    this.review = new RequestOptions()
        .setURI(config.getReview().getPath())
        .setHost(config.getReview().getHost());
    this.config = config;
  }
  @Override
  public CreateWorkflows createUserAction() {
    return new CreateWorkflowsDefault(process, config);
  }
  @Override
  public QueryWorkflows queryUserAction() {
    return new QueryWorkflowsDefault(process, config);
  }
  @Override
  public FillBuilder fill() {
    return new DefaultFillBuilder(fill, config);
  }
  @Override
  public ReviewBuilder review() {
    return new DefaultReviewBuilder(review, config);
  }
  @Override
  public CancelWorkflows cancelUserAction() {
    return new CancelWorkflowsDefault(process, config, () -> queryUserAction());
  }
  @Override
  public ClientConfig config() {
    return config;
  }
  public static Builder builder() {
    return new Builder();
  }
  public static class Builder {
    private ImmutableClientConfig.Builder config = ImmutableClientConfig.builder();
    public Builder config(Function<ImmutableClientConfig.Builder, ImmutableClientConfig.Builder> c) {
      this.config = c.apply(config);
      return this;
    }
    public WorkflowsClientDefault build() {
      return new WorkflowsClientDefault(config.build());
    }
  }
}

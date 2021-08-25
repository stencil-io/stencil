package io.thestencil.workflows.core.mock;

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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.thestencil.workflows.core.api.ImmutableClientConfig;
import io.thestencil.workflows.core.api.ImmutableWorkflow;
import io.thestencil.workflows.core.api.WorkflowsClient;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.WebClient;


public class WorkflowsClientMock implements WorkflowsClient {
  
  private final MockClient dialobClient;
  private final ClientConfig config;
  
  public WorkflowsClientMock(WebClient client, ClientConfig config, String formId, String apiKey) {
    this.dialobClient = new MockClient(client, config, formId, apiKey);
    this.config = config;
  }
  @Override
  public CancelWorkflows cancelUserAction() {
    return new CancelWorkflows() {
      private String processId;
      @Override
      public CancelWorkflows userId(String userId) {
        return this;
      }
      @Override
      public CancelWorkflows processId(String processId) {
        this.processId = processId;
        return this;
      }
      @Override
      public Uni<Workflow> build() {
        return getDialobClient().deleteQuestionnaire(processId).onItem().transform(e -> 
        ImmutableWorkflow.builder()
            .id("mock-id")
            .name("mock-test")
            .reviewUri(config.getReviewPath())
            .formUri(config.getFillPath())
            .formId(processId)
            .formInProgress(true)
            .status("open")
            .build());
      }
    };
  }
  @Override
  public CreateWorkflows createUserAction() {
    return new CreateWorkflows() {
      @Override
      public CreateWorkflows userId(String userId) {
        return this;
      }
      @Override
      public CreateWorkflows actionName(String actionName) {
        return this;
      }
      @Override
      public CreateWorkflows language(String language) {
        return this;
      }
      @Override
      public Uni<Workflow> build() {
        return dialobClient.create();
      }
      @Override
      public CreateWorkflows body(JsonObject body) {
        return this;
      }
    };
  }
  @Override
  public QueryWorkflows queryUserAction() {
    return new QueryWorkflows() {
      @Override
      public QueryWorkflows userId(String userId) {
        return this;
      }
      @Override
      public Multi<Workflow> list() {
        final var action_1 = dialobClient.open().onItem().transform(open ->
          open.stream().map(e -> (Workflow) ImmutableWorkflow.builder()
              .from(e)
              .name("covid-test")
              .build())
          .collect(Collectors.toList())
        );
        final var action_2 = dialobClient.completed().onItem().transform(open -> 
          open.stream().map(e -> (Workflow) ImmutableWorkflow.builder()
              .from(e)
              .name("covid-test")
              .build())
          .collect(Collectors.toList())
        );
        return Uni.combine().all().unis(action_1, action_2).asTuple()
            .onItem().transformToMulti(tuple -> Multi.createFrom().items(Stream
                .concat(tuple.getItem1().stream(), tuple.getItem2().stream())));
      }
      @Override
      public QueryWorkflows processId(String processId) {
        return this;
      }
      @Override
      public QueryWorkflows limit(Integer limit) {
        return this;
      }
    };
  
  }
  
  @Override
  public FillBuilder fill() {
    return new FillBuilder() {
      private String path;
      private HttpMethod method;
      private Buffer body;
      @Override
      public FillBuilder path(String path) {
        this.path = path;
        return this;
      }
      @Override
      public FillBuilder method(HttpMethod method) {
        this.method = method;
        return this;
      }
      @Override
      public FillBuilder body(Buffer body) {
        this.body = body;
        return this;
      }
      @Override
      public Uni<Buffer> build() {
        return dialobClient.fill(method, path, body);
      }
    };
  }
  @Override
  public ReviewBuilder review() {
    return new ReviewBuilder() {
      private String path;
      @Override
      public ReviewBuilder path(String path) {
        this.path = path;
        return this;
      }
      @Override
      public Uni<Buffer> build() {
        return dialobClient.review(path);
      }
    };
  }
  
  @Override
  public ClientConfig config() {
    return config;
  }
  
  public static Builder builder() {
    return new Builder();
  }
  public static class Builder {
    private String formId;
    private String apiKey;
    private WebClient webClient;
    private ImmutableClientConfig.Builder config = ImmutableClientConfig.builder();
    public Builder webClient(WebClient webClient) {
      this.webClient = webClient;
      return this;
    }
    public Builder setFormId(String formId) {
      this.formId = formId;
      return this;
    }
    public Builder setApiKey(String apiKey) {
      this.apiKey = apiKey;
      return this;
    }
    public Builder config(Function<ImmutableClientConfig.Builder, ImmutableClientConfig.Builder> c) {
      this.config = c.apply(config);
      return this;
    }
    public WorkflowsClientMock build() {
      return new WorkflowsClientMock(webClient, config.build(), formId, apiKey);
    }
  }
  public MockClient getDialobClient() {
    return dialobClient;
  }
}

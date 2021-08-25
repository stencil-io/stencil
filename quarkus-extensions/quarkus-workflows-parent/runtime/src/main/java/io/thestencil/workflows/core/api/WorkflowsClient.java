package io.thestencil.workflows.core.api;

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

import javax.annotation.Nullable;

import org.immutables.value.Value;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.WebClient;

public interface WorkflowsClient {

  CreateWorkflows createUserAction();
  QueryWorkflows queryUserAction();
  CancelWorkflows cancelUserAction();
  
  FillBuilder fill();
  ReviewBuilder review();
  ClientConfig config();
  
  interface FillBuilder {
    FillBuilder path(String path);
    FillBuilder method(HttpMethod method);
    FillBuilder body(Buffer body);
    Uni<Buffer> build();
  }

  interface ReviewBuilder {
    ReviewBuilder path(String path);
    Uni<Buffer> build();
  }
  
  @Value.Immutable
  interface Workflow {
    String getId();
    String getName();
    String getStatus();
    String getReviewUri();
    String getMessagesUri();
    String getFormUri();
    String getFormId();
    @Nullable
    String getTaskId();
    Boolean getFormInProgress();
  }
  
  
  interface CreateWorkflows {
    CreateWorkflows actionName(String actionName);
    CreateWorkflows userId(String userId);
    CreateWorkflows language(String language);
    CreateWorkflows body(JsonObject body);
    Uni<Workflow> build();
  }
  interface CancelWorkflows {
    CancelWorkflows processId(String processId);
    CancelWorkflows userId(String userId);
    Uni<Workflow> build();
  }
  
  interface QueryWorkflows {
    QueryWorkflows processId(String processId);
    QueryWorkflows limit(Integer limit);
    QueryWorkflows userId(String userId);
    Multi<Workflow> list();
  }
  
  @Value.Immutable
  public interface ClientConfig {
    WebClient getWebClient();
    String getDefaultLanguage();
    
    String getServicePath();
    String getFillPath();
    String getReviewPath();
    
    RemoteIntegration getProcesses();
    RemoteIntegration getFill();
    RemoteIntegration getReview();
  }
  @Value.Immutable
  public interface RemoteIntegration {
    String getHost();
    String getPath();
  }
}

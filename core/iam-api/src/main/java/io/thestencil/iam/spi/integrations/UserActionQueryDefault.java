package io.thestencil.iam.spi.integrations;

/*-
 * #%L
 * iam-api
 * %%
 * Copyright (C) 2021 - 2022 Copyright 2021 ReSys OÜ
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.thestencil.iam.api.ImmutableUserAction;
import io.thestencil.iam.api.ImmutableUserMessage;
import io.thestencil.iam.api.UserActionsClient.Attachment;
import io.thestencil.iam.api.UserActionsClient.AttachmentQuery;
import io.thestencil.iam.api.UserActionsClient.ClientConfig;
import io.thestencil.iam.api.UserActionsClient.UserAction;
import io.thestencil.iam.api.UserActionsClient.UserActionQuery;
import io.thestencil.iam.spi.support.BuilderTemplate;
import io.thestencil.iam.spi.support.PortalAssert;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.ext.web.client.HttpResponse;



public class UserActionQueryDefault extends BuilderTemplate implements UserActionQuery {
  private static final Logger LOGGER = LoggerFactory.getLogger(UserActionQueryDefault.class);
  private final ClientConfig config;
  private final Supplier<MessagesQueryBuilderDefault> messages;
  private final Supplier<AttachmentQuery> attachments;
  private String userId;
  private String processId;
  private Integer limit;
  private String userName;
  
  public UserActionQueryDefault(
      RequestOptions init, ClientConfig config, 
      Supplier<MessagesQueryBuilderDefault> messages,
      Supplier<AttachmentQuery> attachments) {
    super(config.getWebClient(), init);
    this.config = config;
    this.messages = messages;
    this.attachments = attachments;
  }
  @Override
  public UserActionQuery userId(String userId) {
    this.userId = userId;
    return this;
  }
  @Override
  public UserActionQuery processId(String processId) {
    this.processId = processId;
    return this;
  }
  @Override
  public UserActionQuery userName(String userName) {
    this.userName = userName;
    return this;
  }
  @Override
  public UserActionQuery limit(Integer limit) {
    this.limit = limit;
    return this;
  }
  @Override
  public Multi<UserAction> list() {
    PortalAssert.notEmpty(userId, () -> "userId must be defined!");
    PortalAssert.notEmpty(userName, () -> "userName must be defined!");
    final var tasks = messages.get().getUnreadTasks(userId);
    
    if(processId != null) {
      final var process = super.get(getUri("/processes/" + processId)).send();
      
      return Uni.combine().all().unis(process, tasks).asTuple()
          .onItem()
          .transformToMulti(tuple -> 
            mapToElement(tuple.getItem1(), tuple.getItem2(), config.getFillPath(), config.getReviewPath(), config.getMessagesPath())
          )
          .onItem()
          .transformToUni(action -> addAttachments(action))
          .concatenate();
    } else {
      final var processes  = super.get(getUri("/processesSearch"))
          .addQueryParam("unpaged", "true")
          .addQueryParam("size", limit == null ? "300" : limit + "")
          .addQueryParam("userId", userId)
          .send();  
      
      return Uni.combine().all().unis(processes, tasks).asTuple()
          .onItem()
          .transformToMulti(tuple -> 
            mapToList(tuple.getItem1(), tuple.getItem2(), config.getFillPath(), config.getReviewPath(), config.getMessagesPath())
          )
          .onItem()
          .transformToUni(action -> addAttachments(action))
          .concatenate();
    }
  }

  private Uni<UserAction> addAttachments(UserAction action) {
    final var processAttachments = attachments.get().processId(action.getId());
    final var taskAttachments = action.getTaskId() == null ? 
        Uni.createFrom().item(new ArrayList<Attachment>()) : 
        attachments.get().taskId(action.getTaskId(), action.getId());
    
    return Uni.combine().all().unis(processAttachments, taskAttachments)
        .asTuple().onItem().transform(tuple -> {
          
          final var attachments = new HashMap<>(tuple.getItem1().stream().collect(Collectors.toMap(e -> e.getName(), e -> e)));
          for(final var item : tuple.getItem2()) {
            if(!attachments.containsKey(item.getName())) {
              attachments.put(item.getName(), item);
            }
          }
          
          return ImmutableUserAction.builder()
          .from(action)
          .addAllAttachments(attachments.values())
          .build();
          
        });
        
  }
  
  private Multi<UserAction> mapToElement(HttpResponse<?> resp, List<String> unreadTasks, String fillUri, String reviewUri, String replyUri) {
    if (resp.statusCode() != 200) {
      String error = "USER ACTIONS: Can't create response, e = " + resp.statusCode() + " | " + resp.statusMessage() + " | " + resp.headers();
      LOGGER.error(error);
      return Multi.createFrom().empty();
    }
    
    final JsonObject data = resp.bodyAsJsonObject();
    if(data == null) {
      return Multi.createFrom().empty();
    }
    return Multi.createFrom()
        .items(mapToUserAction(data, fillUri, reviewUri, replyUri))
        .onItem().transformToUniAndMerge(action -> createAction(action, unreadTasks));
  }
  
  private Multi<UserAction> mapToList(HttpResponse<?> resp, List<String> unreadTasks, String fillUri, String reviewUri, String replyUri) {
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
        .items(datalist.stream().map(e -> mapToUserAction((JsonObject) e, fillUri, reviewUri, replyUri)))
        .onItem().transformToUniAndMerge(action -> createAction(action, unreadTasks));
  }
  
  private Uni<UserAction> createAction(UserAction action, List<String> unreadTasks) {
    if(action.getTaskId() != null) {
      return messages.get().getTask(action.getTaskId())
          .onItem().transform(messages -> ImmutableUserAction.builder()
              .from(action)
              .messages(messages.stream()
                  .map(msg -> 
                    ImmutableUserMessage.builder().from(msg).userName(
                        msg.getUserName().equals(userName) ? userName : "" ).build()
                  ).collect(Collectors.toList()))
              .viewed(messages.isEmpty() || !unreadTasks.contains(action.getTaskId()))
              .build());
    }
    return Uni.createFrom().item(action);    
  }

  public static UserAction mapToUserAction(JsonObject entity, String fillUri, String reviewUri, String replyUri) {
    final var workflow = entity.getJsonObject("workflow");
    final var status = entity.getString("status");
    final var formInProgress = "ANSWERING".equalsIgnoreCase(status) || "CREATED".equalsIgnoreCase(status);
    final var formId = entity.getString("questionnaire");
    return ImmutableUserAction.builder()
        .id(entity.getLong("id") + "")
        .status(status)
        .name(workflow.getString("name"))
        .taskId(entity.getString("task"))
        .messagesUri(replyUri)
        .reviewUri(reviewUri)
        .formUri(fillUri)
        .formId(formId)
        .viewed(true)
        .formInProgress(formInProgress)
        .build();
  }
}

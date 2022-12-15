package io.thestencil.quarkus.useractions.handlers;

/*-
 * #%L
 * quarkus-stencil-user-actions
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

import java.util.List;
import java.util.Map;

import io.quarkus.security.identity.CurrentIdentityAssociation;
import io.quarkus.vertx.http.runtime.CurrentVertxRequest;
import io.thestencil.iam.api.IAMClient;
import io.thestencil.quarkus.useractions.UserActionsContext;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.mutiny.core.buffer.Buffer;

public class UserActionsHandler extends UserActionsTemplate {

  public UserActionsHandler(
      CurrentIdentityAssociation currentIdentityAssociation,
      CurrentVertxRequest currentVertxRequest) {
    super(currentIdentityAssociation, currentVertxRequest);
  }
  
  @Override
  protected void handleResource(RoutingContext event, HttpServerResponse response, UserActionsContext ctx, IAMClient iam) {
    response.headers().set(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");

    final var path = event.normalisedPath();
    
    if(path.startsWith(ctx.getMessagesPath())) {
      String actionId = event.request().getParam("id");

      
      iam.userQuery().get().onItem().transformToUni(client -> {

        if(event.request().method() == HttpMethod.POST) {
          final var body = new JsonObject(event.getBody());
          
          return ctx.getClient().replyTo()
            .processId(actionId)
            .userId(client.getUser().getSsn())
            .userName(getUsername(client.getUser()))
            .replyToId(body.getString("replyToId"))
            .text(body.getString("text"))
            .build().onItem().transform(data -> JsonObject.mapFrom(data).toBuffer());
        }
        
        return ctx.getClient().markUser()
          .processId(actionId)
          .userId(client.getUser().getSsn())
          .userName(getUsername(client.getUser()))
          .build().onItem().transform(data -> new JsonArray(data).toBuffer());
      })
      .onFailure().invoke(e -> catch422(e, ctx, response))
      .subscribe().with(data -> response.end(data));
      
      
    } else if(path.startsWith(ctx.getFillPath())) {
      ctx.getClient().fill()
        .path(path)
        .method(event.request().method())
        .body(body(event))
        .build()          
        .onFailure().invoke(e -> catch422(e, ctx, response))
        .subscribe().with(data -> response.end(data.toString()));
      
    } else if(path.startsWith(ctx.getReviewPath())) {
      ctx.getClient().review().path(path).build()
        .onFailure().invoke(e -> catch422(e, ctx, response))
        .subscribe().with(data -> response.end(data.toString()));

    } else if(path.startsWith(ctx.getAttachmentsPath())) {
      handleUserActionAttachments(event, response, ctx, iam);
    } else {
      handleUserAction(event, response, ctx, iam);
    }
  }
  
  private Buffer body(RoutingContext rc) {
    if(rc.getBody() == null) {
      return null;
    } else {
      return Buffer.newInstance(rc.getBody());
    }
  }

  private void handleUserActionAttachments(RoutingContext event, HttpServerResponse response, UserActionsContext ctx, IAMClient iam) {
    String actionId = event.request().getParam("actionId");
    String attachmentId = event.request().getParam("attachmentId");

    if(actionId != null && event.request().method() == HttpMethod.POST) {
      final List<Map<String, String>> files = event.getBodyAsJsonArray().getList();      
      iam.userQuery().get().onItem().transformToUni(client ->
        ctx.getClient().attachment()
        .userId(client.getUser().getSsn())
        .userName(getUsername(client.getUser()))
        .processId(actionId)
        .call(b -> files.stream()
            .forEach(item -> b.data(
                item.get("fileName"), 
                item.get("fileType")
                )))
        .build().collect().asList()
      )
      .onItem().transform(data -> new JsonArray(data).toBuffer())
      .onFailure().invoke(e -> catch422(e, ctx, response))
      .subscribe().with(data -> response.end(data));    
    } else if(actionId != null && attachmentId != null && event.request().method() == HttpMethod.GET) {
        
      iam.userQuery().get().onItem().transformToUni(client ->
        
      ctx.getClient().attachmentDownload()
        .userId(client.getUser().getSsn())
        .userName(getUsername(client.getUser()))
        .processId(actionId)
        .attachmentId(attachmentId)
        .build()
      )
      .onItem().transform(data -> JsonObject.mapFrom(data).toBuffer())
      .onFailure().invoke(e -> catch422(e, ctx, response))
      .subscribe().with(data -> response.end(data));    
      
    } else {
      catch404("unknown user action attachment", ctx, response);      
    }
  }
  
  
  private void handleUserAction(RoutingContext event, HttpServerResponse response, UserActionsContext ctx, IAMClient iam) {
    String actionId = event.request().getParam("id");
    String actionLocale = event.request().getParam("locale");

    if(actionId == null) {
      iam.userQuery().get().onItem().transformToUni(client -> 
        ctx.getClient().queryUserAction()
        .userId(client.getUser().getSsn())
        .userName(getUsername(client.getUser()))
        .list().collect().asList()
      )
      .onItem().transform(data -> new JsonArray(data).toBuffer())
      .onFailure().invoke(e -> catch422(e, ctx, response))
      .subscribe().with(data -> response.end(data));    
    } else if(event.request().method() == HttpMethod.DELETE) {
      iam.userQuery().get().onItem().transformToUni(client ->
      
        ctx.getClient().cancelUserAction()
        .processId(actionId)
        .userId(client.getUser().getSsn())
        .userName(getUsername(client.getUser()))
        .build()
      )
      .onItem().transform(data -> JsonObject.mapFrom(data).toBuffer())
      .onFailure().invoke(e -> catch422(e, ctx, response))
      .subscribe().with(data -> response.end(data));

    } else if(event.request().method() == HttpMethod.GET) {
      iam.userQuery().get().onItem().transformToUni(client ->
        ctx.getClient().createUserAction()
        .actionName(actionId)
        .protectionOrder(client.getUser().getProtectionOrder())
        .userName(client.getUser().getFirstName(), client.getUser().getLastName())
        .language(actionLocale)
        .email(client.getUser().getContact().getEmail())
        .address(client.getUser().getContact().getAddressValue())
        .userId(client.getUser().getSsn())
        .build()
      )
      .onItem().transform(data -> JsonObject.mapFrom(data).toBuffer())
      .onFailure().invoke(e -> catch422(e, ctx, response))
      .subscribe().with(data -> response.end(data));
    } else {
      catch404("unknown user action", ctx, response);
    }
     
  }
}

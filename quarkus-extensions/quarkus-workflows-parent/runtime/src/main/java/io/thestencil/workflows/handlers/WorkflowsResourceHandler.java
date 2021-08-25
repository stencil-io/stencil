package io.thestencil.workflows.handlers;

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

import io.quarkus.security.identity.CurrentIdentityAssociation;
import io.quarkus.vertx.http.runtime.CurrentVertxRequest;
import io.thestencil.workflows.WorkflowsContext;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.mutiny.core.buffer.Buffer;

public class WorkflowsResourceHandler extends HdesResourceHandler {

  public WorkflowsResourceHandler(
      CurrentIdentityAssociation currentIdentityAssociation,
      CurrentVertxRequest currentVertxRequest) {
    super(currentIdentityAssociation, currentVertxRequest);
  }
  
  @Override
  protected void handleResource(RoutingContext event, HttpServerResponse response, WorkflowsContext ctx) {
    response.headers().set(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");

    final var path = event.normalizedPath();
    
     if(path.startsWith(ctx.getFillPath())) {
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
          
    } else {
      handleAction(event, response, ctx);
    }      
  }
  
  private Buffer body(RoutingContext rc) {
    if(rc.getBody() == null) {
      return null;
    } else {
      return Buffer.newInstance(rc.getBody());
    }
  }
  
  private void handleAction(RoutingContext event, HttpServerResponse response, WorkflowsContext ctx) {
    String actionId = event.request().getParam("id");
    String userId = event.request().getParam("userId");
    String actionLocale = event.request().getParam("locale");
    
    if(actionId == null) {
      ctx.getClient().queryUserAction().userId(userId).list().collect().asList()
      .onItem().transform(data -> new JsonArray(data).toBuffer())
      .onFailure().invoke(e -> catch422(e, ctx, response))
      .subscribe().with(data -> response.end(data));    
    } else if(event.request().method() == HttpMethod.DELETE) {
      ctx.getClient().cancelUserAction()
      .processId(actionId)
      .userId(userId)
      .build()
      .onItem().transform(data -> JsonObject.mapFrom(data).toBuffer())
      .onFailure().invoke(e -> catch422(e, ctx, response))
      .subscribe().with(data -> response.end(data));

    } else if(event.request().method() == HttpMethod.POST) {
      
      ctx.getClient().createUserAction()
      .actionName(actionId)
      .language(actionLocale)
      .body(new JsonObject(event.getBody()))
      .build()
      .onItem().transform(data -> JsonObject.mapFrom(data).toBuffer())
      .onFailure().invoke(e -> catch422(e, ctx, response))
      .subscribe().with(data -> response.end(data));
      
    } else {
      catch404("unsupported action", ctx, response);
    }
     
  }
}

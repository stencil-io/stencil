package io.thestencil.quarkus.iam.handler;

/*-
 * #%L
 * quarkus-stencil-iam
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



import io.quarkus.security.identity.CurrentIdentityAssociation;
import io.quarkus.vertx.http.runtime.CurrentVertxRequest;
import io.thestencil.iam.api.IAMClient;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;


public class IAMHandler extends IAMTemplate {

  private String liveness;
  
  public IAMHandler(
      CurrentIdentityAssociation currentIdentityAssociation,
      CurrentVertxRequest currentVertxRequest, String liveness) {
    super(currentIdentityAssociation, currentVertxRequest);
    this.liveness = liveness;
  }
  
  @Override
  protected void handleResource(RoutingContext event, HttpServerResponse response, IAMClient ctx) {
    
    if(event.request().method() != HttpMethod.GET) {
      return;
    }
    
    final var path = event.normalizedPath();
    if(path.startsWith(liveness)) {
      ctx.livenessQuery().get()
      .onItem().transform(data -> JsonObject.mapFrom(data).toBuffer())
      .onFailure().invoke(e -> catch422(e, ctx, response))
      .subscribe().with(data -> response.end(data));
    } else {
      ctx.userQuery().get()
      .onItem().transform(data -> JsonObject.mapFrom(data).toBuffer())
      .onFailure().invoke(e -> catch422(e, ctx, response))
      .subscribe().with(data -> response.end(data));
    }
  
  }
}

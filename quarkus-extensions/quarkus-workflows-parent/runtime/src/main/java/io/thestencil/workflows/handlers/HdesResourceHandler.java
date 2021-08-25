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

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.inject.spi.CDI;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.arc.Arc;
import io.quarkus.arc.ManagedContext;
import io.quarkus.security.identity.CurrentIdentityAssociation;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.vertx.http.runtime.CurrentVertxRequest;
import io.quarkus.vertx.http.runtime.security.QuarkusHttpUser;
import io.thestencil.workflows.WorkflowsContext;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;


public abstract class HdesResourceHandler implements Handler<RoutingContext> {
  private static final Logger LOGGER = LoggerFactory.getLogger(HdesResourceHandler.class);
  private final CurrentIdentityAssociation currentIdentityAssociation;
  private final CurrentVertxRequest currentVertxRequest;
  
  public HdesResourceHandler(
      CurrentIdentityAssociation currentIdentityAssociation,
      CurrentVertxRequest currentVertxRequest) {
    super();
    this.currentIdentityAssociation = currentIdentityAssociation;
    this.currentVertxRequest = currentVertxRequest;
  }
  
  protected abstract void handleResource(RoutingContext event, HttpServerResponse response, WorkflowsContext ctx);
  
  protected void handleSecurity(RoutingContext event) {
    if (currentIdentityAssociation != null) {
      QuarkusHttpUser existing = (QuarkusHttpUser) event.user();
      if (existing != null) {
        SecurityIdentity identity = existing.getSecurityIdentity();
        currentIdentityAssociation.setIdentity(identity);
      } else {
        currentIdentityAssociation.setIdentity(QuarkusHttpUser.getSecurityIdentity(event, null));
      }
    }
    currentVertxRequest.setCurrent(event);
  }
  
  @Override
  public void handle(RoutingContext event) {
    ManagedContext requestContext = Arc.container().requestContext();
    if (requestContext.isActive()) {
      handleSecurity(event);      
      HttpServerResponse response = event.response();
      WorkflowsContext ctx = CDI.current().select(WorkflowsContext.class).get();
      try {
        handleResource(event, response, ctx);
      } catch (Exception e) {
        catch422(e, ctx, response);
      }
     return; 
    }
    
    HttpServerResponse response = event.response();
    WorkflowsContext ctx = CDI.current().select(WorkflowsContext.class).get();
    try {
      requestContext.activate();
      handleSecurity(event);
      handleResource(event, response, ctx);
    } finally {
      requestContext.terminate();
    }
  }
  
  public static void catch404(String id, WorkflowsContext ctx, HttpServerResponse response) {
    
    // Log error
    String log = new StringBuilder().append("Token not found with id: ").append(id).toString();
    String hash = exceptionHash(log);
    LOGGER.error(hash + " - " + log);
    
    Map<String, String> msg = new HashMap<>();
    msg.put("appcode", hash);
    
    response.headers().set(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");
    response.setStatusCode(404);
    response.end( Json.encode(msg) );
  }
  
  public static void catch422(Throwable e, WorkflowsContext ctx, HttpServerResponse response) {
    String stack = ExceptionUtils.getStackTrace(e);
    
    // Log error
    String log = new StringBuilder().append(e.getMessage()).append(System.lineSeparator()).append(stack).toString();
    String hash = exceptionHash(log);
    LOGGER.error(hash + " - " + log);
    
    Map<String, String> msg = new HashMap<>();
    msg.put("appcode", hash);
    
    response.headers().set(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");
    response.setStatusCode(422);
    response.end( Json.encode(msg) );
  }

  public static String exceptionHash(String msg) {
    try {
      MessageDigest md5 = MessageDigest.getInstance("MD5");
      md5.reset();
      md5.update(msg.getBytes(Charset.forName("UTF-8")));
      byte[] digest = md5.digest();
      return Hex.encodeHexString(digest);
    } catch (NoSuchAlgorithmException ex) {
      // Fall back to just hex timestamp in this improbable situation
      LOGGER.warn("MD5 Digester not found, falling back to timestamp hash", ex);
      long timestamp = System.currentTimeMillis();
      return Long.toHexString(timestamp);
    }
  }
}

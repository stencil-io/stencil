package io.thestencil.client.web;

/*-
 * #%L
 * quarkus-stencil-ide-services
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

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.security.identity.CurrentIdentityAssociation;
import io.quarkus.vertx.http.runtime.CurrentVertxRequest;
import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.ImmutableArticleMutator;
import io.thestencil.client.api.ImmutableCreateArticle;
import io.thestencil.client.api.ImmutableCreateLink;
import io.thestencil.client.api.ImmutableCreateLocale;
import io.thestencil.client.api.ImmutableCreatePage;
import io.thestencil.client.api.ImmutableCreateRelease;
import io.thestencil.client.api.ImmutableCreateWorkflow;
import io.thestencil.client.api.ImmutableLinkArticlePage;
import io.thestencil.client.api.ImmutableLinkMutator;
import io.thestencil.client.api.ImmutableLocaleMutator;
import io.thestencil.client.api.ImmutableWorkflowArticlePage;
import io.thestencil.client.api.ImmutableWorkflowMutator;
import io.thestencil.client.api.UpdateBuilder.PageMutator;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class HandlerComposer extends HandlerTemplate {

  public HandlerComposer(CurrentIdentityAssociation currentIdentityAssociation, CurrentVertxRequest currentVertxRequest) {
    super(currentIdentityAssociation, currentVertxRequest);
  }

  @Override
  protected void handleResource(RoutingContext event, HttpServerResponse response, HandlerContext ctx, ObjectMapper objectMapper) {
    response.headers().set(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");
    final var path = getPath(event);
    final var client = ctx.getClient();
    
    if(path.endsWith(ctx.getPaths().getServicePath())) {
      if (event.request().method() == HttpMethod.POST) {
        client.create().repo()
        .onItem().transform(data -> JsonObject.mapFrom(data).toBuffer())
        .onFailure().invoke(e -> catch422(e, ctx, response))
        .subscribe().with(data -> response.end(data));
        
      } else if(event.request().method() == HttpMethod.GET) { 
        subscribe(
            client.query().head(), 
            response, ctx, objectMapper);
      } else {
        catch404("unsupported repository action", ctx, response);
      }
      
    } else if (path.startsWith(ctx.getPaths().getArticlesPath())) {
      
      // ARTICLES
      
      if (event.request().method() == HttpMethod.POST) {
        subscribe(
            client.create().article(read(event, objectMapper, ImmutableCreateArticle.class)), 
            response, ctx, objectMapper);
        
      } else if(event.request().method() == HttpMethod.PUT) {
        subscribe(
            client.update().article(read(event, objectMapper, ImmutableArticleMutator.class)),
            response, ctx, objectMapper);
      } else if(event.request().method() == HttpMethod.DELETE) {
        subscribe(
            client.delete().article(event.pathParam("id")),
            response, ctx, objectMapper);
      } else {
        catch404("unsupported article action", ctx, response);
      }
      
      
    } else if(path.startsWith(ctx.getPaths().getLinksPath())) {
      
      // LINKS
      
      if (event.request().method() == HttpMethod.POST) {
        subscribe(
            client.create().link(read(event, objectMapper, ImmutableCreateLink.class)), 
            response, ctx, objectMapper);
        
      } else if(event.request().method() == HttpMethod.PUT) {
        subscribe(
            client.update().link(read(event, objectMapper, ImmutableLinkMutator.class)),
            response, ctx, objectMapper);
      } else if(event.request().method() == HttpMethod.DELETE) {
        
        final var linkId = event.pathParam("id");
        final var articleId = event.queryParam("articleId");
        
        if(articleId.isEmpty()) {
          subscribe(
              client.delete().link(linkId),
              response, ctx, objectMapper);
        } else {
          subscribe(
              client.delete().linkArticlePage(ImmutableLinkArticlePage.builder()
                  .articleId(articleId.iterator().next())
                  .linkId(linkId)
                  .build()),
              response, ctx, objectMapper);  
        }
        
      } else {
        catch404("unsupported links action", ctx, response);
      }
    
      
    } else if(path.startsWith(ctx.getPaths().getLocalePath())) {
      

      
      // LOCALES
      
      if (event.request().method() == HttpMethod.POST) {
        subscribe(
            client.create().locale(read(event, objectMapper, ImmutableCreateLocale.class)), 
            response, ctx, objectMapper);
        
      } else if(event.request().method() == HttpMethod.PUT) {
        subscribe(
            client.update().locale(read(event, objectMapper, ImmutableLocaleMutator.class)),
            response, ctx, objectMapper);
      } else if(event.request().method() == HttpMethod.DELETE) {
        subscribe(
            client.delete().locale(event.pathParam("id")),
            response, ctx, objectMapper);
      } else {
        catch404("unsupported locale action", ctx, response);
      }
      
      
    } else if(path.startsWith(ctx.getPaths().getReleasesPath())) {

      // RELEASES
      
      if (event.request().method() == HttpMethod.POST) {
        subscribe(
            client.create().release(read(event, objectMapper, ImmutableCreateRelease.class)), 
            response, ctx, objectMapper);
      } else if(event.request().method() == HttpMethod.GET) {
        subscribe(
            client.query().release(event.pathParam("id")), 
            response, ctx, objectMapper);        
      } else {
        catch404("unsupported release action", ctx, response);
      }
      
    } else if(path.startsWith(ctx.getPaths().getWorkflowsPath())) {
      
          
      // WORKFLOWS
      
      if (event.request().method() == HttpMethod.POST) {
        subscribe(
            client.create().workflow(read(event, objectMapper, ImmutableCreateWorkflow.class)), 
            response, ctx, objectMapper);
        
      } else if(event.request().method() == HttpMethod.PUT) {
        subscribe(
            client.update().workflow(read(event, objectMapper, ImmutableWorkflowMutator.class)),
            response, ctx, objectMapper);
      } else if(event.request().method() == HttpMethod.DELETE) {
        
        
        final var workflowId = event.pathParam("id");
        final var articleId = event.queryParam("articleId");
        
        if(articleId.isEmpty()) {
          subscribe(
              client.delete().workflow(workflowId),
              response, ctx, objectMapper);
          
        } else {
          subscribe(
              client.delete().workflowArticlePage(ImmutableWorkflowArticlePage.builder()
                  .articleId(articleId.iterator().next())
                  .workflowId(workflowId)
                  .build()),
              response, ctx, objectMapper);  
        }

      } else {
        catch404("unsupported workflow action", ctx, response);
      }
      
      
    } else if(path.startsWith(ctx.getPaths().getPagesPath())) {
      
      // PAGES
      
      if (event.request().method() == HttpMethod.POST) {
        subscribe(
            client.create().page(read(event, objectMapper, ImmutableCreatePage.class)),
            response, ctx, objectMapper);
      } else if(event.request().method() == HttpMethod.PUT) {
        
        
        try {
          List<PageMutator> pages = objectMapper.readValue(event.getBody().getBytes(), new TypeReference<List<PageMutator>>(){});
          
          subscribe(
              client.update().pages(pages),
              response, ctx, objectMapper);
        } catch(IOException e) {
          throw new RuntimeException(e.getMessage(), e);
        }
        
      } else if(event.request().method() == HttpMethod.DELETE) {
        subscribe(
            client.delete().page(event.pathParam("id")),
            response, ctx, objectMapper);
 
      } else {
        catch404("unsupported page action", ctx, response);
      }
    } else {
      catch404("unsupported action", ctx, response);
    }
  }
  
  public String getPath(RoutingContext event) {
    final var path = event.normalizedPath();
    
    return path.endsWith("/") ? path.substring(0, path.length() -1) : path;
  }
  
  public <T> T read(RoutingContext event, ObjectMapper objectMapper, Class<T> type) {
    
   // return new JsonObject(event.getBody()).mapTo(type);
    try {
      return objectMapper.readValue(event.getBody().getBytes(), type);
    } catch(IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
  
  public <T> List<T> readList(RoutingContext event, ObjectMapper objectMapper, Class<T> type) {
    
   // return new JsonObject(event.getBody()).mapTo(type);
    try {
      return objectMapper.readValue(event.getBody().getBytes(), new TypeReference<List<T>>(){});
    } catch(IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  public <T> void subscribe(Uni<T> uni, HttpServerResponse response, HandlerContext ctx, ObjectMapper objectMapper) {
    uni.onItem().transform(data -> {
      try {
        return Buffer.buffer(objectMapper.writeValueAsBytes(data));
      } catch(IOException e) {
        throw new RuntimeException(e.getMessage(), e);
      }
    })
    .onFailure().invoke(e -> catch422(e, ctx, response))
    .subscribe().with(data -> response.end(data)); 
  }
}

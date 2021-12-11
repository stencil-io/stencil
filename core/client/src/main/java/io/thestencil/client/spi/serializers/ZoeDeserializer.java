package io.thestencil.client.spi.serializers;

/*-
 * #%L
 * stencil-persistence
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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.thestencil.client.api.StencilClient.Article;
import io.thestencil.client.api.StencilClient.Entity;
import io.thestencil.client.api.StencilClient.EntityBody;
import io.thestencil.client.api.StencilClient.EntityType;
import io.thestencil.client.api.StencilClient.Link;
import io.thestencil.client.api.StencilClient.Locale;
import io.thestencil.client.api.StencilClient.Page;
import io.thestencil.client.api.StencilClient.Release;
import io.thestencil.client.api.StencilClient.Template;
import io.thestencil.client.api.StencilClient.Workflow;
import io.thestencil.client.spi.PersistenceConfig;

public class ZoeDeserializer implements PersistenceConfig.Deserializer {

  private ObjectMapper objectMapper;
  
  public ZoeDeserializer(ObjectMapper objectMapper) {
    super();
    this.objectMapper = objectMapper;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends EntityBody> Entity<T> fromString(EntityType entityType, String value) {
    try {
      switch(entityType) {
        case ARTICLE: {
          return (Entity<T>) objectMapper.readValue(value, new TypeReference<Entity<Article>>() {});  
        }
        case LINK: {
          return (Entity<T>) objectMapper.readValue(value, new TypeReference<Entity<Link>>() {});  
        }
        case LOCALE: {
          return (Entity<T>) objectMapper.readValue(value, new TypeReference<Entity<Locale>>() {});  
        }
        case PAGE: {
          return (Entity<T>) objectMapper.readValue(value, new TypeReference<Entity<Page>>() {});  
        }
        case RELEASE: {
          return (Entity<T>) objectMapper.readValue(value, new TypeReference<Entity<Release>>() {});  
        }
        case WORKFLOW: {
          return (Entity<T>) objectMapper.readValue(value, new TypeReference<Entity<Workflow>>() {});  
        }
        case TEMPLATE: {
          return (Entity<T>) objectMapper.readValue(value, new TypeReference<Entity<Template>>() {});  
        }
        default: throw new RuntimeException("can't map: " + entityType);
      }
      
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  @Override
  public Entity<?> fromString(String value) {
    try {
      JsonNode node = objectMapper.readValue(value, JsonNode.class);
      final EntityType type = EntityType.valueOf(node.get("type").textValue());

      switch (type) {
      case ARTICLE: {
        return objectMapper.convertValue(node, new TypeReference<Entity<Article>>() {});
      }
      case LINK: {
        return objectMapper.convertValue(node, new TypeReference<Entity<Link>>() {});
      }
      case LOCALE: {
        return objectMapper.convertValue(node, new TypeReference<Entity<Locale>>() {});
      }
      case PAGE: {
        return objectMapper.convertValue(node, new TypeReference<Entity<Page>>() {});
      }
      case RELEASE: {
        return objectMapper.convertValue(node, new TypeReference<Entity<Release>>() {});
      }
      case WORKFLOW: {
        return objectMapper.convertValue(node, new TypeReference<Entity<Workflow>>() {});
      }
      case TEMPLATE: {
        return objectMapper.convertValue(node, new TypeReference<Entity<Template>>() {});
      }
      default:
        throw new RuntimeException("can't map: " + node);
      }

    } catch (Exception e) {
      throw new RuntimeException(e.getMessage() + System.lineSeparator() + value, e);
    }
  }
}

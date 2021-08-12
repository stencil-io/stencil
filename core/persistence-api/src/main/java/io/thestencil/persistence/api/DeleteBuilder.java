package io.thestencil.persistence.api;

/*-
 * #%L
 * stencil-persistence-api
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

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.smallrye.mutiny.Uni;
import io.thestencil.persistence.api.ZoePersistence.Article;
import io.thestencil.persistence.api.ZoePersistence.Entity;
import io.thestencil.persistence.api.ZoePersistence.Link;
import io.thestencil.persistence.api.ZoePersistence.Locale;
import io.thestencil.persistence.api.ZoePersistence.Page;
import io.thestencil.persistence.api.ZoePersistence.Workflow;

public interface DeleteBuilder {

  Uni<Entity<Locale>> locale(String localeId);
  Uni<Entity<Article>> article(String articleId);
  Uni<Entity<Page>> page(String pageId);
  Uni<Entity<Link>> link(String linkId);
  Uni<Entity<Link>> linkArticlePage(LinkArticlePage linkArticlePage);
  Uni<Entity<Workflow>> workflow(String workflowId);
  Uni<Entity<Workflow>> workflowArticlePage(WorkflowArticlePage workflowArticlePage);

  
  @Value.Immutable
  @JsonSerialize(as = ImmutableLinkArticlePage.class)
  @JsonDeserialize(as = ImmutableLinkArticlePage.class)
  interface LinkArticlePage {
    String getLinkId(); 
    String getArticleId();
  }
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableWorkflowArticlePage.class)
  @JsonDeserialize(as = ImmutableWorkflowArticlePage.class)
  interface WorkflowArticlePage {
    String getWorkflowId();
    String getArticleId();
  }

}

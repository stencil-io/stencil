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

import java.util.List;

import javax.annotation.Nullable;

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

public interface UpdateBuilder {
  
  Uni<Entity<Article>> article(ArticleMutator changes);
  Uni<Entity<Locale>> locale(LocaleMutator changes);
  Uni<Entity<Page>> page(PageMutator changes);
  Uni<List<Entity<Page>>> pages(List<PageMutator> changes);
  Uni<Entity<Link>> link(LinkMutator changes);
  Uni<Entity<Workflow>> workflow(WorkflowMutator changes);

  @Value.Immutable
  @JsonSerialize(as = ImmutableLocaleMutator.class)
  @JsonDeserialize(as = ImmutableLocaleMutator.class)
  interface LocaleMutator {
    String getLocaleId(); 
    String getValue();
    Boolean getEnabled();
  }
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableArticleMutator.class)
  @JsonDeserialize(as = ImmutableArticleMutator.class)
  interface ArticleMutator {
    String getArticleId();
    @Nullable
    String getParentId();
    String getName();
    Integer getOrder();
  }
  @Value.Immutable
  @JsonSerialize(as = ImmutablePageMutator.class)
  @JsonDeserialize(as = ImmutablePageMutator.class)
  interface PageMutator {
    String getPageId();
    String getContent();
    String getLocale();
  }
  @Value.Immutable
  @JsonSerialize(as = ImmutableLinkMutator.class)
  @JsonDeserialize(as = ImmutableLinkMutator.class)
  interface LinkMutator {
    String getLinkId();
    String getContent(); 
    String getLocale(); 
    String getDescription();
    String getType();
    List<String> getArticles();
  }
  @Value.Immutable
  @JsonSerialize(as = ImmutableWorkflowMutator.class)
  @JsonDeserialize(as = ImmutableWorkflowMutator.class)
  interface WorkflowMutator {
    String getWorkflowId(); 
    String getName(); 
    String getLocale(); 
    String getContent();
    List<String> getArticles();
  }
}

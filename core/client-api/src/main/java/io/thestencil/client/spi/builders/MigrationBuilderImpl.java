package io.thestencil.client.spi.builders;

/*-
 * #%L
 * stencil-client
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

import io.resys.thena.docdb.api.actions.CommitActions.CommitStatus;
import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.MigrationBuilder;
import io.thestencil.client.api.StencilComposer.SiteContentType;
import io.thestencil.client.api.StencilComposer.SiteState;
import io.thestencil.client.spi.PersistenceConfig;
import io.thestencil.client.spi.exceptions.ImportException;

public class MigrationBuilderImpl implements MigrationBuilder {

  private final PersistenceConfig config;
  
  public MigrationBuilderImpl(PersistenceConfig config) {
    super();
    this.config = config;
  }

  @Override
  public Uni<SiteState> importData(Sites sites) {
    final Uni<SiteState> query = new QueryBuilderImpl(config).head();
    
    return query.onItem().transformToUni(site -> {
      
      final var builder = new MigrationImportVisitorForStaticContent(config, site).visit(sites).head(config.getRepoName(), config.getHeadName());
      
      if(site.getContentType() == SiteContentType.OK) {
        builder.parent(site.getCommit());
      } else {
        builder.parentIsLatest();
      }
      
      return builder 
      .message("import-sites")
      .author(config.getAuthorProvider().getAuthor())
      .build().onItem().transformToUni(commit -> {
        if(commit.getStatus() == CommitStatus.OK) {
          return new QueryBuilderImpl(config).head();
        }
        throw new ImportException(sites, commit);
      });
    })
    ;
  }

  @Override
  public Uni<SiteState> importData(SiteState sites) {
    final Uni<SiteState> query = new QueryBuilderImpl(config).head();
    
    return query.onItem().transformToUni(site -> {
      
      final var builder = new MigrationImportVisitorForSiteState(config, site).visit(sites).head(config.getRepoName(), config.getHeadName());
      
      if(site.getContentType() == SiteContentType.OK) {
        builder.parent(site.getCommit());
      } else {
        builder.parentIsLatest();
      }
      
      return builder 
      .message("import-sites")
      .author(config.getAuthorProvider().getAuthor())
      .build().onItem().transformToUni(commit -> {
        if(commit.getStatus() == CommitStatus.OK) {
          return new QueryBuilderImpl(config).head();
        }
        throw new ImportException(sites, commit);
      });
    })
    ;
  }
}

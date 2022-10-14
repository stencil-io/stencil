package io.thestencil.client.spi;

/*-
 * #%L
 * stencil-client
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


import io.resys.thena.docdb.api.actions.RepoActions.RepoStatus;
import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.StencilClient;
import io.thestencil.client.api.StencilClient.ClientRepoBuilder;
import io.thestencil.client.spi.exceptions.RepoException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ClientRepoBuilderImpl implements ClientRepoBuilder {
  private final PersistenceConfig config;
  
  private String repoName;
  private String headName;
  @Override
  public ClientRepoBuilder repoName(String repoName) {
    this.repoName = repoName;
    return this;
  }
  @Override
  public ClientRepoBuilder headName(String headName) {
    this.headName = headName;
    return this;
  }
  @Override
  public Uni<StencilClient> create() {
    StencilAssert.notNull(repoName, () -> "repoName must be defined!");
    return config.getClient().repo().create().name(repoName).build()
        .onItem().transform(repoResult -> {
          if(repoResult.getStatus() != RepoStatus.OK) {
            throw new RepoException("Can't create repository with name: '"  + repoName + "'!", repoResult);  
          }
          final var newConfig = ImmutablePersistenceConfig.builder().from(config).repoName(repoName).headName(headName).build();
          return new StencilClientImpl(newConfig);
        });
  }
  @Override
  public StencilClient build() {
    StencilAssert.notNull(repoName, () -> "repoName must be defined!");
    final var newConfig = ImmutablePersistenceConfig.builder().from(config).repoName(repoName).headName(headName).build();
    return new StencilClientImpl(newConfig);
  }
};

package io.thestencil.client.spi;

/*-
 * #%L
 * stencil-client-api
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

import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.StencilClient;
import io.thestencil.client.api.StencilStore;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StencilClientImpl implements StencilClient {
  private final StencilStore store;
  
  @Override
  public StencilTypesMapper mapper() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ClientRepoBuilder repo() {
    return new ClientRepoBuilder() {
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
      return store.repo().repoName(repoName).headName(headName).create()
          .onItem().transform(newConfig -> {
            return new StencilClientImpl(newConfig);
          });
    }
    @Override
    public StencilClient build() {
      StencilAssert.notNull(repoName, () -> "repoName must be defined!");
      final var newConfig = store.repo().repoName(repoName).headName(headName).build();
      return new StencilClientImpl(newConfig);
    }
  };
  }

  @Override
  public StencilStore getStore() {
    return store;
  }

}

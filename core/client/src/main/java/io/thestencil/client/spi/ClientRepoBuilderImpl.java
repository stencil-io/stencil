package io.thestencil.client.spi;


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

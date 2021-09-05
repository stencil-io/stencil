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

public class ServicesPathConfig {
  
  private final String servicePath;
  private final String migrationPath;
  private final String articlesPath;
  private final String pagesPath;
  private final String workflowsPath;
  private final String linksPath;
  private final String releasesPath;
  private final String localePath;
  
  public ServicesPathConfig(
      String servicePath, 
      String migrationPath,
      String articlesPath, 
      String pagesPath, 
      String workflowsPath,
      String linksPath, 
      String releasesPath,
      String localePath) {
    super();
    this.migrationPath = migrationPath;
    this.servicePath = servicePath;
    this.articlesPath = articlesPath;
    this.pagesPath = pagesPath;
    this.workflowsPath = workflowsPath;
    this.linksPath = linksPath;
    this.releasesPath = releasesPath;
    this.localePath = localePath;
  }

  public String getServicePath() {
    return servicePath;
  }
  public String getArticlesPath() {
    return articlesPath;
  }
  public String getPagesPath() {
    return pagesPath;
  }
  public String getWorkflowsPath() {
    return workflowsPath;
  }
  public String getLinksPath() {
    return linksPath;
  }
  public String getReleasesPath() {
    return releasesPath;
  }
  public String getLocalePath() {
    return localePath;
  }
  public String getMigrationPath() {
    return migrationPath;
  }

  
  public static Builder builder() {
    return new Builder();
  }
  
  public static class Builder {
    private String servicePath;
    private String articlesPath; 
    private String pagesPath;
    private String workflowsPath;
    private String linksPath;
    private String releasesPath;
    private String localePath;
    private String migrationPath;
    public Builder servicePath(String servicePath) {
      this.servicePath = servicePath;
      return this;
    }
    public Builder articlesPath(String articlesPath) {
      this.articlesPath = articlesPath;
      return this;
    }
    public Builder pagesPath(String pagesPath) {
      this.pagesPath = pagesPath;
      return this;
    }
    public Builder workflowsPath(String workflowsPath) {
      this.workflowsPath = workflowsPath;
      return this;
    }
    public Builder linksPath(String linksPath) {
      this.linksPath = linksPath;
      return this;
    }
    public Builder releasesPath(String releasesPath) {
      this.releasesPath = releasesPath;
      return this;
    }
    public Builder localePath(String localePath) {
      this.localePath = localePath;
      return this;
    }
    public Builder migrationPath(String migrationPath) {
      this.migrationPath = migrationPath;
      return this;
    }

    
    public ServicesPathConfig build() {
      return new ServicesPathConfig(servicePath, migrationPath, articlesPath, pagesPath, workflowsPath, linksPath, releasesPath, localePath);
    }
  }
}

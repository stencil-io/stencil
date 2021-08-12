package io.thestencil.staticontent.spi.beans;

/*-
 * #%L
 * stencil-static-content
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

import java.util.Map;

import io.thestencil.staticontent.api.SiteContent;

public class MutableStaticContent implements SiteContent {

  private Long created;
  private Map<String, Site> sites;
  public MutableStaticContent() {
  }
  public MutableStaticContent(Long created, Map<String, Site> sites) {
    super();
    this.created = created;
    this.sites = sites;
  }
  public Long getCreated() {
    return created;
  }
  public Map<String, Site> getSites() {
    return sites;
  }
  public void setCreated(Long created) {
    this.created = created;
  }
  public void setSites(Map<String, Site> sites) {
    this.sites = sites;
  }
}

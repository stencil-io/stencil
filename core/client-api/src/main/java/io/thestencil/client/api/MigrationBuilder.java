package io.thestencil.client.api;

/*-
 * #%L
 * stencil-client-api
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
import java.util.Map;

import javax.annotation.Nullable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.StencilClient.SiteState;
import io.thestencil.client.api.beans.LocalizedSiteBean;
import io.thestencil.client.api.beans.TopicBean;
import io.thestencil.client.api.beans.TopicBlobBean;
import io.thestencil.client.api.beans.TopicHeadingBean;
import io.thestencil.client.api.beans.TopicLinkBean;

public interface MigrationBuilder {

  Uni<SiteState> importData(Sites sites);


  interface Sites {
    Long getCreated();
    Map<String, LocalizedSite> getSites();
  }

  @JsonDeserialize(as = LocalizedSiteBean.class)
  interface LocalizedSite {
    String getId();
    String getImages();
    String getLocale();
    
    Map<String, Topic> getTopics();
    Map<String, TopicBlob> getBlobs();
    Map<String, TopicLink> getLinks();
  }

  @JsonDeserialize(as = TopicBlobBean.class)
  interface TopicBlob {
    String getId();
    String getValue();
  }

  @JsonDeserialize(as = TopicBean.class)
  interface Topic {
    String getId();
    String getName();
    List<String> getLinks();
    List<TopicHeading> getHeadings();
    @Nullable
    String getParent();
    @Nullable
    String getBlob();
  }

  @JsonDeserialize(as = TopicHeadingBean.class)
  interface TopicHeading {
    String getId();
    String getName();
    Integer getOrder();
    Integer getLevel();
  }
  
  @JsonDeserialize(as = TopicLinkBean.class)
  interface TopicLink {
    String getId();
    String getType();
    String getName();
    String getValue();
    Boolean getGlobal();
    Boolean getWorkflow();
  }
}

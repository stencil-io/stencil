package io.thestencil.workflows;

/*-
 * #%L
 * quarkus-stencil-workflows-deployment
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

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(name = WorkflowsRecorder.FEATURE_BUILD_ITEM)
public class WorkflowsConfig {
  
  /**
   * Static content routing path
   */
  @ConfigItem(defaultValue = "portal-app/workflows")
  String servicePath;
  
  /**
   * Configuration for mock envir
   */
  @ConfigItem
  WorkflowsMockConfig mock;
}

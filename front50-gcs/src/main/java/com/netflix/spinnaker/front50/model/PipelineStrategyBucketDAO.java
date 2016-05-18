/*
 * Copyright 2016 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.spinnaker.front50.model;

import com.google.api.services.storage.Storage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.spinnaker.front50.exception.NotFoundException;
import com.netflix.spinnaker.front50.model.pipeline.Pipeline;
import com.netflix.spinnaker.front50.model.pipeline.PipelineStrategyDAO;
import rx.Scheduler;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

public class PipelineStrategyBucketDAO extends BucketDAO<Pipeline> implements PipelineStrategyDAO {
  public PipelineStrategyBucketDAO(String basePath,
                                   StorageService service,
                                   Scheduler scheduler,
                                   int refreshIntervalMs) {
      super(Pipeline.class, "pipeline-strategies",
            basePath, service, scheduler, refreshIntervalMs);
  }

  @Override
  public Collection<Pipeline> getPipelineHistory(String name, int maxResults) {
    return allVersionsOf(name, maxResults);
  }

  @Override
  public String getPipelineId(String application, String pipelineName) {
    Pipeline matched = getPipelinesByApplication(application)
        .stream()
        .filter(pipeline -> pipeline.getName().equalsIgnoreCase(pipelineName))
        .findFirst()
        .orElseThrow(() -> new NotFoundException(
            String.format("No pipeline strategy found with name '%s' in application '%s'", pipelineName, application)
        ));

    return matched.getId();
  }

  @Override
  public Collection<Pipeline> getPipelinesByApplication(String application) {
    return all()
        .stream()
        .filter(pipelineStrategy -> pipelineStrategy.getApplication().equalsIgnoreCase(application))
        .collect(Collectors.toList());
  }

  @Override
  public Pipeline create(String id, Pipeline item) {
    if (id == null) {
      id = UUID.randomUUID().toString();
    }
    item.setId(id);

    update(id, item);
    return findById(id);
  }
}

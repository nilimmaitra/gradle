/*
 * Copyright 2022 the original author or authors.
 *
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
 */

package org.gradle.internal.component.model;

import org.gradle.api.artifacts.ModuleVersionIdentifier;
import org.gradle.api.internal.artifacts.ivyservice.resolveengine.artifact.ArtifactSetFactory;
import org.gradle.api.internal.artifacts.ivyservice.resolveengine.artifact.ResolvedVariant;
import org.gradle.api.internal.artifacts.ivyservice.resolveengine.excludes.specs.ExcludeSpec;
import org.gradle.api.internal.artifacts.type.ArtifactTypeRegistry;
import org.gradle.internal.model.CalculatedValueContainerFactory;
import org.gradle.internal.resolve.resolver.ArtifactResolver;

import java.util.HashMap;
import java.util.Map;

public class ResolvedVariantCache {
    private final Map<VariantResolveMetadata.Identifier, ResolvedVariant> variantCache;
    private final ArtifactTypeRegistry artifactTypeRegistry;
    private final CalculatedValueContainerFactory calculatedValueContainerFactory;

    public ResolvedVariantCache(ArtifactTypeRegistry artifactTypeRegistry, CalculatedValueContainerFactory calculatedValueContainerFactory) {
        this.artifactTypeRegistry = artifactTypeRegistry;
        this.calculatedValueContainerFactory = calculatedValueContainerFactory;
        variantCache = new HashMap<>();
    }

    public ResolvedVariant getOrCompute(VariantResolveMetadata variant, ModuleVersionIdentifier ownerId, ModuleSources moduleSources, ExcludeSpec exclusions, ArtifactResolver artifactResolver) {
        return variantCache.computeIfAbsent(variant.getIdentifier(), identifier -> ArtifactSetFactory.toResolvedVariant(variant, ownerId, moduleSources, exclusions, artifactResolver, artifactTypeRegistry, calculatedValueContainerFactory));
    }
}

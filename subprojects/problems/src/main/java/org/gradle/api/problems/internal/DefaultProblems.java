/*
 * Copyright 2023 the original author or authors.
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

package org.gradle.api.problems.internal;

import org.gradle.api.problems.Problems;
import org.gradle.api.problems.interfaces.Problem;
import org.gradle.api.problems.interfaces.ProblemBuilder;
import org.gradle.api.problems.interfaces.ProblemBuilderDefiningLabel;
import org.gradle.api.problems.interfaces.ProblemGroup;
import org.gradle.internal.operations.BuildOperationProgressEventEmitter;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.gradle.api.problems.interfaces.ProblemGroup.DEPRECATION_ID;
import static org.gradle.api.problems.interfaces.ProblemGroup.GENERIC_ID;
import static org.gradle.api.problems.interfaces.ProblemGroup.TYPE_VALIDATION_ID;
import static org.gradle.api.problems.interfaces.ProblemGroup.VERSION_CATALOG_ID;
import static org.gradle.api.problems.interfaces.Severity.ERROR;

public class DefaultProblems extends Problems {
    private final BuildOperationProgressEventEmitter buildOperationProgressEventEmitter;

    private final Map<String, ProblemGroup> problemGroups = new LinkedHashMap<>();

    public DefaultProblems(BuildOperationProgressEventEmitter buildOperationProgressEventEmitter) {
        this.buildOperationProgressEventEmitter = buildOperationProgressEventEmitter;
        addPredefinedGroup(GENERIC_ID);
        addPredefinedGroup(TYPE_VALIDATION_ID);
        addPredefinedGroup(DEPRECATION_ID);
        addPredefinedGroup(VERSION_CATALOG_ID);
    }

    private void addPredefinedGroup(String genericId) {
        problemGroups.put(genericId, new PredefinedProblemGroup(genericId));
    }

    public ProblemBuilderDefiningLabel createProblemBuilder() {
        return createProblemBuilderInternal();
    }

    @Nonnull
    private DefaultProblemBuilder createProblemBuilderInternal() {
        return new DefaultProblemBuilder(this, buildOperationProgressEventEmitter);
    }


    public void collectError(RuntimeException failure) {
        new DefaultProblemBuilder(this, buildOperationProgressEventEmitter)
            .label(failure.getMessage())
            .undocumented()
            .noLocation()
            .type("generic_exception")
            .group(GENERIC_ID)
            .severity(ERROR)
            .withException(failure)
            .report();
    }

    @Override
    public void collectError(Problem problem) {
        buildOperationProgressEventEmitter.emitNowIfCurrent(problem);
//        ProblemsProgressEventEmitterHolder.get().emitNowIfCurrent(problem);
    }

    @Override
    public void collectErrors(Collection<Problem> problem) {
        problem.forEach(this::collectError);
    }

    @Override
    public ProblemGroup getProblemGroup(String groupId) {
        return problemGroups.get(groupId);
    }

    @Override
    public ProblemGroup registerProblemGroup(String typeId) {
        PredefinedProblemGroup value = new PredefinedProblemGroup(typeId);
        problemGroups.put(typeId, value);
        return value;
    }

    @Override
    public ProblemGroup registerProblemGroup(ProblemGroup typeId) {
        problemGroups.put(typeId.getId(), typeId);
        return typeId;
    }

    @Override
    public RuntimeException throwing(ProblemSpec action) {
        DefaultProblemBuilder defaultProblemBuilder = createProblemBuilderInternal();
        throw action.apply(defaultProblemBuilder)
            .throwIt();
    }

    @Override
    public RuntimeException rethrowing(RuntimeException e, ProblemSpec action) {
        DefaultProblemBuilder defaultProblemBuilder = createProblemBuilderInternal();
        ProblemBuilder problemBuilder = action.apply(defaultProblemBuilder);
        problemBuilder.withException(e);
        throw problemBuilder.throwIt();
    }
}

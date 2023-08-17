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

package org.gradle.api.problems;

import org.gradle.api.Incubating;
import org.gradle.api.problems.interfaces.Problem;
import org.gradle.api.problems.interfaces.ProblemBuilder;
import org.gradle.api.problems.interfaces.ProblemBuilderDefiningLabel;
import org.gradle.api.problems.interfaces.ProblemGroup;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

/**
 * Problems API service.
 *
 * @since 8.4
 */
@Incubating
public interface Problems {

    /**
     * A function that can be used to specify a {@link Problem} using a {@link ProblemBuilder}.
     * <p>
     * Usage example:
     *
     * <pre>
     * throw getProblemService().throwing(builder -&gt;
     *        builder.undocumented()
     *            .location(...)
     *            .message(message)
     *            .type("task_selection")
     *            .group(ProblemGroup.GENERIC_ID)
     *            .severity(Severity.ERROR)
     *            .withException(new TaskSelectionException(message)));
     * </pre>
     *
     * Using this instead of an {@link org.gradle.api.Action} forces the user to specify all required properties of a {@link Problem}.
     *
     * @since 8.4
     */
    @Incubating
   interface ProblemSpec {

        @Nonnull
        ProblemBuilder apply(ProblemBuilderDefiningLabel builder);
    }

    ProblemBuilderDefiningLabel createProblemBuilder();

    void collectError(RuntimeException failure);


    void collectError(Problem problem);

    void collectErrors(Collection<Problem> problem);

    @Nullable ProblemGroup getProblemGroup(String groupId);

    RuntimeException throwing(ProblemSpec action);

    RuntimeException rethrowing(RuntimeException e, ProblemSpec action);

    ProblemGroup registerProblemGroup(String typeId);

    ProblemGroup registerProblemGroup(ProblemGroup typeId);

}

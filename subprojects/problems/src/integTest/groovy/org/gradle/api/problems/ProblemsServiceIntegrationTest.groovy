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

package org.gradle.api.problems

import org.gradle.integtests.fixtures.AbstractIntegrationSpec

class ProblemsServiceIntegrationTest extends AbstractIntegrationSpec {

    def setup() {
        enableProblemsApiCheck()
        buildFile << """
            tasks.register("reportProblem", ProblemReportingTask)
        """
    }

    def "can emit a problem"() {
        given:
        buildFile << """
            import org.gradle.api.problems.interfaces.Problem
            import org.gradle.api.problems.interfaces.Severity
            import org.gradle.internal.deprecation.Documentation

            abstract class ProblemReportingTask extends DefaultTask {
                @Inject
                protected abstract Problems getProblems();

                @TaskAction
                void run() {
                    Problem problem = problems.createProblemBuilder()
                        .label("label")
                        .undocumented()
                        .noLocation()
                        .type("type")
                        .severity(Severity.ERROR)
                        .build()
                    problems.collectError(problem)
                }
            }
            """

        when:
        run("reportProblem")

        then:
        this.collectedProblems.size() == 1
        this.collectedProblems[0].details["label"] == "label"
    }

    def "can emit a problem with user-manual documentation"() {
        given:
        buildFile << """
            import org.gradle.api.problems.interfaces.Problem
            import org.gradle.api.problems.interfaces.Severity
            import org.gradle.internal.deprecation.Documentation

            abstract class ProblemReportingTask extends DefaultTask {
                @Inject
                protected abstract Problems getProblems();

                @TaskAction
                void run() {
                    Problem problem = problems.createProblemBuilder()
                        .label("label")
                        .documentedAt(
                            Documentation.userManual("test-id", "test-section")
                        )
                        .noLocation()
                        .type("type")
                        .severity(Severity.ERROR)
                        .build()
                    problems.collectError(problem)
                }
            }
            """

        when:
        run("reportProblem")

        then:
        this.collectedProblems.size() == 1
        this.collectedProblems[0].details["documentationLink"] == [
            "properties": [
                "page": "test-id",
                "section": "test-section"
            ]
        ]
    }


}

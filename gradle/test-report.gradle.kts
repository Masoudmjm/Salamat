import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.TestReport
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType

tasks.register<TestReport>("aggregateTestReports") {
    destinationDirectory.set(
        file("test result")
    )

    // mustRunAfter establishes ordering without creating a task dependency,
    // so running a single module's tests won't force all other modules to run.
    mustRunAfter(
        subprojects.flatMap { subproject ->
            subproject.tasks.withType<Test>().matching { it.name == "testAndroidHostTest" }
        }
    )

    doFirst {
        // Resolve directories at execution time — only include results that already exist.
        subprojects.forEach { subproject ->
            subproject.tasks
                .withType<Test>()
                .matching { it.name == "testAndroidHostTest" }
                .forEach { testTask ->
                    val dir = testTask.binaryResultsDirectory.get().asFile
                    if (dir.exists()) testResults.from(dir)
                }
        }
    }
}

tasks.register("appendAggregatedTestSummary") {
    dependsOn("aggregateTestReports")

    doLast {
        val reportIndexFile = file("test result/index.html")
        if (!reportIndexFile.exists()) return@doLast

        var html = reportIndexFile.readText()
        val markerStart = "<!-- custom-aggregated-summary:start -->"
        val markerEnd = "<!-- custom-aggregated-summary:end -->"
        val existingSummaryRegex = Regex(
            """$markerStart.*?$markerEnd""",
            setOf(RegexOption.MULTILINE, RegexOption.DOT_MATCHES_ALL)
        )
        html = html.replace(existingSummaryRegex, "")

        val testsRegex = Regex(
            """<div class="counter">(\d+)</div>\s*<p>tests</p>""",
            setOf(RegexOption.MULTILINE, RegexOption.DOT_MATCHES_ALL)
        )
        val failuresRegex = Regex(
            """<div class="counter">(\d+)</div>\s*<p>failures</p>""",
            setOf(RegexOption.MULTILINE, RegexOption.DOT_MATCHES_ALL)
        )
        val skippedRegex = Regex(
            """<div class="counter">(\d+)</div>\s*<p>skipped</p>""",
            setOf(RegexOption.MULTILINE, RegexOption.DOT_MATCHES_ALL)
        )

        val total = testsRegex.findAll(html).sumOf { it.groupValues[1].toIntOrNull() ?: 0 }
        val failed = failuresRegex.findAll(html).sumOf { it.groupValues[1].toIntOrNull() ?: 0 }
        val skipped = skippedRegex.findAll(html).sumOf { it.groupValues[1].toIntOrNull() ?: 0 }
        if (total == 0 && failed == 0 && skipped == 0) return@doLast
        val passed = (total - failed - skipped).coerceAtLeast(0)

        val customSummaryBlock =
            """
            $markerStart
            <div class="infoBoxGroup" style="margin: 12px 0 20px 0;">
                <h2 style="margin: 0 0 8px 0;">Execution Summary</h2>
                <table style="border-collapse: collapse; width: 100%; max-width: 520px;">
                    <tr>
                        <td style="padding: 6px 10px; border: 1px solid #ddd;">Total tests</td>
                        <td style="padding: 6px 10px; border: 1px solid #ddd;"><strong>$total</strong></td>
                    </tr>
                    <tr>
                        <td style="padding: 6px 10px; border: 1px solid #ddd;">Passed</td>
                        <td style="padding: 6px 10px; border: 1px solid #ddd;"><strong>$passed</strong></td>
                    </tr>
                    <tr>
                        <td style="padding: 6px 10px; border: 1px solid #ddd;">Failed</td>
                        <td style="padding: 6px 10px; border: 1px solid #ddd;"><strong>$failed</strong></td>
                    </tr>
                    <tr>
                        <td style="padding: 6px 10px; border: 1px solid #ddd;">Skipped</td>
                        <td style="padding: 6px 10px; border: 1px solid #ddd;"><strong>$skipped</strong></td>
                    </tr>
                </table>
            </div>
            $markerEnd
            """.trimIndent()

        val insertAfter = "<h1>All Results</h1>"
        if (!html.contains(insertAfter)) return@doLast

        // Replace verbose Gradle run titles with simple module names in tabs and section headers.
        html = html.replace(
            Regex("""Gradle Test Run\s+(:[^<\s]+):testAndroidHostTest""")
        ) { matchResult ->
            matchResult.groupValues[1].removePrefix(":")
        }

        reportIndexFile.writeText(
            html.replaceFirst(insertAfter, "$insertAfter\n$customSummaryBlock")
        )

        val clickableUrl = reportIndexFile.toURI().toString()
        println("Aggregated KMP test report: $clickableUrl")
    }
}

subprojects {
    afterEvaluate {
        tasks.matching { it.name == "testAndroidHostTest" }.configureEach {
            finalizedBy(rootProject.tasks.named("aggregateTestReports"))
        }
    }
}

tasks.named("aggregateTestReports").configure {
    finalizedBy("appendAggregatedTestSummary")
}

package com.bearmod

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import java.io.File

class CodeQualityPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        // Note: Do NOT apply SonarQube here to avoid classpath/version conflicts.
        // Apply and configure SonarQube in the root build.gradle.kts instead.

        // Register custom tasks
        registerLintTask(project)
        registerDuplicateDetectionTask(project)
        registerCodeQualitySummaryTask(project)
    }

    private fun registerLintTask(project: Project) {
        val isRoot = project == project.rootProject
        val isApp = project.name == "app"
        project.tasks.register<Task>("runLint") {
            group = "code quality"
            description = "Runs Android Lint and generates reports"
            if (isApp) {
                dependsOn("lint")
            } else if (isRoot) {
                dependsOn(":app:lint")
            }
            doLast {
                val report = if (isApp) {
                    File(project.buildDir, "reports/lint-results.html")
                } else {
                    File(project.rootProject.projectDir, "app/build/reports/lint-results.html")
                }
                if (report.exists()) {
                    println("Lint report generated: ${report.absolutePath}")
                } else {
                    println("Lint report not found at ${report.absolutePath}")
                }
            }
        }
    }

    private fun registerDuplicateDetectionTask(project: Project) {
        val isWindows = System.getProperty("os.name").lowercase().startsWith("windows")
        val isRoot = project == project.rootProject
        val jscpdPaths = if (isRoot) {
            "app/src/main/java app/src/main/kotlin mundo_core/src/main/java mundo_core/src/main/kotlin"
        } else {
            "src/main/java src/main/kotlin"
        }
        val jscpdCmd = "npx -y jscpd --min-lines 10 --min-tokens 50 --format \"console\" --output \"./jscpd-report\" --reporters \"console,json\" $jscpdPaths"

        project.tasks.register<Exec>("runDuplicateDetection") {
            group = "code quality"
            description = "Runs duplicate code detection using jscpd (cross-platform)"
            workingDir = project.projectDir
            if (isWindows) {
                commandLine("cmd", "/c", jscpdCmd)
            } else {
                // Use bash if available, otherwise sh
                commandLine("bash", "-lc", jscpdCmd)
                isIgnoreExitValue = false
            }
            doLast {
                val reportDir = File(project.projectDir, "jscpd-report")
                if (reportDir.exists()) {
                    println("Duplicate detection report generated: ${reportDir.absolutePath}")
                } else {
                    println("No jscpd report found at ${reportDir.absolutePath}")
                }
            }
        }
    }

    private fun registerCodeQualitySummaryTask(project: Project) {
        project.tasks.register("generateCodeQualitySummary") {
            group = "code quality"
            description = "Generates a summary report of code quality checks"

            dependsOn("runLint", "runDuplicateDetection")

            doLast {
                val summaryFile = File(project.buildDir, "code-quality-summary.md")
                summaryFile.writeText(generateSummary(project))
                println("Code quality summary generated: ${summaryFile.absolutePath}")
            }
        }
    }

    private fun generateSummary(project: Project): String {
        val summary = StringBuilder()
        summary.append("# Code Quality Summary\n\n")

        // Lint results
        val lintReport = File(project.buildDir, "reports/lint-results.html")
        summary.append("## Lint Results\n")
        if (lintReport.exists()) {
            summary.append("Lint report generated. Check `${lintReport.absolutePath}` for details.\n\n")
        } else {
            summary.append("No lint report found.\n\n")
        }

        // Duplicate detection
        val duplicateReport = File(project.projectDir, "jscpd-report")
        summary.append("## Duplicate Detection\n")
        if (duplicateReport.exists()) {
            summary.append("Duplicate detection report generated. Check `${duplicateReport.absolutePath}` for details.\n\n")
        } else {
            summary.append("No duplicate report found.\n\n")
        }

        // SonarQube note
        summary.append("## SonarQube Analysis\n")
        summary.append("Run `./gradlew sonarqube` to perform SonarQube analysis.\n\n")

        return summary.toString()
    }
}
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
   // id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("com.android.application") version "8.13.0-rc01" apply false
    id("com.android.library") version "8.13.0-rc01" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    id("org.sonarqube") version "6.3.1.5724"
    id("checkstyle")
}

// Apply local buildSrc plugin providing code-quality tasks
apply(plugin = "com.bearmod.code-quality")

checkstyle {
    toolVersion = "10.12.4"
    configFile = file("$rootDir/checkstyle.xml")
    isIgnoreFailures = false
}
tasks.withType<Checkstyle>().configureEach {
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}
// --- Ktlint ---
configurations {
    create("ktlint")
}

dependencies {
    "ktlint"("com.pinterest:ktlint:1.3.1")
}

tasks.register<JavaExec>("ktlintFormat") {
    group = "formatting"
    description = "Auto-format Kotlin sources with ktlint"
    classpath = configurations["ktlint"]
    mainClass.set("com.pinterest.ktlint.Main")
    args("-F", "--relative", "**/*.kt")
}

tasks.register("ktlintCheckRelease") {
    dependsOn("ktlintCheck")
}

// Project-wide dependency alignment and quality settings
allprojects {
    // Mitigate CVE in io.netty:netty-handler by aligning all Netty modules
    // to a patched version, even when introduced transitively by tooling/tests.
    configurations.all {
        resolutionStrategy.eachDependency {
            if (requested.group == "io.netty" && requested.name.startsWith("netty-")) {
                useVersion("4.1.118.Final")
                because("Force patched Netty due to SslHandler packet validation issue (CVE, GHSA-4g4c-8m2k-jhaw)")
            }
        }
    }
}


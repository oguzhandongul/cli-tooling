plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation(libs.clikt)
    implementation(libs.jackson.dataformat.yaml)
    implementation(libs.jackson.module.kotlin)

    testImplementation(kotlin("test"))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj.core)
}

application {
    mainClass.set("io.github.oguzhandongul.citooling.filter.cli.MainKt")
}

tasks.named<JavaExec>("run") {
    workingDir = rootProject.projectDir
}

tasks.named<Test>("test") {
    useJUnitPlatform()

    testLogging {
        events("passed", "skipped", "failed")
    }
}

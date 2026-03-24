plugins {
    kotlin("jvm") version "2.0.21"
    application
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation("com.github.ajalt.clikt:clikt:5.0.1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.2")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.17.2")

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.3")
    testImplementation("org.assertj:assertj-core:3.26.3")
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

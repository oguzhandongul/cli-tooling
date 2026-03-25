plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-gradle-plugin`
}

dependencies {
    implementation(kotlin("stdlib"))
    testImplementation(kotlin("test"))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj.core)
    testImplementation(gradleTestKit())
}

gradlePlugin {
    plugins {
        create("junitXmlFilenamePlugin") {
            id = "io.github.oguzhandongul.junit-xml-filename"
            implementationClass = "io.github.oguzhandongul.citooling.junitxml.plugin.JunitXmlFilenamePlugin"
        }
    }
}
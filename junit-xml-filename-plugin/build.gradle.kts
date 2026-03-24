plugins {
    kotlin("jvm") version "2.0.21"
    `java-gradle-plugin`
}

dependencies {
    implementation(kotlin("stdlib"))

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.3")
    testImplementation("org.assertj:assertj-core:3.26.3")
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
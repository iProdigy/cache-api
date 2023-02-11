dependencies {
    api(project(":cache-core"))
    implementation("org.springframework:spring-context:5.3.25")
    testImplementation("org.springframework.boot:spring-boot-starter-test:2.7.8")
    testImplementation("org.awaitility:awaitility:4.2.0")
    testImplementation(testFixtures(project(":cache-core")))
    testImplementation(project(":cache-provider-caffeine"))
}

publishing.publications.withType<MavenPublication> {
    pom {
        name.set("Xanthic - Spring")
        description.set("Xanthic Cache Spring")
    }
}

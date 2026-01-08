plugins {
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.serialization") version "1.9.22"
    application
}

group = "com.fleet"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    // Ktor Core
    implementation("io.ktor:ktor-server-core-jvm:2.3.7")
    implementation("io.ktor:ktor-server-netty-jvm:2.3.7")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:2.3.7")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:2.3.7")
    
    // WebSockets
    implementation("io.ktor:ktor-server-websockets-jvm:2.3.7")
    
    // CORS
    implementation("io.ktor:ktor-server-cors-jvm:2.3.7")
    
    // Status Pages
    implementation("io.ktor:ktor-server-status-pages-jvm:2.3.7")
    
    // Logback
    implementation("ch.qos.logback:logback-classic:1.4.14")
    
    // Exposed ORM
    implementation("org.jetbrains.exposed:exposed-core:0.46.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.46.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.46.0")
    implementation("org.jetbrains.exposed:exposed-java-time:0.46.0")
    
    // PostgreSQL Driver
    implementation("org.postgresql:postgresql:42.7.1")
    
    // HikariCP (Connection pooling)
    implementation("com.zaxxer:HikariCP:5.1.0")
    
    // Kotlinx DateTime
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    
    // Testing
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.9.22")
}

application {
    mainClass.set("com.fleet.ApplicationKt")
}

kotlin {
    jvmToolchain(17)
}
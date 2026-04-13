import com.google.protobuf.gradle.id

plugins {
	kotlin("jvm") version "2.2.21"
	kotlin("plugin.spring") version "2.2.21"
	id("org.springframework.boot") version "4.0.5"
	id("io.spring.dependency-management") version "1.1.7"
	id("com.google.protobuf") version "0.9.5"
	kotlin("plugin.jpa") version "1.9.24"
	id("io.kotest") version "6.1.11"
	id("org.jetbrains.kotlinx.kover") version "0.9.8"
}

group = "ru.k"
version = "0.0.1-SNAPSHOT"

extra["springGrpcVersion"] = "1.0.2"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}


dependencyManagement {
	imports {
		mavenBom("org.springframework.grpc:spring-grpc-dependencies:${property("springGrpcVersion")}")
	}
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
	}
}

protobuf {
	protoc {
		artifact = "com.google.protobuf:protoc"
	}
	plugins {
		id("grpc") {
			artifact = "io.grpc:protoc-gen-grpc-java"
		}
		create("grpcKt") {
			artifact = "io.grpc:protoc-gen-grpc-kotlin:1.5.0:jdk8@jar"
		}
	}
	generateProtoTasks {
		all().forEach {
			it.plugins {
				id("grpc") {
					option("@generated=omit")
				}
				id("grpcKt")
			}
		}
	}
}

kover {
	reports {
		total {
			html {
				onCheck = true
				htmlDir = file("build/reports/kover/html")
			}
		}
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
	jvmArgs = listOf(
		"-XX:+EnableDynamicAgentLoading",
		"-Djdk.instrument.traceUsage=false"
	)
}

dependencies {
	// Spring
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	developmentOnly("org.springframework.boot:spring-boot-devtools")

	// gRPC
	implementation("io.grpc:grpc-services")
	implementation("org.springframework.grpc:spring-grpc-server-spring-boot-starter")
	implementation("io.grpc:grpc-netty")
	implementation("io.grpc:grpc-protobuf")
	implementation("io.grpc:grpc-stub")
	implementation("io.grpc:grpc-kotlin-stub")
	implementation("com.google.protobuf:protobuf-kotlin")
	implementation("com.google.protobuf:protobuf-java")

	// Database
//	implementation("org.springframework.boot:spring-boot-starter-jdbc")
//	implementation("org.springframework.boot:spring-boot-starter-liquibase") //	runtimeOnly("org.postgresql:postgresql")
	implementation("com.h2database:h2")
	implementation("org.springframework.boot:spring-boot-h2console")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-validation")

	// Core
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.10.2")
	implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1")

	// Testing
	testImplementation("org.springframework.boot:spring-boot-starter-jdbc-test")
	testImplementation("org.springframework.boot:spring-boot-starter-liquibase-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testImplementation("org.springframework.grpc:spring-grpc-test")
	testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	val allureVersion = "2.25.0"
	testImplementation(platform("io.qameta.allure:allure-bom:$allureVersion"))
	testImplementation("io.qameta.allure:allure-junit5")
	val kotestVersion = "6.1.11"
	testImplementation("io.kotest:kotest-assertions-core:${kotestVersion}")
	testImplementation("io.kotest:kotest-property:${kotestVersion}")
	testImplementation("io.kotest:kotest-runner-junit5:${kotestVersion}")
	testImplementation("io.kotest:kotest-extensions-allure:${kotestVersion}")
	testImplementation("io.mockk:mockk:1.14.9")
}

import com.google.protobuf.gradle.id

plugins {
	kotlin("jvm") version "2.2.21"
	kotlin("plugin.spring") version "2.2.21"
	id("org.springframework.boot") version "4.0.5"
	id("io.spring.dependency-management") version "1.1.7"
	id("com.google.protobuf") version "0.9.5"
	kotlin("plugin.jpa") version "1.9.24"
}

group = "ru.k"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

extra["springGrpcVersion"] = "1.0.2"

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
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
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

tasks.withType<Test> {
	useJUnitPlatform()
}

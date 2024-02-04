plugins {
	id("fabric-loom") version "1.5-SNAPSHOT"
	id("io.github.ladysnake.chenille") version "0.12.0"
	id("io.github.juuxel.loom-quiltflower") version "1.6.0"
}

version = providers.gradleProperty("mod_version").get()
group = providers.gradleProperty("maven_group").get()

chenille {
	configurePublishing {
		withLadysnakeMaven()
		withGithubRelease()
	}
	configureTestmod {
		withDependencyConfiguration()
	}

	javaVersion = providers.gradleProperty("java_version").get().toInt()
	license = "MIT"
}

val dummy: SourceSet by sourceSets.creating {}

repositories {
	mavenCentral()
	chenille.repositories {
		ladysnake()
	}
}

dependencies {
	val minecraftVersion: String = providers.gradleProperty("minecraft_version").get()
	val yarnVersion: String = providers.gradleProperty("yarn_mappings").get()
	val loaderVersion: String = providers.gradleProperty("loader_version").get()
	val fabricApiVersion: String = providers.gradleProperty("fabric_version").get()
	val ccaVersion: String = providers.gradleProperty("cca_version").get()

	// To change the versions see the gradle.properties file
	minecraft("com.mojang:minecraft:${minecraftVersion}")
	mappings("net.fabricmc:yarn:${yarnVersion}:v2")
	modImplementation("net.fabricmc:fabric-loader:${loaderVersion}")

	modApi(fabricApi.module("fabric-gametest-api-v1", fabricApiVersion))
	modLocalImplementation(fabricApi.module("fabric-networking-api-v1", fabricApiVersion))
	modCompileOnly("dev.onyxstudios.cardinal-components-api:cardinal-components-base:${ccaVersion}")
	modCompileOnly("dev.onyxstudios.cardinal-components-api:cardinal-components-entity:${ccaVersion}")
	api("junit:junit:4.13.2")
	"testmodImplementation"(sourceSets.main.get().output)
	annotationProcessor(dummy.output)
}

tasks.processResources {
	inputs.property("version", project.version)

	filesMatching("fabric.mod.json") {
		expand("version" to project.version)
	}
}

java {
	// Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
	// if it is present.
	// If you remove this line, sources will not be generated.
	withSourcesJar()
}

tasks.jar {
	from("LICENSE") {
		val archivesBaseName = providers.gradleProperty("archives_base_name").get()
		rename { "${it}_$archivesBaseName"}
	}
}

extensions.configure(PublishingExtension::class.java) {
	publications {
		create("relocation", MavenPublication::class.java) {
			pom {
				// Old artifact coordinates
				groupId = "io.github.ladysnake"

				distributionManagement {
					relocation {
						// New artifact coordinates
						groupId = "org.ladysnake"
						message = "groupId has been changed"
					}
				}
			}
		}
	}
}

plugins {
	id("net.fabricmc.fabric-loom") version "1.14-SNAPSHOT"
	id("io.github.ladysnake.chenille") version "0.18.0-SNAPSHOT"
}

version = providers.gradleProperty("mod_version").get()
group = providers.gradleProperty("maven_group").get()

chenille {
	configurePublishing {
		withLadysnakeMaven()
		withGithubRelease()
	}
	configureTestmod {
//		withDependencyConfiguration()
	}

	javaVersion = providers.gradleProperty("java_version").get().toInt()
	license = "MIT"
}

val dummy: SourceSet by sourceSets.creating {}

repositories {
	mavenLocal()
	mavenCentral()
	maven {
		name = "Ladysnake Mods"
		setUrl("https://maven.ladysnake.org/releases")
	}
}

dependencies {
	val minecraftVersion: String = providers.gradleProperty("minecraft_version").get()
	val loaderVersion: String = providers.gradleProperty("loader_version").get()
	val fabricApiVersion: String = providers.gradleProperty("fabric_version").get()
	val ccaVersion: String = providers.gradleProperty("cca_version").get()

	// To change the versions see the gradle.properties file
	minecraft("com.mojang:minecraft:${minecraftVersion}")
	implementation("net.fabricmc:fabric-loader:${loaderVersion}")

	api(fabricApi.module("fabric-gametest-api-v1", fabricApiVersion))
	implementation(fabricApi.module("fabric-registry-sync-v0", fabricApiVersion))
	localImplementation(fabricApi.module("fabric-networking-api-v1", fabricApiVersion))
//	compileOnly("org.ladysnake.cardinal-components-api:cardinal-components-base:${ccaVersion}")
//	compileOnly("org.ladysnake.cardinal-components-api:cardinal-components-entity:${ccaVersion}")
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

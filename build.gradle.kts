@file:Suppress("UnstableApiUsage")

import java.util.Properties
import net.fabricmc.loom.task.RemapJarTask
import com.modrinth.minotaur.TaskModrinthUpload
import com.matthewprenger.cursegradle.CurseArtifact
import com.matthewprenger.cursegradle.CurseProject
import com.matthewprenger.cursegradle.CurseRelation
import com.matthewprenger.cursegradle.Options

plugins {
    java
    `java-library`
    id("fabric-loom").version(Fabric.Loom.version)

    `maven-publish`
    id("com.matthewprenger.cursegradle").version("1.4.0")
    id("com.modrinth.minotaur").version("1.1.0")
}

base.archivesBaseName = PickUp.name
group = PickUp.group
version = PickUp.version

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenLocal { name = "Maven Local" }
    maven(url = "https://maven.fabricmc.net/") { name = "Fabric" }
    maven(url = "https://cursemaven.com/") {
        name = "Curse Maven"
        content {
            includeGroup("curse.maven")
        }
    }
    maven(url = "https://jitpack.io/") {
        name = "JitPack"
    }
}

dependencies {
    minecraft(Minecraft.coordinate)
    mappings(Fabric.yarn.coordinate)

    deps.forEach {
        modImplementation(it.coordinate) {
            if (it.group != Fabric.api.group) {
                exclude(group = Fabric.api.group)
            }
            if (!it.transitive) {
                isTransitive = false
            }
        }
    }

    jijDeps.forEach { include(it.coordinate) }
    runtimeDeps.forEach { modRuntime(it.coordinate) }

    implementation(Gson.coordinate)
    compileOnly(Jsr305.coordinate)
}

object Meta {
    val changelogLink =
        "See https://github.com/alex5nader/${PickUp.name}/milestones for a list of changelogs."

    val minecraftVersions = listOf(
        "1.16.4"
    )

    val modVersionName = "Version ${PickUp.version}"
}

tasks.getByName<ProcessResources>("processResources") {
    inputs.properties("version" to PickUp.version)
    filesMatching("fabric.mod.json") {
        expand(
            "modId" to PickUp.modId,
            "modVersion" to PickUp.version,
            "modName" to PickUp.name,
            "modPrettyName" to PickUp.prettyName,

            "fapiVersion" to Fabric.api.version,
            "mcVersions" to Meta.minecraftVersions.joinToString("\",\"")
        )
    }
    filesMatching("mixins.${PickUp.modId}.json") {
        expand("modId" to PickUp.modId)
    }
}

val remapJar = tasks.getByName<RemapJarTask>("remapJar")

publishing {
    repositories {
        maven(url = "maven.pkg.github.com/alex5nader/${PickUp.name}") {
            name = "GitHub_Packages"
            credentials {
                username = "alex5nader"
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }

    publications.create<MavenPublication>("maven") {
        artifactId = PickUp.version

        artifact(remapJar) {
            classifier = null
            builtBy(remapJar)
        }
    }
}

val apiKeys by lazy {
    Properties().apply { load(file("apiKeys.properties").inputStream()) }
}

val publishModrinth = tasks.create<TaskModrinthUpload>("publishModrinth") {
    val modrinthApiKey: String by apiKeys
    token = modrinthApiKey
    projectId = PickUp.modrinthId

    changelog = Meta.changelogLink

    versionNumber = PickUp.version
    versionName = Meta.modVersionName
    releaseType = "release"

    uploadFile = remapJar

    Meta.minecraftVersions.forEach(this::addGameVersion)
    addLoader("fabric")
}

curseforge {
    val curseforgeApiKey: String by apiKeys
    apiKey = curseforgeApiKey

    project(closureOf<CurseProject> {
        id = PickUp.curseforgeId

        mainArtifact(remapJar)

        releaseType = "release"

        Meta.minecraftVersions.forEach(this::addGameVersion)
        addGameVersion("Fabric")

        relations(closureOf<CurseRelation> {
            jijDeps.forEach { dep ->
                dep.curseforgeSlug
                    .takeIf { dep.referenceOnCurseforge }
                    ?.let {
                        embeddedLibrary(it)
                    }
            }
            deps.forEach { dep ->
                dep.curseforgeSlug
                    .takeIf { dep.referenceOnCurseforge }
                    ?.takeIf { dep !in jijDeps }
                    ?.let {
                        requiredDependency(it)
                    }
            }
            runtimeDeps.forEach { dep ->
                dep.curseforgeSlug
                    .takeIf { dep.referenceOnCurseforge }
                    ?.let {
                        optionalDependency(it)
                    }
            }
        })

        changelog = Meta.changelogLink

        mainArtifact(
            file("${project.buildDir}/libs/${base.archivesBaseName}-$version.jar"),
            closureOf<CurseArtifact> {
                displayName = Meta.modVersionName
            }
        )
    })

    options(closureOf<Options> {
        forgeGradleIntegration = false
    })
}

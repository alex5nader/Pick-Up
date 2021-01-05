@file:Suppress("MemberVisibilityCanBePrivate", "unused")

open class Dependency(
    val group: String,
    val name: String,
    val version: String,

    val transitive: Boolean = true,

    curseforgeSlug: String? = null,
    val referenceOnCurseforge: Boolean = true
) {
    val curseforgeSlug = curseforgeSlug ?: name

    val coordinate: String get() = "$group:$name:$version"

    override fun toString(): String = coordinate
}

open class CurseMavenDependency(
    descriptor: String,
    projectId: Int,
    fileId: Int,

    curseforgeSlug: String? = null,
    referenceOnCurseforge: Boolean = true
) : Dependency("curse.maven", "$descriptor-$projectId", "$fileId", transitive = false, curseforgeSlug = curseforgeSlug, referenceOnCurseforge = referenceOnCurseforge)

object Fabric {
    object Loom {
        const val version = "0.5-SNAPSHOT"
    }

    val yarn = Dependency("net.fabricmc", "yarn", "1.16.4+build.7:v2")
    val loader = Dependency("net.fabricmc", "fabric-loader", "0.10.8", referenceOnCurseforge = false)
    val api = Dependency("net.fabricmc.fabric-api", "fabric-api", "0.29.3+1.16")
}

object FabricTemplate : Dependency("dev.alexnader", "Fabric-Template", "0") {
    const val modId = "fabric_template"
    const val prettyName = "Fabric Template"

    const val modrinthId = "abcdef"
    const val curseforgeId = "abcdef"
}

object Minecraft : Dependency("com.mojang", "minecraft", "1.16.4") {
    val tag = "mc" + version.split(".").slice(0..1).joinToString()
}

val ModMenu = Dependency("io.github.prospector", "modmenu", "1.14.9+build.14")

val Gson = Dependency("com.google.code.gson", "gson", "2.8.6")
val Jsr305 = Dependency("com.google.code.findbugs", "jsr305", "3.0.2")

val jijDeps = listOf<Dependency>()
val deps = listOf(Fabric.loader, Fabric.api) + jijDeps
val runtimeDeps = listOf(ModMenu)

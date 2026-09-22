import java.util.zip.ZipFile
import javax.imageio.ImageIO

plugins {
    id("net.fabricmc.fabric-loom") version "1.16-SNAPSHOT"
    `maven-publish`
}

group = property("maven_group") as String
version = "${property("mod_version")}+fabric-mc${property("minecraft_version")}"

base {
    archivesName.set(property("archives_base_name") as String)
}

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/")
    maven("https://api.modrinth.com/maven") {
        content { includeGroup("maven.modrinth") }
    }
    maven("https://maven.modmuss50.me/") {
        content { includeGroup("teamreborn") }
    }
}

// The recipe viewer is nested into this jar and compiled against: CI downloads its release
// jar into libs/, a local checkout uses the sibling repo's build output.
repositories {
    flatDir {
        dirs("libs", "../../create-rei/CreateReiViewer-Fly/build/libs")
    }
}
val recipeViewer = ":CreateReiViewer:${property("createreiviewer_version")}+fabric-mc${property("minecraft_version")}"

loom {
    accessWidenerPath = file("src/main/resources/powergrid.accesswidener")
    mods {
        create("powergrid") {
            sourceSet(sourceSets.main.get())
        }
    }
}

sourceSets {
    main {
        resources.srcDir("src/generated/resources")
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    implementation("net.fabricmc:fabric-loader:${property("fabric_loader_version")}")
    implementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_api_version")}")
    implementation("maven.modrinth:create-fly:${property("create_fabric_version")}")
    implementation(include("teamreborn:energy:${property("team_reborn_energy_version")}")!!)

    val ejml = property("ejml_version")
    include(implementation("org.ejml:ejml-core:$ejml")!!)
    include(implementation("org.ejml:ejml-simple:$ejml")!!)
    include(implementation("org.ejml:ejml-ddense:$ejml")!!)
    include(implementation("org.ejml:ejml-dsparse:$ejml")!!)

    compileOnly(recipeViewer)
    include(recipeViewer)
    compileOnly("com.google.code.findbugs:jsr305:3.0.2")

    testImplementation("org.junit.jupiter:junit-jupiter:${property("junit_version")}")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
    withSourcesJar()
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(25)
    options.compilerArgs.addAll(listOf("-Xmaxerrs", "10000", "-Xmaxwarns", "1000"))
}

tasks.withType<AbstractCopyTask>().configureEach {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

val generatedConnectedTextureResources = layout.buildDirectory.dir("generated/connected-texture-resources")

val omniTileIndexes = listOf(
    1, 2, 3, 8, 9, 10, 11, 12, 13, 16, 17, 18, 19, 20, 21, 24, 25, 26, 27, 28, 29, 30,
    32, 33, 34, 35, 36, 37, 38, 40, 41, 42, 43, 44, 45, 46, 48, 49, 50, 51, 52, 53, 54, 56, 57, 58,
)
val rectangleTileIndexes = (0..11).toList() + (13..15).toList()

val connectedTextureSheets = mapOf(
    "assets/powergrid/textures/block/conductive_casing_connected.png" to (8 to omniTileIndexes),
    "assets/powergrid/textures/block/copper_plating_connected.png" to (8 to omniTileIndexes),
    "assets/powergrid/textures/block/solar_panel/solar_panel_connected.png" to (8 to omniTileIndexes),
    "assets/powergrid/textures/block/solar_panel/copper_plating_connected.png" to (8 to omniTileIndexes),
    "assets/powergrid/textures/block/battery/battery_side_connected.png" to (4 to rectangleTileIndexes),
    "assets/powergrid/textures/block/battery/battery_top_connected.png" to (4 to rectangleTileIndexes),
)

val sheetsSampledByModels = setOf(
    "assets/powergrid/textures/block/solar_panel/solar_panel_connected.png",
)

val generateConnectedTextureSprites = tasks.register("generateConnectedTextureSprites") {
    val resourceRoot = file("src/main/resources")
    inputs.files(fileTree(resourceRoot) { include(connectedTextureSheets.keys) })
        .withPropertyName("connectedTextureSheets")
        .withPathSensitivity(PathSensitivity.RELATIVE)
    inputs.property("connectedTextureLayout", "create-fly-26.2-v1")
    outputs.dir(generatedConnectedTextureResources)
    doLast {
        val outputRoot = generatedConnectedTextureResources.get().asFile
        delete(outputRoot)
        var sheetCount = 0
        var spriteCount = 0
        connectedTextureSheets.forEach { (pattern, layout) ->
            val (gridSize, tileIndexes) = layout
            fileTree(resourceRoot) { include(pattern) }.files.sortedBy { it.invariantSeparatorsPath }.forEach { sheetFile ->
                val sheet = ImageIO.read(sheetFile) ?: throw GradleException("Could not decode $sheetFile")
                if (sheet.width != sheet.height || sheet.width % gridSize != 0) {
                    throw GradleException("$sheetFile must be a $gridSize x $gridSize grid of square tiles, but is ${sheet.width} x ${sheet.height}")
                }
                val tileSize = sheet.width / gridSize
                val relativeSheet = resourceRoot.toPath().relativize(sheetFile.toPath()).toString()
                val spriteDirectory = outputRoot.resolve(relativeSheet.removeSuffix(".png"))
                spriteDirectory.mkdirs()
                tileIndexes.forEachIndexed { index, sourceTileIndex ->
                    val tile = sheet.getSubimage(sourceTileIndex % gridSize * tileSize, sourceTileIndex / gridSize * tileSize, tileSize, tileSize)
                    if (!ImageIO.write(tile, "png", spriteDirectory.resolve("${index + 1}.png"))) {
                        throw GradleException("No PNG writer is available for $spriteDirectory")
                    }
                    spriteCount++
                }
                sheetCount++
            }
        }
        logger.lifecycle("Generated $spriteCount connected-texture sprites from $sheetCount sheets")
    }
}

tasks.processResources {
    dependsOn(generateConnectedTextureSprites)
    from(generatedConnectedTextureResources)
    exclude(connectedTextureSheets.keys - sheetsSampledByModels)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    val modMetadata = mapOf(
        "version" to project.version.toString(),
        "minecraft_dependency_version" to project.property("minecraft_dependency_version") as String,
        "fabric_loader_version" to project.property("fabric_loader_version") as String,
        "create_fabric_version_range" to project.property("create_fabric_version_range") as String,
    )
    inputs.properties(modMetadata)
    filesMatching("fabric.mod.json") {
        expand(modMetadata)
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    from("LICENSE")
    from("NOTICE")
    from("licenses") { into("licenses") }
}

tasks.named<Jar>("sourcesJar") {
    from("LICENSE")
    from("NOTICE")
    from("licenses") { into("licenses") }
}

val jarContainmentPrefix = "org/patryk3211/powergrid/"

val checkJarContainment = tasks.register("checkJarContainment") {
    description = "Fails if the mod jar or the sources jar carries a class or source file outside Power Grid's own namespace."
    group = "verification"
    val archives = files(
        tasks.named<Jar>("jar").flatMap { it.archiveFile },
        tasks.named<Jar>("sourcesJar").flatMap { it.archiveFile }
    )
    dependsOn(tasks.named("jar"), tasks.named("sourcesJar"))
    inputs.files(archives)
    val prefix = jarContainmentPrefix
    doLast {
        val offenders = mutableListOf<String>()
        archives.forEach { archive ->
            ZipFile(archive).use { zip ->
                val names = zip.entries().toList().map { it.name }
                for (name in names) {
                    if (!name.endsWith(".class") && !name.endsWith(".java")) continue
                    if (name.startsWith(prefix)) continue
                    offenders += archive.name + " -> " + name
                }
            }
        }
        if (offenders.isNotEmpty()) {
            throw GradleException(
                "Power Grid must ship nothing outside " + prefix + "; found " + offenders.size +
                    " foreign entries:\n" + offenders.joinToString("\n") { "  " + it }
            )
        }
    }
}

tasks.named("check") {
    dependsOn(checkJarContainment)
}

tasks.register("printCompileClasspath") {
    val cp = sourceSets.main.get().compileClasspath
    val out = layout.projectDirectory.file(".classpath.txt")
    doLast {
        out.asFile.writeText(cp.files.joinToString("\n") { it.absolutePath } + "\n")
    }
}

tasks.register("printTestClasspath") {
    val cp = sourceSets.test.get().compileClasspath + sourceSets.test.get().runtimeClasspath
    val out = layout.projectDirectory.file(".testclasspath.txt")
    doLast {
        out.asFile.writeText(cp.files.joinToString("\n") { it.absolutePath } + "\n")
    }
}

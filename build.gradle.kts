import java.util.zip.ZipFile

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
    maven("https://maven.shedaniel.me/") {
        content {
            includeGroup("me.shedaniel.cloth")
            includeGroup("me.shedaniel.cloth.api")
        }
    }
}

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

    compileOnly("maven.modrinth:rei:${property("rei_version")}")
    compileOnly("maven.modrinth:architectury-api:${property("architectury_version")}")
    compileOnly("me.shedaniel.cloth:basic-math:${property("basic_math_version")}")
    compileOnly(files(fileTree("../../create-rei/CreateReiViewer-Fly/build/libs") {
        include("CreateReiViewer-*.jar")
        exclude("*-sources.jar")
    }.files.maxByOrNull { it.lastModified() } ?: error("No Create Fly Recipe Viewer jar in ../../create-rei/CreateReiViewer-Fly/build/libs")))
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

tasks.processResources {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    inputs.property("version", version)
    filesMatching("fabric.mod.json") {
        expand(
            "version" to project.version.toString(),
            "minecraft_dependency_version" to project.property("minecraft_dependency_version") as String,
            "fabric_loader_version" to project.property("fabric_loader_version") as String,
            "create_fabric_version_range" to project.property("create_fabric_version_range") as String,
        )
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

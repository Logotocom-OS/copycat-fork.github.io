import com.github.difflib.DiffUtils
import com.github.difflib.UnifiedDiffUtils
import org.teavm.tooling.RuntimeCopyOperation
import org.teavm.tooling.TeaVMTargetType
import org.teavm.tooling.TeaVMTool
import org.teavm.tooling.TeaVMToolLog
import org.teavm.tooling.sources.DirectorySourceFileProvider
import org.teavm.tooling.sources.JarSourceFileProvider
import org.teavm.vm.TeaVMOptimizationLevel
import java.net.URLClassLoader
import javax.sound.midi.Patch


buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath("org.teavm:teavm-core:${project.properties["teavm_version"]}")
        classpath("org.teavm:teavm-tooling:${project.properties["teavm_version"]}")
        classpath("io.github.java-diff-utils:java-diff-utils:4.0")
    }
}

plugins {
    java
    application
}

repositories {
    mavenCentral()
}
dependencies {
    compileOnly("com.google.code.findbugs:jsr305:3.0.2")

    implementation("com.google.guava:guava:22.0")
    implementation("org.apache.commons:commons-lang3:3.6")
    implementation("io.netty:netty-all:4.1.9.Final")
    implementation("it.unimi.dsi:fastutil:8.2.2")

    implementation("org.teavm:teavm-jso:${project.properties["teavm_version"]}")
    implementation("org.teavm:teavm-jso-apis:${project.properties["teavm_version"]}")
    implementation("org.teavm:teavm-platform:${project.properties["teavm_version"]}")
    implementation("org.teavm:teavm-classlib:${project.properties["teavm_version"]}")
}

application {
    mainClassName = "cc.squiddev.cct.Main"
}

tasks {
    val compileTeaVM by registering {
        group = "build"
        description = "Converts Java code to Javascript using TeaVM"

        inputs.files(project.configurations.getByName("runtime").allArtifacts.files).withPropertyName("jars")

        val output = File(buildDir, "teaVM")
        outputs.dir(output).withPropertyName("output")

        doLast {
            val log = object : TeaVMToolLog {
                override fun info(text: String?) = project.logger.info(text)
                override fun debug(text: String?) = project.logger.debug(text)
                override fun warning(text: String?) = project.logger.warn(text)
                override fun error(text: String?) = project.logger.error(text)
                override fun info(text: String?, e: Throwable?) = project.logger.info(text)
                override fun debug(text: String?, e: Throwable?) = project.logger.debug(text, e)
                override fun warning(text: String?, e: Throwable?) = project.logger.warn(text, e)
                override fun error(text: String?, e: Throwable?) = project.logger.error(text, e)
            }

            fun getSource(f: File) = if (f.isFile && f.absolutePath.endsWith(".jar")) {
                JarSourceFileProvider(f)
            } else {
                DirectorySourceFileProvider(f)
            }

            val runtime = project.configurations.getByName("runtimeClasspath")
            val dependencies = runtime.resolve().map { it.toURI().toURL() }
            val artifacts = runtime.allArtifacts.files.map { it.toURI().toURL() }

            URLClassLoader((dependencies + artifacts).toTypedArray(), log.javaClass.classLoader).use { classloader ->
                val tool = TeaVMTool()
                tool.classLoader = classloader
                tool.targetDirectory = output
                tool.mainClass = application.mainClassName
                tool.runtime = RuntimeCopyOperation.MERGED
                tool.isMinifying = true
                tool.optimizationLevel = TeaVMOptimizationLevel.ADVANCED
                tool.log = log
                tool.targetType = TeaVMTargetType.JAVASCRIPT

                project.convention
                    .getPlugin(JavaPluginConvention::class.java)
                    .sourceSets
                    .getByName(SourceSet.MAIN_SOURCE_SET_NAME)
                    .allSource
                    .forEach { tool.addSourceFileProvider(getSource(it)) }

                tool.generate()

                if (!tool.problemProvider.severeProblems.isEmpty()) throw IllegalStateException("Build failed")

                // "touch" the directory
                output.setLastModified(System.currentTimeMillis())
            }
        }
    }

    val compileTypescript by registering(Exec::class) {
        group = "build"
        description = "Converts TypeScript code to Javascript"

        inputs.files(fileTree("src/web/ts")).withPropertyName("sources")
        outputs.dir("$buildDir/typescript").withPropertyName("output")

        commandLine("node_modules/.bin/tsc.cmd", "--project", "tsconfig.json")
    }

    val rollup by registering(Exec::class) {
        group = "build"
        description = "Combines multiple Javascript files into one"

        dependsOn(compileTypescript)
        inputs.files(fileTree("$buildDir/typescript")).withPropertyName("sources")
        outputs.file("$buildDir/rollup/main.js").withPropertyName("output")

        commandLine("node_modules/.bin/rollup.cmd", "-c")
    }

    val website by registering(Sync::class) {
        group = "build"
        description = "Combines all resource files into one distribution."

        dependsOn(compileTeaVM, rollup)

        from("$buildDir/teaVM/classes.js") {
            into("assets")
        }
        from("$buildDir/rollup/main.js") {
            into("assets")
        }

        from("src/web/public")

        into("$buildDir/web")
    }

    val cleanPatches by registering(Delete::class) {
        delete("src/patches")
        description = "Cleans the patch directory"
    }

    val cleanSources by registering(Delete::class) {
        group = "patch"
        description = "Cleans the modified sources"

        delete("src/main/java/dan200/computercraft/")
        delete("src/main/resources/assets/computercraft")
        delete(fileTree("src/main/java/org/squiddev/cobalt/") { exclude(".editorconfig") })
    }

    val makePatches by registering {
        group = "patch"
        description = "Diffs a canonical directory against our currently modified version"
        dependsOn(cleanPatches)

        doLast {
            listOf(
                Pair("CC-Tweaked", "dan200/computercraft"),
                Pair("Cobalt", "org/squiddev/cobalt")
            ).forEach { (project, packageName) ->
                val patches = File("src/patches/java/$packageName")
                val modified = File("src/main/java/$packageName")
                val original = File("original/$project/src/main/java/$packageName")
                if (!modified.isDirectory) throw IllegalArgumentException("$modified is not a directory or does not exist")
                if (!original.isDirectory) throw IllegalArgumentException("$original is not a directory or does not exist")

                modified.walk()
                    .filter { it.name != ".editorconfig" && it.isFile }
                    .forEach { modifiedFile ->
                        val relativeFile = modifiedFile.relativeTo(modified)

                        val originalFile = original.resolve(relativeFile)
                        val originalContents = originalFile.readLines()

                        val diff = DiffUtils.diff(originalContents, modifiedFile.readLines())
                        val patch = UnifiedDiffUtils.generateUnifiedDiff(originalFile.toString(), modifiedFile.toString(), originalContents, diff, 3)
                        if (!patch.isEmpty()) {
                            val patchFile = File(patches, "$relativeFile.patch")
                            patchFile.parentFile.mkdirs()
                            patchFile.bufferedWriter().use { writer ->
                                patch.forEach {
                                    writer.write(it)
                                    writer.write("\n")
                                }
                            }
                        }
                    }
            }
        }
    }

    val applyPatches by registering {
        group = "patch"
        description = "Applies our patches to the source directories"
        dependsOn(cleanSources)

        doLast {
            listOf(
                fileTree("original/CC-Tweaked/src/main") {
                    include("resources/assets/computercraft/lua/bios.lua")
                    include("resources/assets/computercraft/lua/rom/**")

                    // We need some stuff from the api and shared, but don't want to have to cope with
                    // adding Minecraft as a dependency. This gets a little ugly.
                    exclude("java/dan200/computercraft/**/package-info.java")
                    include("java/dan200/computercraft/api/lua/**")
                    include("java/dan200/computercraft/api/filesystem/**")
                    include("java/dan200/computercraft/api/peripheral/**")
                    exclude("java/dan200/computercraft/api/peripheral/IPeripheralTile.java")
                    exclude("java/dan200/computercraft/api/peripheral/IPeripheralProvider.java")

                    // Core is pretty simple. We just exclude some FS stuff, as it's a bit of a faff to deal with.
                    include("java/dan200/computercraft/core/**")
                    exclude("java/dan200/computercraft/core/filesystem/ComboMount.java")
                    exclude("java/dan200/computercraft/core/filesystem/EmptyMount.java")
                    exclude("java/dan200/computercraft/core/filesystem/FileMount.java")
                    exclude("java/dan200/computercraft/core/filesystem/JarMount.java")
                    exclude("java/dan200/computercraft/core/filesystem/SubMount.java")

                    include("java/dan200/computercraft/shared/util/Colour.java")
                    include("java/dan200/computercraft/shared/util/IoUtil.java")
                    include("java/dan200/computercraft/shared/util/Palette.java")
                    include("java/dan200/computercraft/shared/util/StringUtil.java")
                    include("java/dan200/computercraft/shared/util/ThreadUtils.java")
                    include("java/dan200/computercraft/ComputerCraft.java")
                },
                fileTree("original/Cobalt/src/main") {
                    include("java/**")
                }
            ).forEach { files ->
                val patches = File("src/patches/")
                val modified = File("src/main/")
                val original = files.dir

                files.forEach { originalFile ->
                    val relativeFile = originalFile.relativeTo(original)
                    val modifiedFile = modified.resolve(relativeFile)
                    val patchFile = File(patches, "$relativeFile.patch")

                    modifiedFile.parentFile.mkdirs()
                    if (patchFile.exists()) {
                        println("Patching $relativeFile")
                        val patch = UnifiedDiffUtils.parseUnifiedDiff(patchFile.readLines())
                        val modifiedContents = DiffUtils.patch(originalFile.readLines(), patch)
                        modifiedFile.bufferedWriter().use { writer ->
                            modifiedContents.forEach {
                                writer.write(it)
                                writer.write("\n")
                            }
                        }
                    } else {
                        originalFile.copyTo(modifiedFile)
                    }
                }
            }
        }
    }

    assemble {
        dependsOn(website)
    }
}

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.maven.publish)
}

kotlin {
    android {
        namespace = "io.github.meko123456.markdown"
        // Deliberately 36 while every app in this fleet is on 37, and not an oversight.
        //
        // AGP writes a library's compileSdk straight into the published AAR as minCompileSdk, so
        // this value is a requirement placed on everyone who depends on the library rather than a
        // private build detail. Verified on heatmap-compose rather than assumed: building that
        // module on 37 produced minCompileSdk=37 in its aar-metadata.properties. It is the exact
        // mechanism by which Compose BOM 2026.09.00 and okhttp 5.5.0 broke projects across this
        // fleet all week.
        //
        // Nothing in this parser touches the Android platform at all, and a consumer should not have to
        // move to a newer compile target to adopt a dependency-free markdown library.
        // It moves when something here actually needs an API newer than 36, and not before.
        compileSdk = 36
        minSdk = 26
        withHostTestBuilder {}
    }

    jvm()

    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "MarkdownBlocks"
            isStatic = true
        }
    }

    sourceSets {
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

mavenPublishing {
    publishToMavenCentral(automaticRelease = true)
    signAllPublications()
    coordinates("io.github.meko123456", "markdown-blocks", "0.1.0")

    pom {
        name.set("markdown-blocks")
        description.set(
            "A tiny, dependency-free Kotlin Multiplatform markdown parser that turns text into a " +
                "render-agnostic tree of blocks and spans — render it natively with Compose, SwiftUI, or anything else.",
        )
        url.set("https://github.com/Meko123456/markdown-blocks")
        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
            }
        }
        developers {
            developer {
                id.set("Meko123456")
                name.set("Merab Kochlamazashvili")
                url.set("https://github.com/Meko123456")
            }
        }
        scm {
            url.set("https://github.com/Meko123456/markdown-blocks")
            connection.set("scm:git:git://github.com/Meko123456/markdown-blocks.git")
            developerConnection.set("scm:git:ssh://git@github.com/Meko123456/markdown-blocks.git")
        }
    }
}

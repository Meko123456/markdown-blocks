plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.maven.publish)
}

kotlin {
    androidLibrary {
        namespace = "io.github.meko123456.markdown"
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

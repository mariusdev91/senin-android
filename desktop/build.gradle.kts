import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm") version "2.3.10"
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.10"
    id("org.jetbrains.compose") version "1.10.2"
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.json:json:20240303")
}

compose.desktop {
    application {
        mainClass = "com.mariusdev91.senin.desktop.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Exe)
            packageName = "Senin"
            packageVersion = "0.1.0"
            description = "Senin desktop weather app"
            vendor = "mariusdev91"
            includeAllModules = true

            windows {
                dirChooser = true
                menu = true
                shortcut = true
            }
        }
    }
}

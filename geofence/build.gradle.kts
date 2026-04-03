import java.util.Properties

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    `maven-publish`
    signing
    id("com.gradleup.nmcp") version "0.0.7"
}

group = "io.github.mallikarjunpatelsh"
version = "1.0.1"

kotlin {
    androidTarget {
        publishLibraryVariants("release")
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "geofence"
            isStatic = true
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val androidMain by getting {
            dependencies {
                implementation("com.google.android.gms:play-services-location:21.1.0")
                implementation("androidx.core:core-ktx:1.12.0")
            }
        }
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
        }
    }
}

publishing {
    publications.withType<MavenPublication> {
        pom {
            name.set("KMP Geofence Library")
            description.set("A Kotlin Multiplatform library for geofencing on Android and iOS")
            url.set("https://github.com/mallikarjunpatelsh/kmp-geofence-library")

            licenses {
                license {
                    name.set("MIT License")
                    url.set("https://opensource.org/licenses/MIT")
                }
            }

            developers {
                developer {
                    id.set("mallikarjunpatelsh")
                    name.set("Mallikarjun Patel")
                    email.set("mallikarjunpatelsh@gmail.com")
                }
            }

            scm {
                connection.set("scm:git:git://github.com/mallikarjunpatelsh/kmp-geofence-library.git")
                developerConnection.set("scm:git:ssh://github.com/mallikarjunpatelsh/kmp-geofence-library.git")
                url.set("https://github.com/mallikarjunpatelsh/kmp-geofence-library")
            }
        }
    }
}

nmcp {
    publishAllPublications {
        username = System.getenv("SONATYPE_USERNAME")
        password = System.getenv("SONATYPE_PASSWORD")
        publicationType = "AUTOMATIC"
    }
}

signing {
    useInMemoryPgpKeys(
        System.getenv("MAVEN_GPG_PRIVATE_KEY"),
        System.getenv("MAVEN_GPG_PASSPHRASE")
    )
    sign(publishing.publications)
}

android {
    namespace = "com.kmp.geofence"
    compileSdk = 36
    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
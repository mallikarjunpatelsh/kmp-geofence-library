# Publishing Guide

This guide explains how to publish the KMP Geofence Library to your Git repository and use it in other projects.

## Publishing to GitHub/GitLab

### 1. Initialize Git Repository

```bash
cd kmp-geofence-library
git init
git add .
git commit -m "Initial commit: KMP Geofence Library v1.0.0"
```

### 2. Add Remote Repository

```bash
# For GitHub
git remote add origin https://github.com/yourusername/kmp-geofence-library.git

# For GitLab
git remote add origin https://gitlab.com/yourusername/kmp-geofence-library.git
```

### 3. Push to Remote

```bash
git branch -M main
git push -u origin main
```

### 4. Create a Release Tag

```bash
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0
```

## Using the Library in Your Project

### Option 1: Using JitPack (Recommended)

1. Add JitPack repository to your root `build.gradle.kts`:

```kotlin
allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

2. Add the dependency in your shared module:

```kotlin
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("com.github.yourusername:kmp-geofence-library:1.0.0")
            }
        }
    }
}
```

### Option 2: Using Git Submodule

1. Add as submodule:

```bash
cd your-project
git submodule add https://github.com/yourusername/kmp-geofence-library.git libs/geofence
```

2. Include in `settings.gradle.kts`:

```kotlin
include(":libs:geofence:geofence")
```

3. Add dependency:

```kotlin
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":libs:geofence:geofence"))
            }
        }
    }
}
```

### Option 3: Local Maven Repository

1. Publish to local Maven:

```bash
./gradlew publishToMavenLocal
```

2. Add dependency:

```kotlin
repositories {
    mavenLocal()
}

dependencies {
    implementation("com.kmp.geofence:geofence:1.0.0")
}
```

## Publishing to Maven Central

### Prerequisites

1. Create a Sonatype JIRA account
2. Set up GPG keys for signing
3. Configure `gradle.properties`:

```properties
signing.keyId=YOUR_KEY_ID
signing.password=YOUR_PASSWORD
signing.secretKeyRingFile=/path/to/secring.gpg

ossrhUsername=YOUR_USERNAME
ossrhPassword=YOUR_PASSWORD
```

### Configure Publishing

Add to `geofence/build.gradle.kts`:

```kotlin
plugins {
    `maven-publish`
    signing
}

publishing {
    publications {
        create<MavenPublication>("release") {
            groupId = "com.kmp.geofence"
            artifactId = "geofence"
            version = "1.0.0"
            
            pom {
                name.set("KMP Geofence Library")
                description.set("Kotlin Multiplatform Geofencing Library")
                url.set("https://github.com/yourusername/kmp-geofence-library")
                
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                
                developers {
                    developer {
                        id.set("yourusername")
                        name.set("Your Name")
                        email.set("your.email@example.com")
                    }
                }
                
                scm {
                    connection.set("scm:git:git://github.com/yourusername/kmp-geofence-library.git")
                    developerConnection.set("scm:git:ssh://github.com/yourusername/kmp-geofence-library.git")
                    url.set("https://github.com/yourusername/kmp-geofence-library")
                }
            }
        }
    }
    
    repositories {
        maven {
            name = "sonatype"
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = project.findProperty("ossrhUsername") as String?
                password = project.findProperty("ossrhPassword") as String?
            }
        }
    }
}

signing {
    sign(publishing.publications["release"])
}
```

### Publish

```bash
./gradlew publishReleasePublicationToSonatypeRepository
```

## Version Management

Follow semantic versioning (MAJOR.MINOR.PATCH):

- **MAJOR**: Breaking changes
- **MINOR**: New features (backward compatible)
- **PATCH**: Bug fixes

### Creating a New Release

1. Update version in `build.gradle.kts`
2. Update `CHANGELOG.md`
3. Commit changes
4. Create and push tag:

```bash
git tag -a v1.1.0 -m "Release version 1.1.0"
git push origin v1.1.0
```

## Continuous Integration

### GitHub Actions Example

Create `.github/workflows/publish.yml`:

```yaml
name: Publish Library

on:
  push:
    tags:
      - 'v*'

jobs:
  publish:
    runs-on: macos-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      
      - name: Build
        run: ./gradlew build
      
      - name: Publish to Maven Local
        run: ./gradlew publishToMavenLocal
      
      - name: Create Release
        uses: actions/create-release@v1
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
        with:
          tag_name: ${{ github.ref }}
          release_name: Release ${{ github.ref }}
          draft: false
          prerelease: false
```

## Documentation

Keep documentation up to date:

- Update `README.md` for API changes
- Update `USAGE_EXAMPLES.md` with new examples
- Update `CHANGELOG.md` for each release
- Add migration guides for breaking changes

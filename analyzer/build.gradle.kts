import com.lorenzoog.plank.build.Dependencies

plugins {
  kotlin("multiplatform")
}

group = "com.lorenzoog"
version = "1.0-SNAPSHOT"

repositories {
  mavenCentral()
}

kotlin {
  jvm {
    testRuns["test"].executionTask.configure {
      useJUnitPlatform()
    }
  }

  /* Targets configuration omitted.
   * To find out how to configure the targets, please follow the link:
   * https://kotlinlang.org/docs/reference/building-mpp-with-gradle.html#setting-up-targets
   */
  sourceSets {
    val commonMain by getting {
      dependencies {
        implementation(kotlin("stdlib-common"))
        implementation(project(":grammar"))
        implementation(project(":shared"))
      }
    }

    val commonTest by getting {
      dependencies {
        implementation(kotlin("stdlib-common"))
        implementation(kotlin("test-common"))
        implementation(kotlin("test-annotations-common"))
      }
    }

    val jvmTest by getting {
      dependencies {
        implementation(kotlin("test-junit"))
        implementation(Dependencies.JUnit.JupiterApi)
        implementation(Dependencies.JUnit.JupiterEngine)
      }
    }
  }
}

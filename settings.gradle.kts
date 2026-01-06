enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")

    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Billboard"
include(":app")
include(":core:design-system")
include(":core:design-foundation")
include(":core:network")
include(":core:image-loader")
include(":core:data")
include(":core:common")
include(":core:data-impl")
include(":core:data-test")
include(":core:domain")
include(":feature:splash")
include(":core:circuit")
include(":feature:home")
include(":feature:setting")
include(":core:player")

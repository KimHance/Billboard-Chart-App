import com.android.build.api.dsl.ApplicationExtension
import com.hance.convention.configureFlavors
import com.hance.convention.configureKotlinAndroid
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.application")
                apply("org.jetbrains.kotlin.android")
            }

            extensions.configure<ApplicationExtension> {
                val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
                configureKotlinAndroid(this)
                defaultConfig.targetSdk = Integer.parseInt(libs.findVersion("targetSdk").get().requiredVersion)
                defaultConfig.minSdk = Integer.parseInt(libs.findVersion("minSdk").get().requiredVersion)

                configureFlavors(this)
            }
        }
    }
}

package com.hance.convention

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.ApplicationProductFlavor
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.ProductFlavor

@Suppress("EnumEntryName")
enum class FlavorDimension {
    data
}

@Suppress("EnumEntryName")
enum class BillboardFlavor(
    val dimension: FlavorDimension,
    val applicationIdSuffix: String? = null,
) {
    prod(FlavorDimension.data),
    demo(FlavorDimension.data, applicationIdSuffix = ".demo"),
}

fun configureFlavors(
    commonExtension: CommonExtension<*, *, *, *, *, *>,
    flavorConfigurationBlock: ProductFlavor.(flavor: BillboardFlavor) -> Unit = {},
) {
    commonExtension.apply {
        flavorDimensions += FlavorDimension.data.name

        productFlavors {
            BillboardFlavor.values().forEach { flavor ->
                register(flavor.name) {
                    dimension = flavor.dimension.name
                    flavorConfigurationBlock(this, flavor)
                    if (this@apply is ApplicationExtension && this is ApplicationProductFlavor) {
                        flavor.applicationIdSuffix?.let {
                            applicationIdSuffix = it
                        }
                    }
                }
            }
        }
    }
}

package org.wordpress.android.fluxc.model

class DomainAvailabilityModel(
    val productId: Int?,
    val productSlug: String?,
    val domainName: String?,
    val status: String?,
    val mappable: String?,
    val cost: String?,
    val supportsPrivacy: Boolean = false
)

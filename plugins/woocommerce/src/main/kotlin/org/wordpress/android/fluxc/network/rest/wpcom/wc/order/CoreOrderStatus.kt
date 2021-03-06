package org.wordpress.android.fluxc.network.rest.wpcom.wc.order

/**
 * Standard Core WooCommerce order statuses
 */
enum class CoreOrderStatus(val label: String, val value: String) {
    PENDING("Pending Payment", "pending"),
    PROCESSING("Processing", "processing"),
    ON_HOLD("On-Hold", "on-hold"),
    COMPLETED("Completed", "completed"),
    CANCELLED("Cancelled", "cancelled"),
    REFUNDED("Refunded", "refunded"),
    FAILED("Failed", "failed");

    companion object {
        private val labelMap = CoreOrderStatus.values().associateBy(CoreOrderStatus::label)
        private val valueMap = CoreOrderStatus.values().associateBy(CoreOrderStatus::value)

        /**
         * Convert the label value back into the associated CoreOrderStatus object
         */
        fun fromLabel(label: String) = labelMap[label]

        /**
         * Convert the base value into the associated CoreOrderStatus object
         */
        fun fromValue(value: String) = valueMap[value]
    }
}

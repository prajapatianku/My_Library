package com.example.data.entitlements

import com.example.data.model.SaaSPlanType

object FeatureKeys {
    const val MULTI_BRANCH = "multi_branch"
    const val DETAILED_REVENUE_EXPORTS = "detailed_revenue_exports"
    const val CUSTOM_WHATSAPP_TEMPLATES = "custom_whatsapp_templates"
    const val UNLIMITED_STUDENTS = "unlimited_students"
    const val ADVANCED_REPORTS = "advanced_reports"
    const val AUDIT_LOG_EXPORT = "audit_log_export"
}

object EntitlementManager {

    fun hasFeature(planType: SaaSPlanType, featureKey: String): Boolean {
        return when (featureKey) {
            FeatureKeys.MULTI_BRANCH -> planType == SaaSPlanType.BUSINESS
            FeatureKeys.DETAILED_REVENUE_EXPORTS,
            FeatureKeys.CUSTOM_WHATSAPP_TEMPLATES,
            FeatureKeys.ADVANCED_REPORTS,
            FeatureKeys.AUDIT_LOG_EXPORT -> planType == SaaSPlanType.PREMIUM || planType == SaaSPlanType.BUSINESS
            FeatureKeys.UNLIMITED_STUDENTS -> planType != SaaSPlanType.FREE
            else -> true
        }
    }

    fun getRequiredPlan(featureKey: String): SaaSPlanType {
        return when (featureKey) {
            FeatureKeys.MULTI_BRANCH -> SaaSPlanType.BUSINESS
            FeatureKeys.DETAILED_REVENUE_EXPORTS,
            FeatureKeys.CUSTOM_WHATSAPP_TEMPLATES,
            FeatureKeys.ADVANCED_REPORTS,
            FeatureKeys.AUDIT_LOG_EXPORT,
            FeatureKeys.UNLIMITED_STUDENTS -> SaaSPlanType.PREMIUM
            else -> SaaSPlanType.FREE
        }
    }

    fun getMaxBranchesAllowed(planType: SaaSPlanType): Int {
        return when (planType) {
            SaaSPlanType.BUSINESS -> 3
            else -> 1
        }
    }

    fun getMaxStudentsAllowed(planType: SaaSPlanType): Int {
        return when (planType) {
            SaaSPlanType.FREE -> 30
            SaaSPlanType.PREMIUM -> 500
            SaaSPlanType.BUSINESS -> 2000
        }
    }
}

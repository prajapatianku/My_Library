export type PlanType = 'FREE' | 'PREMIUM' | 'BUSINESS';

export const FeatureKeys = {
  MULTI_BRANCH: 'multi_branch',
  DETAILED_REVENUE_EXPORTS: 'detailed_revenue_exports',
  CUSTOM_WHATSAPP_TEMPLATES: 'custom_whatsapp_templates',
  UNLIMITED_STUDENTS: 'unlimited_students',
  ADVANCED_REPORTS: 'advanced_reports',
  AUDIT_LOG_EXPORT: 'audit_log_export'
} as const;

export function hasFeature(planType: PlanType, featureKey: string): boolean {
  switch (featureKey) {
    case FeatureKeys.MULTI_BRANCH:
      return planType === 'BUSINESS';
    case FeatureKeys.DETAILED_REVENUE_EXPORTS:
    case FeatureKeys.CUSTOM_WHATSAPP_TEMPLATES:
    case FeatureKeys.ADVANCED_REPORTS:
    case FeatureKeys.AUDIT_LOG_EXPORT:
      return planType === 'PREMIUM' || planType === 'BUSINESS';
    case FeatureKeys.UNLIMITED_STUDENTS:
      return planType !== 'FREE';
    default:
      return true;
  }
}

export function getRequiredPlan(featureKey: string): PlanType {
  switch (featureKey) {
    case FeatureKeys.MULTI_BRANCH:
      return 'BUSINESS';
    case FeatureKeys.DETAILED_REVENUE_EXPORTS:
    case FeatureKeys.CUSTOM_WHATSAPP_TEMPLATES:
    case FeatureKeys.ADVANCED_REPORTS:
    case FeatureKeys.AUDIT_LOG_EXPORT:
    case FeatureKeys.UNLIMITED_STUDENTS:
      return 'PREMIUM';
    default:
      return 'FREE';
  }
}

export function getMaxBranches(planType: PlanType): number {
  return planType === 'BUSINESS' ? 3 : 1;
}

export function getMaxStudents(planType: PlanType): number {
  switch (planType) {
    case 'FREE':
      return 30;
    case 'PREMIUM':
      return 500;
    case 'BUSINESS':
      return 2000;
  }
}

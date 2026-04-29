# UI and Prototype Notes

## Prototype Source

The MVP prototype is available as a Figma Make project and an exported React/Vite mockup bundle. The exported bundle uses React 18, Vite, Radix UI, lucide icons, motion, and Recharts.

The prototype should be treated as a product and interaction reference, not production-ready application code.

## Prototype Screens

The current MVP mockups include:

- Splash Screen
- User Type Selection
- Profile Setup
- Home Feed
- Player Profile
- Video Upload
- Scout Dashboard
- Organization Dashboard
- Location Search
- Fan Community
- Web Landing
- Streaming Screen
- Analytics Dashboard

## UX Priorities

- Keep onboarding short and role-specific.
- Show clear progress during profile setup.
- Make evidence upload simple on mobile.
- Always show evidence status: draft, pending verification, verified, rejected, flagged, or archived.
- Make LevelPlay Rank explainable on athlete profiles.
- Keep recruiter search dense, scannable, and filter-driven.
- Give coaches fast approve, reject, flag, and comment actions.
- Make privacy settings visible and understandable.

## Mobile App Direction

The core app experience should prioritize:

- Registration and user type selection
- Athlete profile completion
- Evidence upload and metadata capture
- Verification request flow
- Profile and rank review
- Notifications
- Search and discovery

The mobile UI should support weak connectivity and failed upload recovery.

## Web Portal Direction

The web portal should prioritize:

- Scout and agent discovery workflows
- Institution roster management
- Admin moderation and role management
- Verification queues
- Analytics dashboards
- Future streaming and performance analysis views

## Visual System Notes

- Use consistent Ultron Sport branding, color, typography, and spacing.
- Keep cards and panels practical and information-focused.
- Use clear verification badges and ranking tier indicators.
- Use icons for common actions such as upload, search, save, message, verify, reject, and flag.
- Ensure text remains readable on small screens.
- Maintain accessible contrast and keyboard-friendly controls in web views.

## Implementation Notes

- The production app should not keep the prototype's dev-only quick navigation menu.
- Shared components should be extracted for buttons, forms, badges, profile cards, evidence cards, filters, and rank summaries.
- UI state should map directly to backend evidence, verification, and ranking status values.
- Empty states should guide users to the next useful action, such as uploading first evidence or requesting verification.

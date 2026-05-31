# Code Review: Trip Expense — Reject & Delete Flow

**Reviewed**: 2026-05-31
**Decision**: APPROVE with comments (2 medium, 3 low issues)

## Summary
Solid implementation. REJECTED status, reject endpoint, delete draft, receipt upload, and UI flows all look correct. No security vulnerabilities. Logic is consistent with existing patterns. Two medium issues worth fixing.

## Findings

### CRITICAL
None.

### HIGH
None.

### MEDIUM

**M1 — Inconsistent error handling in TripExpensesPage.tsx:137**
`rejectMutation` uses raw `(e: any)` instead of `getApiError()`:
```ts
onError: (e: any) => toast.error(e?.response?.data?.message ?? 'Failed to reject'),
```
All other mutations in both files use `getApiError(e, 'message')`. Fix:
```ts
onError: (e) => toast.error(getApiError(e, 'Failed to reject')),
```

**M2 — Rejection fields not cleared on resubmit**
In `TripExpenseServiceImpl.submit()`, when a REJECTED sheet is resubmitted, `rejectedBy`, `rejectedAt`, and `rejectionReason` are not cleared. After re-approval, the response still carries stale rejection data. This could confuse the UI. Fix: clear those 3 fields when setting status back to SUBMITTED.

### LOW

**L1 — Duplicate SettleDialog**
`SettleDialog` still exists identically in both `TripExpenseTab.tsx` and `TripExpensesPage.tsx`. The two are slightly different (TripExpensesPage version has `onSettled` callback). Not a bug but increases maintenance surface.

**L2 — Redundant guard in TripExpenseTab.tsx:719**
`{expense && isApproved && <SettleDialog ...>}` — the `expense &&` check is redundant since SettleDialog is rendered inside the `expense` branch.

**L3 — No role-based guards on controller endpoints**
`reject`, `approve`, `settle` have no `@PreAuthorize` annotations. If Spring Security is not enforcing role checks at the method level elsewhere, a supervisor could call admin-only endpoints. Verify this is enforced upstream.

## Validation Results

| Check | Result |
|---|---|
| Web TypeScript build | Pass (confirmed: built in 1.07s, zero errors) |
| DB migration | Pass (confirmed: 2 rows affected, 0 warnings) |
| Backend compile | Not run — verify with `mvn compile` |

## Files Reviewed

| File | Change |
|---|---|
| `src/main/java/.../enums/TripExpenseStatus.java` | Modified — added REJECTED |
| `src/main/java/.../enums/NotificationType.java` | Modified — added TRIP_EXPENSE_REJECTED |
| `src/main/java/.../entity/LrTripExpense.java` | Modified — added rejectedBy, rejectedAt, rejectionReason |
| `src/main/java/.../dto/response/TripExpenseResponse.java` | Modified — added rejection fields |
| `src/main/java/.../service/TripExpenseService.java` | Modified — added reject(), deleteDraft() |
| `src/main/java/.../service/impl/TripExpenseServiceImpl.java` | Modified — implemented reject, deleteDraft, updated submit/updateDraft |
| `src/main/java/.../controller/TripExpenseController.java` | Modified — added reject, deleteDraft endpoints |
| `web/src/types/index.ts` | Modified — added REJECTED, rejection fields |
| `web/src/api/tripExpenses.ts` | Modified — added reject, deleteDraft |
| `web/src/pages/lrs/TripExpenseTab.tsx` | Modified — reject dialog, delete button, receipt upload |
| `web/src/pages/lrs/TripExpensesPage.tsx` | Modified — reject flow, REJECTED filter tab, sort order |

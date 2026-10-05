# Implementation Plan: Fix Duplicate App Listing and Gesture Recognition

This plan addresses two issues:
1.  **Duplicate app listing:** Investigating why `apps -ls` lists applications multiple times.
2.  **Gesture recognition bugs:** Ensuring swipe gestures work reliably.

## User Review Required

- No breaking changes anticipated.
- I will investigate `AppsManager.java` for data source duplication.
- I will investigate `UIManager.java` and `GestureDetector` logic for gesture handling.

## Open Questions

- None at the moment.

## Proposed Changes

### Apps Management
- [MODIFY] [AppsManager.java](file:///C:/Users/antho/StudioProjects/TUI-ConsoleLauncher/app/src/main/java/ohi/andre/consolelauncher/managers/AppsManager.java)
    - Review `createAppMap` and `fill` methods to ensure list initialization does not duplicate apps.
    - Review `AppsHolder` to ensure updates do not cause duplications.

### Gesture Management
- [MODIFY] [UIManager.java](file:///C:/Users/antho/StudioProjects/TUI-ConsoleLauncher/app/src/main/java/ohi/andre/consolelauncher/UIManager.java)
    - Ensure `gestureDetector` on `onTouch` is correctly passed and consumed.
    - Verify `executeGestureCmd` implementation.

## Verification Plan

### Manual Verification
- Execute `apps -ls` and verify unique list of applications.
- Perform swipe gestures and verify commands are executed as expected.

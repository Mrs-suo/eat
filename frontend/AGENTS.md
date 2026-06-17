# Frontend Conventions

## UI Component Library

All UI components **must** use [Vant 4](https://vant-ui.github.io/vant/) as the primary component library.

### Rules

1. **Form elements**: Use van-field, van-cell-group, van-cell for all form layouts. Do not build custom input/label rows.
2. **Buttons**: Use van-button for all actions (submit, cancel, delete, etc.). Style via type, block, round, plain props.
3. **Popups / Modals**: Use van-popup, van-dialog, van-action-sheet for all popups, confirmations, and bottom sheets.
4. Pickers: Use van-picker, van-dropdown-menu, van-dropdown-item for all selection controls.
5. Lists: Use van-list, van-cell, van-cell-group for list pages.
6. Feedback: Use van-toast, van-notify, van-loading for loading and notification states.
7. Images: Use van-image for all image display with lazy loading and placeholder support.
8. Upload: Use van-uploader for all file/image upload scenarios.
9. Navigation: Use van-nav-bar for all page headers with back/title/actions.
10. Layout: Use van-grid, van-row, van-col for grid and column layouts.

### Prohibited

- Do **not** build custom form fields, buttons, modals, or pickers when a Vant equivalent exists.
- Do **not** use raw input/button or custom styled elements for standard UI patterns.

### Exceptions

- Page-level layout shells (e.g. AppShell, PageLayout) may use custom components for structural layout only.
- Vant does not cover: PageLayout wrapper, tab-bar navigation shell, and similar app-level chrome.

## Code Style

- Vue 3 Composition API or Options API (match existing file style).
- <style scoped> for all component styles.
- Use rpx units for sizing to support multi-device rendering.
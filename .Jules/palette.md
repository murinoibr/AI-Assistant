## 2024-05-18 - Allow pressing Enter to send message
**Learning:** Found that the text fields in the chat interfaces didn't support the 'Send' keyboard action which is a common expectation for chat apps.
**Action:** Always add `keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send)` and `keyboardActions = KeyboardActions(onSend = { ... })` to chat input fields.
## 2024-10-27 - Added Confirmation Dialog for Delete Agent Action
**Learning:** Implementing a confirmation dialog for destructive actions (like deleting an agent) is a crucial UX pattern. It prevents accidental data loss and provides a clearer interaction flow, ensuring users are confident before confirming irreversible choices.
**Action:** Always consider adding confirmation modals or inline confirmations for actions that modify or delete user data. Ensure to use local state to track the item targeted for deletion and provide distinct confirmation and cancellation buttons.
## 2025-01-20 - Ensure screen reader accessibility for Jetpack Compose interactable custom components
**Learning:** Found that `.clickable` modifier used in UI without `role = Role.Button` and `onClickLabel` does not announce the action to the screen readers properly, and there were some missing `contentDescription` on some Icons.
**Action:** In Jetpack Compose, replace `contentDescription = null` with actual localized, descriptive strings for functionally interactive icons to ensure screen reader accessibility. Ensure custom interactive components (like generic Surfaces or Rows acting as buttons) use `.clickable(role = Role.Button)` and an `onClickLabel` to provide proper semantic context and action descriptions for screen readers.

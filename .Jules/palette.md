## 2024-05-18 - Allow pressing Enter to send message
**Learning:** Found that the text fields in the chat interfaces didn't support the 'Send' keyboard action which is a common expectation for chat apps.
**Action:** Always add `keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send)` and `keyboardActions = KeyboardActions(onSend = { ... })` to chat input fields.
## 2024-10-27 - Added Confirmation Dialog for Delete Agent Action
**Learning:** Implementing a confirmation dialog for destructive actions (like deleting an agent) is a crucial UX pattern. It prevents accidental data loss and provides a clearer interaction flow, ensuring users are confident before confirming irreversible choices.
**Action:** Always consider adding confirmation modals or inline confirmations for actions that modify or delete user data. Ensure to use local state to track the item targeted for deletion and provide distinct confirmation and cancellation buttons.
## 2024-05-19 - Ensure semantic role and descriptions for interactive components
**Learning:** Found that custom interactive components (like generic Surfaces or Rows acting as buttons) did not provide proper semantic context and action descriptions for screen readers, leading to poor accessibility. Furthermore, many functionally interactive icons used `contentDescription = null`.
**Action:** In Jetpack Compose, ensure custom interactive components use `.clickable(role = androidx.compose.ui.semantics.Role.Button, onClickLabel = "...")` to provide proper semantic context and action descriptions for screen readers. Replace `contentDescription = null` with actual localized, descriptive strings for functionally interactive icons.

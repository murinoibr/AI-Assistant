## 2024-05-18 - Allow pressing Enter to send message
**Learning:** Found that the text fields in the chat interfaces didn't support the 'Send' keyboard action which is a common expectation for chat apps.
**Action:** Always add `keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send)` and `keyboardActions = KeyboardActions(onSend = { ... })` to chat input fields.
## 2024-10-27 - Added Confirmation Dialog for Delete Agent Action
**Learning:** Implementing a confirmation dialog for destructive actions (like deleting an agent) is a crucial UX pattern. It prevents accidental data loss and provides a clearer interaction flow, ensuring users are confident before confirming irreversible choices.
**Action:** Always consider adding confirmation modals or inline confirmations for actions that modify or delete user data. Ensure to use local state to track the item targeted for deletion and provide distinct confirmation and cancellation buttons.
## 2024-05-24 - Semantic Roles for Custom Clickables
**Learning:** When creating custom interactive components (like a Row acting as a button) using the `.clickable` modifier, it lacks semantic context for screen readers. By default, screen readers might not announce it as a button or explain its interactive nature.
**Action:** Always provide semantic roles using `.clickable(role = Role.Button)` or `Modifier.semantics { role = Role.Button }` when a non-standard composable is made interactive, ensuring screen reader users understand the element's purpose.

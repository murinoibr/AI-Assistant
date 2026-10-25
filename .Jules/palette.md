## 2024-05-18 - Allow pressing Enter to send message
**Learning:** Found that the text fields in the chat interfaces didn't support the 'Send' keyboard action which is a common expectation for chat apps.
**Action:** Always add `keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send)` and `keyboardActions = KeyboardActions(onSend = { ... })` to chat input fields.
## 2024-10-27 - Added Confirmation Dialog for Delete Agent Action
**Learning:** Implementing a confirmation dialog for destructive actions (like deleting an agent) is a crucial UX pattern. It prevents accidental data loss and provides a clearer interaction flow, ensuring users are confident before confirming irreversible choices.
**Action:** Always consider adding confirmation modals or inline confirmations for actions that modify or delete user data. Ensure to use local state to track the item targeted for deletion and provide distinct confirmation and cancellation buttons.
## 2024-10-25 - Ensuring semantic role and labels in custom interactive elements
**Learning:** Found several instances where generic Composables like `Box`, `Surface`, or `Row` were made interactive using `.clickable {}`, but lacked proper semantic context for screen readers. This degrades accessibility for users relying on TalkBack, as they only hear "double tap to activate" without context.
**Action:** Always ensure that custom interactive components use `.clickable(onClickLabel = "...", role = Role.Button) { ... }` or equivalent semantics, providing clear action descriptions and semantic roles to improve the screen reader experience.

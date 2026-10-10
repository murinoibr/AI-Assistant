## 2024-05-18 - Allow pressing Enter to send message
**Learning:** Found that the text fields in the chat interfaces didn't support the 'Send' keyboard action which is a common expectation for chat apps.
**Action:** Always add `keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send)` and `keyboardActions = KeyboardActions(onSend = { ... })` to chat input fields.
## 2024-10-27 - Added Confirmation Dialog for Delete Agent Action
**Learning:** Implementing a confirmation dialog for destructive actions (like deleting an agent) is a crucial UX pattern. It prevents accidental data loss and provides a clearer interaction flow, ensuring users are confident before confirming irreversible choices.
**Action:** Always consider adding confirmation modals or inline confirmations for actions that modify or delete user data. Ensure to use local state to track the item targeted for deletion and provide distinct confirmation and cancellation buttons.

## 2026-10-10 - [Missing Role on Generic Interactive Rows]
**Learning:** The application extensively uses generic layouts like `Row` as interactive elements via `.clickable {}`. Screen readers fail to identify these interactive areas as buttons, leading to poor keyboard and assistive tech accessibility.
**Action:** When making structural layout components clickable, always explicitly provide semantic context via `.clickable(role = Role.Button)` or the `Modifier.semantics` block.

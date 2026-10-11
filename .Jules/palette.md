## 2024-05-18 - Allow pressing Enter to send message
**Learning:** Found that the text fields in the chat interfaces didn't support the 'Send' keyboard action which is a common expectation for chat apps.
**Action:** Always add `keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send)` and `keyboardActions = KeyboardActions(onSend = { ... })` to chat input fields.
## 2024-10-27 - Added Confirmation Dialog for Delete Agent Action
**Learning:** Implementing a confirmation dialog for destructive actions (like deleting an agent) is a crucial UX pattern. It prevents accidental data loss and provides a clearer interaction flow, ensuring users are confident before confirming irreversible choices.
**Action:** Always consider adding confirmation modals or inline confirmations for actions that modify or delete user data. Ensure to use local state to track the item targeted for deletion and provide distinct confirmation and cancellation buttons.
## 2024-05-24 - Require Confirmation for Destructive Actions
**Learning:** When users manage lists (like custom agents), providing a one-click delete button without confirmation can easily lead to accidental data loss. This is a common UX pitfall.
**Action:** Always wrap destructive actions (like delete, remove, or clear) in a confirmation `AlertDialog` with distinct confirm/cancel buttons and track local state for the item being deleted.

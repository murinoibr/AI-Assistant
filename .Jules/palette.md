## 2024-05-18 - Allow pressing Enter to send message
**Learning:** Found that the text fields in the chat interfaces didn't support the 'Send' keyboard action which is a common expectation for chat apps.
**Action:** Always add `keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send)` and `keyboardActions = KeyboardActions(onSend = { ... })` to chat input fields.
## 2024-10-27 - Added Confirmation Dialog for Delete Agent Action
**Learning:** Implementing a confirmation dialog for destructive actions (like deleting an agent) is a crucial UX pattern. It prevents accidental data loss and provides a clearer interaction flow, ensuring users are confident before confirming irreversible choices.
**Action:** Always consider adding confirmation modals or inline confirmations for actions that modify or delete user data. Ensure to use local state to track the item targeted for deletion and provide distinct confirmation and cancellation buttons.
## 2024-11-20 - [Confirmation Dialog for Destructive Actions]
**Learning:** Destructive actions like deleting custom data entities directly from lists without confirmation dialogue violate safe UX patterns, often leading to unintended data loss. Even if an action is performed within a quick-access UI element like a BottomSheet, standard safety mechanisms must be implemented consistently across the app.
**Action:** Implemented a confirmation `AlertDialog` triggered by the destructive action to guard against accidental deletions before communicating state changes back to the ViewModel.

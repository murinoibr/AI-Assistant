## 2024-05-18 - Allow pressing Enter to send message
**Learning:** Found that the text fields in the chat interfaces didn't support the 'Send' keyboard action which is a common expectation for chat apps.
**Action:** Always add `keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send)` and `keyboardActions = KeyboardActions(onSend = { ... })` to chat input fields.
## 2024-10-27 - Added Confirmation Dialog for Delete Agent Action
**Learning:** Implementing a confirmation dialog for destructive actions (like deleting an agent) is a crucial UX pattern. It prevents accidental data loss and provides a clearer interaction flow, ensuring users are confident before confirming irreversible choices.
**Action:** Always consider adding confirmation modals or inline confirmations for actions that modify or delete user data. Ensure to use local state to track the item targeted for deletion and provide distinct confirmation and cancellation buttons.

## 2024-11-05 - Add Semantic Roles and Accessibility Descriptions
**Learning:** Generic compose layouts (like `Row` or `Surface`) used as interactive elements without semantic roles or labels can be inaccessible to screen readers. In Compose, standard `.clickable { }` should specify `role = Role.Button` and an `onClickLabel` when acting as a functional button.
**Action:** Ensure custom interactive elements have a `Role.Button` and descriptive labels or descriptions to guarantee screen reader accessibility.

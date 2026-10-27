## 2024-05-18 - Allow pressing Enter to send message
**Learning:** Found that the text fields in the chat interfaces didn't support the 'Send' keyboard action which is a common expectation for chat apps.
**Action:** Always add `keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send)` and `keyboardActions = KeyboardActions(onSend = { ... })` to chat input fields.
## 2024-10-27 - Added Confirmation Dialog for Delete Agent Action
**Learning:** Implementing a confirmation dialog for destructive actions (like deleting an agent) is a crucial UX pattern. It prevents accidental data loss and provides a clearer interaction flow, ensuring users are confident before confirming irreversible choices.
**Action:** Always consider adding confirmation modals or inline confirmations for actions that modify or delete user data. Ensure to use local state to track the item targeted for deletion and provide distinct confirmation and cancellation buttons.
## 2024-10-27 - Add Role.Button and onClickLabel to custom clickable components
**Learning:** Custom interactive components (like a Box acting as a mic button) lack proper semantic meaning for screen readers. Using `Modifier.clickable` without a `role` makes it unclear what type of element it is.
**Action:** Always add `role = Role.Button` and a contextual `onClickLabel` to custom components using `Modifier.clickable` to ensure they are announced correctly by screen readers.

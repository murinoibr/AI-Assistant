## 2024-05-18 - Allow pressing Enter to send message
**Learning:** Found that the text fields in the chat interfaces didn't support the 'Send' keyboard action which is a common expectation for chat apps.
**Action:** Always add `keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send)` and `keyboardActions = KeyboardActions(onSend = { ... })` to chat input fields.
## 2024-10-27 - Added Confirmation Dialog for Delete Agent Action
**Learning:** Implementing a confirmation dialog for destructive actions (like deleting an agent) is a crucial UX pattern. It prevents accidental data loss and provides a clearer interaction flow, ensuring users are confident before confirming irreversible choices.
**Action:** Always consider adding confirmation modals or inline confirmations for actions that modify or delete user data. Ensure to use local state to track the item targeted for deletion and provide distinct confirmation and cancellation buttons.

## 2025-02-18 - Semantic Roles for Interactive Surfaces
**Learning:** Generic Compose elements like `Surface` or `Row` don't automatically announce their interactive nature (like being a button) to screen readers even when the `clickable` modifier is applied. This causes a confusing experience for screen reader users who won't know the element can be activated like a button.
**Action:** When adding `clickable` to structural components that function as buttons (especially list items acting as selection targets), always explicitly provide `role = Role.Button` and a descriptive `onClickLabel` within the `clickable` modifier so that accessibility services announce them correctly.

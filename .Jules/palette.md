## 2024-05-18 - Allow pressing Enter to send message
**Learning:** Found that the text fields in the chat interfaces didn't support the 'Send' keyboard action which is a common expectation for chat apps.
**Action:** Always add `keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send)` and `keyboardActions = KeyboardActions(onSend = { ... })` to chat input fields.
## 2024-10-27 - Added Confirmation Dialog for Delete Agent Action
**Learning:** Implementing a confirmation dialog for destructive actions (like deleting an agent) is a crucial UX pattern. It prevents accidental data loss and provides a clearer interaction flow, ensuring users are confident before confirming irreversible choices.
**Action:** Always consider adding confirmation modals or inline confirmations for actions that modify or delete user data. Ensure to use local state to track the item targeted for deletion and provide distinct confirmation and cancellation buttons.
## 2024-05-19 - Added Semantic Role and onClickLabel to clickable components
**Learning:** Generic layout components (like Surface or Row) that use `.clickable` don't automatically communicate their interactive nature to screen readers, leaving users guessing their purpose.
**Action:** Always add `role = Role.Button` and a descriptive `onClickLabel` to `.clickable` modifiers on generic components to provide proper semantic context and action descriptions for accessibility.

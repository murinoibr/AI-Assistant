## 2024-05-18 - Allow pressing Enter to send message
**Learning:** Found that the text fields in the chat interfaces didn't support the 'Send' keyboard action which is a common expectation for chat apps.
**Action:** Always add `keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send)` and `keyboardActions = KeyboardActions(onSend = { ... })` to chat input fields.
## 2024-10-27 - Added Confirmation Dialog for Delete Agent Action
**Learning:** Implementing a confirmation dialog for destructive actions (like deleting an agent) is a crucial UX pattern. It prevents accidental data loss and provides a clearer interaction flow, ensuring users are confident before confirming irreversible choices.
**Action:** Always consider adding confirmation modals or inline confirmations for actions that modify or delete user data. Ensure to use local state to track the item targeted for deletion and provide distinct confirmation and cancellation buttons.
## 2024-11-04 - Improve Clickable Areas and Content Descriptions
**Learning:** Adding explicit semantic roles to Compose `clickable` modifiers improves accessibility significantly for screen readers, helping users understand that a UI element acts as a button. Also, it's essential to replace `contentDescription = null` with actual descriptive labels when dealing with visually non-obvious interactable elements (such as settings panels or dialog confirm interactions).
**Action:** Use `Modifier.clickable(role = Role.Button, onClickLabel = "...")` for interactive rows and add localized strings for content descriptions instead of `null` on functional icons.

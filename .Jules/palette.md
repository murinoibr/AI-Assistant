## 2024-05-18 - Allow pressing Enter to send message
**Learning:** Found that the text fields in the chat interfaces didn't support the 'Send' keyboard action which is a common expectation for chat apps.
**Action:** Always add `keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send)` and `keyboardActions = KeyboardActions(onSend = { ... })` to chat input fields.
## 2024-10-27 - Added Confirmation Dialog for Delete Agent Action
**Learning:** Implementing a confirmation dialog for destructive actions (like deleting an agent) is a crucial UX pattern. It prevents accidental data loss and provides a clearer interaction flow, ensuring users are confident before confirming irreversible choices.
**Action:** Always consider adding confirmation modals or inline confirmations for actions that modify or delete user data. Ensure to use local state to track the item targeted for deletion and provide distinct confirmation and cancellation buttons.
## 2024-10-27 - Consistency in Destructive Actions
**Learning:** Found that while the `AgentGalleryScreen` required confirmation for deleting agents, the `AgentLibraryBottomSheet` executed the same destructive action instantly without warning. This inconsistency creates a dangerous UX where users might accidentally delete custom agents in one view but not the other.
**Action:** Always verify that destructive actions have consistent confirmation friction across ALL UI surfaces and entry points. If a confirmation dialog exists in the main view, it must also exist in the bottom sheet, drawer, or contextual menu for the same action.

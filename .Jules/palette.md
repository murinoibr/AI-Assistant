## 2024-05-18 - Allow pressing Enter to send message
**Learning:** Found that the text fields in the chat interfaces didn't support the 'Send' keyboard action which is a common expectation for chat apps.
**Action:** Always add `keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send)` and `keyboardActions = KeyboardActions(onSend = { ... })` to chat input fields.
## 2024-10-27 - Added Confirmation Dialog for Delete Agent Action
**Learning:** Implementing a confirmation dialog for destructive actions (like deleting an agent) is a crucial UX pattern. It prevents accidental data loss and provides a clearer interaction flow, ensuring users are confident before confirming irreversible choices.
**Action:** Always consider adding confirmation modals or inline confirmations for actions that modify or delete user data. Ensure to use local state to track the item targeted for deletion and provide distinct confirmation and cancellation buttons.
## 2026-11-07 - Add accessibility descriptions to icons and role=Role.Button to clickable modifiers
**Learning:** Found an accessibility issue pattern where `contentDescription = null` was extensively used for icons with functional context in Compose UI screens, such as `MainScreen`, `VoiceAgentCreationSheet`, and `ConversationalAgentHudCard`. Also observed missing semantic roles in custom interactive elements like generic Surfaces or Rows acting as buttons.
**Action:** Replace `contentDescription = null` with localized descriptive strings for all context-providing and interactive icons. Add `.clickable(role = Role.Button)` and an `onClickLabel` to generic composables with click behaviors to ensure screen readers correctly interpret them as buttons.

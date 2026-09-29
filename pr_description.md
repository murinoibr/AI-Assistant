🧹 [code health] Extract components from ConversationalAgentHudCard

🎯 **What:** The `ConversationalAgentHudCard` composable in `app/src/main/java/com/example/ui/components/ConversationalAgentHudCard.kt` was a long function containing the entire UI layout for the HUD card. This change extracts its logical sections into separate private composables: `AgentHeaderRow`, `AgentCategoryChip`, `IntegratedModulesCard`, and `LivePerformanceCard`.

💡 **Why:** By extracting these components, the main `ConversationalAgentHudCard` function is now much shorter and easier to read. It clearly outlines the high-level structure of the card. The extracted components are self-contained, improving maintainability, modularity, and readability of the codebase without changing any behavior.

✅ **Verification:** I manually inspected the refactored code to ensure the extracted functions accurately reflect the original inline code. I also ran `./gradlew build test` to verify that the project compiles successfully and all tests pass.

✨ **Result:** The `ConversationalAgentHudCard.kt` file is now structured with smaller, focused composables, resolving the "Long Function" issue and making the codebase cleaner.

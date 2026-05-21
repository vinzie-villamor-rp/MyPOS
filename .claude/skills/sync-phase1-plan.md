---
name: sync-phase1-plan
description: >
  After completing any Phase 1 step, update .claude/docs/phase1-plan.md to:
  1. Mark the step as ✅ Complete in the Steps table
  2. Move the step's "Classes to create" table to a "Completed classes" table
  3. Add a "Design decisions" section capturing all non-obvious choices made
  4. Add a "Carry-forward constraints" section for anything future steps must respect
  5. Update the Package Layout to mark completed files with ✅
  Also update the memory file at .claude/projects/memory/project_pos_onboarding.md
  if any architectural decision changes the overall project direction.
triggers:
  - after completing a phase 1 step
  - when the user asks to update or sync the plan
  - before starting a new step (verify previous step is marked complete)
---

## Instructions

1. Read `.claude/docs/phase1-plan.md`
2. Read all source files in the completed step's packages to confirm what was actually built
3. Update the plan with accurate class names, types, and notes — never guess from memory
4. Follow the exact format of Step 1 and Step 2 entries as the template
5. Run `./gradlew :pos-client:build` after any plan-related file changes to confirm nothing broke

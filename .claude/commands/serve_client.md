Launch Minecraft client with the mod. Check if built first, build if needed.

Steps:
1. Check if mod jar exists in build/libs/ (excluding -sources.jar and -javadoc.jar)
2. If NOT found:
   - Run: ./gradlew build
   - Wait for completion
3. Run: ./gradlew runClient (in background if possible)
4. Report to user that client is launching

Use bash commands to execute these steps.

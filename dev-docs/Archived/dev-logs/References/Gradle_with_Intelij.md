# Using Gradle Inside IntelliJ IDEA
**Version:** `1.0`  
**Last Updated:** `March 2025`  
**Author:** Yiwen

## Reloading Gradle in IntelliJ
1. Click **"Reload All Gradle Projects"** in the Gradle tool window.
2. Or, use the shortcut:
    - **`Ctrl + Shift + O` (Windows/Linux)**
    - **`Cmd + Shift + O` (Mac)**
3. Or, right-click `build.gradle` and select **"Reload Gradle Project"**.

## Invalidate Cache and Restart in IntelliJ
1. **Go to:**
   `File` → `Invalidate Caches / Restart` → `Invalidate and Restart`
2. After restart, **reload Gradle** as explained above.

## Deleting `.gradle` and `.idea` and Reopening IntelliJ
If dependencies are not updating:
```bash
rm -rf .gradle .idea
./gradlew clean
```
Then, reopen IntelliJ IDEA and **reload the project**.

## Ensuring Gradle Syncs on Reopen
Simply reopening IntelliJ IDEA **does not** refresh dependencies unless you manually reload the Gradle project. If necessary, use:
```bash
./gradlew build --refresh-dependencies
```

## Other Useful IntelliJ Gradle Features
1. **Run Gradle Tasks from IntelliJ**:
    - Open the **Gradle tool window** → **Tasks** → Click any task to run.
2. **Enable Auto-Import**:
    - `Preferences` → `Build, Execution, Deployment` → `Gradle` → Check **"Auto-Import"**.
3. **Change Gradle JVM**:
    - `Preferences` → `Gradle` → `Gradle JVM` → Select the correct Java version.
4. **Debug Gradle Tasks in IntelliJ**:
    - Open Gradle tool window → Right-click a task → Select **"Debug"**.


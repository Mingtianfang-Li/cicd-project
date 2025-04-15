Use Cases and Core Requirements
List all implemented features and partly implemented features

# **Feature Status Overview**
✅ Done
⚠️ Partial
❌ Not implemented


# **Use Cases**
U1. Remote Repo → Local Run
U2. Local Repo → Local Run

# **Configuration Support**
## **Structure (C1–C5)**

✅ C1. Config files in a folder
✅ C2. Each file is independent
❌ C3. Global section supported
❌ C3.1. Jobs can override global keys
⚠️️️ C3.2. Pipeline names are unique

## **Stages and Jobs**

✅ C4. Default stages with override
✅ C5.1. Job has a name
✅ C5.2. Stage defined in job
✅ C5.3. Docker image specified
✅ C5.4. Commands defined
✅ C5.5. Allow failure supported

## **Dependencies & Artifacts**

✅ C5.6. Job dependencies supported
✅ C5.6.1. Cycles checked and prevented
❌ C5.7. Artifacts can be specified
✅ C5.7.1. Files/folders listed
⚠️️️ C5.7.2. Uploads only on success

# **CLI Features Overview (L1–L6)**

✅ L1. Check Config File
✅ L2. Dry Run Order
✅ L3. Error Reporting
✅ L4. View Past Runs
✅ L5. Local Option
✅ L6. Run Pipeline
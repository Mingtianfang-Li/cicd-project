### Understanding `.save()` in Spring Data JPA

The method `save()` is provided by **Spring Data JPA** in repository interfaces that extend `JpaRepository` or `CrudRepository`. It is used to persist or update entities in a database.

#### **How `.save()` Works**
When you call:
```java
pipelineExecution = pipelineExecutionRepository.save(pipelineExecution);
```
Spring Data JPA handles the persistence as follows:

1. **If the entity is new (i.e., does not have an ID or has an ID that does not exist in the database)**:
   - It performs an **`INSERT`** operation to create a new record in the database.
   - If the primary key (`id`) is auto-generated (e.g., `@GeneratedValue(strategy = GenerationType.AUTO)`), the database assigns a value, and `save()` returns the updated entity with the generated ID.

2. **If the entity already exists (i.e., has an ID that exists in the database)**:
   - It performs a **`SELECT`** to check if the entity exists.
   - If found, it performs an **`UPDATE`** on the existing record.

3. **If `save()` is called inside a transaction (`@Transactional`)**, the changes are committed to the database **only when the transaction completes successfully**.

#### **Example Scenarios**
##### **1. Saving a New Entity**
If the entity is new (i.e., its `id` is `null` or does not exist in the database), `save()` inserts a new row.
```java
PipelineExecutionEntity pipelineExecution = PipelineExecutionEntity.builder()
        .pipelineId("pipeline_123")
        .commitHash("abc123")
        .isLocal(true)
        .status(ExecutionStatus.PENDING)
        .startTime(Instant.now())
        .build();

pipelineExecution = pipelineExecutionRepository.save(pipelineExecution);
System.out.println("Generated ID: " + pipelineExecution.getId());
```
- A new record is inserted into the database.
- The `id` field gets populated with a newly generated value.

##### **2. Updating an Existing Entity**
If an entity with the same ID already exists, `save()` updates the existing record.
```java
Optional<PipelineExecutionEntity> existingPipelineOpt = pipelineExecutionRepository.findById(pipelineId);
if (existingPipelineOpt.isPresent()) {
    PipelineExecutionEntity pipelineExecution = existingPipelineOpt.get();
    pipelineExecution.setStatus(ExecutionStatus.COMPLETED);
    pipelineExecution = pipelineExecutionRepository.save(pipelineExecution);
}
```
- `save()` checks if an entity with the given `id` exists.
- Since it does, it performs an **`UPDATE`** instead of an **`INSERT`**.

##### **3. Handling Transactions**
If `save()` is called within a `@Transactional` method (like in your `@Transactional` `startPipelineExecution()` method), changes are only committed when the transaction completes successfully.

```java
@Transactional
public void updatePipelineExecution(UUID executionId, ExecutionStatus newStatus) {
    PipelineExecutionEntity execution = pipelineExecutionRepository.findById(executionId)
        .orElseThrow(() -> new RuntimeException("Execution not found"));

    execution.setStatus(newStatus);
    pipelineExecutionRepository.save(execution);  // Will not commit until method exits successfully
}
```

#### **Key Takeaways**
- `save()` **inserts a new row** if the entity is new.
- `save()` **updates the row** if the entity already exists.
- If inside a `@Transactional` method, the operation is committed only after the transaction succeeds.
- If an entity has an `@Id` field that is auto-generated, `save()` returns the entity with the assigned ID.
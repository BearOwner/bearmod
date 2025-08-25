# Function Javadoc Template (Java/Kotlin)

```java
/**
 * Brief summary of what the method does.
 *
 * Purpose:
 * - Why this method exists and how it fits the class responsibilities
 *
 * Lifecycle/Threading:
 * - Main thread only or background-safe; cancellation semantics if any
 *
 * @param userId Non-empty user identifier
 * @param retries Number of retry attempts [0..3]
 * @return Result value or default when condition X happens
 * @throws IllegalArgumentException if userId is empty
 */
public Result fetchUser(String userId, int retries) {
    if (userId == null || userId.isEmpty()) {
        throw new IllegalArgumentException("userId must not be empty");
    }
    // ... impl ...
    return Result.success();
}
```

Notes:
- Always document purpose, lifecycle/threading, params/return/throws.
- Prefer explicit error cases and boundary behavior.

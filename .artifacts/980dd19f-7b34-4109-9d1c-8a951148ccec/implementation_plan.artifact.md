# System Backup & Synchronization for Premium Users

To implement a reliable backup system and handle device-to-account synchronization, we need to address two main challenges:
1.  **Backup/Restore Mechanism**: Saving user-specific configurations (home screen layout, favorites, settings) to a remote database.
2.  **UUID/Account Logic**: Properly handling unique device identification versus account-linked synchronization to prevent identity conflicts.

## User Review Required

- **Data Conflict Strategy**: If a user logs into a second device, we must define whether to merge data or perform a hard overwrite of local settings with the remote backup.
- **UUID Strategy**: I propose transitioning from a single "App UUID" to a "Device ID" (local to the install) and an "Account UserID" (constant for the user). This distinction is critical to avoid the collision mentioned.

## Open Questions

- What backend service is being used for data persistence? (e.g., Firebase, custom REST API, Room + Sync Adapter?)
- Should the "backup" be a manual action, or should we implement an automated sync-on-change mechanism?

## Proposed Changes

### Data Structure Modeling
#### [NEW] [BackupEntities.kt](file:///home/radek/StudioProjects/CurrencyFlow/app/src/main/java/com/currencyflow/data/model/BackupEntities.kt)
- Define Data Transfer Objects (DTOs) for:
  - `HomeScreenLayout`
  - `UserPreferences`
  - `FavoritesList`

### UUID & Authentication Logic
#### [MODIFY] [AuthManager.kt](file:///home/radek/StudioProjects/CurrencyFlow/app/src/main/java/com/currencyflow/auth/AuthManager.kt)
- Implement logic to handle:
  - Generation of a persistent `local_device_id`.
  - Fetching of the `account_uuid` upon successful sign-in.
  - Strategy to switch context from local to cloud-synced storage.

### Data Sync Repository
#### [NEW] [BackupRepository.kt](file:///home/radek/StudioProjects/CurrencyFlow/app/src/main/java/com/currencyflow/data/repository/BackupRepository.kt)
- Logic to push local room/datastore data to the cloud.
- Logic to pull and merge remote data into local state.

## Verification Plan

### Automated Tests
- Unit tests for the data merging algorithm.
- Mocking the backend service to verify data integrity during backup/restore.

### Manual Verification
- Testing on multiple emulators:
  - Log in to Device A, change settings, verify cloud backup.
  - Log in to Device B, verify settings are pulled correctly.
  - Verify `local_device_id` remains unique per installation.

------------------------------------------------------
Version 0.12.0
------------------------------------------------------
Updated to 1.20.2

**Changes**
- Updated maven group and package to `org.ladysnake`

------------------------------------------------------
Version 0.11.0
------------------------------------------------------
Updated to 1.20

------------------------------------------------------
Version 0.10.0
------------------------------------------------------
Updated to 1.19.4

------------------------------------------------------
Version 0.9.1
------------------------------------------------------
Fixed incompatibility with latest Quilted Fabric API

------------------------------------------------------
Version 0.9.0
------------------------------------------------------
Updated to 1.19.3

------------------------------------------------------
Version 0.8.0
------------------------------------------------------
**Additions**
- Added a utility method to register test classes at runtime
- Test failures that are not simple assertions now get logged with their full stacktrace

**Fixes**
- Fixed an error when attempting to disconnect a mock player

------------------------------------------------------
Version 0.7.0
------------------------------------------------------
Updated to 1.19.1

------------------------------------------------------
Version 0.6.0
------------------------------------------------------
Updated to 1.19

------------------------------------------------------
Version 0.5.2
------------------------------------------------------
**Changes**
- Switched dependency declaration from Fabric API to fabric-gametest-api-v1

------------------------------------------------------
Version 0.5.1
------------------------------------------------------
**Changes**
- Made Fabric API dependency not transitive

------------------------------------------------------
Version 0.5.0
------------------------------------------------------
**Additions**
- Added test methods for vanilla packets

------------------------------------------------------
Version 0.4.0
------------------------------------------------------
**Additions**
- Now uses injected interfaces to add methods to `TestContext`
- Added helper method to test CCA packet synchronization

------------------------------------------------------
Version 0.3.0
------------------------------------------------------
**Additions**
- Added sequence checks to the packet test framework

------------------------------------------------------
Version 0.2.0
------------------------------------------------------
**Additions**
- Added a test framework for clientbound packet sending
- Added some helper methods for assertions

------------------------------------------------------
Version 0.1.1
------------------------------------------------------
**Additions**
- Added a helper method for spawning server players

------------------------------------------------------
Version 0.1.0
------------------------------------------------------
Initial release

**Additions**
- Fix for test cases that are run multiple times
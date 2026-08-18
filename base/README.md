# Baseline binary

`Npro-1.1.0.jar` is the verified pre-1.2.0 plugin binary supplied for this work. It contains no Java source files.

The 1.2.0 build intentionally preserves its existing runtime classes rather than recreating them from decompiled source. `tools/PatchNpro.java` changes only the already-reviewed 1.2.0 integration points and `DailyLoginRewardListener.java` contains the daily-login implementation.

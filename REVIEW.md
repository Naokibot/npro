# Npro 1.2.1 source review

## Scope

This pass changes only the presentation of daily-login reward messages. Point amounts, once-per-day rules, time-zone handling, commands, permissions, costs and persistence semantics remain unchanged from Npro 1.2.0.

The supplied Npro 1.1.0 JAR did not contain Java source for `NproPlugin`, `BiomeNames` or `ShulkerSession`. Rewriting those classes from decompiled output would create unnecessary behavioral risk, so the release build preserves the verified 1.1.0 bytecode and applies only the reviewed 1.2.x hook/accessor patch.

## Message presentation

- Daily reward success broadcasts now use a gold `[FreeLife]` prefix and yellow body text.
- The already-claimed private message uses the same presentation.
- Existing 1.2.0 installations do not need to delete `config.yml`; the prefix has a code-level default.
- `&` color codes in the prefix/body are translated through Bukkit `ChatColor`.

## Behavioral checks

- `BiomeNames.class` and `NproPlugin$ShulkerSession.class` remain unchanged from the verified 1.1.0 baseline.
- Only the Npro enable-version string changes in the patched main class; feature logic remains binary-preserved.
- Patched 1.2.1 `NproPlugin.class` SHA-256: `f2a26240399614c78864be6e417fe8e7be5f8cef0b57f661d56f4a8caa7ca82a`.
- `plugin.yml` keeps the same commands and permissions while reporting version 1.2.1.
- Costs and daily reward amount remain unchanged.

## Daily reward regression cases

The local regression harness covers:

1. First login of the day adds exactly five points.
2. A second login on the same day does not add points and only tells that player the reward was already claimed.
3. A login on the following date adds five points again.
4. Addition near `Integer.MAX_VALUE` does not overflow negative.
5. Disabled daily rewards do not mutate player data.
6. A failed `data.yml` save rolls points and reward date back and does not broadcast success.

## Persistence

The daily reward writes a temporary file in the same directory and replaces `data.yml` only after a successful YAML save. When atomic replacement is unsupported, it falls back to a normal replace move. A failed save restores the in-memory point/date values.

## Runtime boundary

The repository CI builds the release against the real Spigot 1.21.1 API. The regression harness was executed locally during this review. A real Minecraft client/server E2E session is still outside CI; no claim is made that a player login was executed on the user's live server during this review.

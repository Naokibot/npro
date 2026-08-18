# Npro 1.2.0 source review

## Scope

This pass is a source-structure cleanup only. Runtime features, commands, permissions, configuration keys, point costs and daily-reward behavior are intentionally unchanged from the previously verified Npro 1.2.0 build.

The supplied Npro 1.1.0 JAR did not contain Java source for `NproPlugin`, `BiomeNames` or `ShulkerSession`. Rewriting those classes from decompiled output would create unnecessary behavioral risk, so the release build preserves the verified 1.1.0 bytecode and applies only the already-reviewed 1.2.0 hook/accessor patch.

## Style cleanup

- Small single-purpose methods.
- Descriptive local and method names.
- Guard clauses instead of deeply nested branches.
- Final fields and final implementation classes where appropriate.
- No reflection in the daily-reward path.
- No generated-looking section comments or redundant Javadocs.
- Direct Bukkit APIs and explicit failure paths.

## Behavioral equivalence checks

- The newly organized `PatchNpro.java` produces a patched `NproPlugin.class` byte-for-byte identical to the previous 1.2.0 patcher output.
- Patched `NproPlugin.class` SHA-256: `a2a251b10e6772949934b814d4c87ea0717f65c31279d7314c606d3e5f8c675f`.
- `BiomeNames.class` and `NproPlugin$ShulkerSession.class` are copied unchanged from the verified 1.1.0 baseline.
- `plugin.yml` retains the same 1.2.0 commands and permissions.
- `config.yml` retains the same runtime values and daily-login defaults.

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

The repository CI builds the release against the real Spigot 1.21.1 API. The regression harness was also executed during this review. A real Minecraft client/server E2E session is still outside CI; no claim is made that a player login was executed on the user's live server during this review.

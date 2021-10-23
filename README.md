<h1 align="center">ShatteredReclamation</h1>
<p align="center">
<img alt="GitHub Workflow Status" src="https://img.shields.io/github/workflow/status/ShatteredSoftware/ShatteredReclamation/prerelease?label=Prerelease&style=for-the-badge">
<img alt="GitHub Workflow Status" src="https://img.shields.io/github/workflow/status/ShatteredSoftware/ShatteredReclamation/tagged-release?label=Release&style=for-the-badge">
</p>
<hr>

This is a plugin that allows the wild to slowly take player creations back over.

## Config

### Threads

This plugin uses multiple threads (powered by Kotlin's coroutines) so that 
spreading blocks does not affect the main thread.
```yaml
# Delay in milliseconds between each block processed.
delay: 5
# Number of seconds between each block placement report. Set to 0 to disable.
report_interval: 60.0
# Syncs apply block changes that are queued from spreading/decaying. This 
# controls how frequently those changes are applied, in seconds. Must be set
# above zero.
sync_interval: 0.25
# The max number of blocks that can be changed in one sync.
max_sync_changes: 1000
```

### Decay Group
```yaml
# A list of materials that decay following these rules
materials:
  - GRASS_PATH
# The chance that a block will decay per time period
chance: 0.2
# Added to chance, every non-air block above this until the first block 
# adds this to the above chance of decay. Set to 0 to disable.
weight_mod: 0.0001
# The blocks this can decay into, chosen randomly.
into:
  - DIRT
  - COARSE_DIRT
# Whether block properties (like rotation, half, and shape) should be randomized.
# Defaults to true.
randomize: true
```
### Spread Group
```yaml
# The materials that should spread
materials:
  - GRASS
# The chance between 0 and 1 that a block should spread per time period. 
# Set this lower for more common blocks or else they will quickly overtake 
# everything else.
chance: 0.0005
# How many blocks away should this look for new spaces? 1 means blocks directly
# touching the spreading block, 2 means blocks touching those blocks.
range: 1
# Blocks that this block can spread to
valid_blocks:
  - GRASS_BLOCK
  - DIRT
# If this block should be placed above the target block. Otherwise, replaces the
# target block. Defaults to true.
above: true
# If this block should match the block that's spreading. Defaults to true.
match: false
# If match is false, a block is picked from these types:
# Defaults to empty.
choices: 
  - TALL_GRASS
  - GRASS
# Whether block properties (like rotation, half, and shape) should be randomized.
# Defaults to true.
randomize: true
```

## Libraries
* Kotlin, Kotlin-Reflect and Kotlinx-Coroutines by JetBrains
* [MCCoroutine](https://github.com/Shynixn/MCCoroutine) by Shynixn
* [Jackson](https://github.com/FasterXML/jackson) by FasterXML
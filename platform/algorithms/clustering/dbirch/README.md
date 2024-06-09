# Algorithm impl. - Distributed BIRCH

Simple Distributed BIRCH algorithm where BIRCH is performed in two phases:

- Locally for data
- Globally for found mean models

Finally, global clustering is performed to cluster data samples closest to those centroids.

## Parameters

```
branching_factor [Integer, default=50]: Maximum number of CF sub-cluster in each node
  - 50 - when new sample occurs and 51. sub-cluster should be created, then split of the node is performed

threshold [Double, default=0.5]: Minimum value of radius to start new sub-cluster
  - 0.1 - more splits
  - 10.0 - less splits, etc.

groups [Integer, default=null]: Number of final groups (max leaves)
  - 3 - maximum 3 leaves allowed
  - 30 - maximum 30 leaves allowed

g_threshold [Double, default=0.5]: Minimum value of radius to start new sub-cluster for global step
  - 0.1 - more splits
  - 10.0 - less splits, etc.

g_groups [Integer, default=null]: Number of final groups (max leaves) for global step
  - 3 - maximum 3 leaves allowed
  - 30 - maximum 30 leaves allowed
```

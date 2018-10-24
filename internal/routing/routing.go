// Package routing simulates a compiled policy against the recorded traces. For
// each task the compiler picks the winning model (the highest-scoring eligible
// candidate) and then replays every recorded trace as if the router had sent
// it to that winner, measuring realized cost, latency, quality and reliability.
// Because we only ever replay observations we already hold, the simulation is
// fully deterministic and honest: it never invents a data point.
package routing

import (
	"fmt"
	"sort"


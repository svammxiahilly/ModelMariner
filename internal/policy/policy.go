// Package policy defines the routing-policy language and its evaluator. A
// policy is a set of HARD constraints (budget, latency, privacy, reliability)
// plus a preference expressing how to rank the survivors. Constraints are hard:
// a candidate that violates any constraint is disqualified, never merely
// penalized. This makes the compiler's decisions auditable and safe for
// regulated workloads.
package policy

import (
	"encoding/json"
	"fmt"
	"io"
	"sort"
	"strings"

	"github.com/nauticalabs/modelmariner/internal/reliability"
	"github.com/nauticalabs/modelmariner/internal/trace"
)

// Objective names a rankable dimension used by preferences.
type Objective string

const (
	ObjCost        Objective = "cost"
	ObjLatency     Objective = "latency"
	ObjQuality     Objective = "quality"
	ObjReliability Objective = "reliability"
)

var validObjectives = map[Objective]bool{
	ObjCost: true, ObjLatency: true, ObjQuality: true, ObjReliability: true,

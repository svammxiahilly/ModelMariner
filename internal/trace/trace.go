// Package trace defines the provider-neutral trace record model and the
// ingestion pipeline that reads JSONL logs, validates every field, and
// normalizes values into a canonical form suitable for evaluation.
//
// A "trace" is a single recorded observation of one model handling one task:
// how many tokens it consumed, what it cost, how long it took, how good the
// answer was, whether it errored, and what privacy tier the data belonged to.
// modelmariner never contacts a provider; it reasons entirely over these
// recorded observations, which makes every run deterministic and replayable.
package trace

import (
	"bufio"
	"encoding/json"
	"fmt"
	"io"
	"math"
	"sort"
	"strings"
)

// Package report assembles the outputs of every engine stage into a single
// deterministic document that both the text renderer and the TypeScript
// dashboard consume. Determinism is a first-class requirement: given identical
// inputs the JSON bytes are byte-for-byte identical, which makes reports safe to
// commit, diff, and gate CI on.
package report

import (
	"bytes"
	"encoding/json"
	"fmt"
	"sort"
	"strings"
	"time"

	"github.com/nauticalabs/modelmariner/internal/explain"
	"github.com/nauticalabs/modelmariner/internal/pareto"
	"github.com/nauticalabs/modelmariner/internal/policy"
	"github.com/nauticalabs/modelmariner/internal/reliability"
	"github.com/nauticalabs/modelmariner/internal/routing"
	"github.com/nauticalabs/modelmariner/internal/trace"
)

// SchemaVersion identifies the report format so the dashboard can guard against
// consuming an incompatible document.
const SchemaVersion = "modelmariner/v1"

// Report is the complete, self-describing analysis document.
type Report struct {
	Schema      string                  `json:"schema"`

package reliability

import (
	"math"
	"testing"

	"github.com/nauticalabs/modelmariner/internal/trace"
)

func mk(model, task string, cost, lat, qual float64, err bool, priv trace.PrivacyTier) trace.Trace {
	return trace.Trace{
		Model: model, Task: task, CostUSD: cost, LatencyMS: lat,
		Quality: qual, Error: err, Privacy: priv, Completion: 10, Prompt: 10,
	}
}

func TestComputeBasic(t *testing.T) {
	traces := []trace.Trace{

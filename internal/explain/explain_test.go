package explain

import (
	"strings"
	"testing"

	"github.com/nauticalabs/modelmariner/internal/policy"
	"github.com/nauticalabs/modelmariner/internal/routing"
)

func TestExplainWinner(t *testing.T) {
	d := routing.Decision{
		Task: "t", Policy: "p", Winner: "premium", Runnerup: "cheap",
		Score: 0.9, Margin: 0.2,
		Evaluations: []policy.Evaluation{
			{Model: "premium", Eligible: true, Score: 0.9, Components: map[string]float64{"quality": 0.9}},
		},
		Realized: routing.RealizedMetrics{Model: "premium", Calls: 10, Successes: 10, SuccessRate: 1, MeanQuality: 0.98, MeanLatencyMS: 400, TotalCostUSD: 6.0},
		Baseline: routing.RealizedMetrics{Model: "cheap", Calls: 10, MeanQuality: 0.80, TotalCostUSD: 1.0},
	}
	e := For(d)

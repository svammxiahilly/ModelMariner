package pareto

import (
	"testing"

	"github.com/nauticalabs/modelmariner/internal/reliability"
)

func agg(model string, cost, p95, qual, rel float64) reliability.Aggregate {
	return reliability.Aggregate{
		Model: model, Task: "t", Successes: 10, Samples: 10,
		MeanCostUSD: cost, P95LatencyMS: p95, MeanQuality: qual, WilsonLower: rel,
	}
}


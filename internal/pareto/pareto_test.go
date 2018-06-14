package pareto

import (
	"testing"

	"github.com/nauticalabs/modelmariner/internal/reliability"
)

func agg(model string, cost, p95, qual, rel float64) reliability.Aggregate {
	return reliability.Aggregate{

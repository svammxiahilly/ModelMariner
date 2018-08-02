package policy

import (
	"strings"
	"testing"

	"github.com/nauticalabs/modelmariner/internal/reliability"
	"github.com/nauticalabs/modelmariner/internal/trace"
)

func agg(model string, cost, p95, qual, rel float64, priv trace.PrivacyTier) reliability.Aggregate {
	return reliability.Aggregate{

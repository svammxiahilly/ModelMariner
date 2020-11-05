package report

import (
	"bytes"
	"encoding/json"
	"strings"
	"testing"

	"github.com/nauticalabs/modelmariner/internal/pareto"
	"github.com/nauticalabs/modelmariner/internal/policy"
	"github.com/nauticalabs/modelmariner/internal/reliability"
	"github.com/nauticalabs/modelmariner/internal/routing"
	"github.com/nauticalabs/modelmariner/internal/trace"
)

const corpus = `{"model":"cheap","task":"t","tokens":{"prompt":10,"completion":10},"cost_usd":0.10,"latency_ms":100,"quality":0.80,"error":false,"privacy":"public"}
{"model":"cheap","task":"t","tokens":{"prompt":10,"completion":10},"cost_usd":0.10,"latency_ms":120,"quality":0.82,"error":false,"privacy":"public"}
{"model":"premium","task":"t","tokens":{"prompt":10,"completion":10},"cost_usd":0.60,"latency_ms":400,"quality":0.98,"error":false,"privacy":"public"}
{"model":"premium","task":"t","tokens":{"prompt":10,"completion":10},"cost_usd":0.60,"latency_ms":420,"quality":0.97,"error":false,"privacy":"public"}`

const policyJSON = `{"policies":[{"name":"quality","preference":{"weights":[{"objective":"quality","weight":1}]}}]}`

func buildReport(t *testing.T) Report {
	t.Helper()

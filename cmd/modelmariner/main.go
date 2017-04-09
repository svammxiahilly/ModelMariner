// Command modelmariner is an offline LLM trace evaluation and routing-policy
// compiler. It ingests provider-neutral JSONL traces, computes reliability and
// Pareto frontiers, evaluates hard budget/latency/privacy policies, simulates
// routing against the recorded traces, explains its selections, and emits
// deterministic policy and report artifacts. It never contacts a provider.
//
// Usage:
//
//	modelmariner analyze --traces file.jsonl [--policy policy.json] [flags]
//	modelmariner validate --traces file.jsonl
//	modelmariner version
package main

import (
	"fmt"

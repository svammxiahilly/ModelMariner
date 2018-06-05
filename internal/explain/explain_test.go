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

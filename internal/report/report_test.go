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

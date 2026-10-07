# Security Policy

## Reporting a vulnerability

Please do not open a public issue for a security problem. Use GitHub's private
[security advisory](https://github.com/svammxiahilly/ModelMariner/security/advisories/new) form, or contact a
maintainer directly if the form is unavailable to you.

Include what an attacker gains in one sentence, the version or commit you are running, the smallest
reproduction you can manage, and whether the input is attacker-controlled in your setup. You should
get an acknowledgement within a few working days and a disclosure timeline before anything is
published.

## Scope

ModelMariner scores recorded model traces and compiles routing policies. In
scope: a crafted trace (missing usage fields, clock skew, unicode ids) that scores
wrongly, a compiled policy that admits a route it should refuse, a Pareto tie that
renders twice in the dashboard, and path handling in the trace loader.

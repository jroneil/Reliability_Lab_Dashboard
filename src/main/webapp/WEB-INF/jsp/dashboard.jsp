<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Reliability Lab Dashboard</title>
    <link rel="stylesheet" href="/dashboard.css">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700;800&display=swap" rel="stylesheet">
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
</head>
<body>
    <div class="container">
        <header>
            <h1>Reliability Lab</h1>
            <p style="color: var(--text-secondary)">Observe service behavior under simulated stress and resilience patterns</p>
        </header>

        <section class="controls">
            <div class="control-group">
                <label for="scenario">Scenario</label>
                <select id="scenario">
                    <option value="slow">Slow Dependency (+250ms)</option>
                    <option value="fail">Failing Dependency (30% error)</option>
                    <option value="spike">Traffic Spike (2x Threads)</option>
                </select>
            </div>
            <div class="control-group">
                <label for="mode">Resilience Mode</label>
                <select id="mode">
                    <option value="naive">Naive (Direct Call)</option>
                    <option value="timeouts">Hard Timeouts (200ms)</option>
                    <option value="cb">Full Resilience (CB + Bulkhead)</option>
                </select>
            </div>
            <div class="control-group">
                <label for="threads">Concurrency</label>
                <select id="threads">
                    <option value="1">1 Thread</option>
                    <option value="4" selected>4 Threads</option>
                    <option value="16">16 Threads</option>
                    <option value="32">32 Threads</option>
                </select>
            </div>
            <div class="control-group">
                <label for="durationSec">Duration (sec)</label>
                <input type="number" id="durationSec" value="20" min="5" max="60">
            </div>
            <div class="control-group">
                <label for="warmupSec">Warmup (sec)</label>
                <input type="number" id="warmupSec" value="0" min="0" max="10">
            </div>
            <button id="runBtn">Run Simulation</button>
        </section>

        <div style="margin-bottom: 20px;">
            <span id="statusDot" class="status-indicator"></span>
            <span id="statusText" style="font-weight: 600; color: var(--text-secondary)">READY</span>
        </div>

        <section class="metrics-grid">
            <div class="metric-card">
                <div class="metric-label">p95 Latency</div>
                <div id="p95Ms" class="metric-value">-</div>
            </div>
            <div class="metric-card">
                <div class="metric-label">p99 Latency</div>
                <div id="p99Ms" class="metric-value">-</div>
            </div>
            <div class="metric-card">
                <div class="metric-label">Error Rate</div>
                <div id="errorRate" class="metric-value">-</div>
            </div>
            <div class="metric-card">
                <div class="metric-label">Throughput</div>
                <div id="throughput" class="metric-value">-</div>
            </div>
        </section>

        <section class="charts-grid">
            <div class="chart-container">
                <canvas id="latencyChart"></canvas>
            </div>
            <div class="chart-container">
                <canvas id="errorChart"></canvas>
            </div>
        </section>

        <section class="recommendation-panel">
            <h3>Lab Recommendation</h3>
            <p id="recText">Select a scenario and run the simulation to see recommendations.</p>
            <div id="riskNotes"></div>
        </section>
    </div>

    <script src="/dashboard.js"></script>
</body>
</html>

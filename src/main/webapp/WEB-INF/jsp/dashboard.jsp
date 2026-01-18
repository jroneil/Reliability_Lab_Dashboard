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
                <p style="color: var(--text-secondary)">Observe service behavior under simulated stress and resilience
                    patterns</p>
            </header>

            <div class="versus-header">
                <span style="font-size: 0.9rem; font-weight: 700; color: var(--text-secondary)">VERSUS MODE</span>
                <label class="switch">
                    <input type="checkbox" id="versusMode">
                    <span class="slider"></span>
                </label>
            </div>

            <div class="controls-wrapper">
                <section class="controls-config vs-a-config" id="configA">
                    <div style="width: 100%; margin-bottom: 5px;">
                        <span class="vs-label vs-a-label">SYSTEM A</span>
                    </div>
                    <div class="control-group">
                        <label for="scenarioA">Scenario</label>
                        <select id="scenarioA">
                            <option value="slow">Slow Dependency (+250ms)</option>
                            <option value="fail">Failing Dependency (30% error)</option>
                            <option value="spike">Traffic Spike (2x Threads)</option>
                        </select>
                    </div>
                    <div class="control-group">
                        <label for="modeA">Resilience Mode</label>
                        <select id="modeA">
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
                        <label for="durationSec">Duration (s)</label>
                        <input type="number" id="durationSec" value="20" min="5" max="60" style="width: 60px;">
                    </div>
                    <button id="runBtn">Run Simulation</button>
                </section>

                <section class="controls-config vs-b-config hidden" id="configB">
                    <div style="width: 100%; margin-bottom: 5px;">
                        <span class="vs-label vs-b-label">SYSTEM B</span>
                    </div>
                    <div class="control-group">
                        <label for="scenarioB">Scenario</label>
                        <select id="scenarioB">
                            <option value="slow">Slow Dependency (+250ms)</option>
                            <option value="fail">Failing Dependency (30% error)</option>
                            <option value="spike">Traffic Spike (2x Threads)</option>
                        </select>
                    </div>
                    <div class="control-group">
                        <label for="modeB">Resilience Mode</label>
                        <select id="modeB">
                            <option value="naive">Naive (Direct Call)</option>
                            <option value="timeouts">Hard Timeouts (200ms)</option>
                            <option value="cb" selected>Full Resilience (CB + Bulkhead)</option>
                        </select>
                    </div>
                </section>
            </div>

            <div style="margin-bottom: 20px; display: flex; align-items: center; gap: 20px;">
                <div>
                    <span id="statusDotA" class="status-indicator"></span>
                    <span id="statusTextA" style="font-weight: 600; color: var(--text-secondary)">READY</span>
                </div>
                <div id="statusBWrapper" class="hidden">
                    <span id="statusDotB" class="status-indicator"></span>
                    <span id="statusTextB" style="font-weight: 600; color: var(--text-secondary)">READY</span>
                </div>
            </div>

            <section class="metrics-grid">
                <div class="metric-card">
                    <div class="metric-label">p95 Latency</div>
                    <div id="p95Ms" class="metric-value">
                        <div class="metric-vs">
                            <span id="p95MsA">-</span>
                            <span class="metric-divider">|</span>
                            <span id="p95MsB">-</span>
                        </div>
                    </div>
                </div>
                <div class="metric-card">
                    <div class="metric-label">p99 Latency</div>
                    <div id="p99Ms" class="metric-value">
                        <div class="metric-vs">
                            <span id="p99MsA">-</span>
                            <span class="metric-divider">|</span>
                            <span id="p99MsB">-</span>
                        </div>
                    </div>
                </div>
                <div class="metric-card">
                    <div class="metric-label">Error Rate</div>
                    <div id="errorRate" class="metric-value">
                        <div class="metric-vs">
                            <span id="errorRateA">-</span>
                            <span class="metric-divider">|</span>
                            <span id="errorRateB">-</span>
                        </div>
                    </div>
                </div>
                <div class="metric-card">
                    <div class="metric-label">Throughput</div>
                    <div id="throughput" class="metric-value">
                        <div class="metric-vs">
                            <span id="throughputA">-</span>
                            <span class="metric-divider">|</span>
                            <span id="throughputB">-</span>
                        </div>
                    </div>
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
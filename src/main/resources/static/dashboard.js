let latencyChart;
let errorChart;
let pollInterval;
let activeRunA = null;
let activeRunB = null;

document.addEventListener('DOMContentLoaded', () => {
    initCharts();

    document.getElementById('runBtn').addEventListener('click', startSimulation);
    document.getElementById('versusMode').addEventListener('change', toggleVersusMode);
});

function toggleVersusMode(e) {
    const isVs = e.target.checked;
    document.getElementById('configB').classList.toggle('hidden', !isVs);
    document.getElementById('statusBWrapper').classList.toggle('hidden', !isVs);

    // Refresh chart to update legends/colors if needed
    initCharts();
}

function initCharts() {
    if (latencyChart) latencyChart.destroy();
    if (errorChart) errorChart.destroy();

    const isVs = document.getElementById('versusMode').checked;
    const ctxL = document.getElementById('latencyChart').getContext('2d');

    const datasetsL = [
        { label: 'A: p95', data: [], borderColor: '#38bdf8', tension: 0.1, borderWidth: 3 },
        { label: 'A: p99', data: [], borderColor: '#818cf8', tension: 0.1, borderDash: [5, 5] }
    ];

    if (isVs) {
        datasetsL.push({ label: 'B: p95', data: [], borderColor: '#f472b6', tension: 0.1, borderWidth: 3 });
        datasetsL.push({ label: 'B: p99', data: [], borderColor: '#fbbf24', tension: 0.1, borderDash: [5, 5] });
    }

    latencyChart = new Chart(ctxL, {
        type: 'line',
        data: { labels: [], datasets: datasetsL },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                y: { beginAtZero: true, grid: { color: '#334155' }, title: { display: true, text: 'ms' } },
                x: { grid: { display: false } }
            },
            plugins: { legend: { labels: { color: '#f8fafc' } } }
        }
    });

    const ctxE = document.getElementById('errorChart').getContext('2d');
    const datasetsE = [
        { label: 'A: Errors', data: [], backgroundColor: '#38bdf8' }
    ];

    if (isVs) {
        datasetsE.push({ label: 'B: Errors', data: [], backgroundColor: '#f472b6' });
    }

    errorChart = new Chart(ctxE, {
        type: 'bar',
        data: { labels: [], datasets: datasetsE },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                y: { beginAtZero: true, grid: { color: '#334155' } },
                x: { grid: { display: false } }
            },
            plugins: { legend: { labels: { color: '#f8fafc' } } }
        }
    });
}

async function startSimulation() {
    const isVs = document.getElementById('versusMode').checked;
    const common = {
        threads: parseInt(document.getElementById('threads').value),
        durationSec: parseInt(document.getElementById('durationSec').value),
        warmupSec: 0
    };

    const payloadA = {
        ...common,
        scenario: document.getElementById('scenarioA').value,
        mode: document.getElementById('modeA').value
    };

    document.getElementById('runBtn').disabled = true;
    resetUI();

    try {
        const resA = await fetch('/api/run', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payloadA)
        });
        const dataA = await resA.json();
        activeRunA = dataA.runId;

        if (isVs) {
            const payloadB = {
                ...common,
                scenario: document.getElementById('scenarioB').value,
                mode: document.getElementById('modeB').value
            };
            const resB = await fetch('/api/run', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payloadB)
            });
            const dataB = await resB.json();
            activeRunB = dataB.runId;
        } else {
            activeRunB = null;
        }

        startPolling();
    } catch (err) {
        console.error(err);
        document.getElementById('runBtn').disabled = false;
    }
}

function startPolling() {
    if (pollInterval) clearInterval(pollInterval);
    pollInterval = setInterval(async () => {
        try {
            const [resA, resB] = await Promise.all([
                fetch(`/api/run/${activeRunA}`),
                activeRunB ? fetch(`/api/run/${activeRunB}`) : Promise.resolve(null)
            ]);

            const dataA = await resA.json();
            const dataB = resB ? await resB.json() : null;

            updateUI(dataA, dataB);

            const doneA = dataA.status === 'DONE' || dataA.status === 'FAILED';
            const doneB = !dataB || (dataB.status === 'DONE' || dataB.status === 'FAILED');

            if (doneA && doneB) {
                clearInterval(pollInterval);
                document.getElementById('runBtn').disabled = false;
            }
        } catch (err) {
            console.error('Polling error:', err);
        }
    }, 1000);
}

function updateUI(dataA, dataB) {
    updateSystemUI('A', dataA);
    if (dataB) {
        updateSystemUI('B', dataB);
    }

    // Sync charts
    const labels = dataA.seriesLatency.map(p => p.tSec + 's');
    latencyChart.data.labels = labels;
    latencyChart.data.datasets[0].data = dataA.seriesLatency.map(p => p.p95Ms);
    latencyChart.data.datasets[1].data = dataA.seriesLatency.map(p => p.p99Ms);

    if (dataB) {
        latencyChart.data.datasets[2].data = dataB.seriesLatency.map(p => p.p95Ms);
        latencyChart.data.datasets[3].data = dataB.seriesLatency.map(p => p.p99Ms);
    }
    latencyChart.update('none');

    errorChart.data.labels = labels;
    errorChart.data.datasets[0].data = dataA.seriesErrors.map(p => p.errors);
    if (dataB) {
        errorChart.data.datasets[1].data = dataB.seriesErrors.map(p => p.errors);
    }
    errorChart.update('none');

    // Update Recommendations
    const recText = document.getElementById('recText');
    const riskNotes = document.getElementById('riskNotes');
    riskNotes.innerHTML = '';

    if (dataB) {
        recText.innerHTML = `<span class="v-text-a">System A:</span> ${dataA.recommendation}<br><br><span class="v-text-b">System B:</span> ${dataB.recommendation}`;
        [...dataA.riskNotes, ...dataB.riskNotes].forEach(note => addRiskNote(note, riskNotes));
    } else {
        recText.textContent = dataA.recommendation;
        dataA.riskNotes.forEach(note => addRiskNote(note, riskNotes));
    }
}

function addRiskNote(note, container) {
    const div = document.createElement('div');
    div.className = 'risk-note';
    div.innerHTML = `<span>⚠️</span> ${note}`;
    container.appendChild(div);
}

function updateSystemUI(suffix, data) {
    const statusEl = document.getElementById('statusText' + suffix);
    const dot = document.getElementById('statusDot' + suffix);
    statusEl.textContent = data.status;
    dot.className = 'status-indicator status-' + data.status.toLowerCase();

    document.getElementById('p95Ms' + suffix).textContent = data.summary.p95Ms.toFixed(0) + 'ms';
    document.getElementById('p99Ms' + suffix).textContent = data.summary.p99Ms.toFixed(0) + 'ms';
    document.getElementById('errorRate' + suffix).textContent = data.summary.errorRatePct.toFixed(1) + '%';
    document.getElementById('throughput' + suffix).textContent = data.summary.throughputRps.toFixed(1) + ' rps';
}

function resetUI() {
    latencyChart.data.labels = [];
    latencyChart.data.datasets.forEach(d => d.data = []);
    latencyChart.update();

    errorChart.data.labels = [];
    errorChart.data.datasets.forEach(d => d.data = []);
    errorChart.update();

    ['A', 'B'].forEach(suffix => {
        document.getElementById('p95Ms' + suffix).textContent = '-';
        document.getElementById('p99Ms' + suffix).textContent = '-';
        document.getElementById('errorRate' + suffix).textContent = '-';
        document.getElementById('throughput' + suffix).textContent = '-';
        document.getElementById('statusText' + suffix).textContent = 'READY';
        document.getElementById('statusDot' + suffix).className = 'status-indicator';
    });
}

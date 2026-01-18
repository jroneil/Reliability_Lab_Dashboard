let latencyChart;
let errorChart;
let pollInterval;

document.addEventListener('DOMContentLoaded', () => {
    initCharts();
    
    document.getElementById('runBtn').addEventListener('click', startSimulation);
});

function initCharts() {
    const ctxL = document.getElementById('latencyChart').getContext('2d');
    latencyChart = new Chart(ctxL, {
        type: 'line',
        data: {
            labels: [],
            datasets: [
                { label: 'p50', data: [], borderColor: '#38bdf8', tension: 0.1 },
                { label: 'p95', data: [], borderColor: '#818cf8', tension: 0.1 },
                { label: 'p99', data: [], borderColor: '#f472b6', tension: 0.1 }
            ]
        },
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
    errorChart = new Chart(ctxE, {
        type: 'bar',
        data: {
            labels: [],
            datasets: [
                { label: 'Errors', data: [], backgroundColor: '#ef4444' },
                { label: 'Total', data: [], backgroundColor: '#334155' }
            ]
        },
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
    const payload = {
        scenario: document.getElementById('scenario').value,
        mode: document.getElementById('mode').value,
        threads: parseInt(document.getElementById('threads').value),
        durationSec: parseInt(document.getElementById('durationSec').value),
        warmupSec: parseInt(document.getElementById('warmupSec').value)
    };

    document.getElementById('runBtn').disabled = true;
    resetUI();

    try {
        const response = await fetch('/api/run', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (response.ok) {
            startPolling();
        } else {
            alert('Failed to start simulation');
            document.getElementById('runBtn').disabled = false;
        }
    } catch (err) {
        console.error(err);
        document.getElementById('runBtn').disabled = false;
    }
}

function startPolling() {
    if (pollInterval) clearInterval(pollInterval);
    pollInterval = setInterval(async () => {
        try {
            const res = await fetch('/api/run/latest');
            const data = await res.json();
            
            if (!data) return;

            updateUI(data);

            if (data.status === 'DONE' || data.status === 'FAILED') {
                clearInterval(pollInterval);
                document.getElementById('runBtn').disabled = false;
            }
        } catch (err) {
            console.error('Polling error:', err);
        }
    }, 1000);
}

function updateUI(data) {
    // Update Status
    const statusEl = document.getElementById('statusText');
    const dot = document.getElementById('statusDot');
    statusEl.textContent = data.status;
    dot.className = 'status-indicator status-' + data.status.toLowerCase();

    // Update Summary Cards
    document.getElementById('p95Ms').textContent = data.summary.p95Ms.toFixed(0) + 'ms';
    document.getElementById('p99Ms').textContent = data.summary.p99Ms.toFixed(0) + 'ms';
    document.getElementById('errorRate').textContent = data.summary.errorRatePct.toFixed(1) + '%';
    document.getElementById('throughput').textContent = data.summary.throughputRps.toFixed(1) + ' rps';

    // Update Charts
    const labels = data.seriesLatency.map(p => p.tSec + 's');
    
    latencyChart.data.labels = labels;
    latencyChart.data.datasets[0].data = data.seriesLatency.map(p => p.p50Ms);
    latencyChart.data.datasets[1].data = data.seriesLatency.map(p => p.p95Ms);
    latencyChart.data.datasets[2].data = data.seriesLatency.map(p => p.p99Ms);
    latencyChart.update('none');

    errorChart.data.labels = labels;
    errorChart.data.datasets[0].data = data.seriesErrors.map(p => p.errors);
    errorChart.data.datasets[1].data = data.seriesErrors.map(p => p.total);
    errorChart.update('none');

    // Update Recommendation
    document.getElementById('recText').textContent = data.recommendation;
    const notesEl = document.getElementById('riskNotes');
    notesEl.innerHTML = '';
    data.riskNotes.forEach(note => {
        const div = document.createElement('div');
        div.className = 'risk-note';
        div.innerHTML = `<span>⚠️</span> ${note}`;
        notesEl.appendChild(div);
    });
}

function resetUI() {
    latencyChart.data.labels = [];
    latencyChart.data.datasets.forEach(d => d.data = []);
    latencyChart.update();

    errorChart.data.labels = [];
    errorChart.data.datasets.forEach(d => d.data = []);
    errorChart.update();

    document.getElementById('p95Ms').textContent = '-';
    document.getElementById('p99Ms').textContent = '-';
    document.getElementById('errorRate').textContent = '-';
    document.getElementById('throughput').textContent = '-';
    document.getElementById('recText').textContent = 'Waiting for results...';
    document.getElementById('riskNotes').innerHTML = '';
}

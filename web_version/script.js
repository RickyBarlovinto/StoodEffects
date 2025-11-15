document.addEventListener('DOMContentLoaded', () => {
    // Page Elements
    const frontPage = document.getElementById('frontPage');
    const historyPage = document.getElementById('historyPage');
    const mainGame = document.getElementById('mainGame');
    const resultsPage = document.getElementById('resultsPage');

    // Buttons & Inputs
    const startGameBtn = document.getElementById('startGameBtn');
    const viewHistoryBtn = document.getElementById('viewHistoryBtn');
    const applyCustomBtn = document.getElementById('applyCustomBtn');
    const backToMenuBtn = document.getElementById('backToMenuBtn');
    const deleteHistoryBtn = document.getElementById('deleteHistoryBtn');
    const restartTestBtn = document.getElementById('restartTestBtn');
    const viewHistoryResultsBtn = document.getElementById('viewHistoryResultsBtn');
    const backToMenuResultsBtn = document.getElementById('backToMenuResultsBtn');
    const difficultySelect = document.getElementById('difficultySelect');
    const wordList = document.getElementById('wordList');
    const colorList = document.getElementById('colorList');

    // Game Display
    const timeTv = document.getElementById('timeTv');
    const scoreTv = document.getElementById('scoreTv');
    const accuracyTv = document.getElementById('accuracyTv');
    const wordTv = document.getElementById('wordTv');
    const colorButtonsContainer = document.querySelector('.color-buttons');

    // History & Results Display
    const historyList = document.getElementById('historyList');
    const resultsGrid = document.querySelector('#resultsPage .stats-grid');

    // Default words and colors
    let words = ["RED", "BLUE", "GREEN", "YELLOW", "BLACK"];
    let colors = ["#F44336", "#2196F3", "#4CAF50", "#FFEB3B","#212121"];
    let qualityChart, speedChart; // Chart instances

    // Game State Variables
    let score, totalAttempts, correctAttempts, challengeTime, currentWordInkValue, timer, difficulty, timeLeft, gameData;

    function init() {
        // Event Listeners
        startGameBtn.addEventListener('click', () => {
            difficulty = difficultySelect.value;
            showPage(mainGame, startGame);
        });
        viewHistoryBtn.addEventListener('click', () => showPage(historyPage, displayHistory));
        applyCustomBtn.addEventListener('click', applyCustomSettings);
        backToMenuBtn.addEventListener('click', () => showPage(frontPage));
        deleteHistoryBtn.addEventListener('click', deleteHistory);
        restartTestBtn.addEventListener('click', () => {
            difficulty = difficultySelect.value; 
            showPage(mainGame, startGame);
        });
        viewHistoryResultsBtn.addEventListener('click', () => showPage(historyPage, displayHistory));
        backToMenuResultsBtn.addEventListener('click', () => showPage(frontPage));

        showPage(frontPage);
    }

    function showPage(pageToShow, onShowCallback) {
        [frontPage, mainGame, historyPage, resultsPage].forEach(p => {
            if (p) p.classList.add('hidden');
        });
        if (pageToShow) pageToShow.classList.remove('hidden');
        if (onShowCallback) onShowCallback();
    }

    function applyCustomSettings() {
        const customWords = wordList.value.split(',').map(w => w.trim().toUpperCase()).filter(w => w);
        const customColors = colorList.value.split(',').map(c => c.trim()).filter(c => c.startsWith('#'));
        if (customWords.length > 1 && customColors.length > 1 && customWords.length === customColors.length) {
            words = customWords;
            colors = customColors;
            alert('Custom settings applied!');
        } else {
            alert('Please enter a matching number of valid words and hex color codes (at least 2 of each).');
        }
    }

    function deleteHistory() {
        if (confirm("Are you sure you want to delete all history? This cannot be undone.")) {
            localStorage.removeItem('stroopHistory');
            displayHistory();
        }
    }

    function startGame() {
        score = 0; totalAttempts = 0; correctAttempts = 0; gameData = []; timeLeft = 60;
        updateScoreboard();
        nextChallenge();
        clearInterval(timer);
        timer = setInterval(() => {
            timeLeft--;
            updateScoreboard();
            if (timeLeft < 0) endGame();
        }, 1000);
    }

    function setupButtons() {
        colorButtonsContainer.innerHTML = ''; // Clear old buttons
        let activeColors = [...colors].sort(() => 0.5 - Math.random());
        let btnCount = Math.min(5, activeColors.length);
        
        for(let i=0; i < btnCount; i++) {
            const colorValue = activeColors[i];
            const button = document.createElement('button');
            const wordIndex = colors.indexOf(colorValue);
            if (wordIndex !== -1) {
                button.textContent = words[wordIndex];
            }
            button.style.backgroundColor = colorValue;
            button.onclick = () => onColorChosen(colorValue);
            button.style.color = getTextColorForBackground(colorValue);
            colorButtonsContainer.appendChild(button);
        }
    }

    function nextChallenge() {
        if (difficulty === 'Hard' || totalAttempts === 0) setupButtons();
        const textIndex = Math.floor(Math.random() * words.length);
        const wordColor = colors[textIndex % colors.length];
        let inkColor;
        switch (difficulty) {
            case 'Easy': inkColor = wordColor; break;
            case 'Hard': inkColor = colors.filter(c => c !== wordColor)[Math.floor(Math.random() * (colors.length - 1))]; break;
            default: inkColor = Math.random() < 0.5 ? wordColor : colors.filter(c => c !== wordColor)[Math.floor(Math.random() * (colors.length - 1))]; break;
        }
        wordTv.textContent = words[textIndex];
        wordTv.style.color = inkColor;
        currentWordInkValue = inkColor;
        challengeTime = Date.now();
    }

    function onColorChosen(chosenColor) {
        const reaction = Date.now() - challengeTime;
        const isCorrect = chosenColor === currentWordInkValue;
        gameData.push({ time: 60 - timeLeft, reaction, isCorrect });
        totalAttempts++;
        if (isCorrect) { score++; correctAttempts++; }
        // NO TIME PENALTY
        updateScoreboard();
        nextChallenge();
    }

    function getTextColorForBackground(hexColor) {
        const r = parseInt(hexColor.substr(1, 2), 16);
        const g = parseInt(hexColor.substr(3, 2), 16);
        const b = parseInt(hexColor.substr(5, 2), 16);
        const luminance = (0.299 * r + 0.587 * g + 0.114 * b);
        return luminance > 186 ? '#333' : 'white';
    }

    function updateScoreboard() {
        timeTv.textContent = timeLeft < 0 ? 0 : timeLeft;
        scoreTv.textContent = score;
        accuracyTv.textContent = `${totalAttempts > 0 ? Math.round((correctAttempts / totalAttempts) * 100) : 0}%`;
    }

    function endGame() {
        clearInterval(timer);
        const accuracy = (correctAttempts / totalAttempts) * 100 || 0;
        const avgReaction = gameData.reduce((sum, d) => sum + d.reaction, 0) / gameData.length || 0;
        const questionRate = totalAttempts / 60;
        const performanceIndex = (correctAttempts / 60) * (accuracy / 100) * 10;
        displayResults(performanceIndex, score, questionRate, accuracy, avgReaction, totalAttempts, correctAttempts);
        saveGameResult({ difficulty, score, accuracy, avgReaction });
        showPage(resultsPage);
    }

    function displayResults(pIndex, tScore, qRate, acc, avgR, tQuestions, cAnswers) {
        const stats = {
            'Performance Index': pIndex.toFixed(1),
            'Total Score': tScore,
            'Question Rate': `${qRate.toFixed(1)}/s`,
            'Accuracy Rate': `${acc.toFixed(1)}%`,
            'Avg Response Time': `${(avgR / 1000).toFixed(3)}s`,
            'Total Questions': tQuestions,
            'Correct Answers': cAnswers,
            'Wrong Answers': tQuestions - cAnswers
        };
        resultsGrid.innerHTML = Object.entries(stats).map(([label, value]) => 
            `<div class="stat-card"><div>${label}</div><div>${value}</div></div>`
        ).join('');
        createCharts();
    }

    function createCharts() {
        let qualityScore = 0;
        const qualityData = gameData.map(d => ({ x: d.time, y: (qualityScore += d.isCorrect ? 1 : -1) }));
        const speedData = gameData.map(d => ({ x: d.time, y: d.reaction }));
        
        const chartOptions = { scales: { x: { title: { display: true, text: 'Test Time (s)' } } } };

        if (qualityChart) qualityChart.destroy();
        qualityChart = new Chart(document.getElementById('qualityChart').getContext('2d'), {
            type: 'line', data: { datasets: [{ label: 'Quality Score', data: qualityData, borderColor: '#4CAF50', tension: 0.1 }] }, options: chartOptions
        });

        if (speedChart) speedChart.destroy();
        speedChart = new Chart(document.getElementById('speedChart').getContext('2d'), {
            type: 'line', data: { datasets: [{ label: 'Response Time (ms)', data: speedData, borderColor: '#3F51B5' }] }, options: chartOptions
        });
    }

    function saveGameResult(result) {
        const history = JSON.parse(localStorage.getItem('stroopHistory')) || [];
        history.unshift({ ...result, timestamp: new Date().toISOString() });
        localStorage.setItem('stroopHistory', JSON.stringify(history.slice(0, 50)));
    }

    function displayHistory() {
        const history = JSON.parse(localStorage.getItem('stroopHistory')) || [];
        historyList.innerHTML = history.length ? history.map(r => `<div class="history-item"><p><strong>${r.difficulty}</strong> - Score: ${r.score}, Accuracy: ${r.accuracy.toFixed(1)}%</p><small>${new Date(r.timestamp).toLocaleString()}</small></div>`).join('') : '<p>No history yet.</p>';
    }

    init();
});
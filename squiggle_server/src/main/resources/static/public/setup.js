const canvas = document.getElementById('drawing-board');
const toolbar = document.getElementById('toolbar');
const fillButton = document.getElementById("fill");
const artistButton = document.getElementById("artist");
const clearButton = document.getElementById("clear");
const startNewRoomButton = document.getElementById("startNewGame");
const strokeColorInput = document.getElementById('stroke');
const guessWordInput = document.getElementById('guessWord');
const playButton = document.getElementById('playGame');
const letterBoxes = document.getElementById("letter-boxes");
const playerIdInput = document.getElementById('playerText');
const gameLinkInput = document.getElementById('gameLink');
const ctx = canvas.getContext('2d');
const winnerImg = new Image();
winnerImg.src = 'images/winner.png';
const loserImg = new Image();
loserImg.src = 'images/loser.png';

const canvasOffsetX = canvas.offsetLeft;
const canvasOffsetY = canvas.offsetTop;

canvas.width = window.innerWidth - canvasOffsetX;
canvas.height = window.innerHeight - canvasOffsetY;

let isPainting = false;
let isFilling = false;
let lineWidth = 5;
let isArtist = false;
let playerId = null;
let guessWord = null;
let gameId = null;
let startX;
let startY;


window.onload = function () {
    canvas.style.display = 'none';
    const params = new URLSearchParams(window.location.search);
    const gameIdFromUrl = params.get('gameId');
    if (gameIdFromUrl) {
        gameId = gameIdFromUrl;
        console.log('Game ID from URL:', gameId);
    }
}

toolbar.addEventListener('click', e => {
    if (e.target.id === 'startNewGame') {
        setUpRoom();
    }

    if (e.target.id === 'playGame') {
        startGame();
    }

    if (e.target.id === 'clear') {
        clearDrawing();
    }

    if (e.target.id === 'fill') {
        isFilling = !isFilling;
        updateFillBtn();
        console.log('Canvas cleared');
    }

    if (e.target.id === 'artist') {
        isArtist = !isArtist;
        updateArtistBtn();
        console.log('Canvas cleared');
    }
});


toolbar.addEventListener('change', e => {
    if (e.target.id === 'stroke') {
        ctx.strokeStyle = e.target.value;
        console.log('Stroke color changed to:', e.target.value);
    }

    if (e.target.id === 'lineWidth') {
        lineWidth = e.target.value;
        console.log('Line width changed to:', lineWidth);
    }

    if (e.target.id === 'playerId') {
        playerId = e.target.value;
        console.log('Player Id changed to:', playerId);
    }

    if (e.target.id === 'guessWord') {
        guessWord = e.target.value;
        sendMessage(JSON.stringify({action: 'not-artist', playerId: playerId, guessWord: guessWord, gameId: gameId}));
        console.log('Guess word is:', guessWord);
    }

});

const clearDrawing = () => {
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    console.log('Canvas cleared');
}

const updateFillBtn = () => {
    if (isFilling) {
        fillButton.style.backgroundColor = 'red';
        console.log('Fill mode enabled');
    } else {
        fillButton.style.backgroundColor = 'grey';
        console.log('Fill mode disabled');
    }
}

const updateArtistBtn = () => {
    if (isArtist && guessWord) {
        artistButton.style.backgroundColor = 'orange';
        console.log('Artist mode enabled');
        sendMessage(JSON.stringify({action: 'artist', playerId: playerId, guessWord: guessWord, gameId: gameId}));
    } else {
        artistButton.style.backgroundColor = 'grey';
        console.log('Artist mode disabled');
        guessWord = null;
        guessWordInput.value = '';
        // sendMessage(JSON.stringify({action: 'not-artist', playerId: playerId}));
    }
}

function waitForClick(element = document) {
    return new Promise(resolve => {
        const handler = (event) => {
            element.removeEventListener('mousedown', handler);
            resolve(event);
        };
        element.addEventListener('mousedown', handler);
    });
}

const setUpRoom = () => {
    gameId = crypto.randomUUID();
    gameLinkInput.value = `https://squiggle-dd7ef.web.app?gameId=${encodeURIComponent(gameId)}`;
    console.log('Game ID generated:', gameId);
    sendMessage(JSON.stringify({action: 'newGameRoom', gameId: gameId}));
}

const startGame = () => {
    if (gameId == null) {
        alert('Please set up a game room first');
        return;
    }
    console.log('Game started');
    canvas.style.display = 'block';
    const name = prompt("Welcome! Please enter your name:");
    if (name) {
        playerId = name;
        playerIdInput.textContent = playerId;
        console.log('Player Id set to:', playerId);
        sendMessage(JSON.stringify({action: 'enterRoom', gameId: gameId, playerId: playerId}))
    } else {
        console.warn('Player Id not set');
    }
    disableButtons();
}

const disableButtons = () => {
    startNewRoomButton.disabled = true;
    playButton.disabled = true;
    console.log('Buttons disabled');
}

const enableButtons = () => {
    startNewRoomButton.disabled = false;
    playButton.disabled = false;
    console.log('Buttons enabled');
}

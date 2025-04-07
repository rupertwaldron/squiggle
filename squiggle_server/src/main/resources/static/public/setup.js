const canvas = document.getElementById('drawing-board');
const toolbar = document.getElementById('toolbar');
const fillButton = document.getElementById("fill");
const artistButton = document.getElementById("artist");
const clearButton = document.getElementById("clear");
const strokeColorInput = document.getElementById('stroke');
const guessWordInput = document.getElementById('guessWord');
const letterBoxes = document.getElementById("letter-boxes");
const playerIdInput = document.getElementById('playerText');
const ctx = canvas.getContext('2d');

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
let startX;
let startY;


window.onload = function () {
    const name = prompt("Welcome! Please enter your name:");
    if (name) {
        playerId = name;
        playerIdInput.textContent = playerId;
        console.log('Player Id set to:', playerId);
    } else {
        console.warn('Player Id not set');
    }
}


toolbar.addEventListener('click', e => {
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
        sendMessage(JSON.stringify({action: 'not-artist', playerId: playerId, guessWord: guessWord}));
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
        sendMessage(JSON.stringify({action: 'artist', playerId: playerId, guessWord: guessWord}));
    } else {
        artistButton.style.backgroundColor = 'grey';
        console.log('Artist mode disabled');
        guessWord = null;
        guessWordInput.value = '';
        // sendMessage(JSON.stringify({action: 'not-artist', playerId: playerId}));
    }
}
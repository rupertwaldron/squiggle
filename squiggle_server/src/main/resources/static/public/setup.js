const canvas = document.getElementById('drawing-board');
const toolbar = document.getElementById('toolbar');
const fillButton = document.getElementById("fill");
const artistButton = document.getElementById("artist");
const clearButton = document.getElementById("clear");
const strokeColorInput = document.getElementById('stroke');
const ctx = canvas.getContext('2d');

const canvasOffsetX = canvas.offsetLeft;
const canvasOffsetY = canvas.offsetTop;

canvas.width = window.innerWidth - canvasOffsetX;
canvas.height = window.innerHeight - canvasOffsetY;

let isPainting = false;
let isFilling = false;
let lineWidth = 5;
let isArtist = false;
let startX;
let startY;

toolbar.addEventListener('click', e => {
    if (e.target.id === 'clear') {
        ctx.clearRect(0, 0, canvas.width, canvas.height);
        console.log('Canvas cleared');
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
    if(e.target.id === 'stroke') {
        ctx.strokeStyle = e.target.value;
        console.log('Stroke color changed to:', e.target.value);
    }

    if(e.target.id === 'lineWidth') {
        lineWidth = e.target.value;
        console.log('Line width changed to:', lineWidth);
    }

});

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
    if (isArtist) {
        artistButton.style.backgroundColor = 'orange';
        console.log('Artist mode enabled');
    } else {
        artistButton.style.backgroundColor = 'grey';
        console.log('Artist mode disabled');
    }
}
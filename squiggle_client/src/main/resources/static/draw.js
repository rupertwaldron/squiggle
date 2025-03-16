const canvas = document.getElementById('drawing-board');
const toolbar = document.getElementById('toolbar');
const fillButton = document.getElementById("fill");
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

const draw = (e) => {
    if(!isPainting) {
        return;
    }

    ctx.lineWidth = lineWidth;

    ctx.lineCap = 'round';
    ctx.lineTo(e.clientX - canvasOffsetX, e.clientY);
    console.log('Drawing at:', e.clientX - canvasOffsetX, e.clientY);


    if (isFilling) {
        ctx.fillStyle = ctx.strokeStyle;
        ctx.fill();
        console.log('Canvas filled with color:', ctx.strokeStyle);
    } else {
        ctx.stroke();
    }

    const data = {
        x: e.clientX - canvasOffsetX,
        y: e.clientY - canvasOffsetY,
        lineWidth: lineWidth,
        strokeStyle: ctx.strokeStyle,
        isFilled: isFilling
    };

    sendMessage(JSON.stringify(data));
}

canvas.addEventListener('mousedown', (e) => {
    isPainting = true;
    startX = e.clientX;
    startY = e.clientY;
    console.log('Mouse down at:', startX, startY);
});

canvas.addEventListener('mouseup', e => {
    isPainting = false;
    ctx.stroke();
    ctx.beginPath();
    console.log('Mouse up at:', e.clientX, e.clientY);
});

canvas.addEventListener('mousemove', draw);
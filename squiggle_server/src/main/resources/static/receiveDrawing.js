// const canvas = document.getElementById('drawing-board');
// const toolbar = document.getElementById('toolbar');
// const fillButton = document.getElementById("fill");
// const ctx = canvas.getContext('2d');
//
// const canvasOffsetX = canvas.offsetLeft;
// const canvasOffsetY = canvas.offsetTop;
//
// canvas.width = window.innerWidth - canvasOffsetX;
// canvas.height = window.innerHeight - canvasOffsetY;
//
// let isPainting = false;
// let isFilling = false;
// let lineWidth = 5;
// let startX;
// let startY;


const receiveDraw = (message) => {
    if (isArtist) {
        console.warn('You are the artist, not receiving messages');
        return;
    }

    isFilling = message.isFilled;
    updateFillBtn();

    strokeColorInput.value = message.strokeStyle;




    ctx.lineWidth = message.lineWidth;

    ctx.lineCap = 'round';
    ctx.lineTo(message.x, message.y);
    console.log('Received Drawing at:', message);

    if (message.isFilled) {
        ctx.fillStyle = message.strokeStyle;
        ctx.fill();
        console.log('Canvas filled with color:', message.strokeStyle);
    } else {
        ctx.strokeStyle = message.strokeStyle;
        ctx.stroke();
    }
}
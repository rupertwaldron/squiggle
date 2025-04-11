
const draw = (e) => {
    if(!isPainting || !isArtist) {
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
        action: 'mousemove',
        playerId: playerId,
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
    // ctx.stroke();
    ctx.beginPath();
    console.log('Mouse up at:', e.clientX, e.clientY);
    const data = {
        action: 'mouseup',
        playerId: playerId,
    }
    sendMessage(JSON.stringify(data));
});

canvas.addEventListener('mousemove', draw);

const displayWinner = () => {
    ctx.drawImage(winnerImg, 250, 250, canvas.width / 2, canvas.height / 2);
    console.log('Winner displayed');
}

const displayLoser = () => {
    ctx.drawImage(loserImg, 250, 250, canvas.width / 2, canvas.height / 2);
    console.log('Loser displayed');
}

const writeOnCanvas = (word, color, outline) => {
    // Set text styles
    ctx.font = "48px Arial";
    ctx.fillStyle = color; // text color
    ctx.textAlign = "center";
    ctx.textBaseline = "middle";

    // Draw filled text at center of canvas
    ctx.fillText(word, canvas.width / 2, canvas.height / 2);

    // Optional: outline the text
    ctx.strokeStyle = outline;
    ctx.lineWidth = 2;
    ctx.strokeText(word, canvas.width / 2, canvas.height / 2);
}
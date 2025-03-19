
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
        action: 'mouseup'
    }
    sendMessage(JSON.stringify(data));
});

canvas.addEventListener('mousemove', draw);


const container = document.getElementById("letter-boxes");

const guessBoxes = (word) => {
    for (let i = 0; i < word.length; i++) {
        const box = document.createElement("div");
        box.classList.add("box");
        console.log('Creating box for letter:', word[i]);
        if (word[i] === "*") {
            box.textContent = '';
        } else {
            box.textContent = word[i];
        }
        // keep empty initially
        container.appendChild(box);
    }
}
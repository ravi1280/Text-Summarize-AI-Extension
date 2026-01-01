document.addEventListener("DOMContentLoaded", () => {
    // Your code here
    chrome.storage.local.get(["researchNotes"], (result) => {
        if (result.researchNotes) {
            document.getElementById("notesArea").value = result.researchNotes;
        }

    });

    document.getElementById("summarizeBtn").addEventListener("click", summarizeText);
    document.getElementById("saveNoteBtn").addEventListener("click", saveNotes);

    document.getElementById("refreshBtn").addEventListener("click", () => {
        // document.getElementById("results").value = "";
        showResult("");
        // chrome.storage.local.remove("researchNotes", () => {
        //     alert("Notes cleared!");
        // });
    });

});


async function summarizeText() {
    try {
        const [tab] = await chrome.tabs.query({ active: true, currentWindow: true });
        const [{result}] = await chrome.scripting.executeScript({
            target: { tabId: tab.id },
            function: () => window.getSelection().toString()
        });
        
        if (!result) {
            showResult("No text selected.");
            return;
        }
        const response = await fetch("http://localhost:8080/api/v1/research/process", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"},
                body: JSON.stringify({
                    content: result,operation:"summarize"
                })
        });

        if (!response.ok) {
            throw new Error(`API error: ${response.statusText}`);
        }

        const text = await response.text();
        showResult(text.replace(/\n/g, '</br>'));


    }catch (error) {
        console.error("Error during summarization:", error);
        alert("An error occurred while summarizing the text.");
        showResult("Error: " + error.message);
    }
  
}
async function saveNotes() {
    const notes = document.getElementById('notesArea').value;
   
    chrome.storage.local.set({ 'researchNotes': notes }, () => {
        alert("Notes saved!");
    });

}

function showResult(content) {
     document.getElementById("results").innerHTML = `<div class="result-item"><div class="result-content">${content}</div></div>`;
    
}
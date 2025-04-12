from flask import Flask, request, jsonify
from flask_cors import CORS
from transformers import AutoTokenizer, AutoModelForSequenceClassification
import torch
import torch.nn.functional as F

app = Flask(__name__)
CORS(app)  

# Load PhoBERT
tokenizer = AutoTokenizer.from_pretrained("wonrax/phobert-base-vietnamese-sentiment")
model = AutoModelForSequenceClassification.from_pretrained("wonrax/phobert-base-vietnamese-sentiment")

# Correct label order based on model's output
labels = ['negative', 'positive', 'neutral']

@app.route("/predict", methods=["POST"])
def predict():
    data = request.get_json()
    text = data.get("text", "")

    # Tokenize
    inputs = tokenizer(text, return_tensors="pt", truncation=True, padding=True)

    # Predict
    with torch.no_grad():
        outputs = model(**inputs)
        probs = F.softmax(outputs.logits, dim=1)[0]

    # Format all labels with confidence
    all_confidences = {label: float(f"{probs[i]:.4f}") for i, label in enumerate(labels)}
    predicted_index = torch.argmax(probs).item()

    return jsonify({
        "label": labels[predicted_index],
        "confidence": float(f"{probs[predicted_index]:.4f}"),
        "all": all_confidences
    })

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000)

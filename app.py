from flask import Flask, request, jsonify
from flask_cors import CORS
from transformers import AutoTokenizer, AutoModelForSequenceClassification
import torch
import torch.nn.functional as F

# Initialize Flask app
app = Flask(__name__)
CORS(app)

# Load PhoBERT tokenizer and model
tokenizer = AutoTokenizer.from_pretrained("wonrax/phobert-base-vietnamese-sentiment")
model = AutoModelForSequenceClassification.from_pretrained("wonrax/phobert-base-vietnamese-sentiment")

# Define labels in correct order
labels = ["negative", "positive", "neutral"]

@app.route("/predict", methods=["POST"])
def predict():
    # Get JSON data from request
    data = request.get_json()
    text = data.get("text", "")

    # Tokenize input text
    inputs = tokenizer(
        text,
        return_tensors="pt",
        truncation=True,
        padding=True
    )

    # Perform inference
    with torch.no_grad():
        outputs = model(**inputs)
        probabilities = F.softmax(outputs.logits, dim=1)[0]

    # Extract prediction and confidence
    predicted_index = torch.argmax(probabilities).item()
    predicted_label = labels[predicted_index]
    confidence = float(f"{probabilities[predicted_index]:.4f}")

    return jsonify({
        "label": predicted_label,
        "confidence": confidence
    })

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000)

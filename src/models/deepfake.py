import torch
from PIL import Image
from transformers import AutoModelForImageClassification, AutoImageProcessor

class DeepfakeModel:
    def __init__(self, model_name: str):
        self.processor = AutoImageProcessor.from_pretrained(model_name)
        self.model = AutoModelForImageClassification.from_pretrained(model_name)
        self.model.eval()

    def analyze_image(self, image: Image.Image) -> dict:
        """
        Processes an image and returns a dictionary with the prediction and score.
        """
        inputs = self.processor(images=image, return_tensors="pt")
        with torch.no_grad():
            outputs = self.model(**inputs)
        
        logits = outputs.logits
        probabilities = torch.nn.functional.softmax(logits, dim=1)[0]
        
        # Get the top prediction's index and score
        top_prob, top_idx = torch.max(probabilities, dim=0)
        predicted_label = self.model.config.id2label[top_idx.item()]
        
        return {"prediction": predicted_label, "score": top_prob.item()}

    def analyze_video(self, video_frames: list) -> list:
        """
        Analyzes a list of video frames.
        (Note: Frame extraction logic must be handled before calling this)
        """
        results = []
        for frame in video_frames:
            result = self.analyze_image(frame)
            results.append(result)
        # For simplicity, we can return the result of the most confident frame
        # or an average. Here we return all results.
        return results
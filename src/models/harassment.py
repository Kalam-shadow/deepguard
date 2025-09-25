from typing import List, Dict
from transformers import pipeline

class HarassmentDetector:
    def __init__(self):
        self.model = pipeline("text-classification", model="unitary/toxic-bert")

    def analyze_text(self, text: str) -> Dict[str, float]:
        results = self.model(text)
        return {result['label']: result['score'] for result in results}

    def detect_harassment(self, texts: List[str]) -> List[Dict[str, float]]:
        return [self.analyze_text(text) for text in texts]
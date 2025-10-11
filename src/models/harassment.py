from typing import List, Dict
from transformers import pipeline

class HarassmentDetector:
    def __init__(self):
        # Use keyword-based detection as primary method (more reliable)
        self.model = None
        self.model_type = "keyword"
        print("🛡️ Using keyword-based harassment detection (most reliable method)")
        
        # Optional: Try to load a simple sentiment model as backup
        try:
            # Use a simple, reliable sentiment analysis model
            self.model = pipeline("sentiment-analysis", 
                                model="distilbert-base-uncased-finetuned-sst-2-english",
                                device=-1)
            self.model_type = "sentiment"
            print("✅ Sentiment analysis model loaded as backup")
        except Exception as e:
            print(f"⚠️ AI model not available, using keyword detection only: {e}")
            self.model = None

    def analyze_text(self, text: str) -> Dict[str, float]:
        """Analyze a single text for harassment"""
        # Primary method: Use keyword-based detection (most reliable)
        keyword_check = self._simple_harassment_check(text)
        keyword_toxic = keyword_check.get('TOXIC', 0.0)
        
        # Secondary method: Try AI model if available
        ai_toxic = 0.0
        ai_label = "UNKNOWN"
        
        if self.model and self.model_type == "sentiment":
            try:
                results = self.model(text)
                if isinstance(results, list) and len(results) > 0:
                    result = results[0]
                    label = result['label'].upper()
                    score = result['score']
                    ai_label = label
                    
                    # For sentiment models, strong negative sentiment might indicate harassment
                    if label == "NEGATIVE" and score > 0.8:
                        ai_toxic = score * 0.6  # Scale down AI confidence
            except Exception as e:
                print(f"⚠️ AI analysis failed: {e}")
        
        # Combine results - prioritize keyword detection
        final_toxic_score = max(keyword_toxic, ai_toxic)
        
        return {
            'TOXIC': final_toxic_score,
            'SAFE': 1.0 - final_toxic_score,
            'method': 'hybrid' if ai_toxic > 0 else keyword_check.get('method', 'keyword_based'),
            'keyword_score': keyword_toxic,
            'ai_score': ai_toxic,
            'ai_label': ai_label,
            'matches': keyword_check.get('matches', 0),
            'found_keywords': keyword_check.get('found_keywords', [])
        }

    def _simple_harassment_check(self, text: str) -> Dict[str, float]:
        """Enhanced keyword-based harassment detection"""
        
        # High severity threats (immediate danger)
        high_severity_keywords = [
            'kill', 'murder', 'slaughter', 'assassinate', 'eliminate', 'execute',
            'destroy', 'annihilate', 'harm', 'hurt', 'attack', 'assault', 'beat',
            'violence', 'violent', 'threat', 'threaten', 'revenge', 'payback'
        ]
        
        # Medium severity harassment  
        medium_severity_keywords = [
            'hate', 'despise', 'loathe', 'disgust', 'sick', 'pathetic', 'worthless',
            'useless', 'failure', 'reject', 'trash', 'garbage', 'waste', 'scum'
        ]
        
        # Low severity offensive language
        low_severity_keywords = [
            'stupid', 'idiot', 'moron', 'dumb', 'fool', 'loser', 'freak', 'weirdo',
            'ugly', 'fat', 'gross', 'disgusting', 'annoying', 'irritating'
        ]
        
        # Profanity (context-dependent)
        profanity_keywords = [
            'fuck', 'shit', 'bitch', 'ass', 'damn', 'hell', 'bastard', 'crap'
        ]
        
        text_lower = text.lower().strip()
        high_matches = []
        medium_matches = []
        low_matches = []
        profanity_matches = []
        
        import re
        
        # Check each category
        for keyword in high_severity_keywords:
            pattern = r'\b' + re.escape(keyword) + r'\b'
            if re.search(pattern, text_lower):
                high_matches.append(keyword)
                
        for keyword in medium_severity_keywords:
            pattern = r'\b' + re.escape(keyword) + r'\b'
            if re.search(pattern, text_lower):
                medium_matches.append(keyword)
                
        for keyword in low_severity_keywords:
            pattern = r'\b' + re.escape(keyword) + r'\b'
            if re.search(pattern, text_lower):
                low_matches.append(keyword)
                
        for keyword in profanity_keywords:
            pattern = r'\b' + re.escape(keyword) + r'\b'
            if re.search(pattern, text_lower):
                profanity_matches.append(keyword)
        
        # Calculate threat score based on severity
        toxic_score = 0.0
        all_matches = []
        
        if high_matches:
            toxic_score = 0.85 + len(high_matches) * 0.05  # 85-95% for high threats
            all_matches.extend(high_matches)
        elif medium_matches:
            toxic_score = 0.65 + len(medium_matches) * 0.05  # 65-80% for medium threats  
            all_matches.extend(medium_matches)
        elif low_matches:
            toxic_score = 0.35 + len(low_matches) * 0.05   # 35-55% for low threats
            all_matches.extend(low_matches)
        elif profanity_matches:
            toxic_score = 0.25 + len(profanity_matches) * 0.03  # 25-40% for profanity only
            all_matches.extend(profanity_matches)
            
        # Cap the maximum score
        toxic_score = min(0.95, toxic_score)
        
        if all_matches:
            return {
                'TOXIC': toxic_score,
                'SAFE': 1.0 - toxic_score,
                'method': 'keyword_based',
                'matches': len(all_matches),
                'found_keywords': all_matches
            }
        else:
            return {
                'TOXIC': 0.0, 
                'SAFE': 1.0, 
                'method': 'keyword_based',
                'matches': 0,
                'found_keywords': []
            }

    def detect_harassment(self, texts: List[str]) -> List[Dict[str, float]]:
        """Detect harassment in a list of texts"""
        return [self.analyze_text(text) for text in texts]
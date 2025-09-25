import pytest
from src.models.deepfake import DeepfakeModel
from src.models.harassment import HarassmentModel

@pytest.fixture
def deepfake_model():
    model = DeepfakeModel()
    return model

@pytest.fixture
def harassment_model():
    model = HarassmentModel()
    return model

def test_deepfake_detection(deepfake_model):
    # Test with a sample image
    result = deepfake_model.analyze_image("path/to/sample_image.jpg")
    assert isinstance(result, dict)
    assert "deepfake_score" in result

def test_harassment_detection(harassment_model):
    # Test with a sample text
    result = harassment_model.analyze_text("This is a sample text for harassment detection.")
    assert isinstance(result, dict)
    assert "harassment_score" in result
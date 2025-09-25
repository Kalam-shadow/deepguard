Sure, here's the contents for the file `/deepguard/tests/conftest.py`:

import pytest

@pytest.fixture(scope="session")
def sample_data():
    return {
        "image": "path/to/sample/image.jpg",
        "video": "path/to/sample/video.mp4",
        "text": "Sample text for harassment detection."
    }
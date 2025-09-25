# AI-Powered Deepfake and Cyber Harassment Detection System

## Overview
The AI-Powered Deepfake and Cyber Harassment Detection System is designed to leverage artificial intelligence and machine learning techniques to detect deepfake content and instances of cyber harassment in real-time. The system provides features for alerting users and offering explainable AI feedback to enhance user understanding and trust.

## Features
- **Deepfake Detection**: Utilizes advanced models to analyze images and videos for signs of manipulation.
- **Cyber Harassment Detection**: Analyzes text and visual content to identify potential harassment.
- **Real-time Alerts**: Notifies users and moderators of flagged content for immediate action.
- **Explainable AI**: Provides insights into the detection process to help users understand the rationale behind alerts.

## Project Structure
```
deepguard
├── src
│   ├── api
│   ├── core
│   ├── models
│   ├── services
│   ├── utils
│   └── main.py
├── tests
├── .env
├── .gitignore
├── docker-compose.yml
├── Dockerfile
├── pyproject.toml
└── requirements.txt
```

## Installation
1. Clone the repository:
   ```
   git clone <repository-url>
   cd deepguard
   ```

2. Create a virtual environment and activate it:
   ```
   python -m venv venv
   source venv/bin/activate  # On Windows use `venv\Scripts\activate`
   ```

3. Install the required packages:
   ```
   pip install -r requirements.txt
   ```

4. Set up environment variables in the `.env` file.

## Usage
To run the application, execute the following command:
```
uvicorn src.main:app --reload
```

## Testing
To run the tests, use:
```
pytest
```

## License
This project is licensed under the MIT License. See the LICENSE file for details.
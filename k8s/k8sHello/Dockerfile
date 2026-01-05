FROM python:3.11-slim

# Set environment variables to prevent Python from writing .pyc files and buffering stdout/stderr
ENV PYTHONDONTWRITEBYTECODE 1
ENV PYTHONUNBUFFERED 1

# Set working directory inside the container
WORKDIR /app

# Copy only requirements first to leverage Docker cache
#COPY requirements.txt /app/

# Install dependencies
# RUN pip install --upgrade pip && pip install -r requirements.txt

# Copy the rest of the app source code
COPY . /app/

# Expose port if your app serves HTTP (adjust as necessary)
EXPOSE 8080

# Command to run your app
CMD ["python", "helloService.py"]
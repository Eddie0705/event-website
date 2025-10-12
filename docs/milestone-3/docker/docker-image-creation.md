These instructions for creating off of my personal local device. Havent finished creating an AWS account to host the db. But it still takes parameters for db endpoint. It is deployable ready but some changes will need to be made before then.

## Build and Run

### 1. Load the Docker Image

```bash
docker load -i eventhub-webapp.tar
```

### 2. Run the Container

```bash
docker run -d \
  --platform linux/amd64 \
  -p 8080:8080 \
  --add-host=host.docker.internal:host-gateway \
  --name eventhub-app \
  eventhub-webapp
```

### 3. Access the Application

Open: **http://localhost:8080**

# RMIT COSC2299 SEPT Major Project

# Group Information

## Group-P03-03

## Members

- Harry Marshall 1 (s4015845)
- Luca Grosso 2 (s4093817)
- Khanh Nguyen 3 (s4090097)
- Athrinandana Vase 4 (s4007366)
- Halbert Setiawan 5 (s3962208)
- Yong Tian Goh 6 (s4025884)

## Records

- Github repository: https://github.com/cosc2299-2025/team-project-group-p03-03
- Github Project Board : [https://github.com/orgs/cosc2299-2025/projects/41/views/1?layout=board](https://github.com/orgs/cosc2299-2025/projects/79/views/1)
- Microsoft Teams : https://teams.microsoft.com/l/team/19%3Ahe4l5WjrlcjOpNBSncuKidbNAXV_b8wtXgNDEVkHpd01%40thread.tacv2/conversations?groupId=a5f38729-13ae-4ccc-be11-ee6d903d7d40&tenantId=d1323671-cdbe-4417-b4d4-bdb24b51316b

See [Instructions](INSTRUCTIONS.md)

## Build and Run

### 1. Build the Docker Image

```bash
docker build --platform linux/amd64 -t eventhub-webapp .
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

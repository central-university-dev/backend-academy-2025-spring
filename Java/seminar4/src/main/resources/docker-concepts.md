graph LR
A[Dockerfile] -->|Built from| B[Docker Image]
B -->|Stored in| C[Image Registry]
C -->|Pulled to create| D[Container]
D -->|Runs as| E[Running Application]

    subgraph "Docker Environment"
        A
        B
        D
        E
    end

    subgraph "Storage"
        C
    end

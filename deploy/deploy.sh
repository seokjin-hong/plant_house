#!/bin/bash
IMAGE="devhong/plant-house"
TAG="${1:-latest}"

# 현재 활성 컨테이너 확인
BLUE_RUNNING=$(docker ps --filter "name=blue" --format "{{.Names}}")
GREEN_RUNNING=$(docker ps --filter "name=green" --format "{{.Names}}")

# 새 이미지 Pull
docker pull ${IMAGE}:${TAG}

if [ -n "$BLUE_RUNNING" ]; then
    # Blue가 실행 중 → Green에 배포
    echo "=== Blue is running. Deploying to Green (8081)... ==="
    docker stop green || true
    docker rm green || true
    docker run -d --name green --env-file ~/.env.docker -p 8081:8080 ${IMAGE}:${TAG}

    # 헬스 체크 (최대 30초 대기)
    for i in {1..10}; do
        if curl -s http://localhost:8081 > /dev/null 2>&1; then
            echo "Green is healthy!"

            # Nginx를 Green으로 전환
            sudo sed -i 's/localhost:8080/localhost:8081/' /etc/nginx/conf.d/springboot.conf
            sudo nginx -s reload
            echo "Traffic switched to Green"

            # 기존 요청 완료 대기
            echo "Waiting for in-flight requests to complete..."
            sleep 10

            # Blue graceful shutdown (최대 30초 대기)
            docker stop --time=30 blue || true
            docker rm blue || true
            echo "=== Deploy complete! Green is now active. ==="
            exit 0
        fi
        echo "Waiting for Green... ($i/10)"
        sleep 3
    done

    # 헬스 체크 실패 → 롤백
    echo "=== Green health check failed! Rolling back... ==="
    docker stop green || true
    docker rm green || true
    echo "Rollback complete. Blue is still running."
    exit 1

else
    # Green이 실행 중이거나 둘 다 없음 → Blue에 배포
    echo "=== Deploying to Blue (8080)... ==="
    docker stop blue || true
    docker rm blue || true
    docker run -d --name blue --env-file ~/.env.docker -p 8080:8080 ${IMAGE}:${TAG}

    # 헬스 체크 (최대 30초 대기)
    for i in {1..10}; do
        if curl -s http://localhost:8080 > /dev/null 2>&1; then
            echo "Blue is healthy!"

            # Nginx를 Blue로 전환
            sudo sed -i 's/localhost:8081/localhost:8080/' /etc/nginx/conf.d/springboot.conf
            sudo nginx -s reload
            echo "Traffic switched to Blue"

            # 기존 요청 완료 대기
            echo "Waiting for in-flight requests to complete..."
            sleep 10

            # Green graceful shutdown (최대 30초 대기)
            docker stop --time=30 green || true
            docker rm green || true
            echo "=== Deploy complete! Blue is now active. ==="
            exit 0
        fi
        echo "Waiting for Blue... ($i/10)"
        sleep 3
    done

    # 헬스 체크 실패 → 롤백
    echo "=== Blue health check failed! Rolling back... ==="
    docker stop blue || true
    docker rm blue || true
    echo "Rollback complete. Green is still running."
    exit 1
fi
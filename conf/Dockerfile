# 1. Fluentd 공식 이미지 기반
FROM fluent/fluentd:v1.17.0-debian-1.0

# 2. root 권한으로 변경 (플러그인 설치를 위해 필요)
USER root

RUN apt-get update && apt-get install -y gcc make

# 3. 필요한 Fluentd 플러그인 설치
RUN gem install fluent-plugin-kafka fluent-plugin-record-modifier \
    && gem install fluent-plugin-parser

# 4. Fluentd 설정 파일 복사
COPY /conf/fluent.conf /fluentd/etc/fluent.conf

# 5. Fluentd 실행을 위한 권한 설정
RUN chown fluent /fluentd/etc/fluent.conf

# 6. 기본 실행 명령어 설정
USER fluent
CMD ["fluentd", "-c", "/fluentd/etc/fluent.conf"]

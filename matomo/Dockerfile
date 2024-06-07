FROM fluent/fluentd:v1.17.0-debian-1.0

USER root

# 필요한 플러그인 설치
RUN gem install fluent-plugin-kafka
RUN gem install fluent-plugin-record-modifier

USER fluent

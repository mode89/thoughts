FROM clojure:temurin-18-lein-alpine

RUN apk --update add \
        g++ \
        musl-dev \
        nodejs \
        npm \
        python3 \
        python3-dev \
        py3-pip
RUN pip install \
        flask \
        flask-cors \
        flask_sqlalchemy \
        pyjwt

ARG HOST_GID
ARG HOST_UID
RUN addgroup -g ${HOST_GID:?} user && \
    adduser -u ${HOST_UID:?} -G user -D user

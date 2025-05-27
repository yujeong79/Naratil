FROM bitnami/spark:latest

USER root
RUN apt-get update && apt-get install -y python3-pip
COPY requirements.txt /app/
RUN pip3 install --upgrade pip && pip3 install -r /app/requirements.txt

COPY entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh

ENTRYPOINT ["/app/entrypoint.sh"]

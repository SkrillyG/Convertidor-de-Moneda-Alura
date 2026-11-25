#!/bin/zsh
if [[ -z "$EXCHANGE_RATE_API_KEY" ]]; then
  echo "[✗] Falta EXCHANGE_RATE_API_KEY"
  exit 1
fi
JAR="target/convertidor-monedas-1.0.0.jar"
if [[ ! -f "$JAR" ]]; then
  echo "[!] Ejecuta mvn package primero"
  exit 1
fi
java -jar "$JAR"

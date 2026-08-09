#!/bin/bash
# Cria um banco separado para cada microsserviço (padrão database-per-service),
# rodando dentro do container do Postgres na primeira inicialização.
set -e

if [ -n "$MULTIPLE_DATABASES" ]; then
  echo "Criando múltiplos bancos: $MULTIPLE_DATABASES"
  for db in $(echo "$MULTIPLE_DATABASES" | tr ',' ' '); do
    psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
      CREATE DATABASE $db;
      GRANT ALL PRIVILEGES ON DATABASE $db TO $POSTGRES_USER;
EOSQL
  done
fi

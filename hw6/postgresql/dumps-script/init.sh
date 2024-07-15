#!/bin/bash
set -e
createdb -U postgres archive

psql -U postgres archive < ./tmp/pg/archive.sql
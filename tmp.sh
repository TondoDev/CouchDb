## cor running ad hoc commands

HOST=127.0.0.1:5984
DBNAME=music
INSTANCE=$HOST/$DBNAME
USER=tondodev
PWD=tondodev
LOGGED=$USER:$PWD

echo Working with database $INSTANCE

curl -s -u $LOGGED \
  -H 'Content-type: application/json' \
  -X POST $INSTANCE/cffab83e4e72c0a5222bf3ad330004c9/ \
  -d '{"name" : "pipik"}'

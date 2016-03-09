# Drop DB, if not exists som error is returned
HOST=127.0.0.1:5984
DBNAME=music
INSTANCE=$HOST/$DBNAME
USER=tondodev
PWD=tondodev
LOGGED=$USER:$PWD

echo Working with database $INSTANCE

echo  List database instances
curl -s -u $LOGGED -X GET $HOST/_all_dbs
echo

echo  "Drop database, if doesn't exists error is returned but it is OK, we want to create new"
curl -s -u $LOGGED  -X DELETE $INSTANCE
echo

echo "Create new Database with name music"
curl -s -u $LOGGED -X PUT $INSTANCE
echo

echo "Get created database info"
curl -s $LOGGED -X GET $INSTANCE
echo

echo "Crete some albums in music database"
curl -s -u $LOGGED \
  -H 'Content-type: application/json' \
  -X POST $INSTANCE \
  -d '
  {
    "title" : "Colorado",
    "author" : "Kabat",
    "relased" : "1994-09-07",
    "numOfTracks" : 12,
    "songs" : [
      {
        "title" : "Colorado",
        "length" : 140,
        "purchased" : "2016-02-20",
        "favorite" : true
      }, {
          "title" : "Starej bar",
          "length" : 246,
          "purchased" : "2014-01-20",
          "favorite" : true
      }
    ]
  }'

curl -s -u $LOGGED \
  -H 'Content-type: application/json' \
  -X POST $INSTANCE \
  -d '
  {
    "title" : "Chocolate Starfish and the Hot Dog Flavored Water",
    "author" : "Limp Bizkit",
    "relased" : "2000-10-17",
    "numOfTracks" : 14,
    "songs" : [
      {
        "title" : "My Generation",
        "length" : 221,
        "purchased" : "2016-01-24",
        "favorite" : true
      }, {
          "title" : "Livin'\'' It Up",
          "length" : 264,
          "purchased" : "2014-01-20",
          "favorite" : false
      }, {
          "title" : "Take a Look Arond",
          "length" : 306,
          "purchased" : "2016-01-20",
          "favorite" : true
      }
    ]
  }'

echo "List of all documents"
curl -s -u $LOGGED \
  -H 'Accept: application/json' \
  -X GET $INSTANCE/_all_docs


echo "Creating wiew with map function with name - PUT is important here"
curl -s -u $LOGGED \
  -H 'Content-type: application/json' \
  -X PUT $INSTANCE/_design/songs \
  -d '
  {
    "language": "javascript",
    "views": {
      "tracks": {
        "map": "function(doc) { if(doc.title && doc.songs) {emit(doc.title, doc.songs.length)}}"
      }
    }
  }
'
echo "Calling view - shoud display count of purchased songs in albums"
curl -s -u $LOGGED \
  -H 'Content-type: application/json' \
  -X GET $INSTANCE/_design/songs/_view/tracks

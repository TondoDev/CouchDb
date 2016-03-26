# init login information
DBNAME=cities
source loginInfo.txt


echo Working with database $INSTANCE

echo  List database instances
curl -s -u $LOGGED -X GET $HOST/_all_dbs
echo

echo  "Drop database, if doesn't exists error is returned but it is OK, we want to create new"
curl -s -u $LOGGED  -X DELETE $INSTANCE
echo

echo "Create new Database with name $DBNAME"
curl -s -u $LOGGED -X PUT $INSTANCE
echo

# notice _bull_docs and "docs" root property
# every item of array is inserted as separated document
echo "Insertin simple data for easey map-reduce examples"
curl -s -u $LOGGED -X POST $INSTANCE/_bulk_docs \
    -H 'Content-type: application/json' \
    -d '{
      "docs" :  [
        {
          "city" : "Paris",
          "sales" : 13000,
          "name" : "James"
        },
        {
          "city" : "Tokyo",
          "sales" : 20000,
          "name" : "James"
        },
        {
          "city" : "Paris",
          "sales" : 5000,
          "name" : "James"
        },
        {
          "city" : "Paris",
          "sales" : 22000,
          "name" : "John"
        },
        {
          "city" : "London",
          "sales" : 3000,
          "name" : "John"
        },
        {
          "city" : "London",
          "sales" : 7000,
          "name" : "John"
        },
        {
          "city" : "London",
          "sales" : 7000,
          "name" : "Adam"
        },
        {
          "city" : "Paris",
          "sales" : 19000,
          "name" : "Adam"
        },
        {
          "city" : "Tokyo",
          "sales" : 17000,
          "name" : "Adam"
        }
      ]
    }
    '
echo

# == _sum reduce function
echo "Creating wiew with map function with name - PUT is important here"
curl -s -u $LOGGED \
  -H 'Content-type: application/json' \
  -X PUT $INSTANCE/_design/mr \
  -d @mrDesignDoc.txt
echo

echo "Caling view with both map and reduce functions and sum all records"
curl -s -u  $LOGGED \
  -H 'Content-type: application/json' \
  -X GET $INSTANCE/_design/mr/_view/sumByName
echo

echo "Caling view with reduce for sum records by name"
curl -s -u  $LOGGED \
  -H 'Content-type: application/json' \
  -X GET $INSTANCE/_design/mr/_view/sumByName?group_level=1
echo

echo "Caling view with reduce for sum records by name and town"
curl -s -u  $LOGGED \
  -H 'Content-type: application/json' \
  -X GET $INSTANCE/_design/mr/_view/sumByName?group_level=2
echo

#== _count reduce function
echo "Count all records"
curl -s -u  $LOGGED \
  -H 'Content-type: application/json' \
  -X GET $INSTANCE/_design/mr/_view/countByName
echo

#== _count reduce function
echo "Count all records"
curl -s -u  $LOGGED \
  -H 'Content-type: application/json' \
  -X GET $INSTANCE/_design/mr/_view/countByName
echo

echo "Count by name"
curl -s -u  $LOGGED \
  -H 'Content-type: application/json' \
  -X GET $INSTANCE/_design/mr/_view/countByName?group_level=1
echo

echo "Count by name and town"
curl -s -u  $LOGGED \
  -H 'Content-type: application/json' \
  -X GET $INSTANCE/_design/mr/_view/countByName?group_level=2
echo

#== _stats
echo "Statis by all"
curl -s -u  $LOGGED \
  -H 'Content-type: application/json' \
  -X GET $INSTANCE/_design/mr/_view/statsByName?group_level=0
echo

echo "Stats by name"
curl -s -u  $LOGGED \
  -H 'Content-type: application/json' \
  -X GET $INSTANCE/_design/mr/_view/statsByName?group_level=1
echo

echo "Statis by name and town"
curl -s -u  $LOGGED \
  -H 'Content-type: application/json' \
  -X GET $INSTANCE/_design/mr/_view/statsByName?group_level=1
echo

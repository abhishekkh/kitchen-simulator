# CloudKitchen order simulation

## Usage
To run inside an IDE, run the main method in OrderIngestor.java by passing two arguments as shown below.
The order of arguments is important.
```
Arguments: "<INGESTION_RATE>" "/path/to/file/orders.json"
Example: "2" "/tmp/orders.json"
```

To run using the jar
- first unzip the project
- build the project which will build a jar with dependencies
- run from command line

```
mvn clean install
java -jar target/simulator-1.0-SNAPSHOT-jar-with-dependencies.jar 2 /path/to/file/orders.json

```

## Design choices
1. Added a fixed pool of Courier threads to pickup packages to mimic a real world scenario where resources could be constrained.
2. The Shelf is designed as an in memory map with the shelf name as the key{HOT, COLD, FROZEN, OVERFLOW} and the list of orders as the value.
3. When the overflow shelf is full, we start looking at orders from the end of the overflow shelf list which is also the freshest of all orders sitting on the shelf. Reasoning 
behind this is that it will reduce sliding around the orders after the removal and should take constant time.
4. IShelfAccessor defines the interface to access the Shelf.
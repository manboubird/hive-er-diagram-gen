Hive ER Diagram Generator
=========================

Generate a Graphviz's dot formatted file from hives' `desc formatted {TABLE}` files

## Usage

Quick run 

`sbt 'run ./samples/desc ./samples/erMeta.txt ./target/er.dot'`

Run with standalone jar

```
sbt assembly
java -jar ./target/scala-2.10/hive-er-diagram-gen-assembly-0.0.1.jar ./samples/desc ./samples/erMeta.txt ./target/er.dot
```

Convert to PNG from dot file:

`dot -Tpng ./target/er.dot > ./target/er.png & open ./target/er.png`

To generate input files:

`hive -e "show tables" | xargs -I '{}' sh -c 'hive -e "desc formatted $1" > "./target/desc/$1.txt"' -- {}`

## ER Meta Format

Format consists of a header beggining with # and records.
See also /samples/desc.

**Master table relations record format**

`{MASTER_TABLE_NAME}.{MASTER_TABLE_COLUMN_NAME},<{TABLE_NAME}+>`

ex:
```
# master table relations
profile.uid,uid_map
uid_map.sid,act,excite_log
```

**Sub group record format**

`{SUB_GROUP_LABEL},<{TABLE_NAME}+>`

ex:
```
# sub group
Dimension tables,profile
Fact tables,act,excite_log
Bridge tables,uid_map
```

## Dependencies

*  [Graphviz](http://www.graphviz.org/)

## Compatibility (Verification Environment)

*  hive-0.10.0-cdh4.3.0

## License

Apache Software License 2.0.


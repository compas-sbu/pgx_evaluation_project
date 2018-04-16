javac -cp $1/shared-memory/embedded/*:$1/shared-memory/common/*:$1/shared-memory/third-party/* -d classes $2.java
java -cp $1/shared-memory/embedded/*:$1/shared-memory/common/*:$1/shared-memory/third-party/*:classes  oracle.pgx.demos.$2 $3 $4 $5 $6

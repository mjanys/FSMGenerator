Prerequisites:
  - jvhdl in maven repositories 
  - use mvn install in jvhdl project directory
  
Compile:
  mvn compile
  
Compile to jar:
  mvn package

Run:
  mvn exec:java
  
Compile to single jar:
  mvn compile assembly:single
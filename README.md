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

--
Usage of editor:

  States:
    name: raw input
    moore: raw input
    
  Edges: (if() conditions   --> if (CE = '1' and FSM_CE = '1') then .... )
    input conditions:
      1) | CE      | 1         |
         | FSM_CE  | 1         |
         
         --> if (CE = '1' and FSM_CE = '1') then ....
      
      2) | CE      | 1 or      |
         | FSM_CE  | 1 or      |
         
         --> if (CE = '1' or FSM_CE = '1') then .... 
         
      3) | CE      | /= '1' or |
         | FSM_CE  | 1         |
         
         --> if (CE /= '1' or FSM_CE = '1') then .... 
         
      4) | CE      | "not CE = '0'" or |
         | FSM_CE  | "FSM_CE = 'X'"    |
         
         --> if (not CE = '1' or FSM_CE = 'X') then .... 
         
    mealy: raw input

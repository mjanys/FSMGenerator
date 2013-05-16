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
    
  Edges:
    input conditions:
    
      1) if (CE = '1' and FSM_CE = '1') then ....
         
         | CE      | 1         |
         | FSM_CE  | 1         |
         
      
      2) if (CE = '1' or FSM_CE = '1') then .... 
      
         | CE      | 1 or      |
         | FSM_CE  | 1 or      |
         
         
      3) if (CE /= '1' or FSM_CE = '1') then ....
      
         | CE      | /= '1' or |
         | FSM_CE  | 1         | 
         
      4) if (not CE = '1' or FSM_CE = 'X') then ....
      
         | CE      | "not CE = '0'" or |
         | FSM_CE  | "FSM_CE = 'X'"    |
         
         
    mealy: raw input

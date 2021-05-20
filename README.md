## Introduction
Bee is an object-oriented programming language with static type checking. Now the compiler for the Bee compiles code for MIPS. The Bee supports only three primitive types of data (`int`, `bool`, `char`) and one-dimensional arrays. The Bee does not have GC (garbage collector). To see how it works need to use SPIM simulator and run assembly code. This project is still under development. Documentation will be very soon. Check the directory `examples`.

## How to build
1. `git clone https://github.com/lucky-dev/bee.git`
2. `cd bee`
3. `mkdir build`
4. `javac -classpath src src/bee/lang/Main.java -d build`
5. `cd build`
6. `jar cf bee.jar .`
7. `java -cp bee.jar bee.lang.Main -s ../examples/test_1.bee -d ../examples/test_1.s`
8. Run SPIM simulator and load a file `*.s` (e.g. `test_1.s`).

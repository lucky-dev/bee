## Introduction
Bee is an object-oriented programming language with static type checking. Now the compiler for the Bee compiles code for MIPS. The Bee supports only three primitive types of data (`int`, `bool`, `char`) and one-dimensional arrays. The Bee does not have GC (garbage collector). To see how it works need to use SPIM simulator and run assembly code. This project is still under development. Documentation will be very soon. Check the directory `examples`.

## How to build
1. `git clone https://github.com/lucky-dev/bee.git`
2. `cd bee`
3. `./gradlew jar`
4. `java -jar build/libs/Bee-1.0.jar -s examples/test_1.bee -d examples/test_1.s`
8. Run SPIM simulator, load a file `*.s` (e.g. `test_1.s`) and run a program.

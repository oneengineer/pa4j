### assign
Id <- e1
translate to LLVM store

### variable refer
Id
return ref of that value, 
if it is in the environment, then use LLVM load

### constant refer
true, false, 123
return value directly

### string constant refer
create array first 
TODO
return pointer ref

### new
new Type
LLVM alloca a new struct
initialize the attributes in the right order
TODO


### dispatch
translate to call

### let
use LLVM alloca to allocate a new `S[v/l]`


## common rules when translate operational grammar
#### when upload store
S_3 = S_2[v_1/l_1]
write location l_1 with value v_1

use LLVM store

#### if else while

Do NOT use phi node.
difficult with nested situation.
Use variable instead 
At end BB, load return value from address.

while translate to if else

```
while start:
if expr then 
{
    go to while body
}
else go to while start
```
 
# required libs
org.bytedeco:llvm-platform:10.0.0-1.5.3
edu.princeton.cup:java-cup:10k
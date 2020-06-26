llc --march=x86-64 out.ll
clang out.s -o out.out
chmod +x out.out
./out.out
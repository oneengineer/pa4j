; ModuleID = 'llvmfac'
source_filename = "llvmfac"
target datalayout = "e-m:o-i64:64-f80:128-n8:16:32:64-S128"

@global_text = global [13 x i8] c"hello world\0A\00"
@global_text.1 = global [13 x i8] c"hello: %d !\0A\00"

declare i32 @printf(i8*, ...)

define i32 @myfac(i32) {
if_b:
  %a = alloca i32
  %b = alloca i32
  %c = alloca i32
  %steps = alloca i32
  store i32 1, i32* %b
  store i32 1, i32* %a
  %cmp = icmp slt i32 %0, 3
  br i1 %cmp, label %then_b, label %else_b

then_b:                                           ; preds = %if_b
  ret i32 3

else_b:                                           ; preds = %if_b
  %temp = sub i32 %0, 2
  store i32 %temp, i32* %steps
  br label %while_b

while_b:                                          ; preds = %while_content_b, %else_b
  %temp2 = load i32, i32* %steps
  %called = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([13 x i8], [13 x i8]* @global_text.1, i64 0, i64 0), i32 %temp2)
  %cmp1 = icmp sgt i32 %temp2, 0
  br i1 %cmp1, label %while_content_b, label %end_b

while_content_b:                                  ; preds = %while_b
  %tempa = load i32, i32* %a
  %tempb = load i32, i32* %b
  %tempc = add i32 %tempa, %tempb
  %called2 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([13 x i8], [13 x i8]* @global_text.1, i64 0, i64 0), i32 %tempc)
  store i32 %tempc, i32* %b
  store i32 %tempb, i32* %a
  %step2 = sub i32 %temp2, 1
  %called3 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([13 x i8], [13 x i8]* @global_text, i64 0, i64 0))
  store i32 %step2, i32* %steps
  br label %while_b

end_b:                                            ; preds = %while_b
  %c2 = load i32, i32* %b
  ret i32 %c2
}

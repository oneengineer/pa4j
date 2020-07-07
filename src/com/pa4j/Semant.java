package com.pa4j;/*
Copyright (c) 2000 The Regents of the University of California.
All rights reserved.

Permission to use, copy, modify, and distribute this software for any
purpose, without fee, and without written agreement is hereby granted,
provided that the above copyright notice and the following two
paragraphs appear in all copies of this software.

IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR
DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT
OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF
CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
AND FITNESS FOR A PARTICULAR PURPOSE.  THE SOFTWARE PROVIDED HEREUNDER IS
ON AN "AS IS" BASIS, AND THE UNIVERSITY OF CALIFORNIA HAS NO OBLIGATION TO
PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
*/

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import java_cup.runtime.Symbol;

/**
 * Static semantics driver class
 */
class Semant {

    /**
     * Reads AST from from consosle, and outputs the new AST
     */

    public static List<String> readEntireFile(String path){
        try {
            File file = new File(path);
            var reader = new BufferedReader(new FileReader(file));
            var result = reader.lines().collect(Collectors.toList());
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static boolean cmp_file(String correctPath, String genPath){
        var c = readEntireFile(correctPath);
        var g = readEntireFile(genPath);
        var n = c.size();
        var ic = c.iterator();
        var ig = g.iterator();
        var i = 0;
        while (ic.hasNext() && ig.hasNext()){
            var linec = ic.next();
            var lineg = ig.next();
            if (!linec.equals(lineg)){
                System.out.println("At line: " + i + " not equal");
                System.out.println("correct " + linec);
                System.out.println("generat " + lineg);
                return false;
            }
            i += 1;
        }
        if (ic.hasNext() || ig.hasNext()){
            System.out.println("file length doesn't equal");
            return false;
        }
        return true;
    }

    public static boolean test_single_file(String path){
        InputStreamReader reader1 = null; // TODO
        try {
            reader1 = new InputStreamReader(new FileInputStream("test_cases/" + path + ".test.parsed"));
            ASTLexer lexer = new ASTLexer(reader1);
            ASTParser parser = new ASTParser(lexer);
            Object result = null;
            try {
                result = parser.parse().value;
            } catch (Exception e) {
                e.printStackTrace();
            }

            System.out.println(" END " + result);

            boolean failedSemant = false;
            try {
                ((Program) result).semant();
                var writer = new PrintStream(new FileOutputStream("resource/my.assignment.test.parsed"));
                ((Program) result).dump_with_types(writer, 0);
                writer.close();
            } catch (RuntimeException e) {
                failedSemant = true;
                //e.printStackTrace();
            }

            reader1.close();

            // compare files
            var fileCorrect = "test_cases/" + path + ".test.correct";
            var fileGenerate = "resource/my.assignment.test.parsed";
            var f = new File(fileCorrect);
            if (f.length()<1) {
                if (! failedSemant) {
                    System.out.println("Test failed " + path + " it should have type checking error " + failedSemant);
                    //throw new RuntimeException("Test failed " + path + " it should have type checking error");
                }
            }
            else if (failedSemant){
                //throw new RuntimeException("Test failed " + path + " it should NOT have type checking error");
            }
            else if (!cmp_file(fileCorrect,fileGenerate)){
                System.out.println("Error of " + path);
                Files.copy(Path.of(fileCorrect),
                        Path.of("resource/zzz.assignment.test.parsed"),
                        StandardCopyOption.REPLACE_EXISTING);
                Files.copy(Path.of("test_cases/" + path + ".test"),
                        Path.of("resource/zzz.assignment.test"),
                        StandardCopyOption.REPLACE_EXISTING);
                throw new RuntimeException("Test failed " + path);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public static void test_all(){
        var list = new ArrayList<String>();
        list.add("assignment");
        list.add("basic");
        list.add("classes");
        list.add("compare");

        list.add("comparisons");
        //list.add("dupformals");
        list.add("expressionblock");
        list.add("forwardinherits");


        list.add("if");
        list.add("inheritsObject");
//        list.add("inheritsbool");
//        list.add("inheritsselftype");
//        list.add("initwithself");

        list.add("io");
        list.add("isvoid");
        list.add("letinit");
        list.add("letnoinit");
        //list.add("letself");
        //list.add("letselftype");
        list.add("letshadows");
        //list.add("lubtest");
        //list.add("methodcallsitself");
        //list.add("methodnameclash");

        list.add("list.cl");
        list.add("neg");
        list.add("newselftype");

        list.add("objectdispatchabort");
        //list.add("outofscope");
        list.add("overriderenamearg");
        list.add("overridingmethod");
        list.add("overridingmethod2");
        list.add("overridingmethod3");
        //list.add("overridingmethod4");

        list.add("subtypemethodreturn");
        list.add("trickyatdispatch");
        //list.add("trickyatdispatch2");

        list.add("simplecase");
        list.add("staticdispatch");
        list.add("stringtest");


        for (var i: list){
            System.out.println(i);
            test_single_file(i);
        }

    }

    public static void test_everything_all(){
        File file = new File("test_cases");
        var dirs = file.listFiles();
        for (var i:dirs){
            if (i.getName().endsWith(".test")){
                String s = i.getName();
                var name = s.substring(0, s.length() - 5);
                System.out.println("testing " + name);
                test_single_file( name );
            }
        }
    }

    public static void main(String[] args) throws IOException {
        args = Flags.handleFlags(args);
        //test_all();
        //test_everything_all();
        //System.out.println("Test all good");
        //ASTLexer lexer = new ASTLexer(new InputStreamReader(System.in));

        // CHANGED basic types inited here

        //var reader1 = new InputStreamReader(new FileInputStream("resource/if.test.parsed"));
        //var reader1 = new InputStreamReader(new FileInputStream("resource/abort.cl.parsed"));

        var reader1 = new InputStreamReader(new FileInputStream("resource/assignment-val.cl.parsed"));

        //var reader1 = new InputStreamReader( new FileInputStream( "resource\\list.cl.parsed" ));

        ASTLexer lexer = new ASTLexer(reader1);
        ASTParser parser = new ASTParser(lexer);
        Object result = null;
        try {
            result = parser.parse().value;
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println(" END " + result);
        //return ;

        ((Program) result).semant();
        var writer = new PrintStream( new FileOutputStream("resource/my.assignment.test.parsed"));
        ((Program) result).dump_with_types(writer, 0);

        ((Program) result).cgen( ((programc) result).typeEnv);


        // write to STDOUT
        //((Program) result).dump_with_types(System.out, 0);

        // write to File
        //((Program)result).dump(System.out, 0);
    }
}

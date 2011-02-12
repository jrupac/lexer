import java.util.*;
import java.io.*;

/**
 * This program is a lexer for a straight-line program that is the subset of ML.
 *
 * @author Ajay Roopakalu
 */
public class Lexer
{
    /**
     * Perform lexical analysis on the file given by filename
     *
     * @param filename Name of source file to be parsed
     */
    public Lexer(String filename)
    {
        try 
        {
            Scanner in = new Scanner(new File(filename));
            System.out.println("structure TestProg = struct");
            System.out.println("structure S = SLP");
            System.out.println("val prog = ");
            System.out.println(parseStm(in.nextLine().replaceAll(" ", "")));
            System.out.println("end");
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
            System.exit(-3);
        }
    }

    /**
     * Parse the given object as a statement and return the complete sub-tree
     * of this statement.
     *
     * @param o Object being parsed
     * @return Complete sub-tree of statement o
     */
    public static Object parseStm(Object o)
    {
       if (!(o instanceof String))
           return o;

        String s = (String)o;

        if (s.charAt(0) == '(')
            return Lexer.parseExp(s);

        int pos = s.indexOf(";");
        if (pos > 0)
            return new Compound(s.substring(0, pos), s.substring(pos+1));

        pos = s.indexOf(":=");
        if (pos > 0)
            return new Assign(s.substring(0, pos), s.substring(pos+2));

        pos = s.indexOf("print");
        if (pos >= 0)
            return new Print(s.substring(pos+6, s.length()-1).split(",")); 
        else
            return Lexer.parseExp(s);
    }

    /**
     * Parse the given object as a statement and return the complete sub-tree
     * of this expression.
     *
     * @param o Object being parsed
     * @return Complete sub-tree of expression o
     */
    public static Object parseExp(Object o)
    {
       if (!(o instanceof String))
           return o;

        String s = (String)o;
        int pos;

        if (s.charAt(0) == '(')
        {
            pos = getFirstComma(s);

            if (pos < 0)
            {
                System.err.println("ERROR: Ill-formed EseqExp expression");
                System.err.println("\t" + s);
                System.exit(-1);
                return null;
            }

            return new EseqExp(s.substring(1, pos), 
                               s.substring(pos+1, s.length()-1));
        }

        pos = findFirstOp(s);
        if (pos > 0)
            return new OpExp(s.substring(0, pos), 
                             s.substring(pos, pos+1), 
                             s.substring(pos+1));
       
       if (s.matches("[a-zA-Z][\\w]*"))
           return new IdExp(s);
        
        if (s.matches("\\d+"))
            return new NumExp(s);
        
        // Control should never reach here
        System.err.println("ERROR: Ill-formed expression");
        System.err.println("\t" + s);
        System.exit(-1);
        return null;
    }

    /**
     * Return location of the end of the first argument.
     *
     * @param s String being parsed
     * @return Location of the end of the first argument, 0 if not found
     */
    private static int getFirstComma(String s)
    {
        int len = s.length();
        int parenStack = 0;

        for (int i = 1; i < len; i++)
        {
            switch (s.charAt(i))
            {
                case '(': parenStack++;
                          break;
                case ')': parenStack--;
                          break;
                case ',': if (parenStack == 0)
                            return i;
            }
        }

        return 0;
    }

    /**
     * Return the location of the first binary operation when parsed left-to-right.
     *
     * @param str String being parsed
     * @return Location of first binary operation, -1 if not found
     */
    private static int findFirstOp(String str)
    {
        int a = str.indexOf("+");
        int s = str.indexOf("-");
        int m = str.indexOf("*");
        int d = str.indexOf("/");

        int min = Integer.MAX_VALUE;

        if (a > 0 && a < min)
            min = a;
        if (s > 0 && s < min)
            min = s;
        if (m > 0 && m < min)
            min = m;
        if (d > 0 && d < min)
            min = d;

        return (min == Integer.MAX_VALUE) ? -1 : min;
    }

    /**
     * Run the lexer on the file given by the first argument.
     */
    public static void main(String args[])
    {
        if (args.length != 1)
        {
            System.out.println("Usage: java Lexer filename");
            System.exit(-2);
        }

        new Lexer(args[0]);
    }
}

/**
 * Expression representing a numerical value
 */
class NumExp
{
    int i;

    public NumExp(Object s) { i = Integer.parseInt((String)s); }

    public String toString() { return "S.NumExp " + i; }
}

/**
 * Expression representing a identifier
 */
class IdExp
{
    String s;

    public IdExp(String s) { this.s = (String)s; }

    public String toString() { return "S.IdExp \"" + s + "\""; }
}

/**
 * Expression representing a type with a statement and an expression
 */
class EseqExp
{
    Object o1, o2;

    public EseqExp(Object O1, Object O2)
    {
        o1 = Lexer.parseStm(O1);
        o2 = Lexer.parseExp(O2);
    }

    public String toString() 
    { 
        return "S.EseqExp(" + o1.toString() + "," + o2.toString() + ")"; 
    }
}

/**
 * Expression representing binary operation between two expressions
 */
class OpExp
{
    Object o1, o2;
    String op = null;

    public OpExp(Object O1, String s, Object O2)
    {
        o1 = Lexer.parseExp(O1);
        o2 = Lexer.parseExp(O2);

        if (s.equals("+"))
            op = "S.Plus";
        if (s.equals("-"))
            op = "S.Minus";
        if (s.equals("*"))
            op = "S.Times";
        if (s.equals("/"))
            op = "S.Div";
       
        // control should never reach here
        if (op == null)
        {
            System.err.println("ERROR: Unknown binary operation: ");
            System.err.println("\t" + op);
            System.exit(-1);
        }
    }

    public String toString() 
    { 
        return "S.OpExp(" + o1.toString() + "," + op + "," +  o2.toString() + ")"; 
    }
}

/**
 * Statement representing two statements
 */
class Compound
{
    Object o1, o2;

    public Compound(Object O1, Object O2) 
    {
        o1 = Lexer.parseStm(O1);
        o2 = Lexer.parseStm(O2);
    }

    public String toString() 
    { 
        return "S.CompoundStm(" + o1.toString() + "," + o2.toString() + ")";
    }
}

/**
 * Statement representing the assignment of an expression to a statement
 */
class Assign
{
    String o1;
    Object o2;

    public Assign(String O1, Object O2)
    {
        o1 = O1;
        o2 = Lexer.parseStm(O2);
    }

    public String toString() 
    { 
        return "S.AssignStm(" + "\"" + o1.toString() + "\"" + "," + o2.toString() + ")";
    }
}

/**
 * Statement representing a print() statement with a list of expressions
 */
class Print
{
    Object[] o;

    public Print(Object[] O) 
    { 
        o = new Object[O.length];
        for (int i = 0; i < O.length; i++)
            o[i] = Lexer.parseStm(O[i]);
    }

    public String toString() 
    { 
        String ret = "S.PrintStm[";

        for (int i = 0; i < o.length; i++)
            ret += o[i].toString() + ((i == o.length - 1) ? "" : ",");

        return ret + "]";
    }
}

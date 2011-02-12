import java.util.*;

public class Parser
{
    public Parser()
    {
        Scanner in = new Scanner(System.in);
        System.out.println(parse(in.nextLine().replaceAll(" ", "")));
    }

    public static Object parse(Object o)
    {
       if (!(o instanceof String))
           return o;

        String s = (String)o;

        if (s.charAt(0) == '(')
            return Parser.parseExp(s);

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
            return Parser.parseExp(s);
    }

    public static Object parseExp(Object o)
    {
       if (!(o instanceof String))
           return o;

        String s = (String)o;
        int pos;

        if (s.charAt(0) == '(')
        {
            pos = getFirstComma(s);
            pos = s.indexOf(")");
            if (pos > 0 && pos < s.length()-1)
                pos += s.substring(pos).indexOf(',');
            else
                pos = s.indexOf(",");

            if (pos < 0)
            {
                System.err.println("ERROR: Ill-formed EseqExp expression");
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
        System.err.println(s);
        return null;
    }

    private static int findFirstOp(String str)
    {
        int a = str.indexOf("+");
        int s = str.indexOf("-");
        int m = str.indexOf("*");
        int d = str.indexOf("/");

        int min = 1000;

        if (a > 0 && a < min)
            min = a;
        if (s > 0 && s < min)
            min = s;
        if (m > 0 && m < min)
            min = m;
        if (d > 0 && d < min)
            min = d;

        if (min == 1000)
            return -1;
        return min;
    }

    public static void main(String args[])
    {
        new Parser();
    }
}

class NumExp
{
    int i;

    public NumExp(Object s) { i = Integer.parseInt((String)s); }

    public String toString() { return "NumExp " + i; }
}

class IdExp
{
    String s;

    public IdExp(String s) { this.s = (String)s; }

    public String toString() { return "IdExp \"" + s + "\""; }
}

class EseqExp
{
    Object o1, o2;

    public EseqExp(Object O1, Object O2)
    {
        o1 = Parser.parse(O1);
        o2 = Parser.parseExp(O2);
    }

    public String toString() 
    { 
        return "EseqExp(" + o1.toString() + "," + o2.toString() + ")"; 
    }
}

class OpExp
{
    Object o1, o2;
    String op;

    public OpExp(Object O1, String s, Object O2)
    {
        o1 = Parser.parseExp(O1);
        o2 = Parser.parseExp(O2);

        if (s.equals("+"))
            op = "Plus";
        if (s.equals("-"))
            op = "Minus";
        if (s.equals("*"))
            op = "Times";
        if (s.equals("/"))
            op = "Div";
    }

    public String toString() 
    { 
        return "OpExp(" + o1.toString() + "," + op + "," +  o2.toString() + ")"; 
    }
}

class Compound
{
    Object o1, o2;

    public Compound(Object O1, Object O2) 
    {
        o1 = Parser.parse(O1);
        o2 = Parser.parse(O2);
    }

    public String toString() 
    { 
        return "CompoundStm(" + o1.toString() + "," + o2.toString() + ")";
    }
}

class Assign
{
    Object o1, o2;

    public Assign(Object O1, Object O2)
    {
        o1 = Parser.parse(O1);
        o2 = Parser.parse(O2);
    }

    public String toString() 
    { 
        return "AssignStm(" + o1.toString() + "," + o2.toString() + ")";
    }
}

class Print
{
    Object[] o;

    public Print(Object[] O) 
    { 
        o = new Object[O.length];
        for (int i = 0; i < O.length; i++)
            o[i] = Parser.parse(O[i]);
    }

    public String toString() 
    { 
        String ret = "PrintStm[";

        for (int i = 0; i < o.length; i++)
            ret += o[i].toString() + ((i == o.length - 1) ? "" : ",");

        return ret + "]";
    }
}

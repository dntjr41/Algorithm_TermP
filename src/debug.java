// Debug class
public class debug {

	public static void print(String str)
	{
		System.out.println(str);
	}
	public static void print(Exception e)
	{
		System.out.println(e);
	}
	public static void print(String str, int num1, int num2)
	{
		System.out.printf(str, num1, num2);
	}
}
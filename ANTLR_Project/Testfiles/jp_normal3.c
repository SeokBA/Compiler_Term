public class test {
    public static int add(int x,int y)
    {
        int z=0;
        z = x + y;
        return z;
    }
    public static void main(String[] args)
    {
        int t = 33;
        int k[] = new int[3];
        int j[] = new int[5];
        k[0] = j[2];
        System.out.println(add(1, k[0]));
    }
}
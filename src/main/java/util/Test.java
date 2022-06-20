package util;
import java.util.Scanner;

class Test {
    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        int[] numArr = new int[9];
        for(int i=0; i<numArr.length; i++) {
            numArr[i] = scan.nextInt();
        }
        scan.close();
        
        int max = numArr[0];
        int index = 0;
        for(int j=0; j<numArr.length; j++) {
            if(numArr[j] > max) {
                max = numArr[j];
                index = j+1;
            }
        }
        System.out.println(max + "\n" + index);
        
    }
}
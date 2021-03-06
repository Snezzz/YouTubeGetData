public class BitPresentation {

    private static int number = -2147483647;

    public static void main(String[] args) {

        int absoluteNumberCopy = Math.abs(number);

        int bitCapacity = 2;
        byte numberOfInvolvedBits = 1;

        while (absoluteNumberCopy >= bitCapacity) {

            numberOfInvolvedBits++;

            if (absoluteNumberCopy == bitCapacity || numberOfInvolvedBits == 31)
                break;

            bitCapacity <<= 1;

        }

        byte numberOfBits = (byte) (Math.pow(2, 3) - 1);

        while (numberOfBits < numberOfInvolvedBits)
            numberOfBits = (byte) ((++numberOfBits + 8) - 1);

        long bitModule = (long) Math.pow(2, 31);

        while (bitModule > absoluteNumberCopy)
            bitModule >>= 1;

        byte countOfBitsRead = 1;

        System.out.printf("The number %,d in bit presentation: " + (number >= 0 ? 0 : 1) + " ", number);

        while (bitModule > 0 || number == 0) {

            while (numberOfBits - numberOfInvolvedBits > 0) {

                countOfBitsRead = checkByteSpace(countOfBitsRead);

                if (number >= 0)
                    System.out.print("0");
                else
                    System.out.print("1");

                countOfBitsRead++;

                numberOfInvolvedBits++;

            }

            countOfBitsRead = checkByteSpace(countOfBitsRead);

            if (number < 0 && absoluteNumberCopy == bitModule)
                System.out.print("1");

            else {

                if (number > 0 || (number < 0 && absoluteNumberCopy == 0))
                    System.out.print(absoluteNumberCopy / bitModule > 0 ? 1 : 0);
                else if (number < 0)
                    System.out.print(absoluteNumberCopy / bitModule == 0 ? 1 : 0);

                else {

                    System.out.print("0");
                    break;

                }

            }

            countOfBitsRead++;

            absoluteNumberCopy %= bitModule;
            bitModule >>= 1;

        }

    }

    private static byte checkByteSpace(byte numberOfBits) {

        if (numberOfBits == 8) {

            System.out.print(" ");
            return 0;

        }

        else
            return numberOfBits;

    }

}
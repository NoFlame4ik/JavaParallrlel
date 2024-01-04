import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

public class ParallelArraySumCalculator {

    public static void main(String[] args) {
        int arraySize = 1000;
        int[] array = generateRandomArray(arraySize);
        calculateParallelArraySum(array);
    }

    public static void calculateParallelArraySum(int[] array) {
        List<int[]> iterations = new ArrayList<>();
        iterations.add(array.clone());

        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        long startTime = System.nanoTime();

        while (array.length > 1) {
            int newArrayLength = (array.length % 2 == 0) ? array.length / 2 : (array.length / 2) + 1;
            int[] newArray = new int[newArrayLength];

            int[] results = new int[newArrayLength];
            CountDownLatch latch = new CountDownLatch(newArrayLength);

            for (int i = 0; i < newArrayLength; i++) {
                final int index1 = i;
                final int index2 = array.length - 1 - i;

                int value = array[index1] + ((index2 >= newArrayLength) ? array[index2] : 0);

                int finalIndex = i; // Add this line to make the variable effectively final

                executorService.submit(() -> {
                    results[finalIndex] = value;
                    latch.countDown();
                });
            }

            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.arraycopy(results, 0, newArray, 0, newArrayLength);

            iterations.add(newArray.clone());
            array = newArray;
        }

        executorService.shutdown();

        long endTime = System.nanoTime();
        long executionTimeNano = endTime - startTime;
        long executionTimeMs = executionTimeNano / 1_000_000; // Convert nanoseconds to milliseconds

        // Print intermediate arrays
        printIterations(iterations);

        System.out.println("Final result: " + array[0]);
        System.out.println("Execution time: " + executionTimeMs + " ms");
    }

    private static void printArray(int[] array) {
        for (int element : array) {
            System.out.print(element + " ");
        }
        System.out.println();
    }

    private static void printIterations(List<int[]> iterations) {
        System.out.println("Intermediate arrays after each iteration:");
        for (int i = 1; i < iterations.size(); i++) {
            System.out.print("Iteration " + i + ": ");
            printArray(iterations.get(i));
        }
    }

    private static int[] generateRandomArray(int size) {
        int[] array = new int[size];
        Random random = new Random();
        for (int i = 0; i < size; i++) {
            array[i] = random.nextInt(100); // Додайте максимальне значення, якщо потрібно обмежити числа
        }
        return array;
    }
}

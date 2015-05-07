package nachos.threads;

import java.util.ArrayList;
import java.util.Arrays;

import nachos.machine.Lib;

/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>,
 * and multiple threads can be waiting to <i>listen</i>. But there should never
 * be a time when both a speaker and a listener are waiting, because the two
 * threads can be paired off at this point.
 */
public class Communicator {
    /**
     * Allocate a new communicator.
     */
        Lock lock;
        int speakersWaiting;
        int listenersWaiting;
        int activeListeners;
        int activeSpeakers;
        int currentWord;
        Condition okToTransfer;
        Condition okToReceive;
        Condition transferMade;

    public Communicator() {
        lock = new Lock();
        activeSpeakers = 0;
        speakersWaiting = 0;
        activeListeners = 0;
        listenersWaiting = 0;
        currentWord = 0;
        okToTransfer = new Condition(lock);
        okToReceive = new Condition(lock);
        transferMade = new Condition(lock);
    }

    /**
     * Wait for a thread to listen through this communicator, and then transfer
     * <i>word</i> to the listener.
     *
     * <p>
     * Does not return until this thread is paired up with a listening thread.
     * Exactly one listener should receive <i>word</i>.
     *
     * @param   word    the integer to transfer.
     */
    public void speak(int word) {
        lock.acquire();
        while ((listenersWaiting == 0 && activeListeners == 0) || (activeSpeakers > 0)) {
                speakersWaiting++;
                okToTransfer.sleep();
                speakersWaiting--;
        }
        activeSpeakers++;
        if (listenersWaiting > 0) {
                okToReceive.wake();
        }
        currentWord = word;
        transferMade.wake();
        lock.release();
    }

    /**
     * Wait for a thread to speak through this communicator, and then return
     * the <i>word</i> that thread passed to <tt>speak()</tt>.
     *
     * @return  the integer transferred.
     */
    public int listen() {
        int myWord;
        lock.acquire();
        while ((speakersWaiting == 0 && activeSpeakers == 0) || (activeListeners > 0)) {
                listenersWaiting++;
                okToReceive.sleep();
                listenersWaiting--;
        }
        activeListeners++;
        if (activeSpeakers == 0) {
                okToTransfer.wake();
                transferMade.sleep();
        }
        myWord = currentWord;
        if (speakersWaiting > 0) {
                okToTransfer.wake();
        }
        activeListeners--;
        activeSpeakers--;
        lock.release();
        return myWord;
    }

    public static void selfTest() {
        System.out.println("\nInitiating Communicator tests...");
        int numTests = 3;
        int[] results = new int[numTests];
        results[0] = testOne();
        results[1] = testTwo();
        results[2] = testThree();
        boolean passed = true;
        for (int result: results) {
                if (result == 0) {
                        passed = false;
                }
        }
        if (passed) {
                System.out.println("Passed all Communicator tests");
        } else {
                System.out.println("Failed all Communicator tests");
        }

    }

    public static int testThree() {
        System.out.print("Initiating test 3...");
        final Communicator comm = new Communicator();
        KThread speaker0;
        KThread speaker1;
        KThread listener0;
        KThread listener1;
        final int[] inputWords = {50, 100};
        final int[] returnedWords = new int[2];
        listener0 = new KThread(new Runnable() {
                public void run() {
                        returnedWords[0] = comm.listen();
                }
        });
        listener0.fork();
        speaker0 = new KThread(new Runnable() {
                public void run() {
                        comm.speak(inputWords[0]);
                }
        });
        speaker0.fork();
        speaker1 = new KThread(new Runnable() {
                public void run() {
                        comm.speak(inputWords[1]);
                }
        });
        speaker1.fork();
        listener1 = new KThread(new Runnable() {
                public void run() {
                        returnedWords[1] = comm.listen();
                }
        });
        listener1.fork();
        speaker0.join(); speaker1.join();
        listener0.join(); listener1.join();
        boolean passed = true;
        for (int i = 0; i < inputWords.length; i++) {
                if (returnedWords[i] != 50 && returnedWords[i] != 100) {
                        passed = false;
                }
        }
        if (passed) {
                System.out.println("Passed");
                return 1;
        } else {
                System.out.println("Failed");
                return 0;
        }
    }

    public static int testTwo() {
        System.out.print("Initiating test 2...");
        final Communicator comm = new Communicator();
        KThread speaker0;
        KThread speaker1;
        KThread speaker2;
        KThread speaker3;
        KThread listener0;
        KThread listener1;
        KThread listener2;
        KThread listener3;
        final ArrayList<Integer> inputWords = new ArrayList<Integer>();
        inputWords.add((Integer)50);
        inputWords.add((Integer)100);
        inputWords.add((Integer)150);
        inputWords.add((Integer)200);
        final ArrayList<Integer> returnedWords = new ArrayList<Integer>();
        listener0 = new KThread(new Runnable() {
                public void run() {
                        returnedWords.add(comm.listen());
                }
        });
        listener0.fork();
        listener1 = new KThread(new Runnable() {
                public void run() {
                        returnedWords.add(comm.listen());
                }
        });
        listener1.fork();
        listener2 = new KThread(new Runnable() {
                public void run() {
                        returnedWords.add(comm.listen());
                }
        });
        listener2.fork();
        listener3 = new KThread(new Runnable() {
                public void run() {
                        returnedWords.add(comm.listen());
                }
        });
        listener3.fork();
        speaker0 = new KThread(new Runnable() {
                public void run() {
                        comm.speak(inputWords.get(0));
                }
        });
        speaker0.fork();
        speaker1 = new KThread(new Runnable() {
                public void run() {
                        comm.speak(inputWords.get(1));
                }
        });
        speaker1.fork();
        speaker2 = new KThread(new Runnable() {
                public void run() {
                        comm.speak(inputWords.get(2));
                }
        });
        speaker2.fork();
        speaker3 = new KThread(new Runnable() {
                public void run() {
                        comm.speak(inputWords.get(3));
                }
        });
        speaker3.fork();
        speaker0.join(); speaker1.join(); speaker2.join(); speaker3.join();
        listener0.join(); listener1.join(); listener2.join(); listener3.join();
        boolean passed = true;
        for (int i = 0; i < inputWords.size(); i++) {
                if (!returnedWords.contains(inputWords.get(i))) {
                        passed = false;
                }
        }
        if (passed) {
                System.out.println("Passed");
                return 1;
        } else {
                System.out.println("Failed");
                return 0;
        }
    }

    /*
     * Test #1: Initialize 4 speakers with words 50, 100, 150, 200 and 4 listeners afterwards
     * Return 1 if successful; 0 otherwise
     */
    public static int testOne() {
        System.out.print("Initiating test 1...");
        final Communicator comm = new Communicator();
        KThread speaker0;
        KThread speaker1;
        KThread speaker2;
        KThread speaker3;
        KThread listener0;
        KThread listener1;
        KThread listener2;
        KThread listener3;
        final ArrayList<Integer> inputWords = new ArrayList<Integer>();
        inputWords.add((Integer)50);
        inputWords.add((Integer)100);
        inputWords.add((Integer)150);
        inputWords.add((Integer)200);
        final ArrayList<Integer> returnedWords = new ArrayList<Integer>();
        speaker0 = new KThread(new Runnable() {
                public void run() {
                        comm.speak(inputWords.get(0));
                }
        });
        speaker0.fork();
        speaker1 = new KThread(new Runnable() {
                public void run() {
                        comm.speak(inputWords.get(1));
                }
        });
        speaker1.fork();
        speaker2 = new KThread(new Runnable() {
                public void run() {
                        comm.speak(inputWords.get(2));
                }
        });
        speaker2.fork();
        speaker3 = new KThread(new Runnable() {
                public void run() {
                        comm.speak(inputWords.get(3));
                }
        });
        speaker3.fork();
        listener0 = new KThread(new Runnable() {
                public void run() {
                        returnedWords.add(comm.listen());
                }
        });
        listener0.fork();
        listener1 = new KThread(new Runnable() {
                public void run() {
                        returnedWords.add(comm.listen());
                }
        });
        listener1.fork();
        listener2 = new KThread(new Runnable() {
                public void run() {
                        returnedWords.add(comm.listen());
                }
        });
        listener2.fork();
        listener3 = new KThread(new Runnable() {
                public void run() {
                        returnedWords.add(comm.listen());
                }
        });
        listener3.fork();
        speaker0.join(); speaker1.join(); speaker2.join(); speaker3.join();
        listener0.join(); listener1.join(); listener2.join(); listener3.join();
        boolean passed = true;
        for (int i = 0; i < inputWords.size(); i++) {
                if (!returnedWords.contains(inputWords.get(i))) {
                        passed = false;
                }
        }
        if (passed) {
                System.out.println("Passed");
                return 1;
        } else {
                System.out.println("Failed");
                return 0;
        }
    }

}

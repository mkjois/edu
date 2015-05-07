package nachos.threads;
import java.util.ArrayList;
import nachos.ag.BoatGrader;

public class Boat
{
    static BoatGrader bg;

    static Lock stateLock;
    static Condition makeReturn;
    static Condition sendAdult;
    static Condition sendChildren;
    static int childrenOnO;
    static int childrenOnM;
    static int adultsOnO;
    static int adultsOnM;
    static boolean boatOnO;
    static boolean freeTicket;
    static boolean terminated;
    static boolean active;
    static Alarm ender;

    public static void selfTest()
    {
        BoatGrader b = new BoatGrader();

        System.out.println("\n ***Testing Boats with only 2 children***");
        begin(0, 2, b);
        System.out.println("\n ***Testing Boats with 2 children, 1 adult***");
        begin(1, 2, b);
        System.out.println("\n ***Testing Boats with 3 children, 3 adults***");
        begin(3, 3, b);
        System.out.println("\n ***Testing Boats with 10 children, 10 adults***");
        begin(10, 10, b);
        System.out.println("\n ***Testing Boats with 2 children, 50 adults***");
        begin(50, 2, b);

    }

    public static void begin( int adults, int children, BoatGrader b )
    {
        // Store the externally generated autograder in a class
        // variable to be accessible by children.
        bg = b;

        // Instantiate global variables here
        stateLock = new Lock();
        makeReturn = new Condition(stateLock);
        sendAdult = new Condition(stateLock);
        sendChildren = new Condition(stateLock);
        childrenOnO = 0;
        childrenOnM = 0;
        adultsOnO = 0;
        adultsOnM = 0;
        boatOnO = true;
        freeTicket = false;
        terminated = false;
        active = true;
        ender = new Alarm();

        // Create threads here. See section 3.4 of the Nachos for Java
        // Walkthrough linked from the projects page.

        stateLock.acquire();
        Runnable childRun = new Runnable() {
            public void run() {
                ChildItinerary();
            }
        };
        ArrayList<KThread> childThreads = new ArrayList<KThread>();
        for (int i = 0; i < children; i++) {
            childThreads.add(new KThread(childRun));
            childThreads.get(i).setName("shrayohar");
        }
        Runnable adultRun = new Runnable() {
            public void run() {
                AdultItinerary();
            }
        };
        ArrayList<KThread> adultThreads = new ArrayList<KThread>();
        for (int i = 0; i < adults; i++) {
            adultThreads.add(new KThread(adultRun));
            adultThreads.get(i).setName("manrayus");
        }

        for (KThread childThread : childThreads){
            childThread.fork();
        }
        for (KThread adultThread : adultThreads){
            adultThread.fork();
        }
        stateLock.release();

        for (KThread childThread : childThreads) {
            childThread.join();
        }
        //System.out.println("Begin() terminate");
    }

    static void AdultItinerary()
    {
        bg.initializeAdult(); //Required for autograder interface. Must be the first thing called.
        //DO NOT PUT ANYTHING ABOVE THIS LINE.

        /* This is where you should put your solutions. Make calls
           to the BoatGrader to show that it is synchronized. For
           example:
               bg.AdultRowToMolokai();
           indicates that an adult has rowed the boat across to Molokai
        */
        stateLock.acquire();
        active = true;
        adultsOnO++;
        while (childrenOnM == 0 || !boatOnO) {
            //System.out.println("Adult sleep!");
            sendAdult.sleep();
            active = true;
            //System.out.println("Adult wake!");
        }
        adultsOnO--;
        bg.AdultRowToMolokai();
        adultsOnM++;
        boatOnO = false;
        makeReturn.wake();
        stateLock.release();
        //System.out.println("Adult terminate!");
    }

    static void ChildItinerary()
    {
        bg.initializeChild(); //Required for autograder interface. Must be the first thing called.
        //DO NOT PUT ANYTHING ABOVE THIS LINE.
        stateLock.acquire();
        active = true;
        childrenOnO++;
        while (true) {
            while (childrenOnO < 2 || !boatOnO) {
                //System.out.println("Child sleep (sendChildren)!");
                sendChildren.sleep();
                active = true;
                //System.out.println("Child wake! (sendChildren)");
                if (freeTicket) {
                    break;
                }
            }
            if (freeTicket) {
                //System.out.println("Child taking free ticket!");
                freeTicket = false;
                childrenOnO--;
                bg.ChildRideToMolokai();
                childrenOnM++;
                if (childrenOnO + adultsOnO == 0) {
                    active = false;
                    ender.waitUntil(10000);
                    if (!active) {
                        terminated = true;
                        makeReturn.wakeAll();
                        //System.out.println("Threads inactive! Child terminates all!");
                        stateLock.release();
                        return;
                    }
                }
            } else {
                childrenOnO--;
                bg.ChildRowToMolokai();
                childrenOnM++;
                /*
                System.out.println("childrenOnO: " + childrenOnO);
                System.out.println("adultsOnO: " + adultsOnO);
                System.out.println("childrenOnM: " + childrenOnM);
                System.out.println("adultsOnM: " + adultsOnM + "\n");
                */
                freeTicket = true;
                boatOnO = false;
                sendChildren.wake();
            }
            while (childrenOnO + adultsOnO == 0 || boatOnO || freeTicket) {
                //System.out.println("Child sleep! (makeReturn)");
                makeReturn.sleep();
                active = true;
                //System.out.println("Child wake! (makeReturn)");
                if (terminated) {
                    //System.out.println("Child terminate!");
                    stateLock.release();
                    return;
                }
            }
            childrenOnM--;
            bg.ChildRowToOahu();
            childrenOnO++;
            /*
            System.out.println("childrenOnO: " + childrenOnO);
            System.out.println("adultsOnO: " + adultsOnO);
            System.out.println("childrenOnM: "+childrenOnM);
            System.out.println("adultsOnM: "+adultsOnM+"\n");
            */
            boatOnO = true;
            if (childrenOnO >= 2) {
                sendChildren.wake();
            } else if (adultsOnO > 0) {
                sendAdult.wake();
            }
        }
    }

    static void SampleItinerary()
    {
        // Please note that this isn't a valid solution (you can't fit
        // all of them on the boat). Please also note that you may not
        // have a single thread calculate a solution and then just play
        // it back at the autograder -- you will be caught.
        System.out.println("\n ***Everyone piles on the boat and goes to Molokai***");
        bg.AdultRowToMolokai();
        bg.ChildRideToMolokai();
        bg.AdultRideToMolokai();
        bg.ChildRideToMolokai();
    }

}

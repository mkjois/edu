import hashlib
import random
import os

"""
This file contains a basic test case that just passes packets through the
forwarder. Custom test cases should extend this class, and you should only need
to implement a new handle_packet(), handle_tick(), and/or result() method as
needed.
"""

class BasicTest(object):
    """ A test case should define the following:
        - handle_packet: a method to be called whenever a packet arrives
        - handle_tick: a method to be called at every timestep
        - result: a method to be called when it's time to return a result
    """
    def __init__(self, forwarder, input_file):
        self.forwarder = forwarder

        if not os.path.exists(input_file):
            raise ValueError("Could not find input file: %s" % input_file)
        self.input_file = input_file
        self.forwarder.register_test(self, self.input_file)

    def handle_packet(self):
        """
        This method is called whenever the forwarder receives a packet,
        immediately after the packet has been added to the forwarder's input
        queue.

        The default behavior of the base class is to simply copy whatever is in
        the input queue to the output queue, in the order it was received.
        Most tests will want to override this, since this doesn't give you the
        opportunity to do anything tricksy with the packets.

        Note that you should NEVER make any assumptions about how many packets
        are in the in_queue when this method is called -- there could be zero,
        one, or many!
        """
        for p in self.forwarder.in_queue:
            self.forwarder.out_queue.append(p)
        # empty out the in_queue
        self.forwarder.in_queue = []

    def handle_tick(self, tick_interval):
        """
        This method is called whenever the forwarder has a tick event. This
        gives the test case an opportunity to create behavior that is not
        triggered by packet arrivals. The forwarder will provide the tick
        interval to the test case.

        The default behavior of this method is to do nothing.
        """
        pass

    def result(self, receiver_outfile):
        """
        This should return some meaningful result. You could do something
        like check to make sure both the input and output files are identical,
        or that some other aspect of your test passed. This is called
        automatically once the forwarder has finished executing the test.

        You can return whatever you like, or even just print a message saying
        the test passed. Alternatively, you could use the return value to
        automate testing (i.e., return "True" for every test that passes,
        "False" for every test that fails).
        """
        if not os.path.exists(receiver_outfile):
            raise ValueError("No such file %s" % str(receiver_outfile))
        if self.files_are_the_same(self.input_file, receiver_outfile):
            print "Test passes!"
            return True
        else:
            print "Test fails: original file doesn't match received. :("
            return False

    # Utility methods -- not necessary, just helpful for writing tests
    def files_are_the_same(self, file1, file2):
        """
        Checks if the contents of two files are the same. Returns True if they
        are, and False otherwise.
        """
        return BasicTest.md5sum(file1) == BasicTest.md5sum(file2)

    @staticmethod
    def md5sum(filename, block_size=2**20):
        """
        Calculates the md5sum of a file.

        Precondition: file exists
        """
        f = open(filename, "rb")
        md5 = hashlib.md5()
        while True:
            data = f.read(block_size)
            if not data:
                break
            md5.update(data)
        f.close()
        return md5.digest()


"""
This tests random packet drops. We randomly decide to drop about half of the
packets that go through the forwarder in either direction.

Note that to implement this we just needed to override the handle_packet()
method -- this gives you an example of how to extend the basic test case to
create your own.
"""
class RandomDropTest(BasicTest):
    def handle_packet(self):
        for p in self.forwarder.in_queue:
            if random.choice([True, False]):
                self.forwarder.out_queue.append(p)

        # empty out the in_queue
        self.forwarder.in_queue = []


"""
14.90% chance this corrupts an ack packet
23.41% chance this corrupts a start, data, or end packet
10.00% chance this drops any packet
"""
class MyTest2(BasicTest):
    def handle_packet(self):
        chance_box = [True,True,True,True,True,True,True,True,True,False]
        for p in self.forwarder.in_queue:
            new_type = p.msg_type if random.choice(chance_box) else random.choice(["start","data","end","ack"])
            new_seqno = p.seqno
            new_seqno += 0 if random.choice(chance_box) else random.choice([-2,-1,0,1,2])
            new_data = ""
            if p.data:
                index = random.randrange(len(p.data))
                new_data = p.data if random.choice(chance_box) else p.data[:index]+p.data[index+1:]
            p.update_packet(new_type, new_seqno, new_data, None, False)
            if random.choice(chance_box):
                self.forwarder.out_queue.append(p)
        self.forwarder.in_queue = []

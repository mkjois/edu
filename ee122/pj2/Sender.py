import sys
import getopt

import Checksum
import BasicSender

'''
This is a skeleton sender class. Create a fantastic transport protocol here.
'''
class Sender(BasicSender.BasicSender):

    _WINDOW = 5
    _TIMEOUT = 0.5  # in seconds
    _DATABYTES = 1200  # read this many bytes every time we access the infile
    _SEQNO_START = 0

    def __init__(self, dest, port, filename, debug=False):
        super(Sender, self).__init__(dest, port, filename, debug)

    # Main sending loop.
    def start(self):
        """
        Implements a standard sliding-window protocol using Go-Back-N.
        """
        self.first_unacked = Sender._SEQNO_START
        self.window = Window(Sender._WINDOW)
        for _ in range(Sender._WINDOW):
            self.window.load_data(self.infile.read(Sender._DATABYTES))

        while self.window.has_packets():
            valid_responses = []
            messages = self.window.flush()
            num_sent = len(messages)
            for message, seqno in messages:
                msg_type = "start" if seqno == Sender._SEQNO_START else "data"
                msg_type = msg_type if message else "end"
                packet = self.make_packet(msg_type, seqno, message)
                self.send(packet)

            for _ in range(num_sent):
                response = self.receive(Sender._TIMEOUT)
                if response is None:
                    break
                elif Checksum.validate_checksum(response):
                    valid_responses.append(response)

            for resp in valid_responses:
                self._handle_ack(resp)

        self.infile.close()

    def _handle_ack(self, ack):
        """
        Responds to an ack by either doing nothing if the ack has an out of
        range sequence number, or increments the sliding window accordingly.
        """
        msg_type, seqno, data, checksum = self.split_packet(ack)
        seqno = int(seqno)
        acceptable_ack_range = range(self.first_unacked, self.first_unacked + Sender._WINDOW + 1)
        if msg_type != "ack" or seqno not in acceptable_ack_range:
            return

        for _ in range(seqno - self.first_unacked):
            self.window.pop_last()
            self.first_unacked += 1
            new_data = self.infile.read(Sender._DATABYTES)
            if self.window.open_to_reads():
                self.window.load_data(new_data)


"""
A class to encapsulate the sender's sliding window. Holds a certain number of
data messages at a time and discards them as they get evicted from the window.
In this implementation, the last message we read from data and eventually send
is always the empty string.
"""
class Window(object):
    
    def __init__(self, size):
        self.size = size
        self.seqno = Sender._SEQNO_START
        self.packets = []

    def load_data(self, message):
        """
        Enqueues data into the sliding window and pairs it with the appropriate
        sequence number.
        """
        self.packets.append((message, self.seqno))
        self.seqno += 1

    def has_packets(self):
        """Self-explanatory"""
        return bool(self.packets)

    def open_to_reads(self):
        """
        Returns true if we have not already read from file the empty string
        which represents the end of the file.
        """
        return bool(self.packets) and bool(self.packets[-1][0])

    def pop_last(self):
        """
        Evicts the first message in the window.
        Precondition: the evicted message directly preceeds the first un-ack'd
        packet.
        """
        self.packets.pop(0)

    def flush(self):
        """
        Returns a list of all packets in the window, where each element is of
        the form (data, sequence number).
        """
        to_send = []
        for message, seqno in self.packets:
            to_send.append((message, seqno))
        return to_send


'''
This will be run if you run this script from the command line. You should not
change any of this; the grader may rely on the behavior here to test your
submission.
'''
if __name__ == "__main__":
    def usage():
        print "BEARS-TP Sender"
        print "-f FILE | --file=FILE The file to transfer; if empty reads from STDIN"
        print "-p PORT | --port=PORT The destination port, defaults to 33122"
        print "-a ADDRESS | --address=ADDRESS The receiver address or hostname, defaults to localhost"
        print "-d | --debug Print debug messages"
        print "-h | --help Print this usage message"

    try:
        opts, args = getopt.getopt(sys.argv[1:],
                               "f:p:a:d", ["file=", "port=", "address=", "debug="])
    except:
        usage()
        exit()

    port = 33122
    dest = "localhost"
    filename = None
    debug = False

    for o,a in opts:
        if o in ("-f", "--file="):
            filename = a
        elif o in ("-p", "--port="):
            port = int(a)
        elif o in ("-a", "--address="):
            dest = a
        elif o in ("-d", "--debug="):
            debug = True

    s = Sender(dest,port,filename,debug)
    try:
        s.start()
    except (KeyboardInterrupt, SystemExit):
        exit()

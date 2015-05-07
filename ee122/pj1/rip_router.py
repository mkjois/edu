from sim.api import *
from sim.basics import *

'''
Create your RIP router in this file.
'''
class RIPRouter (Entity):

    # _NUM_UPDATES = 0

    _INF = 100 # max hop count

    def __init__(self):
        self.table = {}
        self.port_map = {}
        self.vector = {} # DOES maintain next hops even if path cost is RIPRouter._INF
        self.vector_changed = False
        self.next_update = None

    def handle_rx (self, packet, port):
        if isinstance(packet, DiscoveryPacket):
            if packet.is_link_up:
                self.add_link(packet.src, port)
            else:
                self.remove_link(packet.src)
        elif isinstance(packet, RoutingUpdate):
            if packet.src not in self.port_map: # not a neighbor anymore, so invalid RoutingUpdate
                return
            self.next_update = packet
            self.process_update()
        else: # regular packet to forward
            try:
                next_hop = self.vector[packet.dst]
                port_id = self.port_map[next_hop]
                if self.table[packet.dst][next_hop] < RIPRouter._INF:
                    self.send(packet, port_id)
            except KeyError: # unknown destination: drop packet
                return

        if self.vector_changed: # need to construct and send appropriate RoutingUpdates
            self.emit_updates()

    def to_x_via_y (self, x, y):
        try:
            return self.table[x][y]
        except KeyError:
            return RIPRouter._INF

    def add_link (self, neighbor, port):
        self.port_map[neighbor] = port
        try:
            self.table[neighbor][neighbor]
        except KeyError:
            self.table[neighbor] = {}
        self.table[neighbor][neighbor] = 1

        next_hops = [[n, self.to_x_via_y(neighbor, n)] for n in self.port_map.keys() if n != neighbor]
        for nei, dist in next_hops:
            self.table[neighbor][nei] = dist

        for key in self.table.keys():
            if key != neighbor:
                self.table[key][neighbor] = RIPRouter._INF

        self.vector[neighbor] = neighbor
        self.vector_changed = True

    def remove_link (self, neighbor):
        self.port_map.pop(neighbor, None)
        need_changes = []

        for key in self.table.keys():
            if self.vector[key] == neighbor:
                need_changes += [key]
            self.table[key].pop(neighbor, None)

        self.force_update_vector(need_changes)
        self.vector_changed = True

    def process_update (self):
        dests, keys = self.next_update.all_dests(), self.table.keys()
        dest_set = set(dests).union(set(keys))
        dest_set.discard(self.next_update.src)
        need_changes = []

        for dest in dest_set:
            if dest in dests and dest not in keys: # new destination is known! add row to table and vector
                distance = self.next_update.get_distance(dest)
                self.table[dest] = {}
                self.table[dest][self.next_update.src] = distance+1 if distance < RIPRouter._INF else RIPRouter._INF
                for neighbor in self.port_map.keys():
                    if neighbor != self.next_update.src:
                        self.table[dest][neighbor] = RIPRouter._INF
                self.vector[dest] = self.next_update.src
                self.vector_changed = True

            elif dest not in dests and dest in keys: # possible implicit withdrawal
                self.table[dest][self.next_update.src] = RIPRouter._INF
                if self.vector[dest] == self.next_update.src:
                    need_changes += [dest]
                    self.vector_changed = True

            else: # check for a better cost
                current = self.table[dest][self.next_update.src]
                potential = 1 + self.next_update.get_distance(dest)
                curr_next_hop = self.vector[dest]
                curr_best_dist = self.table[dest][curr_next_hop]
                next_hop_port = self.port_map[curr_next_hop]
                potential_port = self.port_map[self.next_update.src]
                if potential < current:
                    self.table[dest][self.next_update.src] = potential
                need_to_change_vector = (curr_next_hop != self.next_update.src and
                                         (potential < curr_best_dist or
                                          (potential == curr_best_dist and
                                           potential_port < next_hop_port)))
                if need_to_change_vector:
                    self.vector[dest] = self.next_update.src
                    self.vector_changed = True

        self.force_update_vector(need_changes)
        self.next_update = None

    def emit_updates (self):
        ports_map = self.port_map.items()
        vector_map = self.vector.items()
        update_queue = []

        for neighbor, port in ports_map:
            update = RoutingUpdate()
            for dest, via in vector_map:
                if dest == neighbor or via == neighbor: # poison reverse
                    continue
                distance = self.table[dest][via]
                if distance < RIPRouter._INF: # rule out paths of infinite hop counts
                    update.add_destination(dest, distance)
            update_queue += [[update, port]]

        for u, p in update_queue:
            """
            RIPRouter._NUM_UPDATES += 1
            print("{0}: Update sent from {1}".format(RIPRouter._NUM_UPDATES, self.name))
            """
            self.send(u, p)
        self.vector_changed = False

    def force_update_vector (self, changes):
        """Recalculates the distance vector entries for destinations in 'changes' list.
        """
        for dest in changes:
            vias = self.table[dest].items()
            dests, dists = [k for k,v in vias], [v for k,v in vias]
            try:
                minimum = min(dists)
            except ValueError: # only happens when we remove the last link a router has
                self.vector[dest] = None
                return

            indices = [i for i,v in enumerate(dists) if v == minimum]
            min_neighbor = dests[indices[0]]

            for index in indices:
                if self.port_map[dests[index]] < self.port_map[min_neighbor]:
                    min_neighbor = dests[index]
            self.vector[dest] = min_neighbor
    
#####################################
## The following is for debugging: ##
#####################################

"""
run_tests = False

if run_tests:
    a,b,c,d = RIPRouter('a'), RIPRouter('b'), RIPRouter('c'), RIPRouter('d')

    # initial link setup, teardown testing
    p1 = DiscoveryPacket(b,1)
    p2 = DiscoveryPacket(a,1)
    p3 = DiscoveryPacket(c,1)
    p4 = DiscoveryPacket(b,1)
    p5 = DiscoveryPacket(d,1)
    p6 = DiscoveryPacket(c,1)
    e = RIPRouter('e')
    p7 = DiscoveryPacket(e,1)
    p8 = DiscoveryPacket(c,1)
    a.handle_rx(p1,1)
    b.handle_rx(p2,3)
    b.handle_rx(p3,2)
    c.handle_rx(p4,12)
    c.handle_rx(p5,1)
    d.handle_rx(p6,5)
    p3.is_link_up, p4.is_link_up = False, False
    b.handle_rx(p3,2)
    c.handle_rx(p4,12)
    c.handle_rx(p7,6)
    e.handle_rx(p8,8)
    p3.is_link_up, p4.is_link_up = True, True
    b.handle_rx(p3,7)
    c.handle_rx(p4,10)
    p9 = DiscoveryPacket(d,1)
    p0 = DiscoveryPacket(b,1)
    b.handle_rx(p9,5)
    d.handle_rx(p0,6)
    
    # routing update handling testing
    r1 = RoutingUpdate()
    r1.src = b
    r1.add_destination(c,1)
    r1.add_destination(d,1)
    a.handle_rx(r1,1)
    r2 = RoutingUpdate()
    r2.src = c
    r2.add_destination(d,1)
    r2.add_destination(e,1)
    b.handle_rx(r2,7)
    r3 = RoutingUpdate()
    r3.src = b
    r3.add_destination(a,1)
    r3.add_destination(c,1)
    r3.add_destination(e,2)
    d.handle_rx(r3,6)
    r4 = RoutingUpdate()
    r4.src = b
    r4.add_destination(a,1)
    r4.add_destination(d,1)
    c.handle_rx(r4,10)
    r5 = RoutingUpdate()
    r5.src = c
    r5.add_destination(a,2)
    r5.add_destination(b,1)
    r5.add_destination(e,1)
    d.handle_rx(r5,5)
    r6 = RoutingUpdate()
    r6.src = b
    r6.add_destination(e,2)
    r6.add_destination(c,1)
    r6.add_destination(d,1)
    a.handle_rx(r6,1)
    r7 = RoutingUpdate()
    r7.src = c
    r7.add_destination(a,2)
    r7.add_destination(b,1)
    r7.add_destination(d,1)
    e.handle_rx(r7,8)
    r8 = RoutingUpdate()
    r8.src = d
    r8.add_destination(c,1)
    r8.add_destination(e,2)
    b.handle_rx(r8,5)
    r9 = RoutingUpdate()
    r9.src = d
    r9.add_destination(b,1)
    r9.add_destination(a,2)
    c.handle_rx(r9,1)
    p5.is_link_up, p6.is_link_up = False, False
    c.handle_rx(p5,1)
    d.handle_rx(p6,5)
    p5.is_link_up, p6.is_link_up = True, True
    c.handle_rx(p5,14)
    d.handle_rx(p6,16)
    """

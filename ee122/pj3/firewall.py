#!/usr/bin/env python

from main import PKT_DIR_INCOMING, PKT_DIR_OUTGOING

import re
import socket
import struct

# a tad misleading but will do the job
_TRANSPORT = {"tcp": 6, "udp": 17, "icmp": 1, "dns": 17, "http": 6}
_PORTS = {"dns": 53, "http": 80}


class Firewall:

    _GEODBPATH = "./geoipdb.txt"
    _KITTY_SERVER = socket.inet_aton("169.229.49.109")

    def __init__(self, config, timer, iface_int, iface_ext):
        self.timer = timer
        self.iface_int = iface_int
        self.iface_ext = iface_ext

        self.rules = RuleSet(config["rule"])
        print("Loaded rules file: %s" % config["rule"])
        self.geodb = GeoDB(Firewall._GEODBPATH)
        print("Loaded Geo IP database file: %s" % Firewall._GEODBPATH)
        self.http = {}
        self.logfile = open("http.log", "a")

    def handle_timer(self):
        # TODO: For the timer feature, refer to bypass.py
        pass

    # @pkt_dir: either PKT_DIR_INCOMING or PKT_DIR_OUTGOING
    # @pkt: the actual data of the IPv4 packet (including IP header)
    def handle_packet(self, pkt_dir, pkt):
        nfields = self._get_network_fields(pkt, pkt_dir)
        if not nfields:
            print("Error: Packet is malformed at the network layer: %s" % pkt)
            return
        elif nfields["version"] == 6:
            print("Warning: Sending IPv6 packet by default: %s" % pkt)
            self._send_packet(pkt, pkt_dir)
            return

        tprotocol, toffset = nfields["protocol"], nfields["offset"]
        if tprotocol == _TRANSPORT["tcp"]:
            tfields = self._get_tcp_fields(pkt[toffset:], pkt_dir)
        elif tprotocol == _TRANSPORT["udp"]:
            tfields = self._get_udp_fields(pkt[toffset:], pkt_dir)
        elif tprotocol == _TRANSPORT["icmp"]:
            tfields = self._get_icmp_fields(pkt[toffset:], pkt_dir)
        else:
            print("Warning: Unrecognized transport protocol: %d" % tprotocol)
            self._send_packet(pkt, pkt_dir)
            return

        if not tfields:
            print("Error: Packet is malformed at the transport layer: %s" % pkt)
            return
        doffset = tfields["offset"]
        dns_eligible, http_eligible = tfields["dns_pkt"], tfields["http_pkt"]
        params = [tprotocol, nfields["ext_ip"],
                  tfields["ext_port"], nfields["country"]]
        deny_state = tfields["name"]
        if dns_eligible:
            dfields = self._get_dns_fields(pkt[toffset + doffset:])
            if not dfields:
                print("Error: Packet is malformed at the DNS layer: %s" % pkt)
                return
            if dfields["rule_apply"]:
                params.append(dfields["domain"])
                deny_state = dfields["name"]
        elif http_eligible:
            self._handle_log_http(pkt[toffset + doffset:], tfields["int_port"], nfields["ext_ip"], pkt_dir)

        result = self.rules.match_rules(*params)
        if result is None or result == "pass":
            self._send_packet(pkt, pkt_dir)
        elif result == "drop":
            print("PACKET DROPPED")
            return
        elif result == "deny":
            if deny_state == "tcp":
                pkt = self._handle_deny_tcp(pkt, toffset)
                pkt_dir = (PKT_DIR_OUTGOING if pkt_dir == PKT_DIR_INCOMING
                                            else PKT_DIR_INCOMING)
                self._send_packet(pkt, pkt_dir)
            elif deny_state == "dns":
                pkt = self._handle_deny_dns(pkt, toffset,
                                            doffset, dfields["qnamelen"])
                if pkt:
                    self._send_packet(pkt, PKT_DIR_INCOMING)

    def _handle_log_http(self, httpseg, intport, extip, pkt_dir):
        httpseg = re.sub("[\r]", "", httpseg)
        try:
            if pkt_dir == PKT_DIR_OUTGOING:
                self.http[intport][0] += httpseg
            else:
                self.http[intport][2] += httpseg
        except KeyError:
            self.http[intport] = [httpseg, 0, ""]
        if self.http[intport][1] == 0:
            reqend = self.http[intport][0].find("\n\n") + 1
            if reqend:  # entire request received
                self.http[intport][0] = self.http[intport][0][:reqend]
                self.http[intport][1] += 1
        elif self.http[intport][1] == 1:
            resend = self.http[intport][2].find("\n\n") + 1
            if resend:  # entire response received
                self.http[intport][2] = self.http[intport][2][:resend]
                self.http[intport][1] += 1
        i = self.http[intport]
        p = i[0] + i[2]
        if "\nHTTP" not in p: self.http[intport] = [i[0],1,""]
        if self.http[intport][1] > 1:  # full transaction
            lines = p.split("\n")[:-1]
            #TODO: extract info and write to logfile
            logentry = " ".join(lines[0].split()[:3])
            host, status, length = "", "0", "-1"
            for line in lines[1:]:
                words = line.split()
                if not words: continue
                if words[0].lower() == "host:":
                    host = words[1]
                elif words[0][:4] == "HTTP":
                    status = words[1]
                elif words[0].lower() == "content-length:":
                    length = words[1]
                    if not int(length):
                        length = "-1"
            if self.rules.match_logs(extip, host):
                logentry = " ".join([host, logentry, status, length])
                logentry += "\n"
                self.logfile.write(logentry)
                self.logfile.flush()
            self.http[intport] = ["", 0, ""]

    def _handle_deny_dns(self, pkt, toffset, doffset, qnamelen):
        """
        Returns None if qtype is AAAA, for IPv6 packets. Otherwise returns a DNS
        response packet containing the IP address 169.229.49.109 (the server of
        many kitten photos) for redirection back to the client.
        """
        dnsseg = pkt[toffset + doffset:toffset + doffset + qnamelen + 16]
        if dnsseg[qnamelen + 12:qnamelen + 14] == struct.pack("!H", 28):
            return None
        dnshead, question = dnsseg[:12], dnsseg[12:16 + qnamelen]
        dnshead = dnshead[:2] + self._craft_dns_bits(dnshead[2:4])
        dnshead += struct.pack("!HHHH", 1, 1, 0, 0)
        answer = question[:qnamelen] + struct.pack("!HHLH", 1, 1, 1, 4)
        answer += Firewall._KITTY_SERVER
        dnsseg = dnshead + question + answer
        oldsrcip, olddstip = pkt[12:16], pkt[16:20]
        udphead = pkt[toffset:toffset + doffset]
        udphead = udphead[2:4] + udphead[:2]
        udphead += struct.pack("!HH", len(dnsseg) + doffset, 0)
        udpseg = udphead + dnsseg
        udpseg = self._craft_udp_checksum(udpseg, olddstip, oldsrcip)
        iphead = pkt[:2] + struct.pack("!H", len(udpseg) + toffset) + pkt[4:10]
        iphead += "\x00\x00" + olddstip + oldsrcip
        iphead = self._craft_ip_checksum(iphead)
        return iphead + udpseg

    def _handle_deny_tcp(self, pkt, toffset):
        """
        Takes a packet and returns a valid packet to send back the way it came.
        Note that IP addresses are switched, as are TCP ports. Checksums are
        recomputed and the RST, ACK flags are set in the TCP header.
        """
        oldsrcip, olddstip = pkt[12:16], pkt[16:20]
        newip = pkt[:12] + olddstip + oldsrcip + pkt[20:toffset]
        tcpseg = pkt[toffset:]
        oldsrcport, olddstport = tcpseg[:2], tcpseg[2:4]
        oldseq, oldack = struct.unpack("!LL", tcpseg[4:12])
        newack = struct.pack("!L", oldseq + 1)
        newtcp = olddstport + oldsrcport + struct.pack("!L", oldack) + newack
        newtcp += tcpseg[12] + struct.pack("!B", 0x14) + tcpseg[14:]
        newip = self._craft_ip_checksum(newip)
        newtcp = self._craft_tcp_checksum(newtcp, olddstip, oldsrcip)
        return newip + newtcp

    def _craft_dns_bits(self, dnsbits):
        result, mask = 0x8000, 0x790f
        bits = struct.unpack("!H", dnsbits)[0]
        result |= bits & mask
        return struct.pack("!H", result)

    def _craft_ip_checksum(self, ipseg):
        """This function overwrites checksum to zero automatically."""
        checksum, formatter = 0, "!"
        ipseg = ipseg[:10] + "\x00\x00" + ipseg[12:]
        num_addends = len(ipseg) // 2
        for _ in range(num_addends):
            formatter += "H"
        addends = struct.unpack(formatter, ipseg)
        for addend in addends:
            checksum += addend
        while not (checksum < 65536):
            checksum = (checksum & 0xffff) + (checksum >> 16)
        checksum ^= 0xffff
        ipseg = ipseg[:10] + struct.pack("!H", checksum) + ipseg[12:]
        return ipseg

    def _craft_tcp_checksum(self, tcpseg, srcaddr, dstaddr):
        """
        This function overwrites checksum to zero automatically.
        First 4 lines take care of the pseudo-header.
        """
        checksum, formatter = _TRANSPORT["tcp"] + len(tcpseg), "!"
        srcaddr, dstaddr = struct.unpack("!LL", srcaddr + dstaddr)
        checksum += (srcaddr >> 16) + (srcaddr & 0xffff)
        checksum += (dstaddr >> 16) + (dstaddr & 0xffff)
        tcpseg = tcpseg[:16] + "\x00\x00" + tcpseg[18:]
        padd = len(tcpseg) % 2 == 1
        if padd:
            tcpseg += "\x00"
        num_addends = len(tcpseg) // 2
        for _ in range(num_addends):
            formatter += "H"
        addends = struct.unpack(formatter, tcpseg)
        for addend in addends:
            checksum += addend
        while not (checksum < 65536):
            checksum = (checksum & 0xffff) + (checksum >> 16)
        checksum ^= 0xffff
        tcpseg = tcpseg[:16] + struct.pack("!H", checksum) + tcpseg[18:]
        if padd:
            tcpseg = tcpseg[:-1]
        return tcpseg

    def _craft_udp_checksum(self, udpseg, srcaddr, dstaddr):
        """
        This function overwrites checksum to zero automatically.
        First 4 lines take care of the pseudo-header.
        """
        checksum, formatter = _TRANSPORT["udp"] + len(udpseg), "!"
        srcaddr, dstaddr = struct.unpack("!LL", srcaddr + dstaddr)
        checksum += (srcaddr >> 16) + (srcaddr & 0xffff)
        checksum += (dstaddr >> 16) + (dstaddr & 0xffff)
        udpseg = udpseg[:6] + "\x00\x00" + udpseg[8:]
        padd = len(udpseg) % 2 == 1
        if padd:
            udpseg += "\x00"
        num_addends = len(udpseg) // 2
        for _ in range(num_addends):
            formatter += "H"
        addends = struct.unpack(formatter, udpseg)
        for addend in addends:
            checksum += addend
        while not (checksum < 65536):
            checksum = (checksum & 0xffff) + (checksum >> 16)
        checksum ^= 0xffff
        if checksum == 0:
            checksum = 0xffff
        udpseg = udpseg[:6] + struct.pack("!H", checksum) + udpseg[8:]
        if padd:
            udpseg = udpseg[:-1]
        return udpseg

    def _get_network_fields(self, pkt, pkt_dir):
        if len(pkt) < 20:
            return None
        array = struct.unpack("!BBHHHBBHLL", pkt[:20])
        version = array[0] // 16
        if version not in [4,6]:
            return None
        words = array[0] % 16
        if words < 5:
            return None
        ext_ip = array[9] if pkt_dir == PKT_DIR_OUTGOING else array[8]
        return {    "name": "ip",
                 "version": version,
                  "offset": words * 4,
                 "tlength": array[2],
                "protocol": array[6],
                  "ext_ip": ext_ip,
                 "country": self.geodb.match_country(ext_ip)}

    def _get_tcp_fields(self, pkt, pkt_dir):
        if len(pkt) < 20:
            return None
        array = struct.unpack("!HHLLB", pkt[:13])
        words = array[4] // 16
        if words < 5:
            return None
        ext_port = array[1] if pkt_dir == PKT_DIR_OUTGOING else array[0]
        int_port = array[1] if pkt_dir == PKT_DIR_INCOMING else array[0]
        return {    "name": "tcp",
                  "offset": words * 4,
                "ext_port": ext_port,
                "int_port": int_port,
                "http_pkt": ext_port == _PORTS["http"],
                 "dns_pkt": False}

    def _get_udp_fields(self, pkt, pkt_dir):
        if len(pkt) < 8:
            return None
        array = struct.unpack("!HH", pkt[:4])
        ext_port = array[1] if pkt_dir == PKT_DIR_OUTGOING else array[0]
        int_port = array[1] if pkt_dir == PKT_DIR_INCOMING else array[0]
        return {    "name": "udp",
                  "offset": 8,
                "ext_port": ext_port,
                "int_port": int_port,
                "http_pkt": False,
                 "dns_pkt": array[1] == _PORTS["dns"] and pkt_dir == PKT_DIR_OUTGOING}

    def _get_icmp_fields(self, pkt, pkt_dir):
        if len(pkt) < 8:
            return None
        return {    "name": "icmp",
                  "offset": 8,
                "ext_port": struct.unpack("!B", pkt[0])[0],
                "int_port": None,
                "http_pkt": False,
                 "dns_pkt": False}

    def _get_dns_fields(self, pkt):
        if len(pkt) < 12:
            return None
        try:
            byte, domain = 12, []
            while True:
                lenbyte = struct.unpack("!B", pkt[byte])[0]
                byte += 1
                if lenbyte == 0:
                    break
                domain.append(pkt[byte : byte + lenbyte])
                byte += lenbyte
            qdcount = struct.unpack("!H", pkt[4:6])[0]
            qtype, qclass = struct.unpack("!HH", pkt[byte : byte + 4])
            rule_apply = qdcount == 1 and qtype in [1,28] and qclass == 1
            return {      "name": "dns",
                        "domain": ".".join(domain),
                         "qtype": qtype,
                      "qnamelen": byte - 12,
                    "rule_apply": rule_apply}
        except struct.error:
            return None

    def _send_packet(self, pkt, pkt_dir):
        if pkt_dir == PKT_DIR_INCOMING:
            self.iface_int.send_ip_packet(pkt)
        elif pkt_dir == PKT_DIR_OUTGOING:
            self.iface_ext.send_ip_packet(pkt)
        else:
            print("Unknown direction, dropped packet...")


class RuleSet(object):

    _PROTOCOL, _DNS = 4, 3

    def __init__(self, path):
        self.rules = []
        self.logs = []
        rulefile = open(path)
        for rule in rulefile:
            self._load_rule(rule)
        rulefile.close()

    def _load_rule(self, rule):
        if rule == "" or rule[0] == "%":
            return
        elif rule[-1:] == "\n":
            rule = rule[:-1]
            self._load_rule(rule)
            return
        fields = rule.lower().split()
        if len(fields) in [RuleSet._PROTOCOL, RuleSet._DNS]:
            fields = self._format_fields(fields)
            if fields[0] == "log":
                self.logs.append(fields)
            else:
                self.rules.append(fields)
        else:
            print("Improper rule: %s" % rule)
            return

    def match_logs(self, ipaddr, domain):
        for rule in self.logs:
            if domain:
                try:
                    rawipstring = socket.inet_aton(domain)
                    givenip = struct.unpack("!L", rawipstring)[0]
                    if givenip == rule[2]:
                        return True
                except socket.error:
                    if rule[2].match(domain):
                        return True
            elif ipaddr == rule[2]:
                return True
        return False

    def match_rules(self, protocol, ipaddr, port, country, domain=None):
        last_rule_match = None
        for rule in self.rules:
            if protocol != rule[1]:
                continue
            elif len(rule) == RuleSet._DNS:
                if domain is None:
                    continue
                elif rule[2].match(domain) is None:
                    continue
            else:
                if len(rule[2]) == 2 and isinstance(rule[2], basestring):
                    if country != rule[2]:
                        continue
                elif rule[2] != "any" and (ipaddr < rule[2][0] or
                                           ipaddr >= rule[2][1]):
                    continue
                if rule[3] != "any" and (port < rule[3][0] or
                                         port >= rule[3][1]):
                    continue
            last_rule_match = rule
        return None if last_rule_match is None else last_rule_match[0]

    def _format_fields(self, fields):
        if len(fields) == RuleSet._DNS:  # now includes log http rules
            if fields[2][0] == "*":
                pattern = "(.*)" + "(\.)".join(fields[2][1:].split("."))
                fields[2] = re.compile(pattern)
            else:
                try:
                    rawipstring = socket.inet_aton(fields[2])
                    fields[2] = struct.unpack("!L", rawipstring)[0]
                except socket.error:
                    pattern = "(\.)".join(fields[2].split("."))
                    fields[2] = re.compile(pattern)
        else:
            if len(fields[2]) > 3:
                fields[2] = self._make_range(fields[2])
            if fields[3] != "any":
                fields[3] = self._make_range(fields[3])
        fields[1] = _TRANSPORT[fields[1]]
        return fields

    def _make_range(self, string):
        nums = re.split("[.|\/|-]", string)
        if len(nums) == 1:  # X
            port = int(nums[0])
            return (port, port + 1)
        elif len(nums) == 2:  # X-Y
            return (int(nums[0]), int(nums[1]) + 1)
        elif len(nums) == 4:  # X.Y.Z.W
            ipaddr = struct.unpack("!L", socket.inet_aton(string))[0]
            return (ipaddr, ipaddr + 1)
        elif len(nums) == 5:  # X.Y.Z.W/V
            ipstring, hostbits = ".".join(nums[0:4]), 32 - int(nums[4])
            ipaddr = struct.unpack("!L", socket.inet_aton(ipstring))[0]
            return (ipaddr, ipaddr + 2**hostbits)
        else:
            return (0, 2**32)


class GeoDB(object):

    def __init__(self, path):
        self.rows = []
        try:
            geofile = open(path)
        except IOError:
            return
        for row in geofile:
            self._load_row(row)
        geofile.close()

    def _load_row(self, row):
        if row == "":
            return
        elif row[-1:] == "\n":
            row = row[:-1]
        ip_start, ip_end, code = row.split()
        ip_start = struct.unpack("!L", socket.inet_aton(ip_start))[0]
        ip_end = struct.unpack("!L", socket.inet_aton(ip_end))[0]
        self.rows.append([ip_start, ip_end, code.lower()])

    def match_country(self, address):
        """
        Takes an address as an unsigned 32-bit integer.
        Implements binary search. Returns None if not in a given range.
        """
        if len(self.rows) == 0:
            return None
        left, right = 0, len(self.rows)-1
        while True:
            mid = (left + right) / 2
            midrow = self.rows[mid]
            in_range = address >= midrow[0] and address <= midrow[1]
            if in_range:
                return midrow[2]
            if left >= right:
                if in_range:
                    return midrow[2]
                else:
                    return None
            if address < midrow[0]:
                right = mid - 1
            elif address > midrow[1]:
                left = mid + 1


if __name__ == "__main__":
    config = {"rule": "./rules.conf"}
    fw = Firewall(config, None, None, None)
    for rule in fw.rules.rules:
        print(rule)

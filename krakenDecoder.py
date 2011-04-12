import sys
import getopt
import struct

def main(argv=sys.argv):
	try:
		opts, args = getopt.getopt(argv[1:], "", [])
	except getopt.error, msg:
		print msg
		sys.exit(2)
		
	for arg in args:
		process(arg)
	
def process(arg):
	f = open(arg, "rb")
	try:
		while(True):
			type = decodeByte(f)
			assert(type == 2)
			itemLength = decodeNumber(f)
			type = decodeByte(f)
			assert(type == 2)
			protocolNumber = decodeNumber(f)
			type = decodeByte(f)
			assert(type == 3)
			serverPortNum = decodeNumber(f)
			value = decodeNumber(f)
			print "[protocolNumber = %3d, serverPortNum = %6d, value = %12d]" % (protocolNumber, serverPortNum, value)
	except struct.error, msg:
		None
	
def decodeByte(f):
	return struct.unpack("<B", f.read(1))[0]


def decodeNumber(f):
	ret = 0
	b = 0
	while True: 
		ret = ret << 7;
		b = decodeByte(f)
		ret |= b & 0x7F
		if b & 0x80 != 0x80:
			break
	
	return ret

if __name__ == "__main__":
	main()

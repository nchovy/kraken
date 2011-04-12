uname = $(shell uname -a)
ifneq ($(findstring x86_64, $(uname)),)
libpath = ../resources/lib/linux_x86_64
else ifneq ($(findstring i386, $(uname)),)
libpath = ../resources/lib/linux_x86
else ifneq ($(findstring i586, $(uname)),)
libpath = ../resources/lib/linux_x86
else ifneq ($(findstring i686, $(uname)),)
libpath = ../resources/lib/linux_x86
else
$(error Not supported on this architecture)
endif

libkdnet.so:
ifeq ($(JAVA_HOME),)
	$(error JAVA_HOME is not set)
else
	gcc arp.c firewalling.c util.c -o $(libpath)/libkdnet.so -shared -Wall -I$(JAVA_HOME)/include/ -I$(JAVA_HOME)/include/linux/ -ldnet -liptc -fPIC
endif

clean:
	rm $(libpath)/libkdnet.so

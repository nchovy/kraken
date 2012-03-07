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

libkpcap.so:
ifeq ($(JAVA_HOME),)
	$(error JAVA_HOME is not set)
else
	gcc kpcap.c routingtable.c -o $(libpath)/libkpcap.so -shared -Wall -I$(JAVA_HOME)/include/ -I$(JAVA_HOME)/include/linux/ -lpcap -fPIC
endif

clean:
	rm $(libpath)/libkpcap.so

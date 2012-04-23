CFLAGS = 
uname = $(shell uname -a)
ifneq ($(findstring Darwin, $(uname)),)
  libpath = ../resources/lib/osx_x64/
  CFLAGS+=-I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/darwin
  TARGET=libkpcap.dylib
else ifneq ($(findstring x86_64, $(uname)),)
  libpath = ../resources/lib/linux_x86_64
  CFLAGS += -I$(JAVA_HOME)/include/ -I$(JAVA_HOME)/include/linux/
  TARGET=libkpcap.so
else ifneq ($(findstring i386, $(uname)),)
  libpath = ../resources/lib/linux_x86
  CFLAGS += -I$(JAVA_HOME)/include/ -I$(JAVA_HOME)/include/linux/
  TARGET=libkpcap.so
else ifneq ($(findstring i586, $(uname)),)
  libpath = ../resources/lib/linux_x86
  CFLAGS += -I$(JAVA_HOME)/include/ -I$(JAVA_HOME)/include/linux/
  TARGET=libkpcap.so
else ifneq ($(findstring i686, $(uname)),)
  libpath = ../resources/lib/linux_x86
  CFLAGS += -I$(JAVA_HOME)/include/ -I$(JAVA_HOME)/include/linux/
  TARGET=libkpcap.so
else
$(error Not supported on this architecture)
endif

libkpcap.so:
ifeq ($(JAVA_HOME),)
	$(error JAVA_HOME is not set)
else
	gcc kpcap.c routingtable.c -o $(libpath)/$(TARGET) -shared -Wall $(CFLAGS) -lpcap -fPIC
endif

clean:
	rm $(libpath)/$(TARGET)

#ifndef __TYPES_H_
#define __TYPES_H_

/* type definitions */
typedef unsigned char byte_t;
typedef unsigned short word_t;

#ifdef WIN32

#include <winsock2.h>
#pragma comment(lib, "ws2_32.lib")

typedef SOCKET socket_t;

#endif // WIN32

#endif // __TYPES_H_

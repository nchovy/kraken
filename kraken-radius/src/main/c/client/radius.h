#ifndef __RADIUS_H_
#define __RADIUS_H_

#define IN
#define INOUT
#define OUT

#define RADIUS_AUTH_PORT		1812

/* error codes */
#define ERR_NO_ADDRESS			0x10000001
#define ERR_NO_SHARED_SECRET	0x10000002
#define ERR_INVALID_PORT_RANGE	0x10000003

/* type definitions */
typedef char byte_t;
typedef unsigned short word_t;

#ifdef WIN32

#include <winsock2.h>
#pragma comment(lib, "ws2_32.lib")

typedef SOCKET socket_t;

#endif // WIN32

/* object structures */

#define RADIUS_ATTR_BINARY	1
#define RADIUS_ATTR_STRING	2
#define RADIUS_ATTR_INTEGER	3
#define RADIUS_ATTR_IP		4
#define RADIUS_ATTR_TEXT	5

typedef struct _radius_attr {
	byte_t type;
	byte_t len;
	byte_t data_type;
	byte_t *data;
} radius_attr_t;

typedef struct _radius_attrs {
	radius_attr_t *attr;
	struct _radius_attrs *next;
} radius_attrs_t;

typedef struct _radius_packet {
	byte_t code;
	byte_t id;
	word_t len;
	byte_t *auth;
	radius_attrs_t *attrs;
} radius_packet_t;

typedef struct _radius_client {
	char *addr;
	int port;
	char *shared_secret;
	int next_id;
} radius_client_t;

#endif // __RADIUS_H_

/* function prototypes */
char*	radius_string_clone( char *src );

int		radius_client_new( char *addr, int port, char *shared_secret, OUT radius_client_t **client );
void	radius_client_free( radius_client_t *client );
int		radius_client_papauth( radius_client_t *client, char *username, char *password );

radius_attrs_t*	radius_attrs_new();
void			radius_attrs_free( radius_attrs_t **attrs );
int				radius_attrs_count( radius_attrs_t **attrs );
void			radius_attrs_push_back( radius_attrs_t **attrs, radius_attr_t *attr );
void			radius_attrs_push_front( radius_attrs_t **attrs, radius_attr_t *attr );
radius_attr_t*	radius_attrs_pop( radius_attrs_t **attrs );

radius_attr_t*	radius_attr_new( int type, int len, char *data, int data_type );
radius_attr_t*	radius_attr_new_string( int type, int len, char *data );
radius_attr_t*	radius_attr_new_int( int type, int len, char *data );
radius_attr_t*	radius_attr_new_ip( int type, int len, char *data );
radius_attr_t*	radius_attr_new_text( int type, int len, char *data );
void			radius_attr_free( radius_attr_t *attr );
void			radius_attr_print( radius_attr_t *attr );

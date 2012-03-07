/*
 * Copyright 2011 Future Systems
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#ifndef __RADIUS_H_
#define __RADIUS_H_

#define IN
#define INOUT
#define OUT

#define RADIUS_AUTH_PORT		1812

/* error codes */
#define ERR_NULL_ADDRESS		0xF0000001
#define ERR_NULL_SHARED_SECRET	0xF0000002
#define ERR_NULL_AUTHENTICATOR	0xF0000003
#define ERR_NULL_PASSWORD		0xF0000004
#define ERR_INVALID_PORT_RANGE	0xF0000005
#define ERR_BUFFER_OVERFLOW		0xF0000006
#define ERR_BUFFER_UNDERFLOW	0xF0000007
#define ERR_IP_RESOLVE_ERROR	0xF0000008
#define ERR_MALFORMED_RESPONSE	0xF0000009
#define ERR_TIMEOUT				0xF000000A

/* radius packet codes */
#define RADIUS_ACCESS_REQUEST	1
#define RADIUS_ACCESS_ACCEPT	2
#define RADIUS_ACCESS_REJECT	3

/* attr type codes */
#define RADIUS_USER_NAME		1
#define RADIUS_USER_PASSWORD	2
#define RADIUS_CHAP_PASSWORD	3
#define	RADIUS_NAS_IP_ADDR		4
#define RADIUS_NAS_PORT			5
#define	RADIUS_NAS_ID			32
#define	RADIUS_NAS_PORT_TYPE	61

/* object structures */
#define RADIUS_ATTR_BINARY	1
#define RADIUS_ATTR_STRING	2
#define RADIUS_ATTR_INTEGER	3
#define RADIUS_ATTR_IP		4
#define RADIUS_ATTR_TEXT	5

#include "types.h"

typedef struct _radius_attr {
	byte_t type;
	byte_t len; /* includes metadata length (= data length + 2) */
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
	struct sockaddr_in _addr;
} radius_client_t;

/* function prototypes */
char*	radius_string_clone( char *src );

int		radius_client_new( char *addr, int port, char *shared_secret, OUT radius_client_t **client );
void	radius_client_free( radius_client_t *client );
int		radius_client_next_id( radius_client_t *client );
int		radius_client_papauth( radius_client_t *client, char *username, char *password, OUT radius_packet_t **response );

int		radius_pap_encode_password(byte_t *authenticator, char *shared_secret, char *password, OUT byte **encoded, OUT int *encoded_len);

int		radius_packet_new( byte_t code, byte_t id, byte_t *authenticator, radius_attrs_t *attrs, OUT radius_packet_t **packet );
void	radius_packet_free( radius_packet_t *packet );
byte_t*	radius_packet_gen_auth();
int		radius_packet_serialize( radius_packet_t *packet, OUT byte_t **bytes );
int		radius_packet_parse( char *buf, int buf_len, OUT radius_packet_t **packet );
int		radius_packet_verify_response( radius_packet_t *response, byte_t *request_authenticator, char *shared_secret );

void			radius_attrs_free( radius_attrs_t **attrs );
int				radius_attrs_count( radius_attrs_t **attrs );
void			radius_attrs_push_back( radius_attrs_t **attrs, radius_attr_t *attr );
void			radius_attrs_push_front( radius_attrs_t **attrs, radius_attr_t *attr );
radius_attr_t*	radius_attrs_pop( radius_attrs_t **attrs );
int				radius_attrs_is_empty( radius_attrs_t **attrs );
void			radius_attrs_print( radius_attrs_t **attrs );

radius_attr_t*	radius_attr_new( int type, int len, byte_t *data, int data_type );
radius_attr_t*	radius_attr_new_string( int type, int len, char *data );
radius_attr_t*	radius_attr_new_int( int type, int len, int data );
radius_attr_t*	radius_attr_new_ip( int type, int len, int data );
radius_attr_t*	radius_attr_new_text( int type, int len, char *data );
void			radius_attr_free( radius_attr_t *attr );
void			radius_attr_print( radius_attr_t *attr );
int				radius_attr_serialize( radius_attr_t *attr, byte_t *buf, int offset, int length );

#endif // __RADIUS_H_
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

#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include "radius.h"
#include "md5.h"

/**
 * authenticator and attrs's lifecycles are bound to packet.
 */
int radius_packet_new( byte_t code, byte_t id, byte_t *authenticator, radius_attrs_t *attrs, OUT radius_packet_t **packet )
{
	radius_attrs_t *it = NULL;
	radius_packet_t *p = NULL;

	p = (radius_packet_t*) malloc ( sizeof( radius_packet_t ) );
	memset( p, 0, sizeof( radius_packet_t ) );

	p->code = code;
	p->id = id;
	p->auth = authenticator;
	p->len = 20;
	p->attrs = attrs;

	// add attributes' length
	it = attrs;
	while ( it != NULL )
	{
		p->len += it->attr->len;
		it = it->next;
	}

	*packet = p;
	return 0;
}

void radius_packet_free( radius_packet_t *packet )
{
	if ( packet == NULL )
		return;

	if (packet->attrs != NULL )
		radius_attrs_free( &packet->attrs );

	free( packet->auth );
	free( packet );
}

byte_t* radius_packet_gen_auth()
{
	int i = 0;
	byte_t *auth = NULL;

	auth = (byte_t*) malloc( 16 );

	srand( (unsigned int) time( NULL ) );
	for ( i = 0; i < 16; i++ )
		auth[ i ] = abs( rand() ) % 256;

	return auth;
}

static void serialize_attrs( radius_packet_t *packet, byte_t *buf )
{
	radius_attrs_t *it = NULL;
	radius_attr_t *attr = NULL;
	int offset = 0;

	it = packet->attrs;
	while (it != NULL )
	{
		attr = it->attr;
		radius_attr_serialize( attr, buf, offset, packet->len - offset );
		offset += attr->len;
		it = it->next;
	}
}

static int get_total_attrs_length( radius_packet_t *packet )
{
	int length = 0;
	radius_attrs_t *it = NULL;

	it = packet->attrs;
	while ( it != NULL )
	{
		length += it->attr->len;
		it = it->next;
	}

	return length;
}

int	radius_packet_serialize( radius_packet_t *packet, OUT byte_t **bytes )
{
	int i = 0;
	byte_t *buf = NULL;
	
	// re-calculate total length
	packet->len = 20 + get_total_attrs_length( packet );

	// encode header: 20 octets
	buf = (byte_t*) malloc( packet->len );
	buf[0] = (byte_t) packet->code;
	buf[1] = (byte_t) packet->id;
	buf[2] = (byte_t) (packet->len >> 8);
	buf[3] = (byte_t) (packet->len);
	
	for ( i = 0; i < 16; i++ )
		buf[ 4 + i ] = packet->auth[ i ];

	// encode attrs
	serialize_attrs( packet, buf + 20 );

	*bytes = buf;
	return 0;
}

int radius_packet_parse( char *buf, int buf_len, OUT radius_packet_t **packet )
{
	radius_packet_t *p = NULL;
	radius_attrs_t *attrs = NULL;
	word_t offset = 20;

	// related to header
	int code = buf[ 0 ];
	int id = buf[ 1 ];
	word_t length = 0;
	byte_t *authenticator = NULL;

	// related to attributes
	int attr_type = 0;
	int attr_len = 0;
	byte_t *attr_data = NULL;

	// parse
	memcpy( &length, buf + 2, 2 );
	length = ntohs( length );

	authenticator = (byte_t*) malloc ( 16 );
	memcpy( authenticator, buf + 4, 16 );

	while ( offset < length )
	{
		attr_type = buf[ offset ];
		attr_len = buf[ offset + 1 ];
		attr_data = (byte_t*) malloc( attr_len );

		memcpy( attr_data, buf + offset + 2, attr_len - 2 );
		radius_attrs_push_back( &attrs, radius_attr_new( attr_type, attr_len, attr_data, RADIUS_ATTR_BINARY ) ); 

		offset += attr_len;
	}
	
	radius_packet_new( code, id, authenticator, attrs, &p );

	*packet = p;
	return 0;
}

int radius_packet_verify_response( radius_packet_t *response, byte_t *request_authenticator, char *shared_secret )
{
	MD5_CTX md5;
	byte_t digest[16];
	byte_t header[20];
	byte_t *encoded_attrs = NULL;
	int encoded_len = 0;
	int i = 0;

	header[0] = response->code;
	header[1] = response->id;
	header[2] = (byte_t) (response->len >> 8);
	header[3] = (byte_t) (response->len);
	memcpy( header + 4, request_authenticator, 16 );

	encoded_len = get_total_attrs_length( response );
	encoded_attrs = (byte_t*) malloc( encoded_len );
	serialize_attrs( response, encoded_attrs );

	// md5 sum
	MD5Init( &md5 );
	MD5Update( &md5, header, 20 );
	MD5Update( &md5, encoded_attrs, encoded_len );
	MD5Update( &md5, (byte_t*) shared_secret, strlen( shared_secret ) );
	MD5Final( digest, &md5 );

	free( encoded_attrs );

	// compare
	for ( i = 0; i < 16; i++)
		if ( digest[ i ] != response->auth[ i ] )
			return ERR_MALFORMED_RESPONSE;

	return 0;
}
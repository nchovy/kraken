#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include "radius.h"

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

	free ( packet->auth );

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

int	radius_packet_serialize( radius_packet_t *packet, OUT byte_t **bytes )
{
	radius_attrs_t *it;
	radius_attr_t *attr;
	int i = 0;
	int offset = 20;
	byte_t *buf = NULL;

	// re-calculate total length
	packet->len = 20;
	it = packet->attrs;
	while ( it != NULL )
	{
		packet->len += it->attr->len;
		it = it->next;
	}

	// encode header: 20 octets
	buf = (byte_t*) malloc( packet->len );
	buf[0] = (byte_t) packet->code;
	buf[1] = (byte_t) packet->id;
	buf[2] = (byte_t) (packet->len >> 8);
	buf[3] = (byte_t) (packet->len);
	
	for ( i = 0; i < 16; i++ )
		buf[ 4 + i ] = packet->auth[ i ];

	// encode attrs
	it = packet->attrs;
	while (it != NULL )
	{
		attr = it->attr;
		radius_attr_serialize( attr, buf, offset, packet->len - offset );
		offset += attr->len;
		it = it->next;
	}

	*bytes = buf;
	return 0;
}

int radius_packet_parse( char *buf, int buf_len, radius_packet_t **packet )
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
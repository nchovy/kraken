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
#include "radius.h"

/**
 * 'data' lifecycle is bound to radius_attr_t
 */
radius_attr_t* radius_attr_new( int type, int len, byte_t *data, int data_type )
{
	radius_attr_t *attr = NULL;

	attr = (radius_attr_t*) malloc( sizeof( radius_attr_t ) );
	memset( attr, 0, sizeof( radius_attr_t ) );

	attr->type = type;
	attr->len = len;
	attr->data = data;
	attr->data_type = data_type;

	return attr;
}

radius_attr_t* radius_attr_new_string( int type, int len, char *data )
{
	return radius_attr_new( type, len, (byte_t*) data, RADIUS_ATTR_STRING );
}

radius_attr_t* radius_attr_new_int( int type, int len, int data )
{
	return radius_attr_new( type, len, (byte_t*) data, RADIUS_ATTR_INTEGER );
}

radius_attr_t* radius_attr_new_ip( int type, int len, int data )
{
	return radius_attr_new( type, len, (byte_t*) data, RADIUS_ATTR_IP );
}

radius_attr_t* radius_attr_new_text( int type, int len, char *data )
{
	return radius_attr_new( type, len, (byte_t*) data, RADIUS_ATTR_TEXT );
}

void radius_attr_free( radius_attr_t *attr )
{
	if ( attr->data != NULL &&
		( attr->data_type != RADIUS_ATTR_INTEGER && attr->data_type != RADIUS_ATTR_IP ))
		free( attr->data );

	free( attr );
}

void radius_attr_print( radius_attr_t *attr )
{
	switch ( attr->data_type ) 
	{
	case RADIUS_ATTR_BINARY:
		printf("(t=%d, l=%d, data=binary)", attr->type, attr->len );
		break;
	case RADIUS_ATTR_STRING:
		printf("(t=%d, l=%d, data=%s)", attr->type, attr->len, attr->data );
		break;
	case RADIUS_ATTR_INTEGER:
		printf("(t=%d, l=%d, data=%d)", attr->type, attr->len, attr->data );
		break;
	case RADIUS_ATTR_IP:
		printf("(t=%d, l=%d, data=0x%x)", attr->type, attr->len, attr->data );
		break;
	case RADIUS_ATTR_TEXT:
		printf("(t=%d, l=%d, data=%s)", attr->type, attr->len, attr->data );
		break;
	}
}

int radius_attr_serialize( radius_attr_t *attr, byte_t *buf, int offset, int length )
{
	int i = 0;
	int d;
	byte_t *b;

	if ( attr->len > length )
		return ERR_BUFFER_OVERFLOW;

	buf[ offset ] = attr->type;
	buf[ offset + 1 ] = attr->len;
	
	if (attr->data_type == RADIUS_ATTR_INTEGER || attr->data_type == RADIUS_ATTR_IP )
	{
		d = htonl( (int) attr->data );
		b = (byte_t*) &d;
	}
	else 
		b = attr->data;

	for ( i = 0; i < attr->len - 2; i++ )
		buf[ offset + 2 + i ] = b[ i ];

	return 0;
}
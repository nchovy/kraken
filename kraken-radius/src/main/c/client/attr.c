#include <stdio.h>
#include "radius.h"

/**
 * 'data' lifecycle is bound to radius_attr_t
 */
radius_attr_t* radius_attr_new( int type, int len, char *data, int data_type )
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
	return radius_attr_new( type, len, data, RADIUS_ATTR_STRING );
}

radius_attr_t* radius_attr_new_int( int type, int len, char *data )
{
	return radius_attr_new( type, len, data, RADIUS_ATTR_INTEGER );
}

radius_attr_t* radius_attr_new_ip( int type, int len, char *data )
{
	return radius_attr_new( type, len, data, RADIUS_ATTR_IP );
}

radius_attr_t* radius_attr_new_text( int type, int len, char *data )
{
	return radius_attr_new( type, len, data, RADIUS_ATTR_TEXT );
}

void radius_attr_free( radius_attr_t *attr )
{
	if ( attr->data != NULL )
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
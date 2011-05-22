#include <stdio.h>
#include <stdlib.h>
#include "radius.h"

radius_packet_t* new_radius_packet( byte_t code, byte_t id, radius_attrs_t attr_list )
{
	radius_packet_t *packet = NULL;

	packet = (radius_packet_t*) malloc ( sizeof( radius_packet_t ) );

	return packet;
}

void free_radius_packet( radius_packet_t *packet )
{
	if ( packet != NULL )
		free( packet );
}


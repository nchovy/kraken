#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "radius.h"

char* radius_string_clone( char *src )
{
	char *dst = NULL;
	int len = strlen( src );

	dst = (char *) malloc ( len + 1 );
	memset( dst, 0, len + 1 );
	strncpy( dst, src, len );

	return dst;
}

int radius_client_new( char *addr, int port, char *shared_secret, OUT radius_client_t **client )
{
	radius_client_t *c = NULL;
	
	if (addr == NULL)
		return ERR_NO_ADDRESS;

	if (shared_secret == NULL)
		return ERR_NO_SHARED_SECRET;

	c = (radius_client_t*) malloc ( sizeof( radius_client_t ) );
	memset(client, 0, sizeof( radius_client_t ) );

	c->addr = radius_string_clone( addr );
	c->port = port;
	c->shared_secret = radius_string_clone( shared_secret );
	c->next_id = 1;

	*client = c;

	return 0;
}

void radius_client_free( radius_client_t *client )
{
	if ( client == NULL )
		return;

	free( client->addr );
	free( client->shared_secret );
	free( client );
}

int radius_client_papauth( radius_client_t *client, char *username, char *password )
{
	int ret;
	socket_t sock;

	sock = socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP);

#ifdef WIN32
	closesocket( sock );
#else
	close( sock );
#endif // WIN32

	return 0;
}

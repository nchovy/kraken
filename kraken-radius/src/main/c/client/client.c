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
	struct hostent *h;
	radius_client_t *c = NULL;
	
	if (addr == NULL)
		return ERR_NULL_ADDRESS;

	if (shared_secret == NULL)
		return ERR_NULL_SHARED_SECRET;

	c = (radius_client_t*) malloc ( sizeof( radius_client_t ) );
	memset(c, 0, sizeof( radius_client_t ) );
	
	c->addr = radius_string_clone( addr );
	c->port = port;
	c->shared_secret = radius_string_clone( shared_secret );
	c->next_id = 1;

	// resolve ip
	h = gethostbyname( c->addr );
	if (h == NULL )
		return ERR_IP_RESOLVE_ERROR;

	memset( &c->_addr, 0, sizeof( addr ) );
	memcpy( &c->_addr.sin_addr, h->h_addr_list[0], h->h_length );
	c->_addr.sin_family = PF_INET;
	c->_addr.sin_port = htons( c->port );

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

int radius_client_next_id( radius_client_t *client )
{
	int next = client->next_id;
	client->next_id++;
	return next;
}

static int send_pap_access_request( radius_client_t *client, socket_t sock, char *username, char *password, OUT radius_packet_t **request )
{
	int id;
	radius_packet_t *packet = NULL;
	radius_attrs_t *attrs = NULL;
	byte_t *auth = NULL;
	byte_t *packet_bytes = NULL;
	byte_t *enc_password = NULL;
	int enc_len = 0;

	// build pap auth packet
	id = radius_client_next_id( client );
	auth = radius_packet_gen_auth();

	radius_pap_encode_password( auth, client->shared_secret, password, &enc_password, &enc_len );

	radius_attrs_push_back( &attrs, radius_attr_new_text( RADIUS_USER_NAME, 2 + strlen(username), radius_string_clone( username ) ) );
	radius_attrs_push_back( &attrs, radius_attr_new( RADIUS_USER_PASSWORD, 2 + enc_len, enc_password, RADIUS_ATTR_BINARY ) );
	radius_attrs_push_back( &attrs, radius_attr_new_ip( RADIUS_NAS_IP_ADDR, 6, 0x7f000001 ) );
	radius_attrs_push_back( &attrs, radius_attr_new_int( RADIUS_NAS_PORT, 6, 0 ) );

	radius_packet_new( RADIUS_ACCESS_REQUEST, id, auth, attrs, &packet );
	radius_packet_serialize( packet, &packet_bytes );

	// send packet
	sendto( sock, (const char*) packet_bytes, packet->len, 0, (struct sockaddr *) &client->_addr, sizeof( client->_addr ) );

	free( packet_bytes );

	*request = packet;
	return 0;
}

int recv_pap_auth_result( radius_client_t *client, socket_t sock, OUT radius_packet_t **response )
{
	char buf[4096];
	struct sockaddr_in remote_addr;
	int addr_len;
	int read_bytes;

	addr_len = sizeof( remote_addr );

	// TODO: tx id check
	read_bytes = recvfrom( sock, buf, sizeof( buf ), 0, (struct sockaddr *) &remote_addr, &addr_len );

	// timeout
	if ( read_bytes < 0 )
		return ERR_TIMEOUT;

	radius_packet_parse( buf, read_bytes, response );
	return 0;
}

int radius_client_papauth( radius_client_t *client, char *username, char *password, OUT radius_packet_t **response )
{
	int ret = 0;
	socket_t sock;
	radius_packet_t *request = NULL;
#ifdef _WIN32
	dword_t timeout = 3000;
#else
	struct timeval timeout;
	timeout.tv_sec = 10;
	timeout.tv_usec = 0;
#endif

	// open socket
	sock = socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP);
#ifdef _WIN32
	setsockopt( sock, SOL_SOCKET, SO_RCVTIMEO, (const char*) &timeout, sizeof( dword_t ) );
#else
	setsockopt( sock, SOL_SOCKET, SO_RCVTIMEO, (const char*) &timeout, sizeof( struct timeval ) );
#endif

	ret = connect( sock, (struct sockaddr*) &client->_addr, sizeof( client->_addr ) );

	send_pap_access_request( client, sock, username, password, &request );
	ret = recv_pap_auth_result( client, sock, response );
	if ( ret < 0 )
		goto cleanup;

	ret = radius_packet_verify_response( *response, request->auth, client->shared_secret );

cleanup:
	radius_packet_free( request );

	// close socket
#ifdef WIN32
	closesocket( sock );
#else
	close( sock );
#endif // WIN32

	return ret;
}

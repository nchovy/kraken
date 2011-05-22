#include <stdio.h>
#include <stdlib.h>
#include "radius.h"
/*
int main(int argc, char *argv[]) 
{
	int ret = 0;
	radius_client_t *client;
	
#ifdef WIN32
	WSADATA wsa;
	ret = WSAStartup(MAKEWORD(2, 2), &wsa);
	if (ret != NO_ERROR)
	{
		wprintf(L"initialize error");
		return 1;
	}
#endif // WIN32

	ret = radius_client_new( "localhost", RADIUS_AUTH_PORT, "10testing", &client );
	if ( !ret )
	{
		wprintf (L"qoo~~");
		return 1;
	}

	ret = radius_client_papauth( client, "xeraph", "qooguevara" );
	wprintf( L"result: %x", ret );

	radius_client_free( client );
	
	return 0;
}
*/
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

#include "radius.h"
#include "md5.h"

static void expand( char *password, byte_t **expanded, int *expanded_len ) 
{
	int i = 0;
	int password_len = 0;
	if ( password == NULL )
		return;

	password_len = strlen( password );
	*expanded_len = ( ( ( password_len - 1 ) / 16) + 1 ) * 16;

	*expanded = (byte_t*) malloc( *expanded_len );
	
	for ( i = 0; i < *expanded_len; i++)
	{
		if ( i < password_len )
			(*expanded)[ i ] = password[ i ];
		else
			(*expanded)[ i ] = 0;
	}
}

/**
 * request authenticator = 16 octets
 */
int radius_pap_encode_password(byte_t *authenticator, char *shared_secret, char *password, 
	OUT byte **encoded, OUT int *encoded_len)
{
	byte_t *expanded = NULL;
	byte_t *result = NULL;
	byte_t *seed = NULL;
	byte_t *c = NULL;
	int expanded_len = 0;
	int folds = 0;
	int i = 0;
	int j = 0;

	// md5 vars
	MD5_CTX md5;
	byte_t digest[16];

	if ( authenticator == NULL )
		return ERR_NULL_AUTHENTICATOR;

	if ( shared_secret == NULL )
		return ERR_NULL_SHARED_SECRET;

	if ( password == NULL )
		return ERR_NULL_PASSWORD;

	expand( password, &expanded, &expanded_len );

	result = (byte_t*) malloc( expanded_len );
	memset( result, 0, expanded_len );
	folds = expanded_len / 16;
	seed = authenticator;
	c = (byte_t*) malloc( 16 );

	for ( i = 0; i < folds; i++ )
	{
		// b[n] = md5
		MD5Init( &md5 );
		MD5Update( &md5, (unsigned char*) shared_secret, strlen( shared_secret ) ); 
		MD5Update( &md5, seed, 16 );
		MD5Final( digest, &md5 );

		// c[n] = xor
		for ( j = 0; j < 16; j++ )
			c[j] = expanded[ i * 16 + j ] ^ digest[ j ];

		// copy result
		for ( j = 0; j < 16; j++ )
			result[ i * 16 + j ] = c[ j ];

		seed = c;
	}
	
	free( c );
	free( expanded );

	*encoded = result;
	*encoded_len = expanded_len;
	return 0;
}

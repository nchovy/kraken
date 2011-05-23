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

void radius_attrs_free( radius_attrs_t **attrs )
{
	radius_attr_t *attr;

	if ( *attrs == NULL )
		return;

	while ( !radius_attrs_is_empty( attrs ) )
	{
		attr = radius_attrs_pop( attrs );
		radius_attr_free( attr );
	}

	*attrs = NULL;
}

int radius_attrs_is_empty ( radius_attrs_t **attrs )
{
	return (*attrs) == NULL;
}

int radius_attrs_count( radius_attrs_t **attrs )
{
	radius_attrs_t *it = *attrs;
	radius_attr_t *attr = NULL;
	int count = 0;

	while ( 1 )
	{
		if ( it == NULL )
			break;

		count++;
		it = it->next;
	}

	return count;
}

void radius_attrs_print( radius_attrs_t **attrs )
{
	radius_attrs_t *it = *attrs;

	while ( 1 )
	{
		if ( it == NULL )
			break;

		radius_attr_print( it->attr );
		printf("\n");
		it = it->next;
	}
}

void radius_attrs_push_back( radius_attrs_t **attrs, radius_attr_t *attr )
{
	radius_attrs_t *last;
	radius_attrs_t *node;

	node = (radius_attrs_t*) malloc( sizeof( radius_attrs_t ) );
	memset( node, 0, sizeof( radius_attrs_t ) );
	node->attr = attr;

	if ( *attrs == NULL) 
	{
		*attrs = node;
		return;
	}

	last = *attrs;
	while ( last->next != NULL )
		last = last->next;

	last->next = node;
}

void radius_attrs_push_front( radius_attrs_t **attrs, radius_attr_t *attr )
{
	radius_attrs_t *node;

	node = (radius_attrs_t*) malloc( sizeof( radius_attrs_t ) );
	memset( node, 0, sizeof( radius_attrs_t ) );
	node->attr = attr;

	if ( *attrs == NULL ) 
	{
		*attrs = node;
	}
	else
	{
		node->next = *attrs;
		*attrs = node;
	}
}

radius_attr_t* radius_attrs_pop( radius_attrs_t **attrs )
{
	radius_attrs_t *it;
	radius_attr_t *attr;

	if ( *attrs == NULL )
		return NULL;

	it = *attrs;
	attr = it->attr;
	*attrs = it->next;

	free( it );

	return attr;
}

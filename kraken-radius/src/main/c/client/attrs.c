#include <stdio.h>
#include "radius.h"

radius_attrs_t *radius_attrs_new()
{
	radius_attrs_t *attrs = NULL;

	attrs = (radius_attrs_t *) malloc ( sizeof( radius_attrs_t ) );
	memset( attrs, 0, sizeof( radius_attrs_t ) );
	return attrs;
}

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
	radius_attr_t *attr;

	if ( *attrs == NULL )
		return NULL;

	attr = (*attrs)->attr;
	(*attrs) = (*attrs)->next;
	return attr;
}

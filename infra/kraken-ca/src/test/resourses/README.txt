if you export authority, is the same as the format below

{
    "metadata": {
        "version": 1,
        "date": "2013-01-07 11:49:56+0900"
    },
    "authority": {
        "certs": [
            {
                "subject_dn": "CN=0",
                "binary": "dGVzdF9ieXRl",
                "not_before": "2012-01-01 10:10:10+0900",
                "issued_date": "2013-01-01 10:10:10+0900",
                "type": "JKS",
                "serial": "0",
                "not_after": "2014-01-01 10:10:10+0900"
            },
            {
                "subject_dn": "CN=1",
                "binary": "dGVzdF9ieXRl",
                "not_before": "2012-01-01 10:10:10+0900",
                "issued_date": "2013-01-01 10:10:10+0900",
                "type": "JKS",
                "serial": "1",
                "not_after": "2014-01-01 10:10:10+0900"
            },
            {
                "subject_dn": "CN=2",
                "binary": "dGVzdF9ieXRl",
                "not_before": "2012-01-01 10:10:10+0900",
                "issued_date": "2013-01-01 10:10:10+0900",
                "type": "JKS",
                "serial": "2",
                "not_after": "2014-01-01 10:10:10+0900"
            },
            {
                "subject_dn": "CN=3",
                "binary": "dGVzdF9ieXRl",
                "not_before": "2012-01-01 10:10:10+0900",
                "issued_date": "2013-01-01 10:10:10+0900",
                "type": "JKS",
                "serial": "3",
                "not_after": "2014-01-01 10:10:10+0900"
            },
            {
                "subject_dn": "CN=4",
                "binary": "dGVzdF9ieXRl",
                "not_before": "2012-01-01 10:10:10+0900",
                "issued_date": "2013-01-01 10:10:10+0900",
                "type": "JKS",
                "serial": "4",
                "not_after": "2014-01-01 10:10:10+0900"
            },
            {
                "subject_dn": "CN=5",
                "binary": "dGVzdF9ieXRl",
                "not_before": "2012-01-01 10:10:10+0900",
                "issued_date": "2013-01-01 10:10:10+0900",
                "type": "JKS",
                "serial": "5",
                "not_after": "2014-01-01 10:10:10+0900"
            },
            {
                "subject_dn": "CN=6",
                "binary": "dGVzdF9ieXRl",
                "not_before": "2012-01-01 10:10:10+0900",
                "issued_date": "2013-01-01 10:10:10+0900",
                "type": "JKS",
                "serial": "6",
                "not_after": "2014-01-01 10:10:10+0900"
            },
            {
                "subject_dn": "CN=7",
                "binary": "dGVzdF9ieXRl",
                "not_before": "2012-01-01 10:10:10+0900",
                "issued_date": "2013-01-01 10:10:10+0900",
                "type": "JKS",
                "serial": "7",
                "not_after": "2014-01-01 10:10:10+0900"
            },
            {
                "subject_dn": "CN=8",
                "binary": "dGVzdF9ieXRl",
                "not_before": "2012-01-01 10:10:10+0900",
                "issued_date": "2013-01-01 10:10:10+0900",
                "type": "JKS",
                "serial": "8",
                "not_after": "2014-01-01 10:10:10+0900"
            },
            {
                "subject_dn": "CN=9",
                "binary": "dGVzdF9ieXRl",
                "not_before": "2012-01-01 10:10:10+0900",
                "issued_date": "2013-01-01 10:10:10+0900",
                "type": "JKS",
                "serial": "9",
                "not_after": "2014-01-01 10:10:10+0900"
            }
        ],
        "root_certificate": {
            "subject_dn": "CN=kraken",
            "binary": "dGVzdF9ieXRl",
            "not_before": "2012-01-01 10:10:10+0900",
            "key_password": "kraken",
            "issued_date": "2013-01-01 10:10:10+0900",
            "type": "JKS",
            "serial": "2",
            "not_after": "2014-01-01 10:10:10+0900"
        },
        "name": "local",
        "crl_base_url": "http://localhost",
        "last_serial": 2,
        "revoked": [
            {
                "reason": "Unspecified",
                "date": "2013-01-07 11:49:56+0900",
                "serial": "0"
            },
            {
                "reason": "KeyCompromise",
                "date": "2013-01-07 11:49:56+0900",
                "serial": "1"
            },
            {
                "reason": "CaCompromise",
                "date": "2013-01-07 11:49:56+0900",
                "serial": "2"
            },
            {
                "reason": "AffiliationChanged",
                "date": "2013-01-07 11:49:56+0900",
                "serial": "3"
            },
            {
                "reason": "Superseded",
                "date": "2013-01-07 11:49:56+0900",
                "serial": "4"
            },
            {
                "reason": "CessationOfOperation",
                "date": "2013-01-07 11:49:56+0900",
                "serial": "5"
            },
            {
                "reason": "CertificateHold",
                "date": "2013-01-07 11:49:56+0900",
                "serial": "6"
            },
            {
                "reason": "RemoveFromCrl",
                "date": "2013-01-07 11:49:56+0900",
                "serial": "7"
            },
            {
                "reason": "PrivilegeWithdrawn",
                "date": "2013-01-07 11:49:56+0900",
                "serial": "8"
            },
            {
                "reason": "AaCompromise",
                "date": "2013-01-07 11:49:56+0900",
                "serial": "9"
            }
        ]
    }
}

and if you import the above format, convert as the format below

{
    "metadata": {
        "date": "2013-01-07 13:31:19+0900",
        "version": 1
    },
    "collections": {
        "certs": [
            "list",
            [
                [
                    "map",
                    {
                        "subject_dn": [
                            "string",
                            "CN=0"
                        ],
                        "binary": [
                            "blob",
                            "dGVzdF9ieXRl"
                        ],
                        "not_before": [
                            "date",
                            "2012-01-01 10:10:10+0900"
                        ],
                        "issued_date": [
                            "date",
                            "2013-01-01 10:10:10+0900"
                        ],
                        "type": [
                            "string",
                            "JKS"
                        ],
                        "serial": [
                            "string",
                            "0"
                        ],
                        "not_after": [
                            "date",
                            "2014-01-01 10:10:10+0900"
                        ]
                    }
                ],
                [
                    "map",
                    {
                        "subject_dn": [
                            "string",
                            "CN=1"
                        ],
                        "binary": [
                            "blob",
                            "dGVzdF9ieXRl"
                        ],
                        "not_before": [
                            "date",
                            "2012-01-01 10:10:10+0900"
                        ],
                        "issued_date": [
                            "date",
                            "2013-01-01 10:10:10+0900"
                        ],
                        "type": [
                            "string",
                            "JKS"
                        ],
                        "serial": [
                            "string",
                            "1"
                        ],
                        "not_after": [
                            "date",
                            "2014-01-01 10:10:10+0900"
                        ]
                    }
                ],
                [
                    "map",
                    {
                        "subject_dn": [
                            "string",
                            "CN=2"
                        ],
                        "binary": [
                            "blob",
                            "dGVzdF9ieXRl"
                        ],
                        "not_before": [
                            "date",
                            "2012-01-01 10:10:10+0900"
                        ],
                        "issued_date": [
                            "date",
                            "2013-01-01 10:10:10+0900"
                        ],
                        "type": [
                            "string",
                            "JKS"
                        ],
                        "serial": [
                            "string",
                            "2"
                        ],
                        "not_after": [
                            "date",
                            "2014-01-01 10:10:10+0900"
                        ]
                    }
                ],
                [
                    "map",
                    {
                        "subject_dn": [
                            "string",
                            "CN=3"
                        ],
                        "binary": [
                            "blob",
                            "dGVzdF9ieXRl"
                        ],
                        "not_before": [
                            "date",
                            "2012-01-01 10:10:10+0900"
                        ],
                        "issued_date": [
                            "date",
                            "2013-01-01 10:10:10+0900"
                        ],
                        "type": [
                            "string",
                            "JKS"
                        ],
                        "serial": [
                            "string",
                            "3"
                        ],
                        "not_after": [
                            "date",
                            "2014-01-01 10:10:10+0900"
                        ]
                    }
                ],
                [
                    "map",
                    {
                        "subject_dn": [
                            "string",
                            "CN=4"
                        ],
                        "binary": [
                            "blob",
                            "dGVzdF9ieXRl"
                        ],
                        "not_before": [
                            "date",
                            "2012-01-01 10:10:10+0900"
                        ],
                        "issued_date": [
                            "date",
                            "2013-01-01 10:10:10+0900"
                        ],
                        "type": [
                            "string",
                            "JKS"
                        ],
                        "serial": [
                            "string",
                            "4"
                        ],
                        "not_after": [
                            "date",
                            "2014-01-01 10:10:10+0900"
                        ]
                    }
                ],
                [
                    "map",
                    {
                        "subject_dn": [
                            "string",
                            "CN=5"
                        ],
                        "binary": [
                            "blob",
                            "dGVzdF9ieXRl"
                        ],
                        "not_before": [
                            "date",
                            "2012-01-01 10:10:10+0900"
                        ],
                        "issued_date": [
                            "date",
                            "2013-01-01 10:10:10+0900"
                        ],
                        "type": [
                            "string",
                            "JKS"
                        ],
                        "serial": [
                            "string",
                            "5"
                        ],
                        "not_after": [
                            "date",
                            "2014-01-01 10:10:10+0900"
                        ]
                    }
                ],
                [
                    "map",
                    {
                        "subject_dn": [
                            "string",
                            "CN=6"
                        ],
                        "binary": [
                            "blob",
                            "dGVzdF9ieXRl"
                        ],
                        "not_before": [
                            "date",
                            "2012-01-01 10:10:10+0900"
                        ],
                        "issued_date": [
                            "date",
                            "2013-01-01 10:10:10+0900"
                        ],
                        "type": [
                            "string",
                            "JKS"
                        ],
                        "serial": [
                            "string",
                            "6"
                        ],
                        "not_after": [
                            "date",
                            "2014-01-01 10:10:10+0900"
                        ]
                    }
                ],
                [
                    "map",
                    {
                        "subject_dn": [
                            "string",
                            "CN=7"
                        ],
                        "binary": [
                            "blob",
                            "dGVzdF9ieXRl"
                        ],
                        "not_before": [
                            "date",
                            "2012-01-01 10:10:10+0900"
                        ],
                        "issued_date": [
                            "date",
                            "2013-01-01 10:10:10+0900"
                        ],
                        "type": [
                            "string",
                            "JKS"
                        ],
                        "serial": [
                            "string",
                            "7"
                        ],
                        "not_after": [
                            "date",
                            "2014-01-01 10:10:10+0900"
                        ]
                    }
                ],
                [
                    "map",
                    {
                        "subject_dn": [
                            "string",
                            "CN=8"
                        ],
                        "binary": [
                            "blob",
                            "dGVzdF9ieXRl"
                        ],
                        "not_before": [
                            "date",
                            "2012-01-01 10:10:10+0900"
                        ],
                        "issued_date": [
                            "date",
                            "2013-01-01 10:10:10+0900"
                        ],
                        "type": [
                            "string",
                            "JKS"
                        ],
                        "serial": [
                            "string",
                            "8"
                        ],
                        "not_after": [
                            "date",
                            "2014-01-01 10:10:10+0900"
                        ]
                    }
                ],
                [
                    "map",
                    {
                        "subject_dn": [
                            "string",
                            "CN=9"
                        ],
                        "binary": [
                            "blob",
                            "dGVzdF9ieXRl"
                        ],
                        "not_before": [
                            "date",
                            "2012-01-01 10:10:10+0900"
                        ],
                        "issued_date": [
                            "date",
                            "2013-01-01 10:10:10+0900"
                        ],
                        "type": [
                            "string",
                            "JKS"
                        ],
                        "serial": [
                            "string",
                            "9"
                        ],
                        "not_after": [
                            "date",
                            "2014-01-01 10:10:10+0900"
                        ]
                    }
                ]
            ]
        ],
        "revoked": [
            "list",
            [
                [
                    "map",
                    {
                        "reason": [
                            "string",
                            "Unspecified"
                        ],
                        "date": [
                            "date",
                            "2013-01-07 13:31:19+0900"
                        ],
                        "serial": [
                            "string",
                            "0"
                        ]
                    }
                ],
                [
                    "map",
                    {
                        "reason": [
                            "string",
                            "KeyCompromise"
                        ],
                        "date": [
                            "date",
                            "2013-01-07 13:31:19+0900"
                        ],
                        "serial": [
                            "string",
                            "1"
                        ]
                    }
                ],
                [
                    "map",
                    {
                        "reason": [
                            "string",
                            "CaCompromise"
                        ],
                        "date": [
                            "date",
                            "2013-01-07 13:31:19+0900"
                        ],
                        "serial": [
                            "string",
                            "2"
                        ]
                    }
                ],
                [
                    "map",
                    {
                        "reason": [
                            "string",
                            "AffiliationChanged"
                        ],
                        "date": [
                            "date",
                            "2013-01-07 13:31:19+0900"
                        ],
                        "serial": [
                            "string",
                            "3"
                        ]
                    }
                ],
                [
                    "map",
                    {
                        "reason": [
                            "string",
                            "Superseded"
                        ],
                        "date": [
                            "date",
                            "2013-01-07 13:31:19+0900"
                        ],
                        "serial": [
                            "string",
                            "4"
                        ]
                    }
                ],
                [
                    "map",
                    {
                        "reason": [
                            "string",
                            "CessationOfOperation"
                        ],
                        "date": [
                            "date",
                            "2013-01-07 13:31:19+0900"
                        ],
                        "serial": [
                            "string",
                            "5"
                        ]
                    }
                ],
                [
                    "map",
                    {
                        "reason": [
                            "string",
                            "CertificateHold"
                        ],
                        "date": [
                            "date",
                            "2013-01-07 13:31:19+0900"
                        ],
                        "serial": [
                            "string",
                            "6"
                        ]
                    }
                ],
                [
                    "map",
                    {
                        "reason": [
                            "string",
                            "RemoveFromCrl"
                        ],
                        "date": [
                            "date",
                            "2013-01-07 13:31:19+0900"
                        ],
                        "serial": [
                            "string",
                            "7"
                        ]
                    }
                ],
                [
                    "map",
                    {
                        "reason": [
                            "string",
                            "PrivilegeWithdrawn"
                        ],
                        "date": [
                            "date",
                            "2013-01-07 13:31:19+0900"
                        ],
                        "serial": [
                            "string",
                            "8"
                        ]
                    }
                ],
                [
                    "map",
                    {
                        "reason": [
                            "string",
                            "AaCompromise"
                        ],
                        "date": [
                            "date",
                            "2013-01-07 13:31:19+0900"
                        ],
                        "serial": [
                            "string",
                            "9"
                        ]
                    }
                ]
            ]
        ],
        "metadata": [
            "list",
            [
                [
                    "map",
                    {
                        "type": [
                            "string",
                            "rootpw"
                        ],
                        "password": [
                            "string",
                            "kraken"
                        ]
                    }
                ],
                [
                    "map",
                    {
                        "type": [
                            "string",
                            "crl"
                        ],
                        "base_url": [
                            "string",
                            "http://localhost"
                        ]
                    }
                ],
                [
                    "map",
                    {
                        "type": [
                            "string",
                            "serial"
                        ],
                        "serial": [
                            "string",
                            "2"
                        ]
                    }
                ],
                [
                    "map",
                    {
                        "subject_dn": [
                            "string",
                            "CN=kraken"
                        ],
                        "not_before": [
                            "date",
                            "2012-01-01 10:10:10+0900"
                        ],
                        "binary": [
                            "blob",
                            "dGVzdF9ieXRl"
                        ],
                        "issued_date": [
                            "date",
                            "2013-01-01 10:10:10+0900"
                        ],
                        "type": [
                            "string",
                            "JKS"
                        ],
                        "serial": [
                            "string",
                            "2"
                        ],
                        "not_after": [
                            "date",
                            "2014-01-01 10:10:10+0900"
                        ]
                    }
                ]
            ]
        ]
    }
}
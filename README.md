# NITRO API JSON schema from NITRO Java Client

[NITRO](https://docs.citrix.com/en-us/netscaler/11/nitro-api/nitro-rest/nitro-rest-general.html) is a REST API to program the Citrix NetScaler load balancer. While there are Java and Python clients, it is somewhat difficult to write new clients since the JSON payloads are not well documented. This project reverse engineers the JSON schema from the Nitro Java SDK, then enables a roundtrip to Golang structs. (Golang SDK is currently work-in-progress [here](https://github.com/chiradeep/go-nitro) )

## This repo is stale
There is no official support for generating jsonschema from NITRO. The most up to date schema is usually available at https://github.com/citrix/adc-nitro-go

## Pre-requisites
* JDK 7+
* Maven

## Usage
First, we have to install a custom JSON schema generator forked from [JJSchema](https://github.com/reinert/JJSchema).


```
CURRWD=$PWD
## install jjschema-1.1-SNAPSHOT.jar in ~/.m2/
git clone https://github.com/chiradeep/JJSchema.git
cd JJSchema
mvn install

##go back 
cd $CURRWD

## build our SchemaGenerator
mvn package

## Run
mvn exec:exec

## json is now in $CURRWD/json

```

NOTE: This uses the NITRO jar available on [Sonatype maven repository](http://repo1.maven.org/maven2/com/citrix/netscaler/nitro/nitro/10.1/) (may not be the latest)

## JSON schema to Golang
You can use the generated schema to generate Golang structs:


```
./generate.sh $GOPATH/github.com/chiradeep/go-nitro/netscaler
```

Thanks to [Generate](https://github.com/a-h/generate) from [Adrian](http://adrianhesketh.com). A Go client to NetScaler that uses this generated code is here: [https://github.com/chiradeep/go-nitro](https://github.com/chiradeep/go-nitro)

## Gory Details
The fork from [JJSchema](https://github.com/reinert/JJSchema) was necessary since the NITRO Java SDK followed a different convention for getters: instead of `getFoo()`, it uses `get_foo()`. Another useful hack was to figure out which elements in the JSON were read-only (those without a corresponding `set_` method). Last but not least, the Java code provides enum-like inner classes for various fields. For example, a `persistencetype` field in `lbvserver` has a corresponding `persistencetypeEnum` inner class. The inner class has constants for the possible values. This fork from JJSchema uses this to determine the `enums` in the JSON schema.

The Golang generator is vanilla from  [Generate](https://github.com/a-h/generate). Note that it doesn't take advantage of the `enum` or `readonly` fields generated in the schema. One way to do this would be to add to the `json` tag: instead of ``json:"fieldname"``, it could be ``json:"fieldname",readonly:"true",enum:"foo,bar,ugly"``

Sample Schema:

```

{
  "type" : "object",
  "properties" : {
    "ipaddress" : {
      "type" : "string"
    },
    "katimeout" : {
      "type" : "integer"
    },
    "lbuid" : {
      "type" : "string"
    },
    "port" : {
      "type" : "integer"
    },
    "secure" : {
      "type" : "string",
      "enum" : [ "YES", "NO" ],
      "readonly" : true
    },
    "state" : {
      "type" : "string",
      "enum" : [ "ACTIVE", "INACTIVE", "UNKNOWN" ],
      "readonly" : true
    },
    "wlmname" : {
      "type" : "string"
    }
  },
  "$schema" : "http://json-schema.org/draft-04/schema#",
  "id" : "lbwlm",
  "title" : "lbwlm"
}

```

Sample Go code:

```
package lb

type Lbwlm struct {
  Ipaddress string `json:"ipaddress,omitempty"`
  Katimeout int `json:"katimeout,omitempty"`
  Lbuid string `json:"lbuid,omitempty"`
  Port int `json:"port,omitempty"`
  Secure string `json:"secure,omitempty"`
  State string `json:"state,omitempty"`
  Wlmname string `json:"wlmname,omitempty"`
}
```







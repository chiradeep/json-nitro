/*
Copyright 2016 Citrix Systems, Inc

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.citrix.netscaler.jsongenerator;
 
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.fge.jsonschema.util.JsonLoader;
import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.exception.UnavailableVersion;
import com.github.reinert.jjschema.v1.*;

import org.reflections.Reflections;
import com.citrix.netscaler.nitro.resource.base.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.String;

 
public class SchemaGenerator{
    private static ObjectMapper mapper = new ObjectMapper();

    static {
        // required for pretty printing
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    private static Set<Class<? extends base_resource>>  findAllSubclasses() {
        Reflections reflections = new Reflections("com.citrix.netscaler.nitro.resource.config");

        Set<Class<? extends base_resource>> subTypes = reflections.getSubTypesOf(base_resource.class);
        return subTypes;
    }

    private static String capitalize(String s) {
        if (s.length() == 0) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }
    
    public static void main(String[] args) throws JsonProcessingException {

        JsonSchemaFactory schemaFactory = new JsonSchemaV4Factory();
        schemaFactory.setAutoPutDollarSchema(true);

        Set<Class<? extends base_resource>> subTypes = findAllSubclasses();
        for (Class<? extends base_resource> subType: subTypes) {
            System.out.println("Package " + subType.getPackage().getName());
            System.out.println("Simple Name " + subType.getSimpleName());

            JsonNode objSchema = schemaFactory.createSchema(subType);
            String name = subType.getSimpleName();
            ((ObjectNode) objSchema).put("id", name);
            ((ObjectNode) objSchema).put("title", name);

            String[] pkgComponents = subType.getPackage().getName().split("\\.");
            String dirName = pkgComponents[pkgComponents.length - 1];

            String fileName = subType.getSimpleName() + ".json";

            prettyPrintSchema(dirName, fileName, objSchema);
        }
        
    }
    
    private static void prettyPrintSchema(String dir, String fileName, JsonNode schema) throws JsonProcessingException{
        File directory = new File("./json/" + dir);
        if (!directory.exists()) {
            directory.mkdir();
        }
        String path = "./json/" + dir + "/" + fileName;
        try {
           FileWriter fw = new FileWriter(path);
           fw.write(mapper.writeValueAsString(schema));
           fw.flush();
           fw.close();
        } catch (IOException ioe) {
            System.out.println("Failed to write json file to path " + path);
        }
        //System.out.println(mapper.writeValueAsString(schema));
    }
    
}

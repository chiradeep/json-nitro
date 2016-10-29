#!/bin/bash
outputdir=${1:-./go}
mkdir -p $outputdir
for jsonf in $(find json -name \*.json)
do
    folder=$(basename $(dirname $jsonf))
    name=$(basename $jsonf .json)
    mkdir -p $outputdir/$folder
    bin/schema-generate -i $jsonf -o $outputdir/$folder/$name.go -p $folder
done
